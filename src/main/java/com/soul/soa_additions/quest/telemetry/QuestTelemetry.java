package com.soul.soa_additions.quest.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.soul.soa_additions.anticheat.CheaterManager;
import com.soul.soa_additions.config.ModConfigs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side per-player event poster. Fires on:
 * <ul>
 *   <li>{@code cheat_flag} — player gets flagged by the anticheat</li>
 *   <li>{@code quest_claim} — throttled claim progress update</li>
 *   <li>{@code quest_complete} — player reaches 100% of non-optional quests
 *       in the current packmode (fires exactly once per player per world)</li>
 * </ul>
 *
 * <p>Every payload includes the player's uuid + name, world name, and
 * {@code cheated} boolean so the downstream telemetry service can decide
 * whether the player qualifies for the clean-run Discord role. The cheated
 * flag is authoritative from {@link CheaterManager} — never from the client.</p>
 *
 * <p>Reuses {@link ModConfigs#TELEMETRY_ENDPOINT} so admins don't have to
 * configure a second URL — the quest-event path is derived by swapping the
 * final segment of the configured URL (e.g. {@code /report} → {@code /quest-event}).
 * Events POST as JSON on a daemon thread and never throw into the calling
 * code — a failed send is logged at debug and forgotten.</p>
 */
public final class QuestTelemetry {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/quest-telemetry");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** Last quest_claim POST time per UUID (ms since epoch). Used to throttle. */
    private static final ConcurrentHashMap<UUID, Long> LAST_CLAIM_POST = new ConcurrentHashMap<>();
    /** Minimum ms between quest_claim posts for a single player. */
    private static final long CLAIM_THROTTLE_MS = 30_000L;

    /** Players who have already sent quest_complete this session. Cleared on
     *  server stop. World-level idempotence is enforced by the receiver plus
     *  the completion tracker's per-player latch. */
    private static final java.util.Set<UUID> COMPLETED_POSTED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private QuestTelemetry() {}

    public static void clearSessionState() {
        LAST_CLAIM_POST.clear();
        COMPLETED_POSTED.clear();
    }

    // ---------- public event API ----------

    public static void postCheatFlag(ServerPlayer player, String reason) {
        JsonObject body = baseBody(player, "cheat_flag");
        if (body == null) return;
        body.addProperty("reason", reason);
        send(body);
    }

    public static void postClaim(ServerPlayer player, String questFullId, int completed, int total) {
        long now = System.currentTimeMillis();
        Long prev = LAST_CLAIM_POST.get(player.getUUID());
        if (prev != null && now - prev < CLAIM_THROTTLE_MS) return;
        LAST_CLAIM_POST.put(player.getUUID(), now);

        JsonObject body = baseBody(player, "quest_claim");
        if (body == null) return;
        body.addProperty("quest", questFullId);
        body.addProperty("completed", completed);
        body.addProperty("total", total);
        body.addProperty("percent", total == 0 ? 0.0 : (completed * 100.0 / total));
        send(body);
    }

    /** Fire exactly once per player per session when they hit 100%. */
    public static void postComplete(ServerPlayer player, int completed, int total) {
        if (!COMPLETED_POSTED.add(player.getUUID())) return;
        JsonObject body = baseBody(player, "quest_complete");
        if (body == null) return;
        body.addProperty("completed", completed);
        body.addProperty("total", total);
        body.addProperty("percent", 100.0);
        send(body);
    }

    // ---------- payload + transport ----------

    private static JsonObject baseBody(ServerPlayer player, String event) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;

        JsonObject body = new JsonObject();
        body.addProperty("schema_version", 1);
        body.addProperty("event", event);
        body.addProperty("sent_at", Instant.now().toString());
        body.addProperty("player_uuid", player.getUUID().toString());
        body.addProperty("player_name", player.getGameProfile().getName());
        body.addProperty("world_name", worldName(server));
        body.addProperty("is_dedicated", server.isDedicatedServer());
        // Authoritative cheated flag — pulled from the canonical SavedData,
        // never trusted from the client.
        body.addProperty("cheated", CheaterManager.isFlagged(player));
        return body;
    }

    /**
     * Rewrite the configured heartbeat URL into the quest-event URL by
     * swapping the last path segment. {@code https://host/report} becomes
     * {@code https://host/quest-event}; a URL with no path or a trailing
     * slash gets {@code /quest-event} appended.
     */
    static String questEventEndpoint(String base) {
        String trimmed = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        int schemeEnd = trimmed.indexOf("://");
        int hostStart = schemeEnd < 0 ? 0 : schemeEnd + 3;
        int lastSlash = trimmed.lastIndexOf('/');
        if (lastSlash <= hostStart) {
            // No path segment — URL is just scheme + host.
            return trimmed + "/quest-event";
        }
        return trimmed.substring(0, lastSlash) + "/quest-event";
    }

    private static String worldName(MinecraftServer server) {
        try {
            return server.getWorldData().getLevelName();
        } catch (Throwable t) {
            return "unknown";
        }
    }

    private static void send(JsonObject body) {
        final boolean enabled;
        final String baseEndpoint;
        try {
            enabled = ModConfigs.ENABLE_TELEMETRY.get();
            baseEndpoint = ModConfigs.TELEMETRY_ENDPOINT.get();
        } catch (Throwable t) {
            return; // config not loaded yet
        }
        if (!enabled) return;
        if (baseEndpoint == null || baseEndpoint.isBlank()) return;
        if (!FMLEnvironment.production) {
            LOG.debug("Skipping quest telemetry in dev: {}", body);
            return;
        }
        final String endpoint = questEventEndpoint(baseEndpoint);

        final String json = GSON.toJson(body);
        Thread t = new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json; charset=utf-8")
                        .header("User-Agent", "SoaQuestTelemetry/1")
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                int sc = res.statusCode();
                if (sc < 200 || sc >= 300) {
                    LOG.debug("Quest telemetry returned HTTP {}: {}", sc, res.body());
                }
            } catch (Throwable ex) {
                LOG.debug("Quest telemetry send failed (ignored): {}", ex.toString());
            }
        }, "SOA-QuestTelemetry");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
}
