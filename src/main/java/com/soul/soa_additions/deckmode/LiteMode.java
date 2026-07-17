package com.soul.soa_additions.deckmode;

import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * "Lite Mode": disables the pure client-side cosmetic/audio/overlay mods and
 * turns ModernFix dynamic resources on, for the lightest possible pack.
 * Combined with Deck Mode this is the minimum-footprint configuration.
 *
 * dynamic_resources is coupled to this toggle in BOTH directions: the full
 * pack keeps it OFF because Better Foliage's baked models crash under it;
 * lite mode disables Better Foliage, which is what makes it safe to enable.
 *
 * <p><b>How the renames are applied</b> (deliberately transparent for users
 * and for platform malware scanners): toggling registers a JVM shutdown hook
 * that renames the jars with plain {@link Files#move} when the game closes.
 * On Linux/macOS that always succeeds. On Windows, jars the mod loader still
 * holds open can't be renamed by the running JVM — only those leftovers are
 * handed to a small, human-readable {@code soa_lite_helper.bat} in the game
 * folder, launched visibly (minimized), which waits for this process to exit,
 * performs the renames, and logs everything it does to
 * {@code soa_lite_helper.log}. The helper never deletes itself or anything
 * else; stale helper files are cleaned up on the next game launch. Everything
 * touched lives inside the pack's own game folder.
 *
 * State is derived purely from the filesystem: lite mode is active iff any
 * {@code *.jar.litedisabled} file exists in the mods folder.
 */
public final class LiteMode {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions");
    static final String DISABLED_SUFFIX = ".litedisabled";

    /** Helper artifacts (game-dir file names) cleaned up on the next launch.
     *  Includes the legacy .ps1/.sh names from older versions of this mod. */
    private static final String[] HELPER_ARTIFACTS = {
        "soa_lite_helper.bat", "soa_lite_helper.log",
        "soa_lite_helper.ps1", "soa_lite_helper.sh",
    };

    /** Only rename files whose names are unambiguously safe to embed in a
     *  batch/shell line — no quotes, expansions, or control characters. */
    private static final Pattern SAFE_NAME = Pattern.compile("[\\w .()\\[\\]+,@~#-]+");

    /**
     * Jar-name markers (lowercase substring match) that are SAFE to remove:
     * pure client-side mods with no blocks/items/entities/worldgen, so
     * disabling them can never break worlds or server compatibility.
     * Deliberately absent: maps/Jade/JEI (utility players rely on),
     * musictriggers (pack identity, negligible render cost), anything
     * registering game content, and anything another mod lists as a MANDATORY
     * dependency (verify with tools/check_lite_deps.py before adding entries).
     */
    private static final String[] LITE_DISABLE_MARKERS = {
        "ambientsounds",              // ambient audio engine
        "sound-physics-remastered",   // audio raytracing
        "itemphysic",                 // item render physics
        "notenoughanimations",        // player animations
        "firstperson-forge",          // first-person body render
        "entity_model_features",      // EMF custom entity models
        "entity_texture_features",    // ETF entity texture variants
        "chat_heads",                 // chat avatar render
        "lootbeams",                  // item beam render
        // NOT areaeffectcloud3d — Progressive Bosses declares it a MANDATORY
        // dependency; removing it makes Forge refuse to launch.
        "voidfog",                    // void fog render
        "blur-forge",                 // GUI background blur
        "craftpresence",              // Discord rich presence polling
        "combat_music",               // combat music tracker
        "ding-",                      // launch/join sound
        "watut",                      // player-status overlay
        "catjammies",                 // cat pajama textures
        "torohealth",                 // damage indicator overlay
        "betterfoliage",              // extra foliage geometry (also crashes with dynamic_resources)
        "legendarytooltips",          // tooltip borders
    };

    private static final AtomicBoolean CLEANED = new AtomicBoolean(false);
    private static final AtomicBoolean HOOK_REGISTERED = new AtomicBoolean(false);
    private static volatile List<Path[]> renamesAtExit = List.of();

    private LiteMode() {}

    private static Path modsDir() {
        return FMLPaths.GAMEDIR.get().resolve("mods");
    }

    public static boolean isActive() {
        cleanupHelperArtifacts();
        try (Stream<Path> files = Files.list(modsDir())) {
            return files.anyMatch(p -> p.getFileName().toString().endsWith(DISABLED_SUFFIX));
        } catch (IOException e) {
            return false;
        }
    }

    /** Jars the next toggle would rename: [from, to] pairs. */
    public static List<Path[]> pendingRenames() {
        List<Path[]> renames = new ArrayList<>();
        try (Stream<Path> files = Files.list(modsDir())) {
            boolean restoring = isActive();
            for (Path p : files.toList()) {
                String name = p.getFileName().toString();
                if (!SAFE_NAME.matcher(name).matches()) {
                    if (name.toLowerCase(Locale.ROOT).endsWith(".jar")
                            || name.endsWith(DISABLED_SUFFIX)) {
                        LOG.warn("Lite mode: skipping '{}' — unusual characters in file name", name);
                    }
                    continue;
                }
                if (restoring) {
                    if (name.endsWith(DISABLED_SUFFIX)) {
                        renames.add(new Path[]{p, p.resolveSibling(
                                name.substring(0, name.length() - DISABLED_SUFFIX.length()))});
                    }
                } else if (name.toLowerCase(Locale.ROOT).endsWith(".jar") && isLiteTarget(name)) {
                    renames.add(new Path[]{p, p.resolveSibling(name + DISABLED_SUFFIX)});
                }
            }
        } catch (IOException e) {
            LOG.error("Lite mode: could not scan mods folder", e);
        }
        return renames;
    }

    private static boolean isLiteTarget(String jarName) {
        String lower = jarName.toLowerCase(Locale.ROOT);
        for (String marker : LITE_DISABLE_MARKERS) {
            if (lower.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies the toggle: ensures dynamic resources when enabling, then arms
     * the shutdown-hook renamer. Caller shows the countdown screen and closes
     * the game, which is what triggers the renames.
     *
     * @return number of jars scheduled for rename, or -1 on failure
     */
    public static int applyToggle() {
        boolean enabling = !isActive();
        List<Path[]> renames = pendingRenames();
        // dynamic_resources follows lite mode: ON only while Better Foliage is
        // lite-disabled (its baked models crash under dynamic resources), OFF
        // again when the full mod set comes back.
        setDynamicResources(enabling);
        if (renames.isEmpty()) {
            return 0;
        }
        renamesAtExit = renames;
        if (HOOK_REGISTERED.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(
                    new Thread(LiteMode::renameOnExit, "SOA-LiteMode-Renamer"));
        }
        LOG.info("Lite mode {}: {} jar(s) will be renamed when the game closes",
                enabling ? "ENABLING" : "DISABLING", renames.size());
        return renames.size();
    }

    /** Set mixin.perf.dynamic_resources in the ModernFix config. */
    private static void setDynamicResources(boolean value) {
        Path cfg = FMLPaths.CONFIGDIR.get().resolve("modernfix-mixins.properties");
        String key = "mixin.perf.dynamic_resources";
        try {
            List<String> lines = Files.exists(cfg)
                    ? new ArrayList<>(Files.readAllLines(cfg)) : new ArrayList<>();
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                String t = lines.get(i).trim();
                if (t.startsWith(key + "=") && !t.startsWith("#")) {
                    lines.set(i, key + "=" + value);
                    found = true;
                }
            }
            if (!found) {
                lines.add(key + "=" + value);
            }
            Files.write(cfg, lines);
        } catch (IOException e) {
            LOG.error("Lite mode: could not update ModernFix config", e);
        }
    }

    // ------------------------------------------------------------------
    // Shutdown-hook renamer + transparent fallback helper
    // ------------------------------------------------------------------

    /** Runs as a JVM shutdown hook: renames directly where the OS allows it,
     *  and hands only the still-locked files to the fallback helper. */
    private static void renameOnExit() {
        List<Path[]> stillLocked = new ArrayList<>();
        for (Path[] r : renamesAtExit) {
            try {
                Files.move(r[0], r[1]);
            } catch (IOException e) {
                // Windows keeps loaded jars locked until the process dies.
                stillLocked.add(r);
            }
        }
        if (stillLocked.isEmpty()) {
            return;
        }
        try {
            spawnFallbackHelper(stillLocked);
        } catch (IOException e) {
            // Logging frameworks may already be gone during shutdown.
            System.err.println("[soa_additions] Lite mode: could not start rename helper: " + e);
        }
    }

    /**
     * Writes and launches a plain, commented batch/shell script that waits for
     * this process to exit and renames the listed jars. Runs minimized (not
     * hidden), logs every action to {@code soa_lite_helper.log}, touches only
     * the mods folder, and is removed by {@link #cleanupHelperArtifacts()} on
     * the next launch rather than deleting itself.
     */
    private static void spawnFallbackHelper(List<Path[]> renames) throws IOException {
        long pid = ProcessHandle.current().pid();
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path log = gameDir.resolve("soa_lite_helper.log");

        StringBuilder sb = new StringBuilder();
        if (windows) {
            Path script = gameDir.resolve("soa_lite_helper.bat");
            sb.append("@echo off\r\n")
              .append("REM Souls of Avarice \"Lite Mode\" helper, written by the soa_additions mod\r\n")
              .append("REM when you toggled Lite Mode in-game. It waits for Minecraft (PID ").append(pid).append(")\r\n")
              .append("REM to close, then renames the mod jars listed below inside this pack's mods\r\n")
              .append("REM folder to enable/disable them. Actions are logged to soa_lite_helper.log;\r\n")
              .append("REM both files are cleaned up automatically on the next game launch.\r\n")
              .append(":wait\r\n")
              .append("tasklist /FI \"PID eq ").append(pid).append("\" 2>nul | find \"").append(pid).append("\" >nul\r\n")
              .append("if not errorlevel 1 (\r\n")
              .append("  timeout /t 1 /nobreak >nul\r\n")
              .append("  goto wait\r\n")
              .append(")\r\n");
            for (Path[] r : renames) {
                sb.append("echo renaming \"").append(r[0].getFileName())
                  .append("\" to \"").append(r[1].getFileName())
                  .append("\" >> \"").append(log).append("\"\r\n");
                sb.append("ren \"").append(r[0].toAbsolutePath())
                  .append("\" \"").append(r[1].getFileName())
                  .append("\" >> \"").append(log).append("\" 2>&1\r\n");
            }
            sb.append("echo done >> \"").append(log).append("\"\r\n");
            Files.writeString(script, sb.toString());

            // start /min: visible in the taskbar (nothing hidden) but unobtrusive.
            new ProcessBuilder("cmd", "/c", "start", "SoA Lite Mode helper", "/min",
                    "cmd", "/c", script.toAbsolutePath().toString())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
        } else {
            Path script = gameDir.resolve("soa_lite_helper.sh");
            sb.append("#!/bin/sh\n")
              .append("# Souls of Avarice \"Lite Mode\" helper, written by the soa_additions mod when\n")
              .append("# you toggled Lite Mode in-game. Waits for the game (pid ").append(pid).append(") to exit,\n")
              .append("# then renames the mod jars listed below. Log: soa_lite_helper.log.\n")
              .append("# Cleaned up automatically on the next game launch.\n")
              .append("while kill -0 ").append(pid).append(" 2>/dev/null; do sleep 1; done\n");
            for (Path[] r : renames) {
                sb.append("echo \"renaming ").append(r[0].getFileName()).append("\" >> \"").append(log).append("\"\n");
                sb.append("mv \"").append(r[0].toAbsolutePath())
                  .append("\" \"").append(r[1].toAbsolutePath())
                  .append("\" >> \"").append(log).append("\" 2>&1\n");
            }
            sb.append("echo done >> \"").append(log).append("\"\n");
            Files.writeString(script, sb.toString());
            new ProcessBuilder("sh", script.toAbsolutePath().toString())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
        }
    }

    /** Remove helper script/log left behind by a previous toggle (they never
     *  self-delete — doing the cleanup here keeps the helper trivially simple
     *  and auditable). Runs once per session. */
    private static void cleanupHelperArtifacts() {
        if (!CLEANED.compareAndSet(false, true)) return;
        Path gameDir = FMLPaths.GAMEDIR.get();
        for (String name : HELPER_ARTIFACTS) {
            try {
                if (Files.deleteIfExists(gameDir.resolve(name))) {
                    LOG.debug("Lite mode: removed stale helper file {}", name);
                }
            } catch (IOException ignored) {
                // Still running helper on a very slow shutdown race — next launch gets it.
            }
        }
    }
}
