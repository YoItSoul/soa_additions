package com.soul.soa_additions.mixin.defiledlands;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops Defiled Lands' blastem check from force-loading chunks on the server thread.
 *
 * <p>{@code ForgeEventHandler.onLevelTick} runs at phase END every ten game ticks
 * over {@link ServerLevel#getAllEntities()}, and for every living entity that
 * {@code PlantUtils.vulnerableToBlastem} accepts it calls
 * {@code checkBlastemAround}, which inflates the entity bounding box by 0.1 and
 * walks every block position inside it calling {@link ServerLevel#getBlockState}.
 * None of that path checks whether the position is in a loaded chunk.</p>
 *
 * <p>{@code getBlockState} on an unloaded position does not return air — it goes
 * through {@code ServerChunkCache.getChunkBlocking}, which parks the server
 * thread until the chunk exists. On a dedicated server that is a disk read and
 * costs microseconds. In singleplayer it runs the pack's entire worldgen stack
 * inline on the tick thread, and a 120 second profile of this pack measured
 * 69,688 ms — 58.1% of the whole server thread — parked in exactly that call.
 * It presents as the world freezing while terrain generates.</p>
 *
 * <p>The guard recomputes the same bounds the target sweeps (identical
 * {@code inflate(0.1)} and floor) and cancels when any chunk covering them is
 * absent, using the non-blocking {@code hasChunk}. Inside loaded terrain
 * nothing changes; the only behavioural difference is that blastem no longer
 * reaches an entity standing in a chunk the server has not loaded, which it
 * could only ever have done by stalling the tick to load it.</p>
 *
 * <p>{@code @Pseudo} with a string target: Defiled Lands is not a compile
 * dependency and the mixin config sets {@code defaultRequire: 0}, so with the
 * mod absent this silently does not apply.</p>
 */
@Pseudo
@Mixin(targets = "lykrast.defiledlands.ForgeEventHandler", remap = false)
public abstract class BlastemChunkStallMixin {

    @Inject(method = "checkBlastemAround", at = @At("HEAD"), cancellable = true, remap = false)
    private static void soa$skipUnloadedChunks(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        AABB box = entity.getBoundingBox().inflate(0.1D);
        int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(box.minX));
        int maxChunkX = SectionPos.blockToSectionCoord(Mth.floor(box.maxX));
        int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(box.minZ));
        int maxChunkZ = SectionPos.blockToSectionCoord(Mth.floor(box.maxZ));
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
