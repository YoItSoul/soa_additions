package com.soul.soa_additions.tr.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side registry of {@link AstralWardBlockEntity} positions. Replaces
 * the chunk-traversal approach in {@link com.soul.soa_additions.tr.TrBlocks#isAreaWarded}
 * which deadlocked under C2ME — even with {@code load=false}, calling
 * {@code ServerChunkCache.getChunk()} from inside a chunk-load event ran
 * through {@code managedBlock}, which C2ME mixins re-route through an async
 * wait that hangs forever when the firing event already holds the chunk lock.
 *
 * <p>By tracking ward positions directly (BE adds itself on load, removes
 * on unload/break), ward-area checks are pure {@code Set} iteration —
 * O(N wards) with no chunk system involvement.
 */
public final class WardRegistry {

    private static final Map<ResourceKey<Level>, Set<BlockPos>> WARDS = new ConcurrentHashMap<>();

    private WardRegistry() {}

    public static void add(ResourceKey<Level> dim, BlockPos pos) {
        WARDS.computeIfAbsent(dim, k -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    public static void remove(ResourceKey<Level> dim, BlockPos pos) {
        Set<BlockPos> set = WARDS.get(dim);
        if (set != null) set.remove(pos);
    }

    /** True if any ward in the dimension is within 1 chunk of {@code target}
     *  (i.e. ward chunk == target chunk OR ward chunk is one of the 8
     *  neighbours). */
    public static boolean isChunkInWardArea(ResourceKey<Level> dim, ChunkPos target) {
        Set<BlockPos> set = WARDS.get(dim);
        if (set == null || set.isEmpty()) return false;
        for (BlockPos pos : set) {
            int wardChunkX = pos.getX() >> 4;
            int wardChunkZ = pos.getZ() >> 4;
            if (Math.abs(wardChunkX - target.x) <= 1 && Math.abs(wardChunkZ - target.z) <= 1) {
                return true;
            }
        }
        return false;
    }

    public static int sizeFor(ResourceKey<Level> dim) {
        Set<BlockPos> set = WARDS.get(dim);
        return set == null ? 0 : set.size();
    }
}
