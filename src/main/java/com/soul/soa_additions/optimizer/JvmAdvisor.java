package com.soul.soa_additions.optimizer;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;

/**
 * Generates the launch JVM arguments this pack wants, sized to the machine it is asked about.
 *
 * <p><strong>Why this exists as a copy-able string rather than a live tuner.</strong> On
 * HotSpot only flags marked {@code manageable} can be written after startup. On Java 17 that
 * is the whole list:</p>
 *
 * <pre>
 *   G1PeriodicGCInterval  G1PeriodicGCSystemLoadThreshold  SoftMaxHeapSize
 *   MinHeapFreeRatio      MaxHeapFreeRatio                 HeapDump*
 * </pre>
 *
 * <p>Every flag that actually governs pause behaviour — {@code MaxTenuringThreshold},
 * {@code MaxGCPauseMillis}, {@code SurvivorRatio}, {@code G1NewSizePercent},
 * {@code -Xmx} — is fixed for the life of the process. So the durable fix has to be applied
 * at launch, and {@link GcGovernor} handles only the manageable subset at runtime.</p>
 *
 * <h2>What was wrong with the previous profile</h2>
 *
 * <p>The pack shipped Aikar's flags, which are tuned for a headless server where a 150ms
 * pause is invisible. On a client they are actively harmful. Measured on this pack at 11G
 * (JFR, 435s of travel):</p>
 *
 * <ul>
 *   <li>{@code MaxTenuringThreshold=1} — the tenuring distribution showed 4399 MB at age 1
 *       and <em>zero bytes at every age 2 through 15</em>. Every object that survived one
 *       collection was force-promoted to old gen instead of being allowed to die young.
 *       That drove 16 concurrent old-gen cycles in 7 minutes.</li>
 *   <li>{@code MaxGCPauseMillis=143} — G1 sizes Eden to hit its pause target, so it did:
 *       the longest observed pause was 139ms.</li>
 *   <li>{@code G1NewSizePercent=33} pinned Eden to a ~4GB floor, and evacuation cost scales
 *       with surviving bytes.</li>
 * </ul>
 *
 * <p>Net effect: mean stop-the-world pause 28.2ms, p90 61.7ms — about 10 dropped frames per
 * minute at 60fps. The same pack on 8G before the heap was raised averaged 4.0ms, because a
 * smaller heap accidentally limited the damage.</p>
 */
public final class JvmAdvisor {

    /** Set by the recommended argument list; its absence means the user is on untuned flags. */
    public static final String PROFILE_PROPERTY = "soa.gcprofile";
    public static final String PROFILE_VALUE = "client-lowpause";

    private JvmAdvisor() {}

    /**
     * Heap size for a machine with {@code totalRamGb} of physical memory.
     *
     * <p>Deliberately a table rather than a percentage. Off-heap cost for this pack is roughly
     * constant — ~770MB metaspace (measured peak 746MB), 400MB code cache, plus native and
     * driver allocations, so budget about 3G beyond the heap — and a flat fraction
     * over-allocates on small machines while under-allocating on large ones.</p>
     *
     * <h2>Why this caps at 8G instead of scaling up</h2>
     *
     * <p>Bigger is not better, and this pack has the measurements to prove it. It ran a
     * 34-hour session at 8G with a 4.93ms average GC pause and 7 old-gen collections in the
     * whole session. Raising the heap to 11G made it <em>worse</em> — mean pause 28.2ms and
     * 16 old-gen cycles in 7 minutes — because young-gen sizing scaled with the heap and
     * evacuation cost scales with surviving bytes.</p>
     *
     * <p>Handing a 32G machine a 12G heap therefore buys nothing but longer pauses. Above 8G
     * the extra memory is better left to the OS file cache, which is doing useful work for
     * chunk I/O. The goal is the smallest heap that avoids constant mixed collections, not the
     * largest one that fits.</p>
     */
    public static int recommendedHeapGb(long totalRamGb) {
        if (totalRamGb < 12) return 5;
        if (totalRamGb < 16) return 6;
        if (totalRamGb < 24) return 7;
        return 8;
    }

