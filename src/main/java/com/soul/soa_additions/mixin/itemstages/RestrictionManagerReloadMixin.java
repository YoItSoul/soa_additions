package com.soul.soa_additions.mixin.itemstages;

import com.soul.soa_additions.itemstages.NbtStageGates;
import net.darkhax.itemstages.RestrictionManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-adds {@link NbtStageGates} every time ItemStages rebuilds its restriction
 * table.
 *
 * <p>{@link RestrictionManager} is a reload listener, so a {@code /reload} or a
 * world load discards restrictions and replays them from the CraftTweaker
 * scripts. Anything registered from Java has to be put back afterwards, and
 * Forge offers no way to order our own reload listener after another mod's —
 * so this hooks the rebuild itself, which is the only point guaranteed to be
 * after the table exists.</p>
 */
@Mixin(value = RestrictionManager.class, remap = false)
public abstract class RestrictionManagerReloadMixin {

    @Inject(method = "apply", at = @At("RETURN"), remap = false)
    private void soa$registerNbtGates(Void unused, ResourceManager manager,
                                      ProfilerFiller profiler, CallbackInfo ci) {
        NbtStageGates.register();
    }
}
