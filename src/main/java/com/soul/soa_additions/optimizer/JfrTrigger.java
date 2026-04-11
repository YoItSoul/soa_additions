package com.soul.soa_additions.optimizer;

import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Fires a short Java Flight Recording when the sampler sees a JVM spike. Uses the JDK's
 * built-in {@code default.jfc} profile because it has &lt;1% overhead and is exactly what
 * Mission Control / IntelliJ Profiler can open.
 */
final class JfrTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_JfrTrigger");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private JfrTrigger() {}

    static void startRecording(Path dir, String reason, int seconds) throws Exception {
        Recording rec = new Recording();
        rec.setName("soa-spike-" + reason);
        rec.setSettings(Map.of(
                "jdk.ObjectAllocationSample#enabled", "true",
                "jdk.GCPhasePause#enabled", "true",
                "jdk.GCHeapSummary#enabled", "true",
                "jdk.CPULoad#enabled", "true",
                "jdk.JavaMonitorWait#enabled", "true",
                "jdk.JavaMonitorEnter#enabled", "true",
                "jdk.ThreadAllocationStatistics#enabled", "true"
        ));
        rec.setDuration(Duration.ofSeconds(seconds));
        rec.setMaxSize(64L * 1024 * 1024); // hard cap 64 MB
        Path out = dir.resolve("spike_" + LocalDateTime.now().format(TS) + "_" + reason + ".jfr");
        rec.setDestination(out);
        rec.start();
        LOGGER.warn("JFR recording started ({}s) → {}", seconds, out.getFileName());

        // Auto-close after duration so we don't leak the Recording handle.
        ScheduledExecutorService closer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SOA-JfrCloser");
            t.setDaemon(true);
            return t;
        });
        closer.schedule(() -> {
            try {
                if (rec.getState() == RecordingState.RUNNING) rec.stop();
                rec.close();
                LOGGER.info("JFR recording finished → {}", out.getFileName());
            } catch (Throwable t) {
                LOGGER.warn("JFR close failed", t);
            } finally {
                closer.shutdown();
            }
        }, seconds + 2, TimeUnit.SECONDS);
    }
}
