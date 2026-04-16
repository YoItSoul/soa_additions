package com.soul.soa_additions.quest.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.PackModeData;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.quest.layout.LayoutResult;
import com.soul.soa_additions.quest.layout.QuestLayout;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * Lightweight embedded HTTP server that exposes quest data and live progress
 * for a "second screen" browser experience. Each online player gets a unique
 * token URL so only they can view their own progress.
 *
 * <h3>Endpoints</h3>
 * <ul>
 *   <li>{@code GET /} — the HTML overlay page</li>
 *   <li>{@code GET /api/quests?token=<tok>} — full quest tree + progress JSON</li>
 *   <li>{@code GET /api/events?token=<tok>} — SSE stream of live progress updates</li>
 * </ul>
 */
public final class QuestWebServer {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/quest-web");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static HttpServer server;
    private static MinecraftServer mcServer;

    /** token → player UUID */
    private static final Map<String, UUID> TOKEN_TO_PLAYER = new ConcurrentHashMap<>();
    /** player UUID → token */
    private static final Map<UUID, String> PLAYER_TO_TOKEN = new ConcurrentHashMap<>();
    /** Active SSE connections keyed by player UUID */
    private static final Map<UUID, List<SseClient>> SSE_CLIENTS = new ConcurrentHashMap<>();
    /** Cached layout results — invalidated on quest definition reload. */
    private static volatile Map<String, LayoutResult> layoutCache = new ConcurrentHashMap<>();

    private static final SecureRandom RANDOM = new SecureRandom();

    private QuestWebServer() {}

    // ---------- lifecycle ----------

