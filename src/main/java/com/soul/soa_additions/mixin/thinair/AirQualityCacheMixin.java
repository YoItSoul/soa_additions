package com.soul.soa_additions.mixin.thinair;

import com.soul.soa_additions.util.ThinAirQualityCache;
import fuzs.thinair.api.v1.AirQualityLevel;
import fuzs.thinair.helper.AirQualityHelperImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Memoizes Thin Air's air-quality lookup for one game tick.
 *
 * <p>{@code getAirQualityAtLocation} runs a chunk lookup, a block read and a
 * chunk-capability query, and Thin Air calls it once per living entity per
 * breath tick — spark measured ~1% of server self-time under this pack's
 * typical entity load (2026-07-26 profile). Air quality is purely position-
 * derived, so within a single tick every entity in the same block position
 * shares the answer, and a stationary entity re-uses its own.</p>
 *
 * <p>{@code required=false} / {@code defaultRequire=0} in the config: if a
 * future Thin Air build renames the method, the injection silently skips and
 * Thin Air just runs unpatched.</p>
 */
@Mixin(value = AirQualityHelperImpl.class, remap = false)
public abstract class AirQualityCacheMixin {

    @Inject(method = "getAirQualityAtLocation", at = @At("HEAD"), cancellable = true, remap = false)
    private void soa$cachedLookup(Level level, Vec3 pos, CallbackInfoReturnable<AirQualityLevel> cir) {
        AirQualityLevel hit = ThinAirQualityCache.get(level, pos);
        if (hit != null) {
            cir.setReturnValue(hit);
        }
    }

    @Inject(method = "getAirQualityAtLocation", at = @At("RETURN"), remap = false)
    private void soa$storeLookup(Level level, Vec3 pos, CallbackInfoReturnable<AirQualityLevel> cir) {
        AirQualityLevel result = cir.getReturnValue();
        if (result != null) {
            ThinAirQualityCache.put(level, pos, result);
        }
    }
}
