package com.soul.soa_additions.quest.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Injects a quest-book icon button into the top-center of the survival
 * inventory screen (and skips the creative tab screen, where the layout is
 * crowded and the top-left is already taken by tab navigation).
 *
 * <p>The button renders the quest book item texture instead of text, making
 * it compact and recognizable. Positioned at top-center of the screen so it
 * avoids overlap with FTB buttons on the left.</p>
 *
 * <p>The listener reinstalls on every screen init, so a resize or Esc-reopen
 * re-adds the button without leaking stale ones.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InventoryQuestButton {

    private InventoryQuestButton() {}

    private static ItemStack questBookIcon;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof EffectRenderingInventoryScreen<?>)) return;

        int screenWidth = event.getScreen().width;
        int btnSize = 20;
        int x = (screenWidth - btnSize) / 2;
        int y = 2;

        event.addListener(new QuestIconButton(x, y, btnSize, btnSize));
    }

    /**
     * A small button that renders the quest book item icon instead of text.
     */
    private static class QuestIconButton extends Button {

        QuestIconButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), b -> QuestBookClient.openBook(), DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            if (questBookIcon == null) {
                questBookIcon = new ItemStack(ModItems.QUEST_BOOK.get());
            }
            graphics.renderItem(questBookIcon, getX() + 2, getY() + 2);
        }
    }
}
