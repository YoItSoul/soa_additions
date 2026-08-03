package com.soul.soa_additions.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.compat.StartupProfiler;
import com.soul.soa_additions.config.ModConfigs;
import com.soul.soa_additions.optimizer.SparkIntegration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * One-shot, fire-and-forget telemetry reporter. Sends a single JSON payload to a
 * configured endpoint on the first load-complete after launch, on a daemon thread,
 * with a short connect timeout, so there is no measurable impact on startup time.
 *
 * <p><b>Privacy — strictly opt-in:</b> nothing is ever sent unless the user has
 * explicitly consented via {@link TelemetryConsentScreen} (clients) or the consent
 * file (dedicated servers) — see {@link TelemetryConsent}. {@link ModConfigs#ENABLE_TELEMETRY}
 * is an additional master switch, and telemetry is disabled entirely in dev
 * environments. This telemetry exists only to troubleshoot and optimize the pack
 * during development and will be disabled for the stable public release.
 *
 * <p>Never sends file paths, environment variables, or any JVM arg matching
 * common secret patterns. An anonymous install UUID is persisted to
 * {@code config/soa_additions/install_id.txt}. The player's Minecraft username
 * IS included (disclosed on the consent screen) because the modpack author needs
 * it to correlate reports with support tickets.
 */
public final class Telemetry {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_Telemetry");
    private static final AtomicBoolean SENT = new AtomicBoolean(false);
    private static final AtomicBoolean SPARK_SENT = new AtomicBoolean(false);

    // Cached so the follow-up spark send can reuse the same identifying info
    // without re-reading configs or re-detecting env.
    private static volatile String cachedMcVersion;
    private static volatile String cachedForgeVersion;
    private static volatile String cachedEndpoint;
    private static volatile String lastSparkUrl;

    // Heartbeat scheduler — single daemon thread, created lazily.
    private static ScheduledExecutorService heartbeatExec;
    private static ScheduledFuture<?> heartbeatTask;

    /** JVM arg names / prefixes that look secret-ish and must be stripped before sending. */
    private static final Pattern SECRET_ARG = Pattern.compile(
            "(?i)" +
            "(^-?-?(accessToken|session|uuid|password|token|secret|apikey|api-key|auth)=)" +
            "|(^-?-?accessToken$)" +
            "|(^--accessToken$)" +
            "|(^-Dminecraft\\.api\\.(auth|account|session)\\.)"
    );

    private Telemetry() {}

    /** Call from FMLLoadCompleteEvent (or equivalent). Safe to call from any thread. */
    public static void sendAsync(String minecraftVersion, String forgeVersion) {
        // Cache before any gate so a later consent grant can replay the send.
        cachedMcVersion = minecraftVersion;
        cachedForgeVersion = forgeVersion;

        // Config may not be loaded in some edge cases; bail safely.
        final boolean enabled;
        final String endpoint;
        try {
            enabled = ModConfigs.ENABLE_TELEMETRY.get();
            endpoint = ModConfigs.TELEMETRY_ENDPOINT.get();
        } catch (Throwable t) {
            return;
        }
        if (!enabled) {
            LOGGER.info("Telemetry disabled in config; skipping.");
            return;
        }
        if (endpoint == null || endpoint.isBlank()) {
            LOGGER.info("Telemetry endpoint not configured; skipping.");
            return;
        }
        if (FMLEnvironment.production == false) {
            // Don't spam telemetry from the dev workspace.
            LOGGER.info("Telemetry skipped in dev environment.");
            return;
        }

        // Strict opt-in: without recorded consent, nothing leaves the machine.
        TelemetryConsent.State consent = TelemetryConsent.get();
        if (consent == TelemetryConsent.State.DECLINED) {
            LOGGER.info("Telemetry declined by user; nothing will be sent.");
            return;
        }
        if (consent == TelemetryConsent.State.UNDECIDED) {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                LOGGER.info("Telemetry awaiting consent. To opt in on a dedicated server, write "
                        + "'accepted' to config/soa_additions/telemetry_consent.txt.");
            }
            // Clients: the first-launch consent screen calls onConsentGranted()
            // if the player opts in, which re-enters this method.
            return;
        }

        if (!SENT.compareAndSet(false, true)) return;
        cachedEndpoint = endpoint;

        Thread t = new Thread(() -> {
            try {
                String json = buildPayload(minecraftVersion, forgeVersion, null, false, null);
                postJson(endpoint, json);
            } catch (Throwable ex) {
                // Telemetry must NEVER crash the client. Swallow everything.
                LOGGER.debug("Telemetry send failed (ignored): {}", ex.toString());
            }
        }, "SOA-Telemetry");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /** Called by the consent screen when the player opts in — replays the initial send. */
    public static void onConsentGranted() {
        sendAsync(cachedMcVersion != null ? cachedMcVersion : "unknown",
                cachedForgeVersion != null ? cachedForgeVersion : "unknown");
    }

    /**
     * Kicks off a spark profile (if spark is installed and the feature is enabled)
     * and, once the profile finishes, re-sends the telemetry payload with the spark
     * URL attached. The server uses {@code install_id} as a unique key, so this
     * second send simply overwrites the row from {@link #sendAsync}.
     *
     * <p>Call from {@code ServerStartedEvent}. Safe to call multiple times — only
     * the first call per launch actually does anything.
     */
    public static void sendSparkUpdateAsync() {
        if (!SPARK_SENT.compareAndSet(false, true)) return;

        final boolean enabled;
        final boolean autoSpark;
        try {
            enabled = ModConfigs.ENABLE_TELEMETRY.get();
            autoSpark = ModConfigs.TELEMETRY_AUTO_SPARK.get();
        } catch (Throwable t) {
            return;
        }
        if (!enabled || !autoSpark) return;
        if (!TelemetryConsent.isAccepted()) return;
        if (cachedEndpoint == null) return;
        if (!FMLEnvironment.production) return;
        if (!SparkIntegration.isSparkInstalled()) {
            LOGGER.info("Spark not installed; skipping spark-enriched telemetry.");
            return;
        }

        Thread t = new Thread(() -> {
            try {
                LOGGER.info("Starting 120s spark profile for telemetry enrichment.");
                String sparkUrl = SparkIntegration.tryStartProfileAndCaptureUrl(120)
                        .get(180, java.util.concurrent.TimeUnit.SECONDS);
                if (sparkUrl == null) {
                    LOGGER.info("Spark profile did not produce a URL; skipping update send.");
                    return;
                }
                LOGGER.info("Spark profile uploaded: {}. Sending telemetry update.", sparkUrl);
                lastSparkUrl = sparkUrl;
                String json = buildPayload(cachedMcVersion, cachedForgeVersion, sparkUrl, false, null);
                postJson(cachedEndpoint, json);
            } catch (Throwable ex) {
                LOGGER.debug("Spark telemetry update failed (ignored): {}", ex.toString());
            }
        }, "SOA-Telemetry-Spark");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    // ---------------------------------------------------------------------
    // Heartbeat — "is playing" tracking
    // ---------------------------------------------------------------------

    /**
     * Call from client-side world-join events (ClientPlayerNetworkEvent.LoggingIn).
     * Starts a periodic "I'm still playing" heartbeat that upserts the player's row
     * with {@code is_playing=true}, {@code current_dimension}, refreshed heap stats,
     * and a fresh {@code last_active_at} timestamp.
     *
     * <p>Safe to call multiple times — only the first start per session actually
     * schedules a task. Stop with {@link #stopHeartbeat()}.
     */
    public static void startHeartbeat() {
        final boolean enabled;
        final int intervalMinutes;
        try {
            enabled = ModConfigs.ENABLE_TELEMETRY.get();
            intervalMinutes = ModConfigs.TELEMETRY_HEARTBEAT_MINUTES.get();
        } catch (Throwable t) {
            return;
        }
        if (!enabled) return;
        if (!TelemetryConsent.isAccepted()) return;
        if (cachedEndpoint == null) return; // initial send hasn't happened yet
        if (!FMLEnvironment.production) return;

        synchronized (Telemetry.class) {
            if (heartbeatExec == null) {
                heartbeatExec = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "SOA-Telemetry-Heartbeat");
                    t.setDaemon(true);
                    t.setPriority(Thread.MIN_PRIORITY);
                    return t;
                });
            }
            if (heartbeatTask != null && !heartbeatTask.isDone()) {
                return; // already running
            }
            long period = Math.max(1, intervalMinutes) * 60L;
            heartbeatTask = heartbeatExec.scheduleAtFixedRate(() -> {
                try {
                    String dim = ClientIdentity.getCurrentDimensionOrNull();
                    boolean playing = dim != null;
                    String json = buildPayload(cachedMcVersion, cachedForgeVersion, lastSparkUrl, playing, dim);
                    postJson(cachedEndpoint, json);
                } catch (Throwable ex) {
                    LOGGER.debug("Telemetry heartbeat failed (ignored): {}", ex.toString());
                }
            }, 0, period, TimeUnit.SECONDS);
            LOGGER.info("Telemetry heartbeat started ({}m interval).", intervalMinutes);
        }
    }

    public static synchronized boolean isHeartbeatRunning() {
        return heartbeatTask != null && !heartbeatTask.isDone();
    }

    /**
     * False when the heartbeat can never start this session (telemetry disabled
     * in config, or a non-production environment) — lets the client tick hook
     * stop retrying instead of polling forever. Returns true while the config
     * hasn't loaded yet so the caller keeps retrying until it has.
     */
    public static boolean heartbeatPossible() {
        if (!FMLEnvironment.production) return false;
        if (TelemetryConsent.get() == TelemetryConsent.State.DECLINED) return false;
        // UNDECIDED keeps returning true: the consent screen resolves it early
        // in the session, after which the heartbeat can start (or stop retrying).
        try {
            return ModConfigs.ENABLE_TELEMETRY.get();
        } catch (Throwable t) {
            return true; // config not loaded yet — keep retrying
        }
    }

    /**
     * Sends a beat that explicitly reports not-playing. Used on client logout,
     * where the level may not be torn down yet when the event fires — recomputing
     * the dimension there would report is_playing=true for up to a full heartbeat
     * interval after the player already quit to the menu.
     */
    public static void sendImmediateOfflineBeat() {
        if (!TelemetryConsent.isAccepted()) return;
        if (cachedEndpoint == null) return;
        if (!FMLEnvironment.production) return;
        Thread t = new Thread(() -> {
            try {
                String json = buildPayload(cachedMcVersion, cachedForgeVersion, lastSparkUrl, false, null);
                postJson(cachedEndpoint, json);
            } catch (Throwable ex) {
                LOGGER.debug("Telemetry offline beat failed (ignored): {}", ex.toString());
            }
        }, "SOA-Telemetry-Beat");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /** Fires one immediate heartbeat off the main thread. Safe to call anytime. */
    public static void sendImmediateHeartbeat() {
        if (!TelemetryConsent.isAccepted()) return;
        if (cachedEndpoint == null) return;
        if (!FMLEnvironment.production) return;
        Thread t = new Thread(() -> {
            try {
                String dim = ClientIdentity.getCurrentDimensionOrNull();
                boolean playing = dim != null;
                String json = buildPayload(cachedMcVersion, cachedForgeVersion, lastSparkUrl, playing, dim);
                postJson(cachedEndpoint, json);
            } catch (Throwable ex) {
                LOGGER.debug("Telemetry immediate heartbeat failed (ignored): {}", ex.toString());
            }
        }, "SOA-Telemetry-Beat");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /**
     * Kept for API compatibility. No longer used by the client hooks — heartbeat
     * now runs for the entire client lifetime and is_playing is computed dynamically.
     */
    public static void stopHeartbeat() {
        synchronized (Telemetry.class) {
            if (heartbeatTask != null) {
                heartbeatTask.cancel(false);
                heartbeatTask = null;
            }
            // Release the scheduler thread too — startHeartbeat lazily recreates
            // it, so integrated-server open/close cycles don't leak idle threads.
            if (heartbeatExec != null) {
                heartbeatExec.shutdown();
                heartbeatExec = null;
            }
        }
        if (!TelemetryConsent.isAccepted()) return;
        if (cachedEndpoint == null) return;
        if (!FMLEnvironment.production) return;

        Thread t = new Thread(() -> {
            try {
                String json = buildPayload(cachedMcVersion, cachedForgeVersion, lastSparkUrl, false, null);
                postJson(cachedEndpoint, json);
                LOGGER.info("Telemetry heartbeat stopped, offline state sent.");
            } catch (Throwable ex) {
                LOGGER.debug("Telemetry offline send failed (ignored): {}", ex.toString());
            }
        }, "SOA-Telemetry-Offline");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    // ---------------------------------------------------------------------
    // Payload
    // ---------------------------------------------------------------------

    private static String buildPayload(String mcVersion, String forgeVersion, String sparkProfileUrl,
                                       boolean isPlaying, String currentDimension) {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        com.sun.management.OperatingSystemMXBean osBean = null;
        try {
            osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        } catch (Throwable ignored) {}

        Payload p = new Payload();
        p.schema_version = 1;
        p.sent_at = Instant.now().toString();
        p.install_id = getOrCreateInstallId();
        p.mod_version = readModVersion();

        // Minecraft identity
        p.minecraft = new MinecraftInfo();
        p.minecraft.version = mcVersion;
        p.minecraft.forge_version = forgeVersion;
        p.minecraft.loader = "forge";
        p.minecraft.side = FMLEnvironment.dist == Dist.CLIENT ? "client" : "server";
        p.minecraft.mod_count = ModList.get() != null ? ModList.get().size() : -1;
        p.minecraft.player_username = ClientIdentity.getUsernameOrNull();
        p.minecraft.player_uuid = ClientIdentity.getUuidOrNull();

        // OS
        p.os = new OsInfo();
        p.os.name = System.getProperty("os.name");
        p.os.version = System.getProperty("os.version");
        p.os.arch = System.getProperty("os.arch");
        p.os.available_processors = Runtime.getRuntime().availableProcessors();
        p.os.locale = Locale.getDefault().toString();
        p.os.timezone = TimeZone.getDefault().getID();

        if (osBean != null) {
            try {
                p.os.cpu_name = tryReadCpuName();
                p.os.total_memory_mb = osBean.getTotalMemorySize() / (1024 * 1024);
                p.os.free_memory_mb = osBean.getFreeMemorySize() / (1024 * 1024);
                p.os.system_load_avg = osBean.getSystemLoadAverage();
            } catch (Throwable ignored) {}
        }

        // JVM
        p.jvm = new JvmInfo();
        p.jvm.vendor = System.getProperty("java.vendor");
        p.jvm.version = System.getProperty("java.version");
        p.jvm.runtime_name = runtime.getVmName();
        p.jvm.runtime_version = runtime.getVmVersion();
        p.jvm.uptime_ms_at_send = runtime.getUptime();
        p.jvm.max_heap_mb = mem.getHeapMemoryUsage().getMax() / (1024 * 1024);
        p.jvm.committed_heap_mb = mem.getHeapMemoryUsage().getCommitted() / (1024 * 1024);
        p.jvm.args = sanitizeJvmArgs(runtime.getInputArguments());

        // Load time (from StartupProfiler if available)
        p.load_time = new LoadTimeInfo();
        p.load_time.total_load_ms = StartupProfiler.getTotalLoadMs();
        p.load_time.construct_ms = StartupProfiler.getConstructMs();
        p.load_time.common_setup_ms = StartupProfiler.getCommonSetupMs();

        // GPU (client only; requires LWJGL to be available)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            try {
                p.gpu = ClientIdentity.getGpuInfoOrNull();
            } catch (Throwable ignored) {}
            // In-world playtime; flush piggybacks on sends so the lifetime
            // counter survives crashes without its own write cadence.
            try {
                p.play_session_minutes = PlaytimeTracker.sessionMinutes();
                p.play_total_minutes = PlaytimeTracker.totalMinutes();
                PlaytimeTracker.flush();
            } catch (Throwable ignored) {}
        }

        // Detailed hardware via OSHI (cached after first probe)
        try {
            p.hardware = HardwareProbe.get();
            if (p.hardware != null && p.hardware.cpu != null && p.hardware.cpu.name != null) {
                p.os.cpu_name = p.hardware.cpu.name.trim();
            }
        } catch (Throwable ignored) {}

        // Server-side runtime perf (populated whenever a MinecraftServer is running —
        // dedicated server always, integrated server while a singleplayer world is open).
        try {
            p.server = ServerIdentity.snapshotOrNull();
        } catch (Throwable ignored) {}

        // Optional spark profile URL from an earlier capture
        p.spark_profile_url = sparkProfileUrl;

        // Heartbeat / activity state
        p.is_playing = isPlaying;
        p.current_dimension = currentDimension;

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(p);
    }

    // ---------------------------------------------------------------------
    // HTTP
    // ---------------------------------------------------------------------

    /** Reuse a single HttpClient across all posts — each {@code newBuilder().build()}
     *  spins up a thread pool and an SSL context, which is unnecessary overhead
     *  when every request targets the same endpoint. */
    private static final HttpClient SHARED_HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static void postJson(String endpoint, String json) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("User-Agent", "SoaAdditionsTelemetry/1")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = SHARED_HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        int sc = res.statusCode();
        if (sc >= 200 && sc < 300) {
            LOGGER.info("Telemetry sent OK ({} bytes, HTTP {}).", json.length(), sc);
        } else {
            LOGGER.debug("Telemetry server responded HTTP {}: {}", sc, res.body());
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static List<String> sanitizeJvmArgs(List<String> raw) {
        List<String> out = new ArrayList<>(raw.size());
        for (String arg : raw) {
            if (arg == null) continue;
            if (SECRET_ARG.matcher(arg).find()) {
                out.add("[redacted]");
                continue;
            }
            out.add(scrubPaths(arg));
        }
        return out;
    }

    /**
     * Any absolute path, Windows ({@code E:\dir\file}) or POSIX ({@code /dir/file}).
     * The previous scrubber only knew the three standard home directories, so an
     * install rooted anywhere else — {@code E:\curseforge}, {@code /srv/mc} — shipped
     * its whole directory tree in {@code --module-path} and friends, which the consent
     * screen promises we never send. Keeps the final segment: jar and native library
     * names are the part that is actually diagnostic.
     */
    private static final Pattern ABSOLUTE_PATH = Pattern.compile(
            "([A-Za-z]:[\\\\/]|(?<![\\w.])/)(?:[^;,\"\\s:]*[\\\\/])*");

    private static String scrubPaths(String arg) {
        java.util.regex.Matcher m = ABSOLUTE_PATH.matcher(arg);
        StringBuilder sb = new StringBuilder(arg.length());
        while (m.find()) m.appendReplacement(sb, "<path>/");
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * install_id that leaked into the modpack export's overrides/config folder
     * (Souls of Avarice.zip, Apr 2026). Every player who installed that pack
     * inherited this exact id, collapsing all their reports into one DB row.
     * Treat it as poisoned: regenerate a fresh id when we see it.
     */
    private static final String LEAKED_INSTALL_ID = "db26574e-c088-4c89-9044-2e63f2e7bb1a";

    /**
     * The install_id is a plain random UUID with no machine binding.
     *
     * <p>v3.61.5 through v3.62.0 hashed {@code user.name} + {@code user.home} into a
     * second "binding" line, so an install_id.txt copied between machines would fail
     * the check and rotate. That closed the copied-id bug class, but reading the OS
     * user name and home directory, hashing them, persisting the digest and then
     * POSTing from the same class is — to a static analyzer — an unbroken user
     * fingerprinting chain, and it put every upload into CurseForge's manual review
     * queue. The inputs were never transmitted, but a scanner cannot know that.
     *
     * <p>It is also redundant: the composite builder on the telemetry host strips
     * install_id/telemetry_consent/playtime_minutes from every repack (deployed
     * 2026-08-01), so an id can no longer leave this machine inside a pack export in
     * the first place. Residual duplicates are handled server-side at ingest.
     */
    private static String getOrCreateInstallId() {
        try {
            Path dir = Path.of("config", "soa_additions");
            Files.createDirectories(dir);
            Path file = dir.resolve("install_id.txt");
            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file);
                String id = lines.isEmpty() ? "" : lines.get(0).trim();
                if (!id.isEmpty() && !id.equalsIgnoreCase(LEAKED_INSTALL_ID)) {
                    // Files written by 3.61.5-3.62.0 carry a binding hash on line 2.
                    // Keep the id — rotating it would churn the identity of every
                    // install that updated through those versions — and drop the
                    // second line on the way out.
                    if (lines.size() > 1) writeInstallId(file, id);
                    return id;
                }
            }
            String id = UUID.randomUUID().toString();
            writeInstallId(file, id);
            return id;
        } catch (IOException e) {
            return "unknown-" + UUID.randomUUID();
        }
    }

    private static void writeInstallId(Path file, String id) throws IOException {
        Files.writeString(file, id,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String readModVersion() {
        try {
            return ModList.get().getModContainerById(SoaAdditions.MODID)
                    .map(mc -> mc.getModInfo().getVersion().toString())
                    .orElse("unknown");
        } catch (Throwable t) {
            return "unknown";
        }
    }

    private static String tryReadCpuName() {
        // Reading a real CPU model name cross-platform without JNI is ugly.
        // We just report os.arch and processor count; anything more needs OSHI.
        // If the modpack ships OSHI we could enrich this later.
        String arch = System.getProperty("os.arch", "unknown");
        int cores = Runtime.getRuntime().availableProcessors();
        return arch + " (" + cores + " logical cores)";
    }

    // ---------------------------------------------------------------------
    // Payload shape (serialized by Gson)
    // ---------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static final class Payload {
        int schema_version;
        String sent_at;
        String install_id;
        String mod_version;
        MinecraftInfo minecraft;
        OsInfo os;
        JvmInfo jvm;
        LoadTimeInfo load_time;
        GpuInfo gpu;
        HardwareInfo hardware;
        String spark_profile_url;
        boolean is_playing;
        String current_dimension;
        Integer play_session_minutes;
        Integer play_total_minutes;
        ServerInfo server;
    }

    @SuppressWarnings("unused")
    static final class ServerInfo {
        boolean is_dedicated;
        String motd;
        int player_count;
        int player_max;
        List<String> player_names;
        double tps;
        double mspt_mean;
        double mspt_peak;
        long loaded_chunks;
        long loaded_entities;
        List<String> dimensions;
        int view_distance;
        int simulation_distance;
    }

    @SuppressWarnings("unused")
    static final class HardwareInfo {
        CpuInfo cpu;
        long memory_total_mb;
        long memory_available_mb;
        long memory_page_size;
        List<MemoryStickInfo> memory_sticks;
        SystemInfoBlock system;
        List<GpuCardInfo> gpus;
        List<DiskInfo> disks;
        String os_family;
        String os_manufacturer;
        String os_version_build;
        int os_bitness;
    }

    @SuppressWarnings("unused")
    static final class CpuInfo {
        String name;
        String identifier;
        String vendor;
        String microarchitecture;
        String model;
        String family;
        String stepping;
        boolean is_64bit;
        int physical_package_count;
        int physical_core_count;
        int logical_core_count;
        long max_freq_hz;
    }

    @SuppressWarnings("unused")
    static final class MemoryStickInfo {
        String bank_label;
        long capacity_mb;
        long clock_speed_hz;
        String manufacturer;
        String memory_type;
    }

    @SuppressWarnings("unused")
    static final class SystemInfoBlock {
        String manufacturer;
        String model;
        String board_manufacturer;
        String board_model;
        String board_version;
    }

    @SuppressWarnings("unused")
    static final class GpuCardInfo {
        String name;
        String vendor;
        long vram_mb;
        String version_info;
    }

    @SuppressWarnings("unused")
    static final class DiskInfo {
        String model;
        long size_mb;
    }

    @SuppressWarnings("unused")
    static final class MinecraftInfo {
        String version;
        String forge_version;
        String loader;
        String side;
        int mod_count;
        String player_username;
        String player_uuid;
    }

    @SuppressWarnings("unused")
    static final class OsInfo {
        String name;
        String version;
        String arch;
        int available_processors;
        String cpu_name;
        long total_memory_mb;
        long free_memory_mb;
        double system_load_avg;
        String locale;
        String timezone;
    }

    @SuppressWarnings("unused")
    static final class JvmInfo {
        String vendor;
        String version;
        String runtime_name;
        String runtime_version;
        long uptime_ms_at_send;
        long max_heap_mb;
        long committed_heap_mb;
        List<String> args;
    }

    @SuppressWarnings("unused")
    static final class LoadTimeInfo {
        long total_load_ms;
        long construct_ms;
        long common_setup_ms;
    }

    @SuppressWarnings("unused")
    static final class GpuInfo {
        String renderer;
        String vendor;
        String version;
        int screen_width;
        int screen_height;
    }
}
