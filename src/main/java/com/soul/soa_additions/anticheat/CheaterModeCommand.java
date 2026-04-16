package com.soul.soa_additions.anticheat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
 * {@code /soa quests cheatermode [true|false]} — toggles the executing
 * player's per-player opt-in for flagging cheat commands. Without an
 * argument, reports the current state.
 *
 * <p>When disabled (the default), non-safe OP commands are blocked at the
 * {@code CommandEvent} layer so admins don't accidentally flag themselves
 * for routine operations. When enabled, the command runs AND the player is
 * flagged as a cheater. Console invocation is a no-op — the setting is
 * per-player and only makes sense for a specific {@link ServerPlayer}.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class CheaterModeCommand {

    private CheaterModeCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("quests")
                                .then(Commands.literal("cheatermode")
                                        .executes(CheaterModeCommand::show)
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(CheaterModeCommand::set))))
        );
    }

    private static int show(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        boolean enabled = CheaterModeOptIn.isEnabled(player);
        Component state = enabled
                ? Component.literal("ENABLED").withStyle(ChatFormatting.RED)
                : Component.literal("disabled").withStyle(ChatFormatting.GREEN);
        ctx.getSource().sendSuccess(() ->
                Component.literal("[SOA] Cheater mode: ").append(state), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        CheaterModeOptIn.setEnabled(player, enabled);

        Component state = enabled
                ? Component.literal("ENABLED").withStyle(ChatFormatting.RED)
                : Component.literal("disabled").withStyle(ChatFormatting.GREEN);
        Component suffix = enabled
                ? Component.literal(" — subsequent cheat commands will flag you.").withStyle(ChatFormatting.DARK_GRAY)
                : Component.literal(" — cheat commands will now be blocked.").withStyle(ChatFormatting.DARK_GRAY);
        ctx.getSource().sendSuccess(() ->
                Component.literal("[SOA] Cheater mode: ").append(state).append(suffix), false);

        org.slf4j.LoggerFactory.getLogger("soa_additions/cheatermode")
                .info("cheatermode {} by {}",
                        enabled ? "ON" : "OFF",
                        player.getGameProfile().getName());
        return Command.SINGLE_SUCCESS;
    }
}
