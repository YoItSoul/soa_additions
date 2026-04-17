package com.soul.soa_additions.autoupdate;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.soul.soa_additions.config.ModConfigs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Pulls a configured subdirectory of a GitHub repository into the game directory on a
 * recurring schedule, overwriting local files so CraftTweaker / KubeJS / datapack authors
 * can push hotfixes without shipping a new mod build.
 *
 * <p>Design:
 * <ul>
 *   <li>One daemon thread; work is serialized so a slow download cannot overlap with the next tick.</li>
 *   <li>Cheap path first: GET the latest commit SHA via the GitHub API. If it matches the SHA we
 *       applied last time (persisted to {@code config/soa_additions/autoupdate_sha.txt}), we skip
 *       the zip download entirely.</li>
 *   <li>On a new SHA, we download the branch zipball, extract to a temp directory, filter by the
 *       configured source path + blacklist, and copy/overwrite into the target directory.</li>
 *   <li>After a successful sync, if enabled, we schedule '/reload' on the server thread to apply
 *       datapack/script changes live.</li>
 * </ul>
 *
 * <p>All network and filesystem errors are swallowed after logging — the updater must never crash
 * the game or interrupt a tick.
 */
public final class AutoUpdater {

    private static final Logger LOG = LoggerFactory.getLogger("SOA_AutoUpdate");
    private static final String USER_AGENT = "soa_additions-auto-updater";
    private static final String SHA_FILE = "autoupdate_sha.txt";
    // Lists every repo-relative file we've written into targetPath so that on the next sync we
    // can delete files that vanished from the repo without touching user-local files we never owned.
    private static final String MANIFEST_FILE = "autoupdate_manifest.txt";

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static ScheduledExecutorService exec;
    private static ScheduledFuture<?> task;

    private AutoUpdater() {}

    public static synchronized void start() {
        if (RUNNING.get()) return;

        boolean enabled;
        int intervalMinutes;
        try {
            enabled = ModConfigs.AUTO_UPDATE_ENABLED.get();
            intervalMinutes = ModConfigs.AUTO_UPDATE_INTERVAL_MINUTES.get();
        } catch (Throwable t) {
            // Configs not loaded yet; try again later.
            return;
        }
        if (!enabled) {
            LOG.info("Auto-updater disabled in config, not starting.");
            return;
        }

        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SOA-AutoUpdater");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        long period = Math.max(1, intervalMinutes) * 60L;
        // Delay the first run briefly so startup isn't blocked by a network call.
        task = exec.scheduleAtFixedRate(AutoUpdater::runOnce, 15, period, TimeUnit.SECONDS);
        RUNNING.set(true);
        LOG.info("Auto-updater started ({}m interval).", intervalMinutes);
    }

    public static synchronized void stop() {
        if (!RUNNING.get()) return;
        try {
            if (task != null) task.cancel(false);
            if (exec != null) exec.shutdownNow();
        } catch (Throwable ignored) {
        } finally {
            task = null;
            exec = null;
            RUNNING.set(false);
            LOG.info("Auto-updater stopped.");
        }
    }

