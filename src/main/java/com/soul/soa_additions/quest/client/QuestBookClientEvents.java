package com.soul.soa_additions.quest.client;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Client-side hooks that keep quest-book caches consistent with Forge config
 * reloads and resource-pack reloads.
 *
 * <p>Two independent caches live here:
 * <ul>
 *   <li>{@link QuestBookScreen}'s color table, populated from
 *       {@code soa_additions-common.toml}. Rebuilt on {@link ModConfigEvent.Reloading}
 *       so {@code /reload}'ing configs while the book is open picks up edits.</li>
 *   <li>Silhouette texture cache and {@link IconPickerPopup}'s item list, both
 *       derived from resource-pack content. Invalidated on resource reload so
 *       freshly-registered items and re-skinned icons show up without a client
 *       restart.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class QuestBookClientEvents {

    private QuestBookClientEvents() {}

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (Minecraft.getInstance() == null) return;
        QuestBookScreen.cacheColors();
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) (ResourceManager rm) -> {
            QuestBookScreen.invalidateSilhouetteCache();
            IconPickerPopup.invalidateCache();
        });
    }
}
