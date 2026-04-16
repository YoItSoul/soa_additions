package com.soul.soa_additions.quest.editor;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code /soa quests editmode <true|false> [player]} — toggles in-game quest
 * editor UI for an op. Defaults target to the invoking player when none is
 * given. Console invocation without a target is an error because edit mode
 * is always attached to a specific player's GUI session.
 *
 * <p>Every invocation is logged to the audit log so we can see who turned
 * editing on and when. Turning edit mode OFF still requires op level 2, to
 * prevent a player from silently disabling someone else's session.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestsEditModeCommand {

    private QuestsEditModeCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("quests")
                                .then(Commands.literal("editmode")
                                        .requires(src -> src.hasPermission(2))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(QuestsEditModeCommand::runSelf)
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(QuestsEditModeCommand::runOther)))))
        );
    }

    private static int runSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        return apply(ctx.getSource(), player, enabled);
    }

    private static int runOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        return apply(ctx.getSource(), target, enabled);
    }

    private static int apply(CommandSourceStack src, ServerPlayer target, boolean enabled) {
        boolean changed = EditModeTracker.setActive(target.getUUID(), enabled);
        // Tell the target's client about the flip so the quest book GUI can
        // switch into editor mode (drag handles, save indicators, etc.).
        com.soul.soa_additions.network.ModNetworking.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> target),
                new com.soul.soa_additions.quest.net.QuestEditStatePacket(enabled));
        Component state = enabled
                ? Component.literal("ENABLED").withStyle(ChatFormatting.GREEN)
                : Component.literal("disabled").withStyle(ChatFormatting.GRAY);

        // Tell the target
        target.sendSystemMessage(Component.literal("[SOA] Quest edit mode ")
                .append(state)
                .append(Component.literal(" — the quest book now shows editor controls.").withStyle(ChatFormatting.DARK_GRAY)));

        // Tell the invoker (unless they are the target)
        if (!src.getEntity().equals(target)) {
            src.sendSuccess(() -> Component.literal("[SOA] ")
                    .append(Component.literal(target.getGameProfile().getName()).withStyle(ChatFormatting.AQUA))
                    .append(" quest edit mode: ")
                    .append(state), true);
        }

        // Audit log. The source-name intentionally includes console case.
        org.slf4j.LoggerFactory.getLogger("soa_additions/quest-editor")
                .info("editmode {} {} by {} (changed={})",
                        enabled ? "ON" : "OFF",
                        target.getGameProfile().getName(),
                        src.getTextName(),
                        changed);

        return changed ? Command.SINGLE_SUCCESS : 0;
    }
}
