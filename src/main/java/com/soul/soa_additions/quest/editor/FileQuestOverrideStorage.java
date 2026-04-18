package com.soul.soa_additions.quest.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.QuestSource;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.QuestTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * File-backed {@link QuestOverrideStorage}. Writes chapters as JSON files
 * into the modpack-wide override dir at {@code <gameDir>/soa_additions/quest_edits/}
 * ({@code WORLD_OVERRIDE}) so edits persist across every world opened in the
 * same modpack instance. In dev environments writes can also target the run
 * dir's {@code src/main/resources/data/soa_additions/quests/} ({@code AUTHOR_SOURCE}).
 *
 * <p>On construction, any legacy per-world override files at
 * {@code <world>/soa_additions/quest_edits/} are migrated into the modpack
 * dir (without clobbering existing files), then renamed to {@code .migrated}
 * so subsequent boots don't reattempt the copy.</p>
 *
 * <p>Writes are best-effort atomic: the file is written to a {@code .tmp}
 * sibling and then renamed over the target. A crash mid-write leaves the
 * prior file intact.</p>
 */
public final class FileQuestOverrideStorage implements QuestOverrideStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path worldOverrideDir;
    private final Path legacyWorldOverrideDir;
    private final Path authorSourceDir;

    public FileQuestOverrideStorage(Path worldDir, Path gameDir) {
        // Modpack-wide: edits live next to the modpack instance, not the world.
        this.worldOverrideDir = gameDir.resolve("soa_additions").resolve("quests");
        this.legacyWorldOverrideDir = worldDir.resolve("soa_additions").resolve("quest_edits");
        // Author path exists only in dev environments — the presence of
        // src/main/resources signals this.
        this.authorSourceDir = gameDir.resolve("../src/main/resources/data/soa_additions/quests").normalize();
        log("worldOverrideDir resolved to: " + worldOverrideDir.toAbsolutePath()
                + " (exists=" + Files.isDirectory(worldOverrideDir) + ")");
        migrateLegacyOverrides();
    }

    /** One-shot copy of any per-world overrides into the modpack dir. Files
     *  that already exist in the modpack dir are not clobbered; legacy files
     *  are renamed to {@code .migrated} after a successful copy so we don't
     *  re-run the migration on every boot. */
    private void migrateLegacyOverrides() {
        if (!Files.isDirectory(legacyWorldOverrideDir)) return;
        try {
            Files.createDirectories(worldOverrideDir);
            try (Stream<Path> files = Files.list(legacyWorldOverrideDir)) {
                for (Path src : files.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                    Path dst = worldOverrideDir.resolve(src.getFileName());
                    if (!Files.exists(dst)) {
                        Files.copy(src, dst);
                        log("Migrated legacy world override " + src.getFileName() + " → modpack dir");
                    } else {
                        log("Skipping migration of " + src.getFileName() + " — modpack file already exists");
                    }
                    try {
                        Files.move(src, src.resolveSibling(src.getFileName().toString() + ".migrated"),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ignored) {}
                }
            }
        } catch (IOException e) {
            log("Failed to migrate legacy overrides: " + e.getMessage());
        }
    }

    // ---------- QuestOverrideStorage ----------

    @Override
    public void saveChapter(Chapter chapter, EditTarget target) {
        Path dir = resolveDir(target);
        if (dir == null) {
            log("Author-source directory unavailable; refusing to write " + chapter.id());
            return;
        }
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(chapter.id() + ".json");
            Path tmp = file.resolveSibling(chapter.id() + ".json.tmp");
            String json = GSON.toJson(serializeChapter(chapter));
            Files.writeString(tmp, json, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException amns) {
                // ATOMIC_MOVE can fail on some Windows configurations; fall
                // back to a plain replace which is still safe (tmp is complete).
                Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            log("Wrote chapter " + chapter.id() + " → " + target + " (" + file.toAbsolutePath() + ")");
        } catch (IOException e) {
            log("Failed to save chapter " + chapter.id() + " to " + dir.toAbsolutePath() + ": " + e.getMessage());
        }
    }

    @Override
    public void deleteChapterOverride(String chapterId) {
        try {
            Files.deleteIfExists(worldOverrideDir.resolve(chapterId + ".json"));
        } catch (IOException e) {
            log("Failed to delete override " + chapterId + ": " + e.getMessage());
        }
    }

    @Override
    public Optional<Chapter> loadChapter(String chapterId) {
        // Parsing is handled by QuestLoader; we only provide the raw file list
        // via loadWorldEdits(). A full load-as-Chapter method would duplicate
        // loader parsing, so skip it until the editor GUI actually needs it.
        return Optional.empty();
    }

    @Override
    public List<String> listOverriddenChapterIds() {
        if (!Files.isDirectory(worldOverrideDir)) return List.of();
        try (Stream<Path> files = Files.list(worldOverrideDir)) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replace(".json", ""))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public void saveQuest(String chapterId, Quest quest, EditTarget target) {
        // Convenience — the editor uses this to update one quest inside a
        // chapter without rebuilding the full object graph. Full implementation
        // will be added once the GUI has a concrete editor state shape to
        // lean on; the interface exists now so call sites can be written
        // against the stable API.
        log("saveQuest stub: " + chapterId + "/" + quest.id() + " target=" + target);
    }

    @Override
    public boolean canWrite(Chapter chapter, EditTarget target) {
        if (target == EditTarget.AUTHOR_SOURCE) {
            return chapter.source() != QuestSource.PROGRAMMATIC && Files.isDirectory(authorSourceDir);
        }
        return true; // world overrides always allowed
    }

    // ---------- JSON serialization ----------
    // Format is intentionally the same as QuestLoader's read format.

    private JsonObject serializeChapter(Chapter chapter) {
        JsonObject out = new JsonObject();
        out.addProperty("id", chapter.id());
        out.addProperty("title", chapter.title());
        out.addProperty("icon", chapter.icon());
        out.addProperty("sort_order", chapter.sortOrder());

        JsonArray desc = new JsonArray();
        for (String line : chapter.description()) desc.add(line);
        out.add("description", desc);

        JsonArray modes = new JsonArray();
        chapter.modes().forEach(m -> modes.add(m.lower()));
        out.add("modes", modes);

        JsonArray deps = new JsonArray();
        for (String d : chapter.requiresChapters()) deps.add(d);
        out.add("requires_chapter", deps);

        if (!chapter.requiresQuests().isEmpty()) {
            JsonArray rq = new JsonArray();
            for (String q : chapter.requiresQuests()) rq.add(q);
            out.add("requires_quests", rq);
        }
        if (chapter.visibility() != com.soul.soa_additions.quest.model.Visibility.NORMAL) {
            out.addProperty("visibility", chapter.visibility().lower());
        }
        if (chapter.hasParent()) {
            out.addProperty("parent_chapter", chapter.parentChapter());
        }

        JsonArray quests = new JsonArray();
        for (Quest q : chapter.quests()) quests.add(serializeQuest(q));
        out.add("quests", quests);

        return out;
    }

    private JsonObject serializeQuest(Quest q) {
        JsonObject out = new JsonObject();
        out.addProperty("id", q.id());
        out.addProperty("title", q.title());
        out.addProperty("icon", q.icon());
        if (q.visibility() != com.soul.soa_additions.quest.model.Visibility.NORMAL) {
            out.addProperty("visibility", q.visibility().lower());
        }
        if (q.optional()) out.addProperty("optional", true);
        if (!q.depsAll()) out.addProperty("dependency_logic", "any");
        if (q.minDeps() > 0) out.addProperty("min_deps", q.minDeps());
        if (q.autoClaim()) out.addProperty("auto_claim", true);
        if (!q.showDeps()) out.addProperty("show_deps", false);
        if (q.shape() != com.soul.soa_additions.quest.model.NodeShape.ICON) {
            out.addProperty("shape", q.shape().name().toLowerCase());
        }
        if (q.hasManualPosition()) {
            out.addProperty("x", q.posX());
            out.addProperty("y", q.posY());
        }
        if (q.size() > 0 && q.size() != com.soul.soa_additions.quest.model.Quest.DEFAULT_SIZE) {
            out.addProperty("size", q.size());
        }
        if (q.repeatable()) {
            out.addProperty("repeatable", true);
            if (q.repeatScope() != com.soul.soa_additions.quest.model.RewardScope.TEAM) {
                out.addProperty("repeat_scope", q.repeatScope().lower());
            }
        }
        if (q.exclusions() != null && !q.exclusions().isEmpty()) {
            JsonArray ex = new JsonArray();
            for (String s : q.exclusions()) ex.add(s);
            out.add("exclusions", ex);
        }

        JsonArray desc = new JsonArray();
        for (String line : q.description()) desc.add(line);
        out.add("description", desc);

        JsonArray deps = new JsonArray();
        for (String d : q.dependencies()) deps.add(d);
        out.add("dependencies", deps);

        JsonArray tasks = new JsonArray();
        for (QuestTask t : q.tasks()) {
            JsonObject taskJson = new JsonObject();
            t.writeJson(taskJson);
            tasks.add(taskJson);
        }
        out.add("tasks", tasks);

        JsonArray rewards = new JsonArray();
        for (QuestReward r : q.rewards()) {
            JsonObject rewardJson = new JsonObject();
            r.writeJson(rewardJson);
            rewards.add(rewardJson);
        }
        out.add("rewards", rewards);

        JsonArray modes = new JsonArray();
        q.modes().forEach(m -> modes.add(m.lower()));
        out.add("modes", modes);

        return out;
    }

    // ---------- helpers ----------

    private Path resolveDir(EditTarget target) {
        if (target == EditTarget.AUTHOR_SOURCE) {
            if (!Files.isDirectory(authorSourceDir)) return null;
            return authorSourceDir;
        }
        return worldOverrideDir;
    }

    /**
     * Read every JSON file in the world override dir as a raw {@code JsonObject}
     * so {@code QuestLoader} can re-parse them on reload. Returned in a stable
     * order (alphabetical) so reload output is deterministic.
     */
    public List<JsonObject> loadWorldEditRawJson() {
        if (!Files.isDirectory(worldOverrideDir)) return List.of();
        List<JsonObject> out = new ArrayList<>();
        try (Stream<Path> files = Files.list(worldOverrideDir)) {
            List<Path> sorted = files
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
            for (Path p : sorted) {
                try {
                    JsonElement el = JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8));
                    if (el.isJsonObject()) out.add(el.getAsJsonObject());
                } catch (Exception e) {
                    log("Failed to parse override " + p.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log("Failed to list override dir: " + e.getMessage());
        }
        return out;
    }

    private static void log(String msg) {
        org.slf4j.LoggerFactory.getLogger("soa_additions/quest-editor").info(msg);
    }
}
