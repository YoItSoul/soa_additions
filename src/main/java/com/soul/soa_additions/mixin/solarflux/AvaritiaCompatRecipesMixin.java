package com.soul.soa_additions.mixin.solarflux;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels SolarFlux Reborn's Avaritia recipe compat.
 *
 * <p>SFR 20.1.11 was compiled against a different Avaritia fork — its
 * {@code AvaritiaCompat.registerRecipes} references
 * {@code morph.avaritia.init.AvaritiaModContent}, which doesn't exist in this
 * pack's Re-Avaritia, so every boot logs a FATAL
 * {@code NoClassDefFoundError} and the recipes never register anyway. The
 * pack supplies its own extreme-table recipes for both avaritia-tier panels
 * (kubejs/data/soa_additions/recipes/solarflux_avaritia_*.json), and the
 * panel <em>registration</em> half of the compat doesn't touch Avaritia
 * classes, so head-cancelling this method loses nothing.</p>
 *
 * <p>{@code @Pseudo} + string target: SolarFlux isn't a compile dependency,
 * and if it's ever removed the mixin silently skips (defaultRequire=0).</p>
 */
@Pseudo
@Mixin(targets = "org.zeith.solarflux.compat.avaritia.AvaritiaCompat", remap = false)
public abstract class AvaritiaCompatRecipesMixin {

    @Inject(method = "registerRecipes", at = @At("HEAD"), cancellable = true, remap = false)
    private void soa$skipBrokenAvaritiaRecipeCompat(CallbackInfo ci) {
        ci.cancel();
    }
}
