package com.soul.soa_additions.quest.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.PendingPackMode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds a "Pack Mode: X" cycling button to the top-right corner of the
 * vanilla create-world screen. The selected mode is stashed in
 * {@link PendingPackMode} and consumed on first access by
 * {@link com.soul.soa_additions.quest.PackModeData#get} once the integrated
 * server starts, so the brand-new world launches in the chosen mode.
 *
 * <p>Pack mode is locked at world creation time (or on the first
 * {@code lock_packmode} reward), so without this button singleplayer
 * players had no way to start in anything but ADVENTURE.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CreateWorldPackModeButton {

    private CreateWorldPackModeButton() {}

    private static final int BTN_W = 130;
    private static final int BTN_H = 20;
    private static final int MARGIN = 5;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof CreateWorldScreen screen)) return;

        // Default the selection to ADVENTURE if the player hasn't touched the
        // button yet. Keeps the displayed label consistent with what would
        // actually be used.
        if (PendingPackMode.get() == null) {
            PendingPackMode.set(PackMode.ADVENTURE);
        }

        int x = screen.width - BTN_W - MARGIN;
        int y = MARGIN;
        event.addListener(Button.builder(label(), b -> cycle(b))
                .bounds(x, y, BTN_W, BTN_H)
                .build());
    }

    private static void cycle(Button b) {
        PackMode cur = PendingPackMode.get();
        if (cur == null) cur = PackMode.ADVENTURE;
        PackMode[] all = PackMode.values();
        PackMode next = all[(cur.ordinal() + 1) % all.length];
        PendingPackMode.set(next);
        b.setMessage(label());
    }

    private static Component label() {
        PackMode cur = PendingPackMode.get();
        String name = cur == null ? "ADVENTURE" : cur.name();
        return Component.literal("Pack Mode: " + name);
    }
}
