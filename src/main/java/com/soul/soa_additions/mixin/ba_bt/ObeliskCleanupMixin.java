package com.soul.soa_additions.mixin.ba_bt;

import com.soul.soa_additions.compat.ChunkLoadGuard;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops Brass Amber's obelisk cleanup sweep from force-loading chunks.
 *
 * <p>{@code BTCoreObelisk.removeMotionActiveBlocks} walks the box bounded by the
 * obelisk's {@code top}/{@code bottom}/{@code westWall}/{@code eastWall}/
 * {@code northWall}/{@code southWall} fields calling {@code Level.getBlockState}
 * on every position, with no load check, from the entity's tick. Positions in
 * unloaded chunks park the server thread.</p>
 *
 * <p>The wall bounds are private fields on the superclass, so rather than shadow
 * them this guards a fixed 64-block (4 chunk) radius around the obelisk — larger
 * than any obelisk arena, so it strictly over-covers the swept box. Over-guarding
 * only defers the sweep to a later tick, which is harmless: it runs every tick
 * anyway, and cleanup of an area nobody has loaded has no observable effect.</p>
 *
 * <p>Note this only removes the blocking (~2% of the server thread). The sweep's
 * own CPU cost — ~12% while an obelisk is active — is inherent to scanning that
 * volume every tick and would need an upstream fix.</p>
 */
@Pseudo
@Mixin(targets = "com.brass_amber.ba_bt.entity.block.BTCoreObelisk", remap = false)
public abstract class ObeliskCleanupMixin {

    @Inject(method = "removeMotionActiveBlocks", at = @At("HEAD"), cancellable = true, remap = false)
    private void soa$skipUnloadedCleanup(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!ChunkLoadGuard.loadedAround(self.level(), self.blockPosition(), 64,
                "ba_bt:removeMotionActiveBlocks")) {
            ci.cancel();
        }
    }
}
