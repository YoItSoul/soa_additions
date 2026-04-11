package com.soul.soa_additions.quest.importer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;

/**
 * {@code /soa quests import-ftb [source]} — one-shot migration from FTB
 * Quests SNBT into our JSON format. Writes converted chapter files into the
 * current world's quest-override directory, where the registry picks them up
 * on next reload (or immediately via {@code /reload}).
 *
 * <p>Source defaults to {@code <gamedir>/config/ftbquests/quests}. Pass an
 * absolute or relative path to override — useful for importing from a
 * different instance, e.g. your existing Souls of Avarice config dir.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class FtbQuestsImportCommand {

    private FtbQuestsImportCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.literal("quests")
                                .then(Commands.literal("import-ftb")
                                        .executes(FtbQuestsImportCommand::runDefault)
                                        .then(Commands.argument("source", StringArgumentType.greedyString())
                                                .executes(FtbQuestsImportCommand::runPath))))
        );
    }

    private static int runDefault(CommandContext<CommandSourceStack> ctx) {
        Path gameDir = ctx.getSource().getServer().getServerDirectory().toPath();
        return run(ctx, gameDir.resolve("config/ftbquests/quests"));
    }

    private static int runPath(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "source");
        return run(ctx, Path.of(raw));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, Path source) {
        CommandSourceStack src = ctx.getSource();
        var server = src.getServer();
        Path worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        Path outDir = worldDir.resolve("soa_additions").resolve("quest_edits");

        try {
            FtbQuestsImporter.Result r = FtbQuestsImporter.importFrom(source, outDir);
            src.sendSuccess(() -> Component.literal(
                    "[SOA] FTB import: " + r.chapters + " chapter(s), " + r.quests + " quest(s) → " + outDir)
                    .withStyle(ChatFormatting.GREEN), true);
            int toShow = Math.min(r.warnings.size(), 10);
            for (int i = 0; i < toShow; i++) {
                final String w = r.warnings.get(i);
                src.sendSystemMessage(Component.literal("  ! " + w).withStyle(ChatFormatting.YELLOW));
            }
            if (r.warnings.size() > toShow) {
                final int more = r.warnings.size() - toShow;
                src.sendSystemMessage(Component.literal("  (+ " + more + " more warnings — see log)")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            src.sendSystemMessage(Component.literal("Run /reload to pick up the imported chapters.")
                    .withStyle(ChatFormatting.GRAY));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            src.sendFailure(Component.literal("[SOA] FTB import failed: " + e.getMessage()));
            org.slf4j.LoggerFactory.getLogger("soa_additions/ftb-import")
                    .error("FTB import from {} failed", source, e);
            return 0;
        }
    }
}
