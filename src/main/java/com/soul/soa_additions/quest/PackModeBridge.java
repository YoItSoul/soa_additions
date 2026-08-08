package com.soul.soa_additions.quest;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The single point of truth KubeJS reads to learn the active pack mode.
 *
 * <p><b>Why this exists.</b> The pack has two pack-mode systems that were set
 * independently: this mod's {@link PackModeData} (chosen on the create-world
 * screen, stored per world) and KubeJS's {@code global.SOA_PACKMODE}, which was
 * a hardcoded constant in {@code _packmode.js}. A player could create an Expert
 * world and still get Adventure recipes, because nothing connected the two.
 * soa_additions is the master; {@code _packmode.js} now calls
 * {@link #current()} instead of hardcoding a value.</p>
 *
 * <p><b>Why the resolution order is what it is.</b> KubeJS evaluates its scripts
 * during datapack load, and for a brand-new singleplayer world that happens
 * <em>before</em> a {@code MinecraftServer} or any level exists — so
 * {@link PackModeData} cannot be consulted yet. The chosen mode is only in
 * {@link PendingPackMode} at that point. For an existing world the server is
 * usually up, but on the very first load the levels may still be missing, so a
 * mirror file written on the previous run covers the gap.</p>
 *
 * <p>Order: pending create-world choice, then live world data, then the
 * server-config override, then the mirror file, then {@code adventure}.</p>
 */
public final class PackModeBridge {

    /** Mirror of the last resolved mode, so a cold start has something to read. */
    private static final String MIRROR_FILE = "soa_packmode.txt";

    private PackModeBridge() {}

    private static Path mirrorPath() {
        return FMLPaths.CONFIGDIR.get().resolve(MIRROR_FILE);
    }

    /**
     * The active pack mode as a lowercase string: {@code casual},
     * {@code adventure} or {@code expert}. Never null, never throws — KubeJS
     * calls this during script evaluation and an exception there would take
     * every recipe script down with it.
     */
    public static String current() {
        try {
            // 1. Brand-new world: the create-world screen already picked a mode.
            //    Peek without consuming — PackModeData.get() consumes it later
            //    when it seeds the world's SavedData.
            PackMode pending = PendingPackMode.get();
            if (pending != null) return pending.lower();

            // 2. Running world: PackModeData is authoritative. Only reachable
            //    once the overworld exists, which is not the case during the
            //    initial datapack load.
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && server.overworld() != null) {
                String mode = PackModeData.get(server).mode().lower();
                remember(mode);
                return mode;
            }

            // 3. Dedicated server override.
            String cfg = com.soul.soa_additions.config.ModConfigs.SERVER_PACKMODE.get();
            if (cfg != null && !cfg.isBlank()) {
                PackMode target = PackMode.fromString(cfg);
                if (target.name().equalsIgnoreCase(cfg.trim())) return target.lower();
            }

            // 4. Whatever the last run resolved.
            Path p = mirrorPath();
            if (Files.exists(p)) {
                String s = Files.readString(p).trim().toLowerCase(Locale.ROOT);
                if (!s.isEmpty()) return PackMode.fromString(s).lower();
            }
        } catch (Throwable ignored) {
            // fall through to the default
        }
        return PackMode.ADVENTURE.lower();
    }

    /**
     * Record the authoritative mode for the next cold start. Called whenever
     * the mode is chosen or changes, so the mirror never lags by more than one
     * datapack reload.
     */
    public static void remember(String mode) {
        try {
            Files.writeString(mirrorPath(), mode);
        } catch (IOException ignored) {
            // a stale mirror only costs us one reload; never fail the caller
        }
    }

    public static void remember(PackMode mode) {
        if (mode != null) remember(mode.lower());
    }

    /**
     * Rebuild datapacks so KubeJS re-evaluates its scripts against the current
     * pack mode. Called whenever the mode changes, so recipes never sit built
     * for a mode the world is no longer in.
     */
    public static void reloadDatapacks(MinecraftServer server) {
        if (server == null) return;
        server.execute(() -> server.reloadResources(
                server.getPackRepository().getSelectedIds()));
    }
}
