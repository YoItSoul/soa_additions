package com.soul.soa_additions.quest.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Tiny client-side façade for opening the quest book screen. Split out of
 * {@code QuestBookItem} so the item class — which loads on both sides —
 * doesn't hold a hard reference to {@code Screen}, which is client-only
 * and would crash a dedicated server on class verification.
 */
@OnlyIn(Dist.CLIENT)
public final class QuestBookClient {

    private QuestBookClient() {}

    public static void openBook() {
        Minecraft.getInstance().setScreen(new QuestBookScreen());
    }
}
