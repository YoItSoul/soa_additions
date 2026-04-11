package com.soul.soa_additions.telemetry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

/**
 * Side-safe accessors for dedicated/integrated-server runtime metrics.
 * All methods return {@code null} if no {@link MinecraftServer} is currently
 * running (e.g. on a client at the main menu) and swallow any errors so
 * telemetry can never take the game down.
 */
public final class ServerIdentity {

    private ServerIdentity() {}

    /** @return a populated {@link Telemetry.ServerInfo}, or null if no server is running. */
    public static Telemetry.ServerInfo snapshotOrNull() {
        MinecraftServer server;
        try {
            server = ServerLifecycleHooks.getCurrentServer();
        } catch (Throwable t) {
            return null;
        }
        if (server == null) return null;
        // Skip integrated servers entirely — singleplayer and LAN-opened worlds
        // are "players", not servers, and we already have the client heartbeat
        // covering them. Only dedicated servers get server-side metrics.
        try {
            if (!server.isDedicatedServer()) return null;
        } catch (Throwable t) {
            return null;
        }

        Telemetry.ServerInfo s = new Telemetry.ServerInfo();
        try {
            // Tick timing: MinecraftServer.tickTimes is a rolling ring of the last
            // 100 tick durations in nanoseconds. Mean → MSPT; TPS = 1000/max(50,mspt).
            long[] ticks = server.tickTimes;
            if (ticks != null && ticks.length > 0) {
                long sum = 0L;
                long max = 0L;
                int n = 0;
                for (long t : ticks) {
                    if (t <= 0) continue;
                    sum += t;
                    if (t > max) max = t;
                    n++;
                }
                if (n > 0) {
                    double meanMs = (sum / (double) n) / 1_000_000.0;
                    double peakMs = max / 1_000_000.0;
                    s.mspt_mean = round2(meanMs);
                    s.mspt_peak = round2(peakMs);
                    s.tps = round2(Math.min(20.0, 1000.0 / Math.max(50.0, meanMs)));
                }
            }
        } catch (Throwable ignored) {}

        try {
            s.player_count = server.getPlayerCount();
            s.player_max = server.getMaxPlayers();
        } catch (Throwable ignored) {}

        try {
            List<String> names = new ArrayList<>();
            for (var p : server.getPlayerList().getPlayers()) {
                names.add(p.getGameProfile().getName());
            }
            s.player_names = names;
        } catch (Throwable ignored) {}

        try {
            long chunks = 0L;
            long entities = 0L;
            List<String> dims = new ArrayList<>();
            for (ServerLevel lvl : server.getAllLevels()) {
                dims.add(lvl.dimension().location().toString());
                try { chunks += lvl.getChunkSource().getLoadedChunksCount(); } catch (Throwable ignored) {}
                try {
                    int ec = 0;
                    for (var ignored2 : lvl.getAllEntities()) ec++;
                    entities += ec;
                } catch (Throwable ignored) {}
            }
            s.loaded_chunks = chunks;
            s.loaded_entities = entities;
            s.dimensions = dims;
        } catch (Throwable ignored) {}

        try {
            s.is_dedicated = server.isDedicatedServer();
            s.motd = server.getMotd();
            s.view_distance = server.getPlayerList().getViewDistance();
            s.simulation_distance = server.getPlayerList().getSimulationDistance();
        } catch (Throwable ignored) {}

        return s;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
