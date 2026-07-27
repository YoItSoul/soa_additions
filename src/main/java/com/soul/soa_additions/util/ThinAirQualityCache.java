package com.soul.soa_additions.util;

import fuzs.thinair.api.v1.AirQualityLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

/**
 * One-tick memoization store for {@code AirQualityCacheMixin}. Lives outside
 * the mixin package because Mixin forbids ordinary classloading of anything
 * inside a configured mixin package.
 *
 * <p>Per-thread (server thread and client render thread never share a cache)
 * with a {@value #TTL_TICKS}-tick window: the map is wiped every half second,
 * so a stationary entity recomputes twice a second instead of every tick and
 * air quality is never staler than 0.5s — imperceptible for breathing
 * mechanics, ~90% fewer real lookups.</p>
 */
public final class ThinAirQualityCache {

    private static final int TTL_TICKS = 10;

    private static final ThreadLocal<ThinAirQualityCache> CACHE =
            ThreadLocal.withInitial(ThinAirQualityCache::new);

    private long windowStart = Long.MIN_VALUE;
    private final HashMap<Long, AirQualityLevel> byPos = new HashMap<>(64);

    private ThinAirQualityCache() {}

    public static AirQualityLevel get(Level level, Vec3 pos) {
        ThinAirQualityCache c = CACHE.get();
        long now = level.getGameTime();
        if (now - c.windowStart >= TTL_TICKS || now < c.windowStart) {
            c.windowStart = now;
            c.byPos.clear();
        }
        return c.byPos.get(key(level, pos));
    }

    public static void put(Level level, Vec3 pos, AirQualityLevel result) {
        // get() already rolled the tick window for this thread this call chain;
        // storing without re-checking keeps the pair cheap.
        CACHE.get().byPos.put(key(level, pos), result);
    }

    private static Long key(Level level, Vec3 pos) {
        // Block-quantized position XOR'd with dimension identity; matches the
        // impl's own BlockPos.containing() quantization.
        return BlockPos.containing(pos).asLong() ^ ((long) System.identityHashCode(level.dimension()) << 1);
    }
}
