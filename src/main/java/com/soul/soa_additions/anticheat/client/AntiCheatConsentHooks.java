package com.soul.soa_additions.anticheat.client;

import com.mojang.brigadier.CommandDispatcher;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.anticheat.AntiCheatConsent;
import com.soul.soa_additions.client.SoaConsentScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The {@code /soa anticheat} command: read the disclosure, check the current answer, change it.
 *
 * <p>The first-launch prompt itself is {@link SoaConsentScreen}, which asks about telemetry and the
 * cheat scan together, and the title screen carries a toggle for each
 * ({@code ConsentTitleButton}) — this command exists for players already in a world.</p>
 *
 * <p>The command is client-side because the answer is client state — it lives in the player's own
 * config folder and governs what their game is willing to send. A server cannot set it, which is
 * the point.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AntiCheatConsentHooks {

    private AntiCheatConsentHooks() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("soa")
                .then(Commands.literal("anticheat")
                        .executes(ctx -> status(ctx.getSource()))
                        .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("review").executes(ctx -> {
                            Minecraft mc = Minecraft.getInstance();
                            // Deferred: the command is still executing inside the current screen's
                            // input handling, and swapping screens underneath that crashes.
                            mc.execute(() -> mc.setScreen(new SoaConsentScreen(mc.screen, false)));
                            return 1;
                        }))
                        .then(Commands.literal("allow").executes(ctx -> {
                            AntiCheatConsent.set(AntiCheatConsent.State.ACCEPTED);
                            reply(ctx.getSource(), Component.literal(
                                            "Anticheat scan allowed. It takes effect the next time you join a world.")
                                    .withStyle(ChatFormatting.GREEN));
                            return 1;
                        }))
                        .then(Commands.literal("deny").executes(ctx -> {
                            AntiCheatConsent.set(AntiCheatConsent.State.DECLINED);
                            reply(ctx.getSource(), Component.literal(
                                            "Anticheat scan refused. Nothing about your installation will be sent — "
                                            + "but servers that require the scan will not let you connect.")
                                    .withStyle(ChatFormatting.YELLOW));
                            return 1;
                        }))));
    }

    private static int status(CommandSourceStack source) {
        Component state = switch (AntiCheatConsent.get()) {
            case ACCEPTED -> Component.literal("allowed").withStyle(ChatFormatting.GREEN);
            case DECLINED -> Component.literal("refused").withStyle(ChatFormatting.YELLOW);
            case UNDECIDED -> Component.literal("not answered yet").withStyle(ChatFormatting.GRAY);
        };
        reply(source, Component.literal("Anticheat scan: ").withStyle(ChatFormatting.WHITE).append(state));
        reply(source, Component.literal(
                        "/soa anticheat review shows exactly what it collects · allow · deny")
                .withStyle(ChatFormatting.DARK_GRAY));
        return 1;
    }

    private static void reply(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }
}
