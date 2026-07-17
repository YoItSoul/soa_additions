package com.soul.soa_additions.telemetry;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only event subscriber. Shows the first-launch consent dialog before
 * the title screen, then runs the telemetry heartbeat for the entire client
 * lifetime (main menu included) and computes is_playing dynamically from
 * whether the client is currently in a world. LoggingIn/LoggingOut only
 * fire immediate beats so the dashboard flips state instantly.
 */
@Mod.EventBusSubscriber(modid = com.soul.soa_additions.SoaAdditions.MODID, value = Dist.CLIENT)
public final class ClientTelemetryHooks {

    private ClientTelemetryHooks() {}

    private static boolean gpuCaptured = false;
    private static boolean heartbeatStarted = false;
    private static int retryCounter = 0;

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        // Intercept the first title screen of the session with the consent
        // dialog. Once a choice is recorded shouldShow() goes false, so later
        // title screens (returning from a world, etc.) open normally.
        if (!(event.getNewScreen() instanceof TitleScreen title)) return;
        if (event.getCurrentScreen() instanceof TelemetryConsentScreen) return;
        if (!TelemetryConsentScreen.shouldShow()) return;
        event.setNewScreen(new TelemetryConsentScreen(title));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        PlaytimeTracker.tick(net.minecraft.client.Minecraft.getInstance().level != null);
        if (!gpuCaptured) {
            ClientIdentity.captureGpuOnRenderThread();
            gpuCaptured = true;
        }
        if (heartbeatStarted) return;
        // Retry once a second (not every tick) until the initial send has
        // populated cachedEndpoint; give up permanently when telemetry can
        // never start this session (disabled in config / dev environment)
        // instead of re-reading the config 20x/s for the whole session.
        if (++retryCounter % 20 != 0) return;
        if (!Telemetry.heartbeatPossible()) {
            heartbeatStarted = true;
            return;
        }
        Telemetry.startHeartbeat();
        heartbeatStarted = Telemetry.isHeartbeatRunning();
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Telemetry.sendImmediateHeartbeat();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        // Explicit offline beat: the level may still be loaded while this event
        // fires, so recomputing the dimension would report "still playing".
        Telemetry.sendImmediateOfflineBeat();
    }
}
