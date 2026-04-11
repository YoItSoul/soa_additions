package com.soul.soa_additions.compat;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Times each mod-loading stage and attributes wall-clock to individual mods via a thread
 * sampler. The sampler runs at 5 Hz on a daemon thread, snapshots every JVM thread, walks the
 * top frame, and increments a counter for the mod that owns it. After load completes, sample
 * counts × interval ≈ wall-clock per mod. Output: {@code logs/soa_compat_report/startup_<ts>.md}.
 *
 * <p>Overhead: one daemon thread sleeping 200 ms between samples; each sample is one
 * {@link Thread#getAllStackTraces()} call. On a typical pack startup that's ~1500 samples
 * total before the sampler stops itself.
 */
public final class StartupProfiler {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_StartupProfiler");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final long SAMPLE_INTERVAL_MS = 200L;

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static long constructStart;
    private static long commonSetupStart;
    private static long loadCompleteAt;
    private static long stageConstructMs;
    private static long stageCommonSetupMs;
    private static Thread samplerThread;
    private static final Map<String, long[]> SAMPLE_COUNTS = new HashMap<>(); // modid → {samples}

    private StartupProfiler() {}

    /** Total wall-clock ms from mod construct to FMLLoadCompleteEvent. Returns 0 until load completes. */
    public static long getTotalLoadMs() {
        if (loadCompleteAt == 0L || constructStart == 0L) return 0L;
        return (loadCompleteAt - constructStart) / 1_000_000L;
    }

    public static long getConstructMs() { return stageConstructMs; }

    public static long getCommonSetupMs() { return stageCommonSetupMs; }

    /** Called from the SoaAdditions constructor — earliest hook we have. */
    public static void onConstruct() {
        constructStart = System.nanoTime();
        startSampler();
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        long now = System.nanoTime();
        stageConstructMs = (now - constructStart) / 1_000_000L;
        commonSetupStart = now;
    }

    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        long now = System.nanoTime();
        stageCommonSetupMs = (now - commonSetupStart) / 1_000_000L;
        loadCompleteAt = now;
        stopSampler();
        try {
            writeReport();
        } catch (IOException e) {
            LOGGER.warn("Could not write startup report", e);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Safety: ensure sampler is stopped even if FMLLoadCompleteEvent didn't fire on this side.
        stopSampler();
    }

    private static void startSampler() {
        if (!RUNNING.compareAndSet(false, true)) return;
        ModPackageIndex.buildIfNeeded();
        samplerThread = new Thread(() -> {
            while (RUNNING.get()) {
                try {
                    sampleAllThreads();
                    Thread.sleep(SAMPLE_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable t) {
                    // Sampling must never throw out — degrade silently.
                }
            }
        }, "SOA-StartupSampler");
        samplerThread.setDaemon(true);
        samplerThread.setPriority(Thread.MIN_PRIORITY);
        samplerThread.start();
    }

    private static void stopSampler() {
        if (!RUNNING.compareAndSet(true, false)) return;
        if (samplerThread != null) samplerThread.interrupt();
    }

    private static final String SELF_PACKAGE = "com.soul.soa_additions";

    private static void sampleAllThreads() {
        Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> e : all.entrySet()) {
            StackTraceElement[] frames = e.getValue();
            if (frames == null || frames.length == 0) continue;
            // Skip our own background threads (StartupSampler, ConfigScanner, JvmStatsSampler,
            // TickAttribution) so they don't inflate soa_additions' attribution share.
            if (frames[0].getClassName().startsWith(SELF_PACKAGE)) continue;
            for (int i = 0; i < Math.min(8, frames.length); i++) {
                String cls = frames[i].getClassName();
                if (cls.startsWith(SELF_PACKAGE)) continue;
                String modId = ModPackageIndex.lookup(cls);
                if (modId != null) {
                    SAMPLE_COUNTS.computeIfAbsent(modId, k -> new long[]{0L})[0]++;
                    break;
                }
            }
        }
    }

    private static void writeReport() throws IOException {
        Path dir = Path.of("logs", "soa_compat_report");
        Files.createDirectories(dir);
        Path file = dir.resolve("startup_" + LocalDateTime.now().format(FILE_TS) + ".md");

        long totalMs = (loadCompleteAt - constructStart) / 1_000_000L;
        long totalSamples = SAMPLE_COUNTS.values().stream().mapToLong(arr -> arr[0]).sum();

        StringBuilder md = new StringBuilder(4096);
        md.append("# SOA startup profile\n\n");
        md.append("_generated ").append(LocalDateTime.now()).append("_\n\n");
        md.append("| stage | duration |\n|---|---|\n");
        md.append("| CONSTRUCT | ").append(stageConstructMs).append(" ms |\n");
        md.append("| COMMON_SETUP | ").append(stageCommonSetupMs).append(" ms |\n");
        md.append("| **total (construct → loadComplete)** | **").append(totalMs).append(" ms** |\n\n");

        md.append("## Per-mod attribution (sampled)\n\n");
        md.append("Sampled ").append(totalSamples).append(" thread frames at ")
          .append(SAMPLE_INTERVAL_MS).append(" ms intervals. Time = sample share × total wall-clock.\n\n");

        if (totalSamples == 0) {
            md.append("_No samples attributable to mods (loading completed faster than the sampler could see)._\n");
        } else {
            List<Map.Entry<String, long[]>> sorted = new java.util.ArrayList<>(SAMPLE_COUNTS.entrySet());
            sorted.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
            md.append("| mod | samples | est. ms | share |\n|---|---|---|---|\n");
            for (var e : sorted) {
                long s = e.getValue()[0];
                double share = s * 100.0 / totalSamples;
                long estMs = (long) (totalMs * share / 100.0);
                md.append("| `").append(e.getKey()).append("` | ").append(s).append(" | ")
                  .append(estMs).append(" ms | ").append(String.format("%.1f", share)).append("% |\n");
            }
        }

        md.append("\n_Tip: re-run with the `-Dmixin.debug.export=true` flag if you want to inspect ")
          .append("transformed classes for the slowest mods._\n");

        Files.writeString(file, md.toString());
        LOGGER.info("Startup profile written → {}", file.getFileName());
    }
}
