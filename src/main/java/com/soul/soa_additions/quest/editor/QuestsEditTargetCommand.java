package com.soul.soa_additions.quest.editor;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code /soa quests edittarget <world|author>} — chooses whether in-game edits
 * write to a per-world override or back to the source JSON files. Op level 2+;
 * every switch is audit-logged because {@code author} mode lets a player modify
 * files that ship with the pack.
 *
 * <p>The command does NOT enable edit mode. You still need {@code /soa quests
 * editmode true} first. This is deliberate: "am I editing?" and "where do my
 * edits land?" are separate axes.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestsEditTargetCommand {

    private QuestsEditTargetCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("quests")
                                .then(Commands.literal("edittarget")
                                        .then(Commands.literal("world")
                                                .executes(ctx -> apply(ctx, EditTarget.WORLD_OVERRIDE)))
                                        .then(Commands.literal("author")
                                                .executes(ctx -> apply(ctx, EditTarget.AUTHOR_SOURCE)))))
        );
    }

    private static int apply(CommandContext<CommandSourceStack> ctx, EditTarget target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        boolean changed = EditModeTracker.setTarget(player.getUUID(), target);

        Component label = target == EditTarget.AUTHOR_SOURCE
                ? Component.literal("AUTHOR (writes to source files)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                : Component.literal("WORLD (writes to per-world override)").withStyle(ChatFormatting.GREEN);

        player.sendSystemMessage(Component.literal("[SOA] Quest edit target: ").append(label));

        org.slf4j.LoggerFactory.getLogger("soa_additions/quest-editor")
                .info("edittarget {} by {} (changed={})",
                        target, player.getGameProfile().getName(), changed);

        return changed ? Command.SINGLE_SUCCESS : 0;
    }
}