    private static void runOnce() {
        try {
            String repo = sanitizeRepo(ModConfigs.AUTO_UPDATE_REPO.get());
            String branch = ModConfigs.AUTO_UPDATE_BRANCH.get();
            if (repo == null || branch == null || branch.isBlank()) {
                LOG.warn("Auto-updater: invalid repo or branch, skipping.");
                return;
            }

            String latestSha = fetchLatestSha(repo, branch);
            if (latestSha == null) {
                LOG.debug("Auto-updater: could not read latest commit SHA, skipping this cycle.");
                return;
            }

            Path shaFile = stateDir().resolve(SHA_FILE);
            String appliedSha = readShaFile(shaFile);
            if (latestSha.equals(appliedSha)) {
                LOG.debug("Auto-updater: already at {}@{}, no work.", repo, latestSha);
                return;
            }

            LOG.info("Auto-updater: new commit detected on {}@{} ({} -> {}), syncing.",
                    repo, branch, appliedSha == null ? "(none)" : appliedSha, latestSha);

            String targetRel = normalizeRelPath(ModConfigs.AUTO_UPDATE_TARGET_PATH.get());
            Path targetDir = FMLPaths.GAMEDIR.get().resolve(targetRel).normalize();

            SyncResult result = downloadAndApply(repo, branch, targetDir);

            Path manifestFile = stateDir().resolve(MANIFEST_FILE);
            Set<String> previous = readManifest(manifestFile);
            int removed = deleteStale(targetDir, previous, result.writtenRelPaths);

            writeManifest(manifestFile, result.writtenRelPaths);
            writeShaFile(shaFile, latestSha);
            LOG.info("Auto-updater: sync complete — {} file(s) written, {} removed, SHA={}.",
                    result.writtenRelPaths.size(), removed, latestSha);

            boolean changed = result.writtenRelPaths.size() > 0 || removed > 0;
            if (changed && safeGetBool(ModConfigs.AUTO_UPDATE_AUTO_RELOAD, true)) {
                triggerReload();
            }
        } catch (Throwable t) {
            LOG.warn("Auto-updater: cycle failed ({}), will retry on next tick.", t.toString());
        }
    }

    // ---------------------------------------------------------------------
    // GitHub interaction
    // ---------------------------------------------------------------------

