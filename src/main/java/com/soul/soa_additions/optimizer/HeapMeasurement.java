package com.soul.soa_additions.optimizer;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;

/**
 * This JVM's actual memory behaviour, so heap advice can be measured rather than guessed.
 *
 * <h2>Live set, not peak</h2>
 *
 * <p>The metric that matters is the <strong>live set</strong> — how much memory survives a
 * collection — not peak occupancy. Peak includes garbage that simply had not been collected
 * yet, and sizing for it inflates the heap for no benefit. This pack is a worked example:</p>
 *
 * <ul>
 *   <li>Peak occupancy measured 9,216 MB. The old peak-based formula (peak × 1.4) recommended
 *       about 12.6 GB.</li>
 *   <li>The live set measured 6,246 MB. Live set × 1.3 gives about 8 GB.</li>
 *   <li>8 GB is the size the pack demonstrably runs well at; 11 GB measurably made pauses
 *       <em>worse</em>, because collection cost scales with surviving bytes.</li>
 * </ul>
 *
 * <p>So the measured live set is the honest input, and it is per-machine: a player with fewer
 * render-distance chunks, no shaders or Lite Mode enabled genuinely has a smaller live set and
 * genuinely needs less heap. That is the difference between a lookup table and surgical tuning.</p>
 *
 * <p>Read straight off {@link MemoryPoolMXBean#getCollectionUsage()}, which the JVM already
 * maintains — no sampling thread, no disk writes, nothing running in the background. This can
 * be called on demand and costs nothing when it is not.</p>
 */
public record HeapMeasurement(long liveSetMb, long peakMb, long currentMb, long maxMb,
                              long uptimeMinutes, boolean measured) {

    /** Below this, no old-gen collection has happened yet and the reading means nothing. */
    private static final long MIN_CREDIBLE_MB = 256;

    /** The live set is not representative until the world has actually been loaded a while. */
    private static final long WARMUP_MINUTES = 5;

    public static HeapMeasurement capture() {
        long live = 0;
        long peak = 0;
        boolean sawCollectionUsage = false;

        try {
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                if (pool.getType() != MemoryType.HEAP) continue;
                // Usage after the JVM last collected this pool. Summed across heap pools this
                // is the surviving set: eden reads ~0, survivor is small, old gen is the bulk.
                MemoryUsage cu = pool.getCollectionUsage();
                if (cu != null) {
                    live += cu.getUsed();
                    sawCollectionUsage = true;
                }
                MemoryUsage pu = pool.getPeakUsage();
                if (pu != null) peak += pu.getUsed();
            }
        } catch (Throwable ignored) {
            // Any JMX oddity just means we fall back to the RAM table.
        }

        long liveMb = live / 1048576;
        long peakMb = peak / 1048576;
        Runtime rt = Runtime.getRuntime();
        long currentMb = (rt.totalMemory() - rt.freeMemory()) / 1048576;
        long maxMb = rt.maxMemory() / 1048576;

        long uptimeMin = 0;
        try {
            uptimeMin = ManagementFactory.getRuntimeMXBean().getUptime() / 60_000L;
        } catch (Throwable ignored) {
            // Non-fatal; only used to decide whether to trust the reading.
        }

        boolean ok = sawCollectionUsage && liveMb >= MIN_CREDIBLE_MB && uptimeMin >= WARMUP_MINUTES;
        return new HeapMeasurement(liveMb, peakMb, currentMb, maxMb, uptimeMin, ok);
    }

    /** True when the reading exists but the session is too young to trust it. */
    public boolean tooEarly() {
        return !measured && liveSetMb >= MIN_CREDIBLE_MB && uptimeMinutes < WARMUP_MINUTES;
    }

    /**
     * Heap this JVM's own behaviour argues for, in GB.
     *
     * <p>1.3× the live set: enough headroom for allocation between collections without the
     * oversized young generation that made pauses worse at 11 GB. Rounded up, floored at 4 GB.</p>
     */
    public int impliedHeapGb() {
        double gb = (liveSetMb / 1024.0) * 1.3;
        return Math.max(4, (int) Math.ceil(gb));
    }
}