    public static void start(MinecraftServer mc, int port) {
        if (server != null) return;
        mcServer = mc;
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.setExecutor(Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r, "soa-quest-web");
                t.setDaemon(true);
                return t;
            }));
            server.createContext("/", QuestWebServer::handleRoot);
            server.createContext("/api/quests", QuestWebServer::handleQuestsApi);
            server.createContext("/api/events", QuestWebServer::handleSse);
            server.start();
            LOG.info("[SOA Quest Web] Overlay server started on port {}", port);
        } catch (IOException e) {
            LOG.error("[SOA Quest Web] Failed to start on port {}: {}", port, e.getMessage());
            server = null;
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            LOG.info("[SOA Quest Web] Overlay server stopped");
        }
        TOKEN_TO_PLAYER.clear();
        PLAYER_TO_TOKEN.clear();
        for (List<SseClient> clients : SSE_CLIENTS.values()) {
            for (SseClient c : clients) c.close();
        }
        SSE_CLIENTS.clear();
        layoutCache.clear();
        mcServer = null;
    }

    public static boolean isRunning() { return server != null; }

    /** Clear cached layout results — call after quest definitions change. */
    public static void invalidateLayoutCache() { layoutCache.clear(); }

    // ---------- token management ----------

    /** Generate or retrieve a token for a player. Called on login. */
    public static String tokenFor(ServerPlayer player) {
        UUID uuid = player.getUUID();
        String existing = PLAYER_TO_TOKEN.get(uuid);
        if (existing != null) return existing;
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TOKEN_TO_PLAYER.put(token, uuid);
        PLAYER_TO_TOKEN.put(uuid, token);
        return token;
    }

    /** Remove token on logout. */
    public static void revokeToken(ServerPlayer player) {
        UUID uuid = player.getUUID();
        String token = PLAYER_TO_TOKEN.remove(uuid);
        if (token != null) TOKEN_TO_PLAYER.remove(token);
        // Close SSE connections
        List<SseClient> clients = SSE_CLIENTS.remove(uuid);
        if (clients != null) {
            for (SseClient c : clients) c.close();
        }
    }

    // ---------- SSE push ----------

    /** Push a progress update to all SSE clients for a player. Called from
     *  the server main thread whenever a QuestSyncPacket is built. */
    public static void pushUpdate(ServerPlayer player) {
        if (server == null || mcServer == null) return;
        UUID uuid = player.getUUID();
        List<SseClient> clients = SSE_CLIENTS.get(uuid);
        if (clients == null || clients.isEmpty()) return;

        // Build progress JSON on the server thread
        JsonObject data = buildProgressJson(uuid);
        String json = GSON.toJson(data);
        String ssePayload = "data: " + json + "\n\n";

        clients.removeIf(c -> {
            try {
                c.send(ssePayload);
                return false;
            } catch (IOException e) {
                c.close();
                return true;
            }
        });
    }

    /** Push update to all SSE clients for all team members. */
    public static void pushUpdateForTeam(ServerPlayer actor) {
        if (server == null || mcServer == null) return;
        TeamData teams = TeamData.get(mcServer);
        QuestTeam team = teams.teamOf(actor);
        if (team.solo()) {
            pushUpdate(actor);
            return;
        }
        for (ServerPlayer p : teams.onlineMembers(mcServer, team.id())) {
            pushUpdate(p);
        }
    }

    // ---------- HTTP handlers ----------

    private static void handleRoot(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendText(ex, 405, "Method Not Allowed");
            return;
        }
        // Serve the HTML page from classpath resources
        try (InputStream is = QuestWebServer.class.getResourceAsStream("/assets/soa_additions/web/quest_overlay.html")) {
            if (is == null) {
                sendText(ex, 500, "Overlay HTML not found in mod resources");
                return;
            }
            byte[] html = is.readAllBytes();
            ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            ex.getResponseHeaders().set("Cache-Control", "no-cache");
            ex.sendResponseHeaders(200, html.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(html);
            }
        }
    }

    private static void handleQuestsApi(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendText(ex, 405, "Method Not Allowed");
            return;
        }
        String token = queryParam(ex, "token");
        UUID uuid = token != null ? TOKEN_TO_PLAYER.get(token) : null;
        if (uuid == null) {
            sendJson(ex, 401, "{\"error\":\"Invalid or expired token\"}");
            return;
        }

        JsonObject response = new JsonObject();

        // Pack mode
        PackMode mode = PackModeData.get(mcServer).mode();
        response.addProperty("packMode", mode.lower());

        // Player name
        ServerPlayer player = mcServer.getPlayerList().getPlayer(uuid);
        response.addProperty("playerName", player != null ? player.getGameProfile().getName() : "Unknown");

        // Chapters + quests
        List<Chapter> chapters = QuestRegistry.chaptersFor(mode);
        JsonArray chaptersArr = new JsonArray();
        for (Chapter ch : chapters) {
            JsonObject chObj = new JsonObject();
            chObj.addProperty("id", ch.id());
            chObj.addProperty("title", ch.title());
            chObj.addProperty("icon", ch.icon());
            chObj.addProperty("parentChapter", ch.parentChapter());
            chObj.addProperty("sortOrder", ch.sortOrder());
            JsonArray descArr = new JsonArray();
            for (String line : ch.description()) descArr.add(line);
            chObj.add("description", descArr);

            // Compute layout using the same algorithm the in-game GUI uses.
            // Cached to avoid recomputing on every API request.
            LayoutResult layout = layoutCache.computeIfAbsent(ch.id(), k -> QuestLayout.compute(ch));

            JsonArray questsArr = new JsonArray();
            for (Quest q : ch.quests()) {
                JsonObject qObj = new JsonObject();
                qObj.addProperty("id", q.id());
                qObj.addProperty("fullId", q.fullId());
                qObj.addProperty("title", q.title());
                qObj.addProperty("icon", q.icon());
                qObj.addProperty("optional", q.optional());
                qObj.addProperty("repeatable", q.repeatable());
                qObj.addProperty("visibility", q.visibility().lower());
                qObj.addProperty("shape", q.shape().name().toLowerCase());
                qObj.addProperty("size", q.size());
                qObj.addProperty("depsAll", q.depsAll());

                // Server-computed pixel positions — exact same as in-game GUI
                LayoutResult.GridPosition gp = layout.positions().get(q.fullId());
                if (gp != null) {
                    qObj.addProperty("layoutX", gp.pixelX());
                    qObj.addProperty("layoutY", gp.pixelY());
                } else {
                    qObj.addProperty("layoutX", -1);
                    qObj.addProperty("layoutY", -1);
                }

                JsonArray descQ = new JsonArray();
                for (String line : q.description()) descQ.add(line);
                qObj.add("description", descQ);
                JsonArray depsArr = new JsonArray();
                for (String dep : q.dependencies()) depsArr.add(dep);
                qObj.add("dependencies", depsArr);

                // Tasks
                JsonArray tasksArr = new JsonArray();
                for (QuestTask task : q.tasks()) {
                    JsonObject tObj = new JsonObject();
                    tObj.addProperty("type", task.type().toString());
                    tObj.addProperty("target", task.target());
                    tObj.addProperty("description", task.describe());
                    tasksArr.add(tObj);
                }
                qObj.add("tasks", tasksArr);

                // Rewards
                JsonArray rewardsArr = new JsonArray();
                for (QuestReward reward : q.rewards()) {
                    JsonObject rObj = new JsonObject();
                    rObj.addProperty("type", reward.type().toString());
                    rObj.addProperty("description", reward.describe());
                    rewardsArr.add(rObj);
                }
                qObj.add("rewards", rewardsArr);

                questsArr.add(qObj);
            }
            chObj.add("quests", questsArr);

            // Edges from the layout (same as in-game dependency lines)
            JsonArray edgesArr = new JsonArray();
            for (LayoutResult.Edge edge : layout.edges()) {
                JsonObject eObj = new JsonObject();
                eObj.addProperty("from", edge.from());
                eObj.addProperty("to", edge.to());
                eObj.addProperty("or", edge.orGroup());
                edgesArr.add(eObj);
            }
            chObj.add("edges", edgesArr);
            chaptersArr.add(chObj);
        }
        response.add("chapters", chaptersArr);

        // Progress
        response.add("progress", buildProgressJson(uuid));

        sendJson(ex, 200, GSON.toJson(response));
    }

    private static void handleSse(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendText(ex, 405, "Method Not Allowed");
            return;
        }
        String token = queryParam(ex, "token");
        UUID uuid = token != null ? TOKEN_TO_PLAYER.get(token) : null;
        if (uuid == null) {
            sendText(ex, 401, "Invalid token");
            return;
        }

        ex.getResponseHeaders().set("Content-Type", "text/event-stream");
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.getResponseHeaders().set("Connection", "keep-alive");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, 0); // chunked

        OutputStream os = ex.getResponseBody();
        SseClient client = new SseClient(os);

        SSE_CLIENTS.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(client);

        // Send initial keepalive + immediate progress snapshot so the browser
        // has fresh data the moment the SSE stream opens (covers the gap
        // between page load and the first server-side progress event).
        try {
            os.write(": connected\n\n".getBytes(StandardCharsets.UTF_8));
            os.flush();
            // Schedule the initial snapshot on the server main thread so we
            // read progress data safely.
            final UUID finalUuid = uuid;
            mcServer.execute(() -> {
                JsonObject data = buildProgressJson(finalUuid);
                String json = GSON.toJson(data);
                String payload = "data: " + json + "\n\n";
                try {
                    client.send(payload);
                } catch (IOException ignored) {
                    client.close();
                }
            });
        } catch (IOException e) {
            client.close();
        }
        // Connection stays open — client.send() will be called by pushUpdate()
    }

    // ---------- JSON builders ----------

    private static JsonObject buildProgressJson(UUID playerUuid) {
        JsonObject prog = new JsonObject();
        ServerPlayer player = mcServer.getPlayerList().getPlayer(playerUuid);
        if (player == null) return prog; // player offline
        TeamData teams = TeamData.get(mcServer);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData data = QuestProgressData.get(mcServer);
        TeamQuestProgress tp = data.forTeam(team.id());

        for (QuestProgress qp : tp.all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("status", qp.status().name());
            entry.addProperty("teamClaimed", qp.teamClaimed());
            entry.addProperty("localClaimed", qp.hasClaimed(playerUuid));
            entry.addProperty("everClaimed", qp.everClaimed());
            JsonArray taskCounts = new JsonArray();
            for (var t : qp.tasks()) taskCounts.add(t.count());
            entry.add("taskCounts", taskCounts);
            prog.add(qp.fullId(), entry);
        }
        return prog;
    }

    // ---------- utilities ----------

    private static String queryParam(HttpExchange ex, String key) {
        String query = ex.getRequestURI().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }

    private static void sendText(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    /** Wrapper around an SSE output stream. */
    private static final class SseClient {
        private final OutputStream os;
        private volatile boolean closed;

        SseClient(OutputStream os) { this.os = os; }

        void send(String payload) throws IOException {
            if (closed) throw new IOException("closed");
            synchronized (os) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }

        void close() {
            closed = true;
            try { os.close(); } catch (IOException ignored) {}
        }
    }
}
