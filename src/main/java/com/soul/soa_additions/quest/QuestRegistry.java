package com.soul.soa_additions.quest;

import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * In-memory home of all loaded chapters + quests. Replaced wholesale on each
 * {@code /reload}. Also holds programmatic sources: any mod (or soa_additions
 * itself) can register Java-defined chapters via {@link #registerProgrammaticSource}.
 */
public final class QuestRegistry {

    private static volatile Map<String, Chapter> chaptersById = Collections.emptyMap();
    private static volatile Map<String, Quest> questsByFullId = Collections.emptyMap();
    /**
     * Index of every task in every loaded quest, keyed by task type. Lets event
     * handlers and pollers walk only the tasks that could possibly care about
     * the event instead of scanning the full quest tree. Rebuilt whenever the
     * chapter set or any single chapter changes — mutations are rare so the
     * cost is negligible compared to per-event savings.
     */
    private static volatile Map<ResourceLocation, List<TaskRef>> tasksByType = Collections.emptyMap();
    private static final List<Supplier<List<Chapter>>> PROGRAMMATIC = new ArrayList<>();

    /** A single task located inside the quest tree. */
    public record TaskRef(Quest quest, int taskIndex, QuestTask task) {}

    private QuestRegistry() {}

    /** Replace the active chapter set. Called by the loader after each reload. */
    public static void replace(List<Chapter> chapters) {
        Map<String, Chapter> ch = new LinkedHashMap<>();
        Map<String, Quest> qs = new HashMap<>();
        for (Chapter c : chapters) {
            ch.put(c.id(), c);
            for (Quest q : c.quests()) {
                qs.put(q.fullId(), q);
            }
        }
        chaptersById = ch;
        questsByFullId = qs;
        rebuildTaskIndex();
        com.soul.soa_additions.quest.web.QuestWebServer.invalidateLayoutCache();
    }

    /**
     * Swap a single chapter in place. Used by the in-game editor when a tweak
     * (move a quest, rename a task) needs to take effect without a full
     * datapack reload. The replacement must carry the same id — otherwise the
     * old chapter stays and a stray copy lands under the new id.
     */
    public static synchronized void updateChapter(Chapter updated) {
        Map<String, Chapter> ch = new LinkedHashMap<>(chaptersById);
        ch.put(updated.id(), updated);
        Map<String, Quest> qs = new HashMap<>(questsByFullId);
        // Drop old quests for this chapter, then re-add from the replacement.
        qs.values().removeIf(q -> q.chapterId().equals(updated.id()));
        for (Quest q : updated.quests()) qs.put(q.fullId(), q);
        chaptersById = ch;
        questsByFullId = qs;
        rebuildTaskIndex();
        com.soul.soa_additions.quest.web.QuestWebServer.invalidateLayoutCache();
    }

    /** Drop a chapter and every quest inside it. Used by the editor's
     *  delete-category flow. */
    public static synchronized void removeChapter(String chapterId) {
        if (!chaptersById.containsKey(chapterId)) return;
        Map<String, Chapter> ch = new LinkedHashMap<>(chaptersById);
        ch.remove(chapterId);
        Map<String, Quest> qs = new HashMap<>(questsByFullId);
        qs.values().removeIf(q -> q.chapterId().equals(chapterId));
        chaptersById = ch;
        questsByFullId = qs;
        rebuildTaskIndex();
    }

    /** Reorder the chapter map to match the given id sequence. Ids not in the
     *  current map are ignored; missing ids are appended in their existing
     *  order so partial reorders don't drop chapters. */
    public static synchronized void reorderChapters(List<String> orderedIds) {
        Map<String, Chapter> next = new LinkedHashMap<>();
        for (String id : orderedIds) {
            Chapter c = chaptersById.get(id);
            if (c != null) next.put(id, c);
        }
        for (var e : chaptersById.entrySet()) next.putIfAbsent(e.getKey(), e.getValue());
        chaptersById = next;
    }

    /** Unmodifiable live view — zero allocation. Callers must not try to
     *  modify the returned list. Safe because {@code chaptersById} is swapped
     *  atomically on mutation (never mutated in place). */
    public static java.util.Collection<Chapter> allChapters() {
        return Collections.unmodifiableCollection(chaptersById.values());
    }

    public static List<Chapter> chaptersFor(PackMode mode) {
        List<Chapter> out = new ArrayList<>();
        for (Chapter c : chaptersById.values()) {
            if (c.availableIn(mode)) out.add(c);
        }
        return out;
    }

    public static Optional<Chapter> chapter(String id) {
        return Optional.ofNullable(chaptersById.get(id));
    }

    public static Optional<Quest> quest(String fullId) {
        return Optional.ofNullable(questsByFullId.get(fullId));
    }

    /** Look up a quest by bare id (no chapter prefix) across all chapters.
     *  Returns the first match. Used as a fallback when a dependency was
     *  stored without its chapter prefix (cross-chapter bare id). */
    public static Optional<Quest> questByBareId(String bareId) {
        for (Quest q : questsByFullId.values()) {
            if (q.id().equals(bareId)) return Optional.of(q);
        }
        return Optional.empty();
    }

    public static int questCount() { return questsByFullId.size(); }

    // ---------- task-type index ----------

    /** All tasks in the loaded quest tree of the given type. Empty list if none. */
    public static List<TaskRef> tasksOfType(ResourceLocation type) {
        List<TaskRef> hit = tasksByType.get(type);
        return hit == null ? Collections.emptyList() : hit;
    }

    /** Cheap existence check — pollers use this to skip work entirely when no
     *  task of their type is currently loaded anywhere in the quest tree. */
    public static boolean hasTasksOfType(ResourceLocation type) {
        List<TaskRef> hit = tasksByType.get(type);
        return hit != null && !hit.isEmpty();
    }

    private static void rebuildTaskIndex() {
        Map<ResourceLocation, List<TaskRef>> idx = new HashMap<>();
        for (Chapter c : chaptersById.values()) {
            for (Quest q : c.quests()) {
                var tasks = q.tasks();
                for (int i = 0; i < tasks.size(); i++) {
                    QuestTask t = tasks.get(i);
                    idx.computeIfAbsent(t.type(), k -> new ArrayList<>()).add(new TaskRef(q, i, t));
                }
            }
        }
        tasksByType = idx;
    }

    // ---------- programmatic sources ----------

    /**
     * Register a source of Java-defined chapters. The supplier is invoked every
     * reload so mods can produce fresh content that reflects their own state
     * (e.g. auto-generated "reach Y blocks" quests).
     */
    public static void registerProgrammaticSource(Supplier<List<Chapter>> source) {
        PROGRAMMATIC.add(source);
    }

    static List<Chapter> programmaticChapters() {
        List<Chapter> out = new ArrayList<>();
        for (Supplier<List<Chapter>> s : PROGRAMMATIC) {
            try { out.addAll(s.get()); }
            catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger("soa_additions/quest")
                        .error("Programmatic quest source failed: {}", e.getMessage(), e);
            }
        }
        return out;
    }

    // ---------- in-game editor hook ----------

    private static volatile Supplier<List<Chapter>> worldEditsSource = Collections::emptyList;

    /**
     * Registered once by {@code QuestOverrideStorage} when it loads a world.
     * Returns the chapters that live in {@code <world>/soa_additions/quest_edits/}
     * so they can layer on top of datapack and programmatic content.
     */
    public static void setWorldEditsSource(Supplier<List<Chapter>> source) {
        worldEditsSource = source;
    }

    static List<Chapter> worldEditChapters() {
        try { return worldEditsSource.get(); }
        catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger("soa_additions/quest")
                    .error("World-edits quest source failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
