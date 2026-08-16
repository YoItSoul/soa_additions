package com.soul.soa_additions.optimizer;

import com.sun.management.GcInfo;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Map;

/**
 * How fast this machine actually collects garbage, in the only units that matter.
 *
 * <h2>Why this exists instead of reading a CPU clock</h2>
 *
 * <p>Clock speed is not reachable from the JVM without native calls, and it would be a poor
 * input anyway — evacuation performance depends on cache behaviour, memory bandwidth and core
 * count as much as on frequency, and two 4GHz CPUs can differ by more than 2x here. What GC
 * tuning actually needs is a single number: <strong>how many megabytes this machine can
 * evacuate per millisecond of pause</strong>. That is directly measurable, and it is exactly
 * what clock speed would only have been a proxy for.</p>
 *
 * <p>Measured on the development machine (Ryzen 7 7800X3D) this reads about 134 MB/ms. A
 * thin laptop core will be a fraction of that, and the difference decides whether a given
 * pause target is achievable at all.</p>
 *
 * <h2>What it changes</h2>
 *
 * <p>{@code MaxGCPauseMillis} is a target G1 chases by resizing the young generation:</p>
 *
 * <pre>   pause ≈ bytes evacuated / reclaim rate</pre>
 *
 * <p>G1 will not shrink young gen below {@code G1NewSizePercent} (5% by default), so there is
 * a floor on how short a pause can possibly be. Ask a slow machine for 25ms when its floor is
 * 60ms and G1 pins young gen at the minimum, misses the target every time anyway, and collects
 * far more often than it needs to — strictly worse than having asked for something achievable.
 * So the target is set from what this machine can actually deliver.</p>
 *
 * <p>Read on demand from {@code GarbageCollectorMXBean.getLastGcInfo()}. No sampling thread,
 * no background cost.</p>
 */
public record GcCapability(double reclaimMbPerMs, boolean measured) {

    /** G1 will not size young gen below this fraction of the heap. */
    private static final double MIN_YOUNG_FRACTION = 0.05;

    /** Aim comfortably above the floor rather than at it, so G1 has room to adapt. */
    private static final double SAFETY = 1.5;

    /** What we would like on a capable machine. */
    private static final int PREFERRED_PAUSE_MS = 25;

    /** Never recommend a target so loose it stops protecting frame time. */
    private static final int MAX_PAUSE_MS = 60;

    public static GcCapability measure() {
        double best = 0;
        try {
            for (var bean : ManagementFactory.getGarbageCollectorMXBeans()) {
                if (!(bean instanceof com.sun.management.GarbageCollectorMXBean sun)) continue;
                // Young collections are the ones whose cost we are sizing against; old-gen
                // cycles are mostly concurrent and not comparable.
                if (!bean.getName().toLowerCase().contains("young")) continue;

                GcInfo info = sun.getLastGcInfo();
                if (info == null || info.getDuration() <= 0) continue;

                long before = sum(info.getMemoryUsageBeforeGc());
                long after = sum(info.getMemoryUsageAfterGc());
                long reclaimedMb = (before - after) / 1048576L;
                if (reclaimedMb <= 0) continue;

                double rate = reclaimedMb / (double) info.getDuration();
                if (rate > best) best = rate;
            }
        } catch (Throwable ignored) {
            // Any JMX shortfall just means we fall back to the preferred target.
        }
        return new GcCapability(best, best > 0);
    }

    private static long sum(Map<String, MemoryUsage> usage) {
        long total = 0;
        if (usage != null) {
            for (MemoryUsage u : usage.values()) {
                if (u != null) total += u.getUsed();
            }
        }
        return total;
    }

    /**
     * The shortest pause this machine could achieve at the given heap, in ms.
     *
     * <p>Young gen cannot go below 5% of the heap, so that much has to be walked however fast
     * the CPU is.
     */
    public double floorPauseMs(int heapGb) {
        if (!measured) return 0;
        double minYoungMb = heapGb * 1024.0 * MIN_YOUNG_FRACTION;
        return minYoungMb / reclaimMbPerMs;
    }

    /** The pause target to actually ship for this machine and heap. */
    public int recommendedPauseMs(int heapGb) {
        if (!measured) return PREFERRED_PAUSE_MS;
        int achievable = (int) Math.ceil(floorPauseMs(heapGb) * SAFETY);
        return Math.min(MAX_PAUSE_MS, Math.max(PREFERRED_PAUSE_MS, achievable));
    }

    /** Human-readable reasoning for the command output. */
    public String explain(int heapGb) {
        if (!measured) {
            return "no GC sample yet — using the default " + PREFERRED_PAUSE_MS + "ms target";
        }
        int rec = recommendedPauseMs(heapGb);
        String base = String.format("%.0f MB/ms reclaim, floor ~%.0fms at %dG",
                reclaimMbPerMs, floorPauseMs(heapGb), heapGb);
        return rec > PREFERRED_PAUSE_MS
                ? base + " — target relaxed to " + rec + "ms because " + PREFERRED_PAUSE_MS
                        + "ms is not reachable here"
                : base + " — " + rec + "ms is comfortably achievable";
    }
}
