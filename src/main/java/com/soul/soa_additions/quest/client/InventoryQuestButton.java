package com.soul.soa_additions.quest.client;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Injects a "Quests" button into the top-left of the survival inventory
 * screen (and skips the creative tab screen, where the layout is crowded
 * and the top-left is already taken by tab navigation).
 *
 * <p>Positioned relative to the screen's {@code leftPos}/{@code topPos} so
 * it tracks when other mods shift the inventory around (curios, etc.). The
 * listener reinstalls on every screen init, so a resize or Esc-reopen
 * re-adds the button without leaking stale ones.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InventoryQuestButton {

    private InventoryQuestButton() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        // Anchor the button to the top-left of the whole screen so it's never
        // crowded by the inventory window or creative tabs. Still gated on
        // the inventory screens so it only appears when the player opens
        // their inventory, not on every container.
        if (!(event.getScreen() instanceof EffectRenderingInventoryScreen<?>)) return;

        Button btn = Button.builder(Component.literal("Quests"), b -> QuestBookClient.openBook())
                .bounds(2, 2, 56, 16)
                .build();
        event.addListener(btn);
    }
}
