package com.soul.soa_additions.liteboot;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Applies the mod-jar renames that {@link LiteMode} could not perform itself.
 *
 * <p>On Windows the mod loader keeps every loaded jar open for the lifetime of
 * the process, so the shutdown hook in {@code LiteMode} cannot rename them —
 * it writes the outstanding renames to {@value #PENDING_FILE} in the game
 * folder instead. This service picks that list up on the next launch and
 * performs the renames before the loader has opened anything, so a Lite Mode
 * toggle still costs exactly one restart.
 *
 * <p>Registered as a {@link ITransformationService} purely for the timing:
 * {@link #onLoad} is the first callback ModLauncher makes, and it runs before
 * FML's {@code beginScanning} phase discovers the mods folder. This service
 * contributes {@linkplain #transformers() no transformers} and rewrites no
 * bytecode — it only renames files inside {@code <gamedir>/mods}.
 *
 * <p>Deliberately self-contained: it is loaded into the boot plugin layer,
 * separately from the rest of the mod, so it references nothing outside the
 * JDK and the ModLauncher API.
 */
public final class LiteModeBootstrap implements ITransformationService {

    /** Game-folder file listing renames left over from the previous session. */
    static final String PENDING_FILE = "soa_lite_pending.txt";

    /** Field separator in {@value #PENDING_FILE}; excluded by {@link #SAFE_NAME}. */
    static final String SEPARATOR = "|";

    /** Mirrors {@code LiteMode.SAFE_NAME}. Deliberately duplicated rather than
     *  shared: this class runs in the boot plugin layer and must not pull the
     *  game-layer {@code LiteMode} in with it. Keep the two in step. */
    private static final Pattern SAFE_NAME = Pattern.compile("[\\w .()\\[\\]+,@~#-]+");

    @Override
    public String name() {
        return "soa_additions_litemode";
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
        env.getProperty(IEnvironment.Keys.GAMEDIR.get())
                .ifPresent(LiteModeBootstrap::applyPending);
    }

    @Override
    public void initialize(IEnvironment environment) {
        // Nothing to do — all the work happens in onLoad, before the mod scan.
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<ITransformer> transformers() {
        return List.of();
    }

    /**
     * Reads the pending-rename list and applies it, then removes the list.
     *
     * <p>The file is deleted whether or not every rename succeeded: Lite Mode
     * derives its state from the mods folder, so a failed entry simply leaves
     * that jar as it was and the player can toggle again. Retrying forever
     * would be worse than doing nothing.
     */
    private static void applyPending(Path gameDir) {
        Path pending = gameDir.resolve(PENDING_FILE);
        if (!Files.isRegularFile(pending)) {
            return;
        }
        Path modsDir = gameDir.resolve("mods");
        int renamed = 0;
        try {
            for (String line : Files.readAllLines(pending)) {
                if (rename(modsDir, line)) {
                    renamed++;
                }
            }
        } catch (IOException e) {
            System.err.println("[soa_additions] Lite mode: could not read " + PENDING_FILE + ": " + e);
        }
        try {
            Files.deleteIfExists(pending);
        } catch (IOException e) {
            System.err.println("[soa_additions] Lite mode: could not remove " + PENDING_FILE + ": " + e);
        }
        if (renamed > 0) {
            System.out.println("[soa_additions] Lite mode: applied " + renamed + " pending mod rename(s)");
        }
    }

    /**
     * Renames one {@code from|to} entry. Both names must be plain file names
     * that pass {@link #SAFE_NAME}, so a hand-edited list can never reach
     * outside the mods folder.
     */
    private static boolean rename(Path modsDir, String line) {
        String entry = line.trim();
        if (entry.isEmpty() || entry.startsWith("#")) {
            return false;
        }
        int split = entry.indexOf(SEPARATOR);
        if (split <= 0 || split == entry.length() - 1) {
            return false;
        }
        String from = entry.substring(0, split);
        String to = entry.substring(split + 1);
        if (!SAFE_NAME.matcher(from).matches() || !SAFE_NAME.matcher(to).matches()) {
            System.err.println("[soa_additions] Lite mode: ignoring rename with unusual file name: " + entry);
            return false;
        }
        Path source = modsDir.resolve(from);
        Path target = modsDir.resolve(to);
        if (!source.getParent().equals(modsDir) || !target.getParent().equals(modsDir)) {
            return false;
        }
        try {
            Files.move(source, target);
            return true;
        } catch (IOException e) {
            // Already renamed by the shutdown hook, or removed by the player.
            return false;
        }
    }
}
