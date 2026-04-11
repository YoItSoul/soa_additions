package com.soul.soa_additions.quest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code /soa packmode show|set <mode>|force <mode>|lock} — op-gated management
 * of per-world packmode. {@code set} respects the 30-minute soft window and
 * the hard lock; {@code force} bypasses both and is audit-logged as such,
 * for the rare "we rolled back a save and need to restore state" case.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class PackModeCommand {

    private PackModeCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("packmode")
                                .then(Commands.literal("show").executes(PackModeCommand::show))
                                .then(Commands.literal("lock").executes(PackModeCommand::lock))
                                .then(Commands.literal("set")
                                        .then(Commands.literal("casual").executes(c -> set(c, PackMode.CASUAL, false)))
                                        .then(Commands.literal("adventure").executes(c -> set(c, PackMode.ADVENTURE, false)))
                                        .then(Commands.literal("expert").executes(c -> set(c, PackMode.EXPERT, false))))
                                .then(Commands.literal("force")
                                        .requires(src -> src.hasPermission(4))
                                        .then(Commands.literal("casual").executes(c -> set(c, PackMode.CASUAL, true)))
                                        .then(Commands.literal("adventure").executes(c -> set(c, PackMode.ADVENTURE, true)))
                                        .then(Commands.literal("expert").executes(c -> set(c, PackMode.EXPERT, true)))))
        );
    }

    private static int show(CommandContext<CommandSourceStack> ctx) {
        PackModeData data = PackModeData.get(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Packmode: ")
                .append(Component.literal(data.mode().lower()).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(data.locked() ? " (LOCKED)" : (data.isClosedForChange() ? " (window closed)" : " (editable)"))
                        .withStyle(ChatFormatting.GRAY)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int set(CommandContext<CommandSourceStack> ctx, PackMode mode, boolean force) throws CommandSyntaxException {
        PackModeData data = PackModeData.get(ctx.getSource().getServer());
        if (!force && data.isClosedForChange()) {
            ctx.getSource().sendFailure(Component.literal("Packmode is locked for this world. Use 'force' (perm 4) if absolutely necessary."));
            return 0;
        }
        PackMode previous = data.mode();
        if (force) data.forceMode(mode); else data.setMode(mode);
        ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Packmode: " + previous.lower() + " → " + mode.lower()
                + (force ? " (forced)" : "")).withStyle(ChatFormatting.GOLD), true);
        org.slf4j.LoggerFactory.getLogger("soa_additions/packmode")
                .warn("{} {} → {} by {}", force ? "FORCE" : "set", previous, mode, ctx.getSource().getTextName());
        return Command.SINGLE_SUCCESS;
    }

    private static int lock(CommandContext<CommandSourceStack> ctx) {
        PackModeData data = PackModeData.get(ctx.getSource().getServer());
        data.lock();
        ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Packmode locked at: " + data.mode().lower())
                .withStyle(ChatFormatting.GOLD), true);
        org.slf4j.LoggerFactory.getLogger("soa_additions/packmode")
                .warn("LOCK by {} (mode={})", ctx.getSource().getTextName(), data.mode());
        return Command.SINGLE_SUCCESS;
    }
}