    /**
     * Which machine this advice is for. The two are genuinely opposite problems.
     *
     * <p>A client is tuned for <em>frame consistency</em>: a 150ms pause is a visible lurch, so
     * short pauses are worth paying throughput for. A dedicated server has no frame budget at
     * all — nobody perceives a pause between ticks — so it should take long pauses in exchange
     * for collecting more efficiently, and it holds far more loaded chunks and player data, so
     * its live set is larger.</p>
     *
     * <p>This is why Aikar's flags are correct on a server and were wrong on a client: they
     * were designed for exactly this case.</p>
     */
    public enum Profile { CLIENT, SERVER }

    /** Server heaps hold more chunks and players, and are not capped for pause reasons. */
    public static int recommendedHeapGb(long totalRamGb, Profile profile) {
        if (profile == Profile.CLIENT) return recommendedHeapGb(totalRamGb);
        if (totalRamGb <= 0) return 8;
        long budget = totalRamGb - (totalRamGb <= 8 ? 2 : 4);   // leave room for the OS
        return (int) Math.max(4, Math.min(16, budget));
    }

    /** Which profile this process should be advised about. */
    public static Profile currentProfile() {
        return net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()
                ? Profile.CLIENT : Profile.SERVER;
    }

    /** How a heap recommendation was arrived at, so the player can see the reasoning. */
    public record Recommendation(int heapGb, String basis, boolean measured) {}

    /**
     * The per-machine recommendation: measured where possible, table where not.
     *
     * <p>The RAM table above is a fallback. It answers "what does a machine like yours usually
     * need", which is the best you can do before the game has run. Once it has, this JVM knows
     * something better — its own live set — and that reflects the player's actual render
     * distance, shader choice, Lite Mode state and world. Two players on identical 32G machines
     * can legitimately need different heaps, and only the measurement sees that.</p>
     *
     * <p>The RAM ceiling still applies: an 8G machine cannot have a 9G heap however large its
     * live set is, and a machine that measures small is not given more than the table's tier
     * either, because the cap exists to protect pause times, not just memory.</p>
     */
    public static Recommendation recommend(HardwareProfile hw, HeapMeasurement heap) {
        return recommend(hw, heap, currentProfile());
    }

    public static Recommendation recommend(HardwareProfile hw, HeapMeasurement heap, Profile profile) {
        int fromTable = hw.ramUnknown()
                ? recommendedHeapGb(16, profile)
                : recommendedHeapGb(hw.totalRamGb(), profile);

        if (!heap.measured()) {
            String why = heap.tooEarly()
                    ? "system RAM (play ~5 more minutes for a measured figure)"
                    : "system RAM";
            return new Recommendation(fromTable, why, false);
        }

        int fromMeasurement = heap.impliedHeapGb();
        int chosen = Math.min(fromMeasurement, fromTable);
        String basis = String.format(Locale.ROOT,
                "measured live set %.1fG x1.3 = %dG%s",
                heap.liveSetMb() / 1024.0, fromMeasurement,
                fromMeasurement > fromTable ? ", capped at " + fromTable + "G for your RAM" : "");
        return new Recommendation(chosen, basis, true);
    }

    /** True when the pack's measured working set does not fit the machine. */
    public static boolean belowRecommendedRam(long totalRamGb) {
        return totalRamGb < 14;
    }

    /**
     * G1 region size in MB, targeting roughly 1500-2000 regions.
     *
     * <p>Kept numerically identical to {@code calcRegionSize} in the site's JVM wizard
     * (soulsofavarice.com/#jvm-wizard) so that {@code /soa jvm} and the web page never hand a
     * player two different answers for the same machine. Change both or neither.</p>
     */
    static int regionSizeMb(int heapGb) {
        double ideal = (heapGb * 1024.0) / 1500.0;
        int best = 4;
        for (int p : new int[]{1, 2, 4, 8, 16, 32}) {
            if (p <= ideal * 1.3) best = p;
        }
        return Math.max(1, Math.min(32, best));
    }

    // Hardware detection deliberately does NOT live here — see HardwareProfile. This class
    // turns a machine description into launch flags; it does not decide what the machine is.

