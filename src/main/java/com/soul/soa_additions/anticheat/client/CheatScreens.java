package com.soul.soa_additions.anticheat.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.network.CheatDetectedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side reception of {@link CheatDetectedPacket}.
 *
 * <p>Enforcement is entirely server-side — the disconnect happens whether or not any of this runs.
 * This only decides what the player <em>sees</em>, so a client that fails to render our screen
 * still gets the vanilla disconnect screen carrying the same text.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CheatScreens {

    /** Set when a BLOCKED packet arrives, consumed by the disconnect screen swap that follows. */
    private static CheatDetectedPacket pendingBlock;

    private CheatScreens() {}

    public static void onDetected(CheatDetectedPacket msg) {
        switch (msg.mode()) {
            case CHOICE -> Minecraft.getInstance().setScreen(new CheatChoiceScreen(msg));
            // The server disconnects immediately after sending this; park it for the swap below.
            case BLOCKED -> pendingBlock = msg;
            case ALLOWED -> { /* chat message already sent server-side */ }
        }
    }

    /**
     * Replaces the vanilla disconnect screen with ours when the disconnect was ours.
     *
     * <p>Swapping on open rather than reading {@code DisconnectedScreen.reason} keeps this off
     * reflection and off a mixin — we already know why, because the packet said so.</p>
     */
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (pendingBlock == null) return;
        if (!(event.getNewScreen() instanceof DisconnectedScreen)) return;
        CheatDetectedPacket msg = pendingBlock;
        pendingBlock = null;
        event.setNewScreen(new CheatBlockedScreen(msg, event.getCurrentScreen()));
    }
}
