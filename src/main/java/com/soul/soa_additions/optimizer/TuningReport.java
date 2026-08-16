package com.soul.soa_additions.optimizer;

import com.soul.soa_additions.telemetry.Telemetry;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Assembles the measurements that decide this pack's JVM tuning, for submission.
 *
 * <p>Field names here must match the {@code jvm_tuning} table's columns exactly — the receiver
 * maps named fields and silently drops anything it does not recognise, so a typo produces a
 * row with a null column rather than an error.</p>
 *
 * <p><strong>CPU frequency comes from OSHI</strong>, which vanilla Minecraft already ships and
 * uses for its own system reports, so this adds no dependency. It gives the actual clock and
 * the physical/logical core split, which {@code Runtime.availableProcessors()} cannot
 * distinguish. Guarded so that a missing or changed OSHI degrades to the JMX values rather
 * than failing the report.</p>
 */
public final class TuningReport {

    private TuningReport() {}

    /** Builds the payload and submits it. {@code onDone} receives whether it was accepted. */
    public static void submit(MinecraftServer server, Consumer<Boolean> onDone) {
        Telemetry.sendJvmTuningAsync(build(server), onDone);
    }

    public static Map<String, Object> build(MinecraftServer server) {
        Map<String, Object> m = new LinkedHashMap<>();
        HardwareProfile hw = HardwareProfile.detect();
        HeapMeasurement heap = HeapMeasurement.capture();
        GcCapability gc = GcCapability.measure();

        m.put("side", FMLEnvironment.dist.isClient() ? "client" : "server");

        // --- machine ---
        m.put("total_ram_mb", hw.ramUnknown() ? null : hw.totalRamGb() * 1024L);
        m.put("cpu_logical_cores", hw.cores());
        m.put("gpu_renderer", hw.gpuRenderer());
        addCpuDetail(m);

        // --- jvm configuration actually in use ---
        m.put("gc_profile", System.getProperty(JvmAdvisor.PROFILE_PROPERTY, "untuned"));
        m.put("heap_max_mb", heap.maxMb());
        m.put("gc_pause_target_ms", pauseTargetFromArgs());
        m.put("jvm_args", String.join(" ", JvmAdvisor.currentGcArgs()));

        // --- measurements: the reason this report exists ---
        m.put("reclaim_mb_per_ms", gc.measured() ? round(gc.reclaimMbPerMs(), 2) : null);
        m.put("live_set_mb", heap.measured() ? heap.liveSetMb() : null);
        m.put("peak_heap_mb", heap.peakMb());
        m.put("uptime_min", heap.uptimeMinutes());
        addGcTotals(m);

        if (server != null) {
            m.put("server_mspt_mean", round(server.getAverageTickTime(), 2));
        }
        return m;
    }

    /** Physical core count and clock speed, which JMX does not expose. */
    private static void addCpuDetail(Map<String, Object> m) {
        try {
            oshi.SystemInfo si = new oshi.SystemInfo();
            var cpu = si.getHardware().getProcessor();
            m.put("cpu_name", cpu.getProcessorIdentifier().getName());
            m.put("cpu_max_freq_hz", cpu.getMaxFreq());
            m.put("cpu_physical_cores", cpu.getPhysicalProcessorCount());
            m.put("cpu_logical_cores", cpu.getLogicalProcessorCount());
        } catch (Throwable t) {
            // OSHI absent or changed: the JMX logical-core count already in the map stands.
            m.putIfAbsent("cpu_name", System.getProperty("os.arch"));
        }
    }

    /** Cumulative young-collection counters, for pause behaviour over the session. */
    private static void addGcTotals(Map<String, Object> m) {
        try {
            long count = 0;
            long millis = 0;
            for (var bean : ManagementFactory.getGarbageCollectorMXBeans()) {
                if (!bean.getName().toLowerCase().contains("young")) continue;
                count += Math.max(0, bean.getCollectionCount());
                millis += Math.max(0, bean.getCollectionTime());
            }
            m.put("gc_young_count", count);
            m.put("gc_young_avg_ms", count > 0 ? round(millis / (double) count, 2) : null);
        } catch (Throwable t) {
            m.put("gc_young_count", null);
            m.put("gc_young_avg_ms", null);
        }
    }

    /** The pause target this process was actually launched with, or null if unset. */
    private static Integer pauseTargetFromArgs() {
        for (String a : JvmAdvisor.currentGcArgs()) {
            if (a.startsWith("-XX:MaxGCPauseMillis=")) {
                try {
                    return Integer.parseInt(a.substring(a.indexOf('=') + 1).trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static double round(double v, int places) {
        double f = Math.pow(10, places);
        return Math.round(v * f) / f;
    }
}
