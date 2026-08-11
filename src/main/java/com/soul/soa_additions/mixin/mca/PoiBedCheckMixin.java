package com.soul.soa_additions.mixin.mca;

import com.soul.soa_additions.compat.ChunkLoadGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops MCA's bed-occupancy check from force-loading chunks.
 *
 * <p>{@code ExtendedFindPointOfInterestTask.isBedOccupiedByOthers} reads the block
 * state at a candidate bed with no load check, from villager brain ticking. Beds
 * found via the POI index can sit in chunks that are no longer loaded, so the read
 * parks the server thread — 2.2% of the server thread while travelling.</p>
 *
 * <p>Returning {@code true} (treat as occupied) is the safe degradation: the
 * villager skips that bed and looks elsewhere, rather than claiming one in a chunk
 * the server would have to generate to inspect.</p>
 */
@Pseudo
@Mixin(targets = "forge.net.mca.entity.ai.brain.tasks.ExtendedFindPointOfInterestTask", remap = false)
public abstract class PoiBedCheckMixin {

    @Inject(method = "isBedOccupiedByOthers", at = @At("HEAD"), cancellable = true, remap = false)
    private void soa$skipUnloadedBed(ServerLevel level, BlockPos pos, LivingEntity entity,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (!ChunkLoadGuard.loadedAt(level, pos, "mca:isBedOccupiedByOthers")) {
            cir.setReturnValue(true);
        }
    }
}
