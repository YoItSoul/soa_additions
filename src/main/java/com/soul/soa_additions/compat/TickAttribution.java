package com.soul.soa_additions.compat;

import com.soul.soa_additions.SoaAdditions;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Always-on, low-frequency profiler for the server thread. Wakes up at 10 Hz, grabs the
 * server thread's top stack frames, and increments a counter for the mod whose package
 * appears closest to the top. Over a play session this builds a coarse "% of server time
 * spent in each mod" view — like spark, but always running and persistent across sessions.
 *
 * <p>Overhead: 10 stack-frame reads per second on a single named thread. Negligible.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class TickAttribution {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_TickAttribution");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final long SAMPLE_INTERVAL_MS = 100L;

    private static final Map<String, AtomicLong> COUNTS = new HashMap<>();
    private static final AtomicLong totalSamples = new AtomicLong();
    private static final AtomicLong attributedSamples = new AtomicLong();

    private static ScheduledExecutorService executor;
    private static volatile Thread serverThread;

    private TickAttribution() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ModPackageIndex.buildIfNeeded();
        serverThread = findServerThread();
        if (serverThread == null) {
            LOGGER.warn("Could not locate server thread — tick attribution disabled");
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SOA-TickAttribution");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        executor.scheduleAtFixedRate(TickAttribution::sample,
                SAMPLE_INTERVAL_MS, SAMPLE_INTERVAL_MS, TimeUnit.MILLISECONDS);
        LOGGER.info("Tick attribution sampler started");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        try {
            writeReport();
        } catch (IOException e) {
            LOGGER.warn("Could not write tick attribution report", e);
        }
    }

    private static Thread findServerThread() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if ("Server thread".equals(t.getName())) return t;
        }
        return null;
    }

    private static void sample() {
        Thread st = serverThread;
        if (st == null || !st.isAlive()) return;
        StackTraceElement[] frames = st.getStackTrace();
        if (frames.length == 0) return;
        totalSamples.incrementAndGet();
        for (int i = 0; i < Math.min(15, frames.length); i++) {
            String mod = ModPackageIndex.lookup(frames[i].getClassName());
            if (mod != null) {
                COUNTS.computeIfAbsent(mod, k -> new AtomicLong()).incrementAndGet();
                attributedSamples.incrementAndGet();
                return;
            }
        }
    }

    private static void writeReport() throws IOException {
        long total = totalSamples.get();
        if (total == 0) return;
        Path dir = Path.of("logs", "soa_compat_report");
        Files.createDirectories(dir);
        Path file = dir.resolve("ticks_" + LocalDateTime.now().format(FILE_TS) + ".md");

        StringBuilder md = new StringBuilder(2048);
        md.append("# SOA server-tick attribution\n\n");
        md.append("Samples taken: **").append(total).append("** at ").append(SAMPLE_INTERVAL_MS).append(" ms intervals  \n");
        md.append("Attributed to a mod: **").append(attributedSamples.get()).append("**\n\n");
        md.append("Each row is the share of server-thread time spent inside a mod's code. ")
          .append("Vanilla / forge frames are intentionally excluded — they dominate every profile and ")
          .append("aren't actionable for modpack tuning.\n\n");

        Map<String, Long> snapshot = new HashMap<>();
        COUNTS.forEach((k, v) -> snapshot.put(k, v.get()));
        List<Map.Entry<String, Long>> sorted = new java.util.ArrayList<>(snapshot.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        md.append("| mod | samples | share of total | share of attributed |\n|---|---|---|---|\n");
        for (var e : sorted) {
            double shareTotal = e.getValue() * 100.0 / total;
            double shareAttr = attributedSamples.get() == 0 ? 0
                    : e.getValue() * 100.0 / attributedSamples.get();
            md.append("| `").append(e.getKey()).append("` | ").append(e.getValue()).append(" | ")
              .append(String.format("%.2f", shareTotal)).append("% | ")
              .append(String.format("%.2f", shareAttr)).append("% |\n");
        }
        Files.writeString(file, md.toString());
        LOGGER.info("Tick attribution written → {}", file.getFileName());
    }
}