    private static String fetchLatestSha(String repo, String branch) {
        String url = "https://api.github.com/repos/" + repo + "/commits/" + enc(branch);
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                LOG.debug("Auto-updater: GitHub API returned HTTP {} for {}", resp.statusCode(), url);
                return null;
            }
            JsonElement root = JsonParser.parseString(resp.body());
            if (root == null || !root.isJsonObject()) return null;
            JsonElement sha = root.getAsJsonObject().get("sha");
            if (sha == null || !sha.isJsonPrimitive()) return null;
            String s = sha.getAsString();
            return (s == null || s.isBlank()) ? null : s;
        } catch (IOException | InterruptedException e) {
            LOG.debug("Auto-updater: SHA lookup failed: {}", e.toString());
            Thread.currentThread().interrupt();
            return null;
        } catch (Throwable t) {
            LOG.debug("Auto-updater: SHA lookup error: {}", t.toString());
            return null;
        }
    }

    /** Result of a successful zip download+apply: the set of target-relative paths we wrote. */
    private static final class SyncResult {
        final Set<String> writtenRelPaths;
        SyncResult(Set<String> paths) { this.writtenRelPaths = paths; }
    }

    /**
     * Downloads the branch zipball, extracts it to a temp directory, and copies matching files
     * under the configured source path into the target path. Returns the set of relative paths
     * (target-dir-relative, forward slashes) that were written — used for mirror-mode diffing.
     */
    private static SyncResult downloadAndApply(String repo, String branch, Path targetDir)
            throws IOException, InterruptedException {
        String sourcePath = normalizeRelPath(ModConfigs.AUTO_UPDATE_SOURCE_PATH.get());
        List<? extends String> blacklist = ModConfigs.AUTO_UPDATE_BLACKLIST.get();

        Path gameDir = FMLPaths.GAMEDIR.get();
        if (!targetDir.startsWith(gameDir)) {
            LOG.warn("Auto-updater: targetPath escapes game dir, refusing.");
            return new SyncResult(new LinkedHashSet<>());
        }
        Files.createDirectories(targetDir);

        Path staging = Files.createTempDirectory("soa-autoupdate-");
        try {
            Set<String> written;
            try (InputStream in = openZipball(repo, branch)) {
                written = extractAndApply(in, staging, targetDir, sourcePath, blacklist);
            }
            return new SyncResult(written);
        } finally {
            deleteRecursive(staging);
        }
    }

    private static InputStream openZipball(String repo, String branch) throws IOException, InterruptedException {
        String url = "https://github.com/" + repo + "/archive/refs/heads/" + enc(branch) + ".zip";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/zip")
                .GET()
                .build();
        HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() / 100 != 2) {
            resp.body().close();
            throw new IOException("zipball HTTP " + resp.statusCode() + " for " + url);
        }
        return resp.body();
    }

    /**
     * Streams the zip, strips the single top-level folder GitHub wraps archives in
     * (e.g. {@code Souls-of-Avarice-main/}), filters by sourcePath + blacklist, and copies
     * files into targetDir. Returns the number of files written.
     */
    private static Set<String> extractAndApply(InputStream zipStream,
                                               Path staging,
                                               Path targetDir,
                                               String sourcePath,
                                               List<? extends String> blacklist) throws IOException {
        Set<String> written = new LinkedHashSet<>();
        try (ZipInputStream zin = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory()) continue;
                String repoRel = stripTopLevel(name);
                if (repoRel == null || repoRel.isEmpty()) continue;
                if (isBlacklisted(repoRel, blacklist)) continue;

                String relInSource;
                if (sourcePath.isEmpty()) {
                    relInSource = repoRel;
                } else {
                    String prefix = sourcePath + "/";
                    if (!repoRel.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
                        continue;
                    }
                    relInSource = repoRel.substring(prefix.length());
                }
                if (relInSource.isEmpty()) continue;

                Path out = targetDir.resolve(relInSource).normalize();
                if (!out.startsWith(targetDir)) {
                    LOG.warn("Auto-updater: skipping zip-slip entry '{}'", name);
                    continue;
                }
                // Stage first so a failed write doesn't leave a half-written file in target.
                Path stagedFile = staging.resolve(relInSource).normalize();
                if (!stagedFile.startsWith(staging)) continue;
                Files.createDirectories(stagedFile.getParent());
                Files.copy(zin, stagedFile, StandardCopyOption.REPLACE_EXISTING);
                Files.createDirectories(out.getParent());
                try {
                    Files.move(stagedFile, out,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicFailed) {
                    // Some filesystems (notably older Windows setups) refuse atomic moves across
                    // different volumes or when the target is held open. Fall back to a non-atomic
                    // replace — still correct, just momentarily non-atomic.
                    Files.move(stagedFile, out, StandardCopyOption.REPLACE_EXISTING);
                }
                written.add(relInSource);
            }
        }
        return written;
    }

    private static String stripTopLevel(String zipEntryName) {
        // GitHub archives always wrap content in a single top-level folder like 'repo-branch/'.
        int slash = zipEntryName.indexOf('/');
        if (slash < 0) return "";
        return zipEntryName.substring(slash + 1);
    }

    private static boolean isBlacklisted(String repoRel, List<? extends String> blacklist) {
        if (blacklist == null || blacklist.isEmpty()) return false;
        String lower = repoRel.toLowerCase(Locale.ROOT);
        for (String raw : blacklist) {
            if (raw == null) continue;
            String needle = normalizeRelPath(raw).toLowerCase(Locale.ROOT);
            if (needle.isEmpty()) continue;
            if (lower.equals(needle) || lower.startsWith(needle + "/")) return true;
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // /reload on the server thread
    // ---------------------------------------------------------------------

    private static void triggerReload() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            LOG.debug("Auto-updater: no server running, skipping /reload.");
            return;
        }
        server.execute(() -> {
            try {
                CommandSourceStack src = server.createCommandSourceStack()
                        .withPermission(4)
                        .withSuppressedOutput();
                server.getCommands().performPrefixedCommand(src, "reload");
                LOG.info("Auto-updater: /reload issued after sync.");
            } catch (Throwable t) {
                LOG.warn("Auto-updater: /reload failed: {}", t.toString());
            }
        });
    }

    // ---------------------------------------------------------------------
    // SHA persistence
    // ---------------------------------------------------------------------

    private static Path stateDir() throws IOException {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("soa_additions");
        Files.createDirectories(dir);
        return dir;
    }

    private static String readShaFile(Path file) {
        try {
            if (!Files.exists(file)) return null;
            String s = Files.readString(file).trim();
            return s.isEmpty() ? null : s;
        } catch (IOException e) {
            return null;
        }
    }

    private static void writeShaFile(Path file, String sha) {
        try {
            Files.writeString(file, sha, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOG.debug("Auto-updater: could not persist SHA: {}", e.toString());
        }
    }

    // ---------------------------------------------------------------------
    // Mirror-mode manifest: remember which files this updater wrote last time
    // so we can delete ones that have disappeared from the repo without
    // touching files the user placed in targetPath themselves.
    // ---------------------------------------------------------------------

    private static Set<String> readManifest(Path file) {
        Set<String> out = new LinkedHashSet<>();
        try {
            if (!Files.exists(file)) return out;
            for (String line : Files.readAllLines(file)) {
                String s = line.trim();
                if (!s.isEmpty()) out.add(s);
            }
        } catch (IOException e) {
            LOG.debug("Auto-updater: could not read manifest: {}", e.toString());
        }
        return out;
    }

    private static void writeManifest(Path file, Set<String> paths) {
        try {
            List<String> sorted = new ArrayList<>(paths);
            sorted.sort(String::compareTo);
            Files.write(file, sorted, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOG.debug("Auto-updater: could not persist manifest: {}", e.toString());
        }
    }

    /**
     * Deletes files in {@code targetDir} that we wrote last cycle but that are no longer
     * present in the freshly-written set. Only removes files we previously recorded, so
     * user-placed files are never touched. Also prunes now-empty directories we created.
     */
    private static int deleteStale(Path targetDir, Set<String> previous, Set<String> current) {
        if (previous == null || previous.isEmpty()) return 0;
        int removed = 0;
        // Directories that may have become empty after file deletion — process deepest first.
        Set<Path> dirsTouched = new TreeSet<>(Comparator.comparingInt(
                (Path p) -> p.getNameCount()).reversed());
        for (String rel : previous) {
            if (current.contains(rel)) continue;
            Path f = targetDir.resolve(rel).normalize();
            if (!f.startsWith(targetDir)) continue; // defensive
            try {
                if (Files.deleteIfExists(f)) {
                    removed++;
                    Path parent = f.getParent();
                    while (parent != null && parent.startsWith(targetDir) && !parent.equals(targetDir)) {
                        dirsTouched.add(parent);
                        parent = parent.getParent();
                    }
                }
            } catch (IOException e) {
                LOG.debug("Auto-updater: could not delete stale file {}: {}", rel, e.toString());
            }
        }
        for (Path dir : dirsTouched) {
            try {
                if (Files.isDirectory(dir) && isEmpty(dir)) Files.deleteIfExists(dir);
            } catch (IOException ignored) {
            }
        }
        return removed;
    }

    private static boolean isEmpty(Path dir) throws IOException {
        try (var stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static String sanitizeRepo(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        // Accept full https URL too; reduce to owner/repo.
        s = s.replaceFirst("^https?://github\\.com/", "");
        s = s.replaceFirst("\\.git$", "");
        s = s.replaceAll("/+$", "");
        if (!s.matches("[A-Za-z0-9._-]+/[A-Za-z0-9._-]+")) return null;
        return s;
    }

    private static String normalizeRelPath(String p) {
        if (p == null) return "";
        String s = p.replace('\\', '/').trim();
        while (s.startsWith("/")) s = s.substring(1);
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private static String enc(String s) {
        return s.replace(" ", "%20");
    }

    private static boolean safeGetBool(net.minecraftforge.common.ForgeConfigSpec.BooleanValue v, boolean fallback) {
        try { return v.get(); } catch (Throwable t) { return fallback; }
    }

    private static void deleteRecursive(Path root) {
        if (root == null || !Files.exists(root)) return;
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
        }
    }
}
