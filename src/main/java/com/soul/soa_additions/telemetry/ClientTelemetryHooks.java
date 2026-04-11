package com.soul.soa_additions.telemetry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only event subscriber. Runs the telemetry heartbeat for the entire
 * client lifetime (main menu included) and computes is_playing dynamically
 * from whether the client is currently in a world. LoggingIn/LoggingOut only
 * fire immediate beats so the dashboard flips state instantly.
 */
@Mod.EventBusSubscriber(modid = com.soul.soa_additions.SoaAdditions.MODID, value = Dist.CLIENT)
public final class ClientTelemetryHooks {

    private ClientTelemetryHooks() {}

    private static boolean gpuCaptured = false;
    private static boolean heartbeatStarted = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!gpuCaptured) {
            ClientIdentity.captureGpuOnRenderThread();
            gpuCaptured = true;
        }
        if (!heartbeatStarted) {
            // Retries each tick until initial send has populated cachedEndpoint.
            Telemetry.startHeartbeat();
            heartbeatStarted = Telemetry.isHeartbeatRunning();
        }
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Telemetry.sendImmediateHeartbeat();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Telemetry.sendImmediateHeartbeat();
    }
}