    /**
     * The recommended argument line for a given heap size.
     *
     * <p>Three things scale with heap rather than being fixed, because a profile that suits a
     * 32G desktop actively hurts an 8G laptop:</p>
     *
     * <ul>
     *   <li><strong>{@code AlwaysPreTouch}</strong> commits the entire heap at startup. On a
     *       roomy machine that trades a few seconds of load time for no page-fault stalls
     *       later. On a RAM-constrained one it is how you end up swapping, so it is only
     *       enabled from 8G of heap up.</li>
     *   <li><strong>{@code MaxGCPauseMillis}</strong> is a target G1 meets by shrinking Eden.
     *       A small heap already collects often; demanding 25ms there just raises GC frequency
     *       for no smoothness gain, so small heaps get a looser target.</li>
     *   <li><strong>{@code G1HeapRegionSize}</strong> wants roughly 2048 regions. Forcing 8M on
     *       a 4G heap gives 512 oversized regions and coarse, wasteful collections.</li>
     * </ul>
     *
     * <p>Metaspace and code cache are <em>not</em> scaled: they are driven by the pack's mod
     * count, not the user's RAM. A small machine still loads all 508 mods, so trimming them
     * would only cause repeated metaspace-triggered collections during load.</p>
     */
    public static String argsFor(int heapGb) {
        return argsFor(heapGb, GcCapability.measure(), currentProfile());
    }

    public static String argsFor(int heapGb, GcCapability gc) {
        return argsFor(heapGb, gc, currentProfile());
    }

    /**
     * Aikar's flags, which are the right answer for a dedicated server.
     *
     * <p>Values follow the same curves as the site's JVM wizard so the two never disagree —
     * {@code norm(gb) = (gb - 4) / 12} interpolated across a 4-16G range. The tenuring settings
     * that were harmful on a client are correct here: server objects really are either per-tick
     * garbage or long-lived, so promoting immediately avoids pointless survivor copying.</p>
     */
    private static String serverArgs(int heapGb) {
        double n = Math.max(0, Math.min(1, (heapGb - 4) / 12.0));
        int newMin = (int) Math.round(lerp(45, 25, n));
        int newMax = newMin + 10;
        int reserve = (int) Math.round(lerp(25, 12, n));
        int ihop = (int) Math.round(lerp(12, 22, n));
        int pause = (int) Math.round(lerp(160, 200, n));
        int metaInit = (int) Math.round(lerp(384, 512, n));

        return String.join(" ",
                "-Xms" + heapGb + "G", "-Xmx" + heapGb + "G",
                "-XX:+UseG1GC",
                "-XX:G1HeapRegionSize=" + regionSizeMb(heapGb) + "M",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:G1NewSizePercent=" + newMin,
                "-XX:G1MaxNewSizePercent=" + newMax,
                "-XX:G1ReservePercent=" + reserve,
                "-XX:InitiatingHeapOccupancyPercent=" + ihop,
                "-XX:MaxGCPauseMillis=" + pause,
                "-XX:SurvivorRatio=32",
                "-XX:MaxTenuringThreshold=1",
                "-XX:G1MixedGCCountTarget=4",
                "-XX:G1MixedGCLiveThresholdPercent=90",
                "-XX:G1HeapWastePercent=5",
                "-XX:G1RSetUpdatingPauseTimePercent=5",
                "-XX:+ParallelRefProcEnabled",
                "-XX:+AlwaysPreTouch",
                "-XX:+UseStringDeduplication",
                "-XX:+DisableExplicitGC",
                "-XX:MetaspaceSize=" + metaInit + "m",
                "-XX:ReservedCodeCacheSize=400M",
                "-XX:NmethodSweepActivity=1",
                "-XX:+AlwaysActAsServerClassMachine",
                "-XX:-DontCompileHugeMethods",
                // Compile tick-hot methods 20x sooner so TPS recovers faster after a restart.
                "-XX:CompileThreshold=500",
                "-XX:+PerfDisableSharedMem",
                "-Dusing.aikars.flags=https://mcflags.emc.gs",
                "-Daikars.new.flags=true");
    }

    private static double lerp(double lo, double hi, double t) {
        return lo + (hi - lo) * t;
    }

