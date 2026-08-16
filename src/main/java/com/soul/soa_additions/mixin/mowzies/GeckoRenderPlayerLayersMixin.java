package com.soul.soa_additions.mixin.mowzies;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Mowzie's {@code GeckoRenderPlayer} extends vanilla {@code PlayerRenderer} and implements
 * GeckoLib's {@code GeoRenderer}, relying on the interface's default {@code getRenderLayers()}
 * (an empty list) because it has no GeoRenderLayers of its own.
 *
 * <p>First-Person Model mixes a concrete {@code getRenderLayers()} into {@code PlayerRenderer}
 * that returns the <em>vanilla</em> layer list. A class method beats an interface default, so
 * GeckoLib's {@code preApplyRenderLayers} ends up casting vanilla {@code RenderLayer}s to
 * {@code GeoRenderLayer} and dies with a ClassCastException on Mowzie's {@code GeckoArmorLayer}
 * the moment a Mowzie ability animation renders the player (sculptor staff, gauntlet, ...).
 *
 * <p>Re-declaring the empty list on the subclass restores GeckoLib's default without touching
 * what First-Person Model sees on the real player renderer.
 */
@Mixin(targets = "com.bobmowzie.mowziesmobs.client.render.entity.player.GeckoRenderPlayer", remap = false)
public abstract class GeckoRenderPlayerLayersMixin {
    public List<?> getRenderLayers() {
        return List.of();
    }
}
