package com.soul.soa_additions.quest;

import com.soul.soa_additions.SoaAdditions;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers {@link QuestLoader} with the datapack reload pipeline. Also where we
 * bootstrap task/reward registries so a datapack-only reload (no mod re-init)
 * still works.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestReloadHandler {

    private QuestReloadHandler() {}

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(new QuestLoader());
    }
}
