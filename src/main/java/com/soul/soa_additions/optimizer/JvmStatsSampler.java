package com.soul.soa_additions.optimizer;

import com.soul.soa_additions.config.ModConfigs;
import com.sun.management.OperatingSystemMXBean;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Lite, persistent JVM/server profiler tuned for modpack tuning. One sampler thread, one open
 * writer, cached MX-bean references — designed so the act of measuring doesn't perturb what is
 * being measured.
 *
 * <p>Captures, per sample:
 * <ul>
 *   <li>Heap (used/committed/max/pct), non-heap, and per-pool breakdown (Eden/Old/Metaspace/…)</li>
 *   <li>Allocation rate (MB/s) — derived from inter-sample heap_used delta plus collected bytes</li>
 *   <li>Per-collector GC delta count + delta ms (so spikes plot cleanly)</li>
 *   <li>Threads (current + peak), classes loaded, process/system CPU, system load, RAM</li>
 *   <li>Server-side: TPS (mean tick ms), loaded chunks, total entities, online players</li>
 * </ul>
 *
 * <p>On startup it prunes old session files past {@link ModConfigs#JVM_PROFILER_KEEP_SESSIONS}
 * and writes a {@code README.txt} explaining the columns + recommended {@code -Xlog:gc*} flag.
 * On shutdown it writes a {@code summary.txt} with peak heap, avg/longest GC pause, avg TPS,
 * and a suggested {@code -Xmx}. When a long GC pause or near-OOM heap is observed, it can fire
 * a 30-second JFR recording (one per session) at a hot moment for offline analysis.
 */
public final class JvmStatsSampler {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_JvmProfiler");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final double MB = 1024.0 * 1024.0;

    /** Spike thresholds — kept tight so JFR triggers rarely. */
    private static final long SPIKE_GC_PAUSE_MS = 500L;
    private static final double SPIKE_HEAP_PCT = 90.0;
    private static final int JFR_RECORDING_SECONDS = 30;

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static ScheduledExecutorService executor;
    private static BufferedWriter writer;
    private static Path sessionFile;
    private static Path sessionDir;
    private static int intervalSeconds;

    // Cached MX beans — fetched once.
    private static RuntimeMXBean rt;
    private static MemoryMXBean memBean;
    private static ThreadMXBean threadBean;
    private static ClassLoadingMXBean classBean;
    private static OperatingSystemMXBean osBean;
    private static List<GarbageCollectorMXBean> gcBeans;
    private static List<MemoryPoolMXBean> poolBeans;

    // Inter-sample state.
    private static final Map<String, long[]> LAST_GC = new HashMap<>();
    private static long lastSampleNanos;
    private static long lastHeapUsed;
    private static long cumulativeCollectedBytes;
    private static boolean jfrAlreadyTriggered;
    private static long lastDiskRead = -1;
    private static long lastDiskWrite = -1;

    // Aggregates for the shutdown summary.
    private static long sampleCount;
    private static long peakHeapUsed;
    private static long peakHeapCommitted;
    private static long totalGcCount;
    private static long totalGcMillis;
    private static long longestGcMillis;
    private static double tpsSum;
    private static int tpsCount;
    private static double peakProcessCpu;

    private JvmStatsSampler() {}

    // ────────────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ────────────────────────────────────────────────────────────────────────────────

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;

        if (!ModConfigs.ENABLE_JVM_PROFILER.get()) {
            LOGGER.info("JVM profiler disabled by config");
            STARTED.set(false);
            return;
        }
        intervalSeconds = ModConfigs.JVM_PROFILER_INTERVAL_SECONDS.get();

        rt = ManagementFactory.getRuntimeMXBean();
        memBean = ManagementFactory.getMemoryMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
        classBean = ManagementFactory.getClassLoadingMXBean();
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        poolBeans = ManagementFactory.getMemoryPoolMXBeans();

        try {
            sessionDir = Path.of("logs", "soa_jvm_stats");
            Files.createDirectories(sessionDir);
            pruneOldSessions(ModConfigs.JVM_PROFILER_KEEP_SESSIONS.get());
            writeReadme();

            sessionFile = sessionDir.resolve("session_" + LocalDateTime.now().format(FILE_TS) + ".csv");
            writer = Files.newBufferedWriter(sessionFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            writeHeader();
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Could not create profiler session — disabled", e);
            STARTED.set(false);
            return;
        }

        // Seed inter-sample state so the first sample's deltas are sane.
        lastSampleNanos = System.nanoTime();
        lastHeapUsed = memBean.getHeapMemoryUsage().getUsed();
        for (GarbageCollectorMXBean gc : gcBeans) {
            LAST_GC.put(gc.getName(), new long[]{gc.getCollectionCount(), gc.getCollectionTime()});
        }
        long[] io = DiskIoReader.read();
        lastDiskRead = io[0];
        lastDiskWrite = io[1];

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SOA-JvmProfiler");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        executor.scheduleAtFixedRate(JvmStatsSampler::sampleSafely,
                intervalSeconds, intervalSeconds, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(JvmStatsSampler::stop, "SOA-JvmProfilerShutdown"));
        LOGGER.info("JVM profiler running ({}s interval) → {}", intervalSeconds, sessionFile.toAbsolutePath());
    }

    public static void stop() {
        if (executor == null) return;
        try {
            sampleSafely();
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            writeSummary();
        } catch (Exception e) {
            LOGGER.warn("Error during profiler shutdown", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Sampling
    // ────────────────────────────────────────────────────────────────────────────────

    private static void sampleSafely() {
        try {
            sample();
        } catch (Throwable t) {
            LOGGER.warn("Sample failed", t);
        }
    }

    private static void sample() throws IOException {
        long nowNanos = System.nanoTime();
        double elapsedSeconds = (nowNanos - lastSampleNanos) / 1.0e9;
        if (elapsedSeconds <= 0) elapsedSeconds = intervalSeconds;
        lastSampleNanos = nowNanos;

        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();
        long heapUsed = heap.getUsed();
        long heapCommitted = heap.getCommitted();
        long heapMax = heap.getMax();
        double heapPct = heapMax > 0 ? (heapUsed * 100.0 / heapMax) : -1.0;

        // Allocation rate = (heap delta if positive, else 0) + bytes collected this interval
        long heapDelta = heapUsed - lastHeapUsed;
        lastHeapUsed = heapUsed;

        long sampleGcCount = 0;
        long sampleGcMillis = 0;
        long sampleLongestGc = 0;
        StringBuilder gcCols = new StringBuilder();
        for (GarbageCollectorMXBean gc : gcBeans) {
            long count = gc.getCollectionCount();
            long time = gc.getCollectionTime();
            long[] prev = LAST_GC.get(gc.getName());
            long dCount = Math.max(0L, count - prev[0]);
            long dTime = Math.max(0L, time - prev[1]);
            prev[0] = count;
            prev[1] = time;
            gcCols.append(dCount).append(',').append(dTime).append(',');
            sampleGcCount += dCount;
            sampleGcMillis += dTime;
            if (dCount > 0 && dTime > sampleLongestGc) sampleLongestGc = dTime;
        }
        // Approx bytes freed by GC during the interval (used dropped while GCs ran).
        long approxFreed = sampleGcCount > 0 && heapDelta < 0 ? -heapDelta : 0;
        cumulativeCollectedBytes += approxFreed;
        double allocRateMbS = ((Math.max(0, heapDelta)) + approxFreed) / MB / elapsedSeconds;

        double cpuProcess = osBean.getProcessCpuLoad() * 100.0;
        double cpuSystem = osBean.getCpuLoad() * 100.0;

        ServerSnapshot srv = ServerSnapshot.capture();

        double diskReadMbS = -1, diskWriteMbS = -1;
        long[] io = DiskIoReader.read();
        if (io[0] >= 0 && lastDiskRead >= 0) {
            diskReadMbS = Math.max(0, io[0] - lastDiskRead) / MB / elapsedSeconds;
            diskWriteMbS = Math.max(0, io[1] - lastDiskWrite) / MB / elapsedSeconds;
        }
        if (io[0] >= 0) {
            lastDiskRead = io[0];
            lastDiskWrite = io[1];
        }

        StringBuilder pools = new StringBuilder();
        for (MemoryPoolMXBean pool : poolBeans) {
            pools.append(fmt(pool.getUsage().getUsed() / MB)).append(',');
        }

        StringBuilder row = new StringBuilder(256);
        row.append(LocalDateTime.now().format(TS)).append(',')
           .append(rt.getUptime() / 1000L).append(',')
           .append(fmt(heapUsed / MB)).append(',')
           .append(fmt(heapCommitted / MB)).append(',')
           .append(fmt(heapMax / MB)).append(',')
           .append(fmt(heapPct)).append(',')
           .append(fmt(allocRateMbS)).append(',')
           .append(fmt(nonHeap.getUsed() / MB)).append(',')
           .append(fmt(nonHeap.getCommitted() / MB)).append(',')
           .append(threadBean.getThreadCount()).append(',')
           .append(threadBean.getPeakThreadCount()).append(',')
           .append(classBean.getLoadedClassCount()).append(',')
           .append(classBean.getTotalLoadedClassCount()).append(',')
           .append(fmt(cpuProcess)).append(',')
           .append(fmt(cpuSystem)).append(',')
           .append(fmt(osBean.getSystemLoadAverage())).append(',')
           .append(fmt(osBean.getFreeMemorySize() / MB)).append(',')
           .append(fmt(osBean.getTotalMemorySize() / MB)).append(',')
           .append(fmt(srv.meanTickMs)).append(',')
           .append(fmt(srv.tps)).append(',')
           .append(srv.loadedChunks).append(',')
           .append(srv.entities).append(',')
           .append(srv.players).append(',')
           .append(fmt(diskReadMbS)).append(',')
           .append(fmt(diskWriteMbS)).append(',')
           .append(pools)
           .append(gcCols);
        row.setLength(row.length() - 1);
        row.append('\n');
        writer.write(row.toString());
        writer.flush(); // line-flush so a crash still leaves data on disk

        // Aggregates
        sampleCount++;
        if (heapUsed > peakHeapUsed) peakHeapUsed = heapUsed;
        if (heapCommitted > peakHeapCommitted) peakHeapCommitted = heapCommitted;
        totalGcCount += sampleGcCount;
        totalGcMillis += sampleGcMillis;
        if (sampleLongestGc > longestGcMillis) longestGcMillis = sampleLongestGc;
        if (cpuProcess > peakProcessCpu) peakProcessCpu = cpuProcess;
        if (srv.tps > 0) {
            tpsSum += srv.tps;
            tpsCount++;
        }

        // Spike detection → JFR + top-CPU thread dump (one per session)
        if (!jfrAlreadyTriggered && ModConfigs.JVM_PROFILER_AUTO_JFR.get()
                && (sampleLongestGc >= SPIKE_GC_PAUSE_MS || heapPct >= SPIKE_HEAP_PCT)) {
            jfrAlreadyTriggered = true;
            String reason = sampleLongestGc >= SPIKE_GC_PAUSE_MS
                    ? "gc_pause_" + sampleLongestGc + "ms"
                    : "heap_" + (int) heapPct + "pct";
            triggerSpikeCapture(reason);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Spike capture: top-CPU threads + JFR
    // ────────────────────────────────────────────────────────────────────────────────

    private static void triggerSpikeCapture(String reason) {
        LOGGER.warn("JVM spike detected ({}). Capturing top threads + JFR.", reason);
        try {
            Path top = sessionDir.resolve("spike_" + LocalDateTime.now().format(FILE_TS) + "_threads.txt");
            Files.writeString(top, captureTopThreads(reason));
        } catch (IOException e) {
            LOGGER.warn("Could not write thread snapshot", e);
        }
        try {
            JfrTrigger.startRecording(sessionDir, reason, JFR_RECORDING_SECONDS);
        } catch (Throwable t) {
            LOGGER.warn("JFR trigger failed (non-fatal)", t);
        }
        if (SparkIntegration.tryStartProfile()) {
            LOGGER.info("spark profiler dispatched (120s)");
        }
    }

    private static String captureTopThreads(String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("Spike reason: ").append(reason).append('\n')
          .append("Captured at: ").append(LocalDateTime.now().format(TS)).append("\n\n");
        if (!threadBean.isThreadCpuTimeSupported() || !threadBean.isThreadCpuTimeEnabled()) {
            sb.append("(Thread CPU time unsupported on this JVM)\n");
            return sb.toString();
        }
        long[] ids = threadBean.getAllThreadIds();
        record Row(String name, long cpuMs, Thread.State state) {}
        List<Row> rows = new ArrayList<>(ids.length);
        for (long id : ids) {
            long cpu = threadBean.getThreadCpuTime(id);
            ThreadInfo info = threadBean.getThreadInfo(id);
            if (info == null || cpu < 0) continue;
            rows.add(new Row(info.getThreadName(), cpu / 1_000_000L, info.getThreadState()));
        }
        rows.sort(Comparator.comparingLong(Row::cpuMs).reversed());
        sb.append(String.format("%-12s  %-12s  %s%n", "cpu_ms", "state", "thread"));
        for (int i = 0; i < Math.min(10, rows.size()); i++) {
            Row r = rows.get(i);
            sb.append(String.format("%-12d  %-12s  %s%n", r.cpuMs, r.state, r.name));
        }
        return sb.toString();
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Headers, README, summary, pruning
    // ────────────────────────────────────────────────────────────────────────────────

    private static void writeHeader() throws IOException {
        StringBuilder header = new StringBuilder();
        header.append("timestamp,uptime_s,")
              .append("heap_used_mb,heap_committed_mb,heap_max_mb,heap_pct,alloc_rate_mb_s,")
              .append("nonheap_used_mb,nonheap_committed_mb,")
              .append("threads,threads_peak,classes_loaded,classes_total_loaded,")
              .append("cpu_process_pct,cpu_system_pct,sys_load_avg,")
              .append("free_phys_mb,total_phys_mb,")
              .append("mean_tick_ms,tps,loaded_chunks,entities,players,")
              .append("disk_read_mb_s,disk_write_mb_s,");
        for (MemoryPoolMXBean pool : poolBeans) {
            header.append("pool_").append(sanitize(pool.getName())).append("_mb,");
        }
        for (GarbageCollectorMXBean gc : gcBeans) {
            String name = sanitize(gc.getName());
            header.append("gc_").append(name).append("_delta_count,")
                  .append("gc_").append(name).append("_delta_ms,");
        }
        header.setLength(header.length() - 1);
        header.append('\n');
        writer.write(header.toString());
    }

    private static void writeReadme() throws IOException {
        Path readme = sessionDir.resolve("README.txt");
        if (Files.exists(readme)) return;
        String body = """
                SOA JVM Profiler — output guide
                ================================
                Each `session_<timestamp>.csv` is one game launch. Open in any spreadsheet or
                plot with pandas/matplotlib.

                Key columns:
                  heap_used_mb / heap_pct      — current heap occupancy
                  alloc_rate_mb_s              — bytes/s your modpack is allocating; the
                                                 single most useful metric for sizing -Xmn
                                                 (young gen) or G1's region count.
                  gc_<name>_delta_ms           — pause time per sample (already a delta —
                                                 just plot it). Spikes mean you need a
                                                 bigger heap, a different GC, or a leak fix.
                  pool_<name>_mb               — Eden / Survivor / Old / Metaspace breakdown.
                                                 Watch Old gen to spot memory leaks.
                  mean_tick_ms / tps           — server tick health (server side only).

                Recommended JVM flags to add ALONGSIDE this profiler so the data lines up
                with the actual GC log:
                  -Xlog:gc*,gc+heap=debug:file=logs/soa_jvm_stats/gc.log:time,level,tags

                summary.txt is written on shutdown with peak heap, avg/longest GC pause,
                avg TPS, and a suggested -Xmx value (peak * 1.3).

                spike_*_threads.txt + JFR recordings are written when a long GC pause or
                near-OOM heap is detected — that's the moment you actually want to profile.
                """;
        Files.writeString(readme, body);
    }

    private static void writeSummary() {
        if (sessionFile == null) return;
        Path summary = sessionDir.resolve(sessionFile.getFileName().toString().replace(".csv", "_summary.txt"));
        try {
            double avgGcMs = totalGcCount == 0 ? 0 : (double) totalGcMillis / totalGcCount;
            double avgTps = tpsCount == 0 ? -1 : tpsSum / tpsCount;
            long suggestedXmxMb = (long) Math.ceil(peakHeapUsed * 1.3 / MB);
            Duration uptime = Duration.ofMillis(rt.getUptime());

            StringBuilder s = new StringBuilder();
            s.append("SOA JVM Profiler — session summary\n");
            s.append("===================================\n");
            s.append("session file        : ").append(sessionFile.getFileName()).append('\n');
            s.append("samples taken       : ").append(sampleCount).append('\n');
            s.append("uptime              : ").append(formatDuration(uptime)).append('\n');
            s.append('\n');
            s.append("peak heap used      : ").append(fmt(peakHeapUsed / MB)).append(" MB\n");
            s.append("peak heap committed : ").append(fmt(peakHeapCommitted / MB)).append(" MB\n");
            s.append("peak process cpu    : ").append(fmt(peakProcessCpu)).append(" %\n");
            s.append('\n');
            s.append("total gc count      : ").append(totalGcCount).append('\n');
            s.append("total gc time       : ").append(totalGcMillis).append(" ms\n");
            s.append("avg gc pause        : ").append(fmt(avgGcMs)).append(" ms\n");
            s.append("longest gc pause    : ").append(longestGcMillis).append(" ms\n");
            if (avgTps >= 0) s.append("avg tps             : ").append(fmt(avgTps)).append('\n');
            s.append('\n');
            s.append("SUGGESTED -Xmx      : ").append(suggestedXmxMb).append("M  (peak * 1.3)\n");
            s.append("                      Increase further if longest_gc_pause is high or\n");
            s.append("                      alloc_rate_mb_s is consistently above ~200.\n");
            Files.writeString(summary, s.toString());
            LOGGER.info("Wrote profiler summary → {}", summary.getFileName());
        } catch (IOException e) {
            LOGGER.warn("Could not write summary", e);
        }
    }

    private static void pruneOldSessions(int keep) {
        try (Stream<Path> files = Files.list(sessionDir)) {
            List<Path> sessions = files
                    .filter(p -> p.getFileName().toString().startsWith("session_")
                            && p.getFileName().toString().endsWith(".csv"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
            int excess = sessions.size() - keep;
            for (int i = 0; i < excess; i++) {
                Path old = sessions.get(i);
                Files.deleteIfExists(old);
                Path oldSummary = sessionDir.resolve(
                        old.getFileName().toString().replace(".csv", "_summary.txt"));
                Files.deleteIfExists(oldSummary);
            }
        } catch (IOException e) {
            LOGGER.debug("Could not prune old sessions", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Public read access for the in-game command
    // ────────────────────────────────────────────────────────────────────────────────

    public record Snapshot(long heapUsedMb, long heapMaxMb, double heapPct,
                           double allocRateGuessMbS, long totalGcMillis, long longestGcMillis,
                           double avgTps, long suggestedXmxMb, long sampleCount,
                           Path sessionFile) {}

    public static Snapshot snapshot() {
        if (!STARTED.get()) return null;
        long heapUsed = memBean.getHeapMemoryUsage().getUsed();
        long heapMax = memBean.getHeapMemoryUsage().getMax();
        return new Snapshot(
                (long) (heapUsed / MB),
                (long) (heapMax / MB),
                heapMax > 0 ? heapUsed * 100.0 / heapMax : -1,
                cumulativeCollectedBytes / MB / Math.max(1, rt.getUptime() / 1000.0),
                totalGcMillis, longestGcMillis,
                tpsCount == 0 ? -1 : tpsSum / tpsCount,
                (long) Math.ceil(peakHeapUsed * 1.3 / MB),
                sampleCount,
                sessionFile);
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────────────

    private static String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "";
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static String sanitize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
    }

    private static String formatDuration(Duration d) {
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        return String.format("%dh %02dm %02ds", h, m, s);
    }

    /** Tiny holder so the sample method doesn't reach into Forge directly. */
    private record ServerSnapshot(double meanTickMs, double tps, int loadedChunks, int entities, int players) {
        static final ServerSnapshot EMPTY = new ServerSnapshot(0, -1, 0, 0, 0);

        static ServerSnapshot capture() {
            try {
                var server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) return EMPTY;
                long sum = 0L;
                long[] ticks = server.tickTimes;
                if (ticks == null || ticks.length == 0) return EMPTY;
                for (long t : ticks) sum += t;
                double meanMs = sum / (double) ticks.length / 1_000_000.0;
                double tps = meanMs <= 50.0 ? 20.0 : (1000.0 / meanMs);
                int chunks = 0, entities = 0;
                for (var lvl : server.getAllLevels()) {
                    chunks += lvl.getChunkSource().getLoadedChunksCount();
                    for (var ignored : lvl.getAllEntities()) entities++;
                }
                int players = server.getPlayerList().getPlayerCount();
                return new ServerSnapshot(meanMs, tps, chunks, entities, players);
            } catch (Throwable t) {
                return EMPTY;
            }
        }
    }
}
