package com.soul.soa_additions.liteboot;

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
 * dynamic_resources is ALWAYS ON as of 3.58.4: Better Foliage (whose baked
 * models crashed under it, and which used to couple the flag to this toggle)
 * was removed from the pack 2026-07-26. The toggle now only re-asserts the
 * flag so stale user configs heal themselves; it never turns it off.
 *
 * <p><b>How the renames are applied</b> (deliberately transparent for users
 * and for platform malware scanners): toggling registers a JVM shutdown hook
 * that renames the jars with plain {@link Files#move} when the game closes.
 * On Linux/macOS that always succeeds. On Windows, jars the mod loader still
 * holds open can't be renamed by the running JVM — those leftovers are written
 * to a plain-text {@code soa_lite_pending.txt} in the game folder and applied
 * by {@link LiteModeBootstrap} at the start of the next launch, before the
 * loader opens the mods folder. No external process is ever started and no
 * script is written; everything touched lives inside the pack's own game
 * folder.
 *
 * State is derived purely from the filesystem: lite mode is active iff any
 * {@code *.jar.litedisabled} file exists in the mods folder.
 */
public final class LiteMode {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions");
    static final String DISABLED_SUFFIX = ".litedisabled";

    /** Leftovers from the script-based helper used before v3.55.7, swept out of
     *  the game folder on the next launch so upgrading installs come out clean. */
    private static final String[] HELPER_ARTIFACTS = {
        "soa_lite_helper.bat", "soa_lite_helper.log",
        "soa_lite_helper.ps1", "soa_lite_helper.sh",
    };

    /** Handoff file read by {@code LiteModeBootstrap} on the next launch, and
     *  its field separator. Deliberately duplicated there rather than shared:
     *  the bootstrap runs in the boot plugin layer and must not pull this
     *  class in with it. Keep the two in step. */
    private static final String PENDING_FILE = "soa_lite_pending.txt";
    private static final String PENDING_SEPARATOR = "|";

    /** Only rename files with unremarkable names — no quotes, control
     *  characters, or path separators, and never the {@code |} used as the
     *  field separator in the pending-rename list. Mirrored by
     *  {@code LiteModeBootstrap.SAFE_NAME}; keep the two in step. */
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
        // dynamic_resources is pack-wide ON since Better Foliage was removed
        // (2026-07-26). Re-assert true on every toggle so configs from older
        // versions (which wrote false on lite-disable) self-heal.
        setDynamicResources(true);
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
    // Shutdown-hook renamer + next-launch handoff
    // ------------------------------------------------------------------

    /** Runs as a JVM shutdown hook: renames directly where the OS allows it,
     *  and defers only the still-locked files to the next launch. */
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
            writePendingRenames(stillLocked);
        } catch (IOException e) {
            // Logging frameworks may already be gone during shutdown.
            System.err.println("[soa_additions] Lite mode: could not record pending renames: " + e);
        }
    }

    /**
     * Records the renames the running JVM could not perform, as a plain
     * {@code from|to} list of file names for {@link LiteModeBootstrap} to
     * apply at the start of the next launch. Both names are bare file names,
     * so nothing outside the mods folder is reachable from the file.
     */
    private static void writePendingRenames(List<Path[]> renames) throws IOException {
        List<String> lines = new ArrayList<>(renames.size() + 3);
        lines.add("# Souls of Avarice \"Lite Mode\": mod jars still open when the game closed.");
        lines.add("# soa_additions renames these in the pack's mods folder on the next launch,");
        lines.add("# then deletes this file. Safe to delete by hand to cancel the change.");
        for (Path[] r : renames) {
            lines.add(r[0].getFileName() + PENDING_SEPARATOR + r[1].getFileName());
        }
        Files.write(FMLPaths.GAMEDIR.get().resolve(PENDING_FILE), lines);
    }

    /** Remove the script helper and log left behind by pre-3.55.7 versions of
     *  this mod, which had no way to delete them itself. Runs once per session. */
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