    /**
     * Flags for this heap, with the pause target set from what the machine can actually deliver.
     *
     * <p>The pause target is the one flag that genuinely depends on CPU speed: G1 meets it by
     * resizing young gen, and a machine that cannot evacuate fast enough will pin young gen at
     * its 5% floor, miss the target anyway, and collect far more often than necessary. See
     * {@link GcCapability}.</p>
     */
    public static String argsFor(int heapGb, GcCapability gc, Profile profile) {
        if (profile == Profile.SERVER) return serverArgs(heapGb);

        StringBuilder sb = new StringBuilder(512);

        sb.append("-Xms").append(heapGb).append("G");
        sb.append(" -Xmx").append(heapGb).append("G");
        sb.append(" -XX:+UseG1GC");
        sb.append(" -XX:+UnlockExperimentalVMOptions");

        sb.append(" -XX:G1HeapRegionSize=").append(regionSizeMb(heapGb)).append("M");

        // The single most important value here, and the only one that depends on CPU speed.
        // Aikar's 143 told G1 that 143ms freezes were acceptable and it obliged — the longest
        // measured pause was 139ms. GcCapability lowers this to 25ms on a machine that can hold
        // it, and relaxes it on one that cannot rather than leaving G1 chasing an impossible
        // target with a permanently minimum-sized young generation.
        sb.append(" -XX:MaxGCPauseMillis=").append(gc.recommendedPauseMs(heapGb));

        sb.append(" -XX:G1RSetUpdatingPauseTimePercent=5");
        sb.append(" -XX:+ParallelRefProcEnabled");

        if (heapGb >= 8) {
            sb.append(" -XX:+AlwaysPreTouch");
        }

        // Kept on small heaps too: it costs one background thread but reclaims duplicate
        // strings, and memory is exactly what those machines are short of.
        sb.append(" -XX:+UseStringDeduplication");
        sb.append(" -XX:+DisableExplicitGC");

        // Measured peak metaspace for this pack is 746MB; starting below that forces repeated
        // metaspace-triggered collections during load.
        sb.append(" -XX:MetaspaceSize=768m");
        sb.append(" -XX:ReservedCodeCacheSize=400M");
        sb.append(" -XX:+AlwaysActAsServerClassMachine");

        // Double negative: this ENABLES compiling oversized methods, which modded Minecraft
        // is full of.
        sb.append(" -XX:-DontCompileHugeMethods");
        sb.append(" -XX:+PerfDisableSharedMem");
        sb.append(" -D").append(PROFILE_PROPERTY).append("=").append(PROFILE_VALUE);

        return sb.toString();
    }

    /** Notably absent from {@link #argsFor}, and why — shown by {@code /soa jvm why}. */
    public static List<String> removedFlagNotes() {
        return List.of(
                "-XX:MaxTenuringThreshold=1 — force-promoted every survivor straight to old gen "
                        + "(JFR: 4399MB at age 1, 0MB at ages 2-15). Now default 15.",
                "-XX:SurvivorRatio=32 — survivor space too small to hold a generation, so "
                        + "overflow promoted early. Now default 8.",
                "-XX:MaxGCPauseMillis=143 — G1 sizes Eden to hit this target; observed max "
                        + "pause was 139ms. Now 25.",
                "-XX:G1NewSizePercent=33 / G1MaxNewSizePercent=43 — pinned Eden near 4GB. "
                        + "Unpinned, G1 now adapts between 5% and 60%.",
                "-XX:InitiatingHeapOccupancyPercent=18 — disabled G1's adaptive IHOP. Removed.",
                "-XX:G1MixedGCCountTarget=4 — fewer, longer mixed collections. Now default 8.",
                "-XX:G1ReservePercent=17 — reclaims usable heap at default 10.",
                "-XX:NmethodSweepActivity=1 — removed; also invalid on JDK 20+.");
    }

    /** Whether this process was launched with the recommended profile. */
    public static boolean runningTunedProfile() {
        return PROFILE_VALUE.equals(System.getProperty(PROFILE_PROPERTY));
    }

    /** The GC-relevant flags this process actually started with. */
    public static List<String> currentGcArgs() {
        try {
            return ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                    .filter(a -> a.startsWith("-Xm") || a.startsWith("-XX:"))
                    .toList();
        } catch (Throwable t) {
            return List.of();
        }
    }

    /** Heap the running JVM was actually given, which is not necessarily what we recommend. */
    public static String runningHeapSummary() {
        return String.format(Locale.ROOT, "%.1fG allocated",
                Runtime.getRuntime().maxMemory() / (double) (1L << 30));
    }
}
