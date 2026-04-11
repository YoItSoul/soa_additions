package com.soul.soa_additions.compat;

import com.soul.soa_additions.SoaAdditions;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.StackWalker.StackFrame;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts {@link ChunkEvent.Load} events and attributes each one to the mod that caused it,
 * via {@link StackWalker}. Produces a "top chunk-loaders" table at server stop. The single
 * biggest cause of late-game TPS death in big modpacks is mods force-loading chunks; this
 * tells you which mod is doing it without spark or a debugger attached.
 *
 * <p>Overhead: one shallow {@code StackWalker.walk} per chunk-load event. We sample 1-in-N
 * loads after the initial spawn region to avoid noise during world generation.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class ChunkLoadAttribution {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_ChunkAttribution");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int SAMPLE_EVERY = 8;        // examine 1 of every N loads
    private static final int WARMUP_LOADS = 4096;     // skip initial spawn area
    private static final StackWalker WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static final AtomicLong totalLoads = new AtomicLong();
    private static final AtomicLong sampledLoads = new AtomicLong();
    private static final Map<String, AtomicLong> COUNTS = new ConcurrentHashMap<>();

    private ChunkLoadAttribution() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        long n = totalLoads.incrementAndGet();
        if (n < WARMUP_LOADS) return;
        if ((n % SAMPLE_EVERY) != 0) return;

        String mod = WALKER.walk(stream -> stream
                .limit(20)
                .map(StackFrame::getClassName)
                .map(ModPackageIndex::lookup)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null));
        if (mod == null) return;
        sampledLoads.incrementAndGet();
        COUNTS.computeIfAbsent(mod, k -> new AtomicLong()).incrementAndGet();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        try {
            writeReport();
        } catch (IOException e) {
            LOGGER.warn("Could not write chunk attribution report", e);
        }
    }

    private static void writeReport() throws IOException {
        if (COUNTS.isEmpty()) return;
        Path dir = Path.of("logs", "soa_compat_report");
        Files.createDirectories(dir);
        Path file = dir.resolve("chunkloads_" + LocalDateTime.now().format(FILE_TS) + ".md");

        long total = totalLoads.get();
        long sampled = sampledLoads.get();
        Map<String, Long> snapshot = new HashMap<>();
        COUNTS.forEach((k, v) -> snapshot.put(k, v.get()));

        StringBuilder md = new StringBuilder(2048);
        md.append("# SOA chunk-load attribution\n\n");
        md.append("Total chunk loads observed: **").append(total).append("**  \n");
        md.append("Sampled (post-warmup, every ").append(SAMPLE_EVERY).append("th): **").append(sampled).append("**\n\n");
        md.append("Mods listed below are the ones triggering chunk loads from non-vanilla code paths. ")
          .append("High counts here are the #1 cause of late-game TPS drops.\n\n");

        List<Map.Entry<String, Long>> sorted = new java.util.ArrayList<>(snapshot.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        md.append("| mod | sampled loads | est. share |\n|---|---|---|\n");
        for (var e : sorted) {
            double share = sampled == 0 ? 0 : e.getValue() * 100.0 / sampled;
            md.append("| `").append(e.getKey()).append("` | ").append(e.getValue()).append(" | ")
              .append(String.format("%.1f", share)).append("% |\n");
        }
        Files.writeString(file, md.toString());
        LOGGER.info("Chunk attribution written → {}", file.getFileName());
    }
}
