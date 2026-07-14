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
 * Mod jars are file-locked while the game runs, so the actual renames are done
 * by a tiny helper process that waits for this JVM to exit. The game then
 * closes via {@link LiteCountdownScreen}; the player relaunches manually.
 *
 * State is derived purely from the filesystem: lite mode is active iff any
 * {@code *.jar.litedisabled} file exists in the mods folder.
 */
public final class LiteMode {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions");
    static final String DISABLED_SUFFIX = ".litedisabled";

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

    private LiteMode() {}

    private static Path modsDir() {
        return FMLPaths.GAMEDIR.get().resolve("mods");
    }

    public static boolean isActive() {
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
     * Applies the toggle: ensures dynamic resources when enabling, then spawns
     * the rename helper. Caller shows the countdown screen and closes the game.
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
        try {
            spawnRenameHelper(renames);
        } catch (IOException e) {
            LOG.error("Lite mode: could not spawn rename helper", e);
            return -1;
        }
        LOG.info("Lite mode {}: {} jar(s) scheduled for rename after exit",
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

    /**
     * Writes and launches a detached helper that waits for this JVM to exit,
     * performs the renames (jars are unlocked by then), and deletes itself.
     */
    private static void spawnRenameHelper(List<Path[]> renames) throws IOException {
        long pid = ProcessHandle.current().pid();
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        Path script = FMLPaths.GAMEDIR.get().resolve(windows ? "soa_lite_helper.ps1" : "soa_lite_helper.sh");

        StringBuilder sb = new StringBuilder();
        if (windows) {
            sb.append("try { Wait-Process -Id ").append(pid).append(" -ErrorAction Stop } catch {}\n");
            for (Path[] r : renames) {
                sb.append("Rename-Item -LiteralPath '").append(r[0].toAbsolutePath())
                  .append("' -NewName '").append(r[1].getFileName())
                  .append("' -ErrorAction SilentlyContinue\n");
            }
            sb.append("Remove-Item -LiteralPath $MyInvocation.MyCommand.Path -ErrorAction SilentlyContinue\n");
        } else {
            sb.append("#!/bin/sh\n");
            sb.append("while kill -0 ").append(pid).append(" 2>/dev/null; do sleep 1; done\n");
            for (Path[] r : renames) {
                sb.append("mv \"").append(r[0].toAbsolutePath())
                  .append("\" \"").append(r[1].toAbsolutePath()).append("\"\n");
            }
            sb.append("rm -- \"$0\"\n");
        }
        Files.writeString(script, sb.toString());

        ProcessBuilder pb = windows
                ? new ProcessBuilder("powershell", "-WindowStyle", "Hidden",
                        "-ExecutionPolicy", "Bypass", "-File", script.toAbsolutePath().toString())
                : new ProcessBuilder("sh", script.toAbsolutePath().toString());
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.start();
    }
}
