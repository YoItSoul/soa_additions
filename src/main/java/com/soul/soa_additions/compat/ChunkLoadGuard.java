package com.soul.soa_additions.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Non-blocking "is this area actually loaded?" checks, for guarding third-party
 * code that reads the world from a tick handler.
 *
 * <p>Several mods in this pack call {@code Level.getBlockState} / {@code clip} /
 * {@code getChunk} from {@code LevelTickEvent} or {@code ServerTickEvent} without
 * checking whether the target chunk exists. On a miss that becomes
 * {@code ServerChunkCache.getChunkBlocking}, which parks the server thread until
 * the chunk finishes generating — in singleplayer, the whole worldgen stack runs
 * inline on the tick thread. Measured stalls of 40 and 80 seconds per tick.</p>
 *
 * <p><strong>Only {@code getChunkNow} is safe for this test.</strong>
 * {@code hasChunkAt} and {@code getChunk(x, z, FULL, false)} both resolve through
 * {@code getChunkBlocking} and park on any {@code ChunkHolder} that exists but has
 * not finished generating — so using either as a pre-check <em>is</em> the blocking
 * call. Two call sites in this mod were written that way and had comments
 * explaining why they were safe; both were wrong.</p>
 *
 * <p>Each guard site logs once per session the first time it defers. A
 * {@code @Pseudo} mixin against an absent or renamed target fails silently by
 * design, so that line is the only runtime proof the injection actually applied.</p>
 */
public final class ChunkLoadGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_ChunkGuard");
    private static final Set<String> ANNOUNCED = ConcurrentHashMap.newKeySet();

    private ChunkLoadGuard() {}

    /** True when every chunk within {@code blockRadius} of {@code center} is loaded. */
    public static boolean loadedAround(Level level, BlockPos center, int blockRadius, String site) {
        return loaded(level,
                center.getX() - blockRadius, center.getZ() - blockRadius,
                center.getX() + blockRadius, center.getZ() + blockRadius, site);
    }

    /** True when every chunk the box touches is loaded. */
    public static boolean loadedIn(Level level, AABB box, String site) {
        return loaded(level,
                Mth.floor(box.minX), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxZ), site);
    }

    /** True when the single chunk containing {@code pos} is loaded. */
    public static boolean loadedAt(Level level, BlockPos pos, String site) {
        return loaded(level, pos.getX(), pos.getZ(), pos.getX(), pos.getZ(), site);
    }

    private static boolean loaded(Level level, int minX, int minZ, int maxX, int maxZ, String site) {
        if (!(level instanceof ServerLevel serverLevel)) return true;
        int cx0 = SectionPos.blockToSectionCoord(minX), cx1 = SectionPos.blockToSectionCoord(maxX);
        int cz0 = SectionPos.blockToSectionCoord(minZ), cz1 = SectionPos.blockToSectionCoord(maxZ);
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                if (serverLevel.getChunkSource().getChunkNow(cx, cz) == null) {
                    if (ANNOUNCED.add(site)) {
                        LOGGER.info("Guard active for {} — deferred a world read into an unloaded "
                                + "chunk (logs once per site per session).", site);
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
