package com.soul.soa_additions.quest.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InventoryQuestButton {

    private InventoryQuestButton() {}

    private static ItemStack questBookIcon;
    private static Button activeButton;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof EffectRenderingInventoryScreen<?>)) return;

        int screenWidth = event.getScreen().width;
        int btnSize = 20;
        int x = (screenWidth - btnSize) / 2;
        int y = 2;

        activeButton = Button.builder(Component.empty(), b -> QuestBookClient.openBook())
                .pos(x, y).size(btnSize, btnSize).build();
        event.addListener(activeButton);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (activeButton == null || !activeButton.visible) return;
        if (!(event.getScreen() instanceof EffectRenderingInventoryScreen<?>)) return;
        if (questBookIcon == null) {
            questBookIcon = new ItemStack(ModItems.QUEST_BOOK.get());
        }
        event.getGuiGraphics().renderItem(questBookIcon, activeButton.getX() + 2, activeButton.getY() + 2);
    }
}
