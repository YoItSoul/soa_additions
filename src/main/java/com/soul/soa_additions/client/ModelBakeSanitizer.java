package com.soul.soa_additions.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Replaces null entries in the baked-model map with the missing model before other
 * {@code ModifyBakingResult} listeners run.
 *
 * <p>CTM's {@code TextureMetadataHandler} iterates every entry of the map and calls methods on
 * the value unconditionally — a single null entry (a model whose bake failed or a loader that
 * returned null) NPEs the whole client resource reload, which FML swallows, leaving the loading
 * screen frozen forever with every thread idle. This listener runs at HIGHEST priority so the
 * map is clean before MysticalAgriculture's model replacement and CTM's wrapping pass, and logs
 * each replaced location so the underlying producer can be identified and fixed.
 */
@Mod.EventBusSubscriber(modid = "soa_additions", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModelBakeSanitizer {

    private static final Logger LOG = LogManager.getLogger("SOA-ModelSanitizer");

    private ModelBakeSanitizer() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        BakedModel missing = models.get(ModelBakery.MISSING_MODEL_LOCATION);
        if (missing == null) {
            LOG.warn("Missing-model entry itself is absent; cannot sanitize null baked models");
            return;
        }
        List<ResourceLocation> nulls = new ArrayList<>();
        for (Map.Entry<ResourceLocation, BakedModel> entry : models.entrySet()) {
            if (entry.getValue() == null) {
                nulls.add(entry.getKey());
                entry.setValue(missing);
            }
        }
        if (!nulls.isEmpty()) {
            LOG.warn("Replaced {} null baked models with the missing model (these would have "
                    + "crashed CTM's model pass). Fix their producers:", nulls.size());
            for (ResourceLocation loc : nulls) {
                LOG.warn("  null baked model: {}", loc);
            }
        }
    }
}
