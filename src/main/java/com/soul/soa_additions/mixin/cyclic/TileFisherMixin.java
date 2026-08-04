package com.soul.soa_additions.mixin.cyclic;

import com.soul.soa_additions.cyclicaqua.CyclicAquaFisher;
import com.soul.soa_additions.cyclicaqua.SoaFisherPatched;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hands Cyclic's Fishing Net over to Aquaculture's fishing rules.
 *
 * <p>{@code TileFisher.doFishing} is the whole of Cyclic's catch logic: roll a
 * chance, roll {@code minecraft:gameplay/fishing} with Luck of the Sea plus one,
 * drop the result, damage the rod. It has no concept of a hook, bait, lure or
 * open water, and nothing short of replacing it can add one — the loot context
 * it builds is what decides whether treasure and Neptune's Bounty are even
 * reachable. So this cancels it outright and runs
 * {@link CyclicAquaFisher#fish} in its place, which walks the same sequence a
 * real bobber walks.</p>
 *
 * <p>{@code @Pseudo} with a string target: Cyclic is a compile-only dependency
 * and the mixin config sets {@code defaultRequire: 0}, so with Cyclic absent
 * this silently does not apply. It is also inert until
 * {@link CyclicAquaFisher#shouldReplace()} agrees, which additionally requires
 * Aquaculture and the config switch — with Cyclic installed alone the net
 * behaves exactly as it always has.</p>
 *
 * <p>Implementing {@link SoaFisherPatched} is what makes the patch observable:
 * startup checks the target class for that interface and reports the result.</p>
 */
@Pseudo
@Mixin(targets = "com.lothrazar.cyclic.block.fishing.TileFisher", remap = false)
public abstract class TileFisherMixin implements SoaFisherPatched {

    @Inject(method = "doFishing", at = @At("HEAD"), cancellable = true, remap = false)
    private void soa$aquacultureCatch(ItemStack fishingRod, BlockPos center, CallbackInfo ci) {
        if (!CyclicAquaFisher.shouldReplace()) {
            return;
        }
        ci.cancel();
        CyclicAquaFisher.fish((BlockEntity) (Object) this, fishingRod, center);
    }

    /**
     * Cyclic gates the whole tick on the sampled block being water, which would
     * bar a lava-capable hook before {@code doFishing} is ever reached. The
     * fluid the rod can actually work is decided later, with the rod in hand.
     */
    @Inject(method = "isWater", at = @At("HEAD"), cancellable = true, remap = false)
    private static void soa$allowHookFluids(Level level, BlockPos center, CallbackInfoReturnable<Boolean> cir) {
        if (CyclicAquaFisher.isFishableFluid(level, center)) {
            cir.setReturnValue(true);
        }
    }
}
