package com.soul.soa_additions.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.quest.reward.RewardRegistry;
import com.soul.soa_additions.quest.task.TaskRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Datapack-style loader for quest chapters.
 *
 * <p>Path: {@code data/<namespace>/quests/<chapter>.json}. Each file is a full
 * chapter with its embedded quests. This mirrors vanilla's loot_tables layout and
 * means anyone (datapacks, addons, in-game editor writing to a world datapack)
 * can contribute quests without touching mod code.</p>
 *
 * <p>Also populates the registry from any {@code ProgrammaticQuestSource}s
 * registered via {@link QuestRegistry#registerProgrammaticSource}, so devs can
 * define quests in pure Java via the {@code Quests} builder DSL.</p>
 */
public final class QuestLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/quest-loader");
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public QuestLoader() {
        super(GSON, "quests");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager mgr, ProfilerFiller profiler) {
        TaskRegistry.bootstrap();
        RewardRegistry.bootstrap();

        Map<String, Chapter> chapters = new LinkedHashMap<>();

        // 1. JSON chapters from datapacks (builtin or external). We can't distinguish
        //    the two reliably from SimpleJsonResourceReloadListener's input — it merges
        //    all datapacks. For editor purposes we treat everything here as DATAPACK
        //    except the known soa_additions builtin namespace, which we tag BUILTIN_JSON.
        for (Map.Entry<ResourceLocation, JsonElement> e : files.entrySet()) {
            try {
                QuestSource src = e.getKey().getNamespace().equals(SoaAdditions.MODID)
                        ? QuestSource.BUILTIN_JSON
                        : QuestSource.DATAPACK;
                Chapter ch = parseChapter(e.getKey(), e.getValue().getAsJsonObject(), src);
                if (ch != null) chapters.put(ch.id(), ch);
            } catch (Exception ex) {
                LOG.error("Failed to load quest file {}: {}", e.getKey(), ex.getMessage(), ex);
            }
        }

        // 2. Programmatic chapters contributed via Java builders
        for (Chapter ch : QuestRegistry.programmaticChapters()) {
            chapters.put(ch.id(), ch);
        }

        // 3. World-level overrides and in-game-edited chapters win last.
        //    Populated by QuestRegistry once QuestOverrideStorage is wired.
        for (Chapter ch : QuestRegistry.worldEditChapters()) {
            chapters.put(ch.id(), ch);
        }

        // Sort chapters by sort_order then id for a deterministic GUI layout
        List<Chapter> sorted = new ArrayList<>(chapters.values());
        sorted.sort(Comparator.comparingInt(Chapter::sortOrder).thenComparing(Chapter::id));

        QuestRegistry.replace(sorted);
        int totalQuests = sorted.stream().mapToInt(c -> c.quests().size()).sum();
        int totalRewards = sorted.stream()
                .flatMap(c -> c.quests().stream())
                .mapToInt(q -> q.rewards().size())
                .sum();
        LOG.info("[SOA Quests] Loaded {} chapters, {} quests, {} rewards", sorted.size(), totalQuests, totalRewards);

        // Broadcast updated quest definitions to all connected clients so a
        // /reload doesn't leave them stale. This is a no-op during initial
        // startup (no players connected yet); on subsequent reloads it ensures
        // every client's QuestRegistry matches the server's.
        mgr.listPacks().findFirst().ifPresent(p -> {
            try {
                net.minecraft.server.MinecraftServer server =
                        net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
                if (server != null && !server.getPlayerList().getPlayers().isEmpty()) {
                    com.soul.soa_additions.quest.net.QuestDefinitionSyncPacket.sendToAll(server);
                }
            } catch (Exception ignored) { /* startup race — no players yet */ }
        });
    }

    // ---------- parsing ----------

    /**
     * Parse a raw JSON object as a {@link QuestSource#WORLD_EDITS} chapter.
     * Used by the lifecycle handler to hand world-override files to the
     * registry supplier without routing them through the resource-manager
     * loop. Returns null on failure (caller logs).
     */
    public static Chapter parseChapterForWorldEdits(JsonObject root) {
        try {
            return new QuestLoader().parseChapter(
                    new ResourceLocation(SoaAdditions.MODID, "world_edit"),
                    root,
                    QuestSource.WORLD_EDITS);
        } catch (Exception e) {
            LOG.error("Failed to parse world-edit chapter: {}", e.getMessage(), e);
            return null;
        }
    }

    private Chapter parseChapter(ResourceLocation path, JsonObject root, QuestSource source) {
        String id = root.has("id") ? root.get("id").getAsString() : path.getPath();
        String title = root.has("title") ? root.get("title").getAsString() : id;
        List<String> desc = readStrings(root, "description");
        String icon = root.has("icon") ? root.get("icon").getAsString() : "minecraft:writable_book";
        int sortOrder = root.has("sort_order") ? root.get("sort_order").getAsInt() : 1000;
        List<String> requiresChapters = readStrings(root, "requires_chapter");
        List<String> requiresQuests = readStrings(root, "requires_quests");
        com.soul.soa_additions.quest.model.Visibility visibility =
                root.has("visibility")
                        ? com.soul.soa_additions.quest.model.Visibility.fromString(root.get("visibility").getAsString())
                        : (root.has("hidden") && root.get("hidden").getAsBoolean()
                                ? com.soul.soa_additions.quest.model.Visibility.HIDDEN_UNTIL_DEPS
                                : com.soul.soa_additions.quest.model.Visibility.NORMAL);
        Set<PackMode> modes = readModes(root);

        List<Quest> quests = new ArrayList<>();
        JsonArray arr = root.has("quests") ? root.getAsJsonArray("quests") : new JsonArray();
        for (JsonElement qEl : arr) {
            Quest q = parseQuest(id, qEl.getAsJsonObject(), modes, source);
            if (q != null) quests.add(q);
        }
        String parentChapter = root.has("parent_chapter") ? root.get("parent_chapter").getAsString() : "";
        return new Chapter(id, title, desc, icon, sortOrder, requiresChapters, requiresQuests, visibility, modes, quests, source, parentChapter);
    }

    private Quest parseQuest(String chapterId, JsonObject root, Set<PackMode> chapterModes, QuestSource source) {
        String id = root.get("id").getAsString();
        Set<PackMode> modes = root.has("modes") ? readModes(root) : EnumSet.copyOf(chapterModes);
        // Chapter modes are the upper bound: a quest can't be in a mode its chapter isn't
        modes.retainAll(chapterModes);
        if (modes.isEmpty()) return null;

        String title = root.has("title") ? root.get("title").getAsString() : id;
        List<String> description = readStrings(root, "description");
        String icon = root.has("icon") ? root.get("icon").getAsString() : "minecraft:paper";
        com.soul.soa_additions.quest.model.Visibility visibility =
                root.has("visibility")
                        ? com.soul.soa_additions.quest.model.Visibility.fromString(root.get("visibility").getAsString())
                        : (root.has("hidden") && root.get("hidden").getAsBoolean()
                                ? com.soul.soa_additions.quest.model.Visibility.HIDDEN_UNTIL_DEPS
                                : com.soul.soa_additions.quest.model.Visibility.NORMAL);
        boolean optional = root.has("optional") && root.get("optional").getAsBoolean();
        List<String> deps = readStrings(root, "dependencies");
        boolean depsAll = !root.has("dependency_logic") || "all".equalsIgnoreCase(root.get("dependency_logic").getAsString());
        int minDeps = root.has("min_deps") ? root.get("min_deps").getAsInt() : -1;
        boolean autoClaim = root.has("auto_claim") && root.get("auto_claim").getAsBoolean();
        com.soul.soa_additions.quest.model.NodeShape shape =
                com.soul.soa_additions.quest.model.NodeShape.fromString(
                        root.has("shape") ? root.get("shape").getAsString() : null);

        List<QuestTask> tasks = new ArrayList<>();
        if (root.has("tasks")) {
            for (JsonElement t : root.getAsJsonArray("tasks")) {
                tasks.add(TaskRegistry.deserialize(t.getAsJsonObject()));
            }
        }
        List<QuestReward> rewards = new ArrayList<>();
        if (root.has("rewards")) {
            for (JsonElement r : root.getAsJsonArray("rewards")) {
                // Per-entry try so a single unknown/malformed reward (e.g. a
                // future registered type on a newer server, a typo in a
                // datapack) can't take the entire chapter offline. We log and
                // drop just the offending entry.
                try {
                    rewards.add(RewardRegistry.deserialize(r.getAsJsonObject()));
                } catch (Exception rex) {
                    LOG.warn("Skipping malformed reward in quest {}: {}", id, rex.getMessage());
                }
            }
        }

        // mode_overrides: per-mode field patch. For v1 we support tasks + rewards overrides.
        // For each mode in `modes`, if there's an override entry, we emit a distinct Quest.
        // Otherwise we emit a single Quest covering all of `modes`.
        if (root.has("mode_overrides")) {
            // Expand: produce multiple mode-specific Quests that each carry one mode.
            // QuestRegistry stores quests by (chapter, mode); the GUI picks the right one.
            // For now we only materialize the first matching variant into the unified list —
            // full per-mode divergence will layer on top when PackModeData arrives.
            JsonObject overrides = root.getAsJsonObject("mode_overrides");
            for (PackMode mode : modes) {
                if (overrides.has(mode.lower())) {
                    JsonObject patch = overrides.getAsJsonObject(mode.lower());
                    if (patch.has("tasks")) {
                        tasks = new ArrayList<>();
                        for (JsonElement t : patch.getAsJsonArray("tasks"))
                            tasks.add(TaskRegistry.deserialize(t.getAsJsonObject()));
                    }
                    if (patch.has("rewards")) {
                        rewards = new ArrayList<>();
                        for (JsonElement r : patch.getAsJsonArray("rewards")) {
                            try {
                                rewards.add(RewardRegistry.deserialize(r.getAsJsonObject()));
                            } catch (Exception rex) {
                                LOG.warn("Skipping malformed mode-override reward in quest {}: {}", id, rex.getMessage());
                            }
                        }
                    }
                    break; // TODO: emit per-mode duplicates once PackModeData filters at query time
                }
            }
        }

        int posX = root.has("x") ? root.get("x").getAsInt() : -1;
        int posY = root.has("y") ? root.get("y").getAsInt() : -1;
        boolean showDeps = !root.has("show_deps") || root.get("show_deps").getAsBoolean();
        int size = root.has("size") ? root.get("size").getAsInt() : com.soul.soa_additions.quest.model.Quest.DEFAULT_SIZE;
        boolean repeatable = root.has("repeatable") && root.get("repeatable").getAsBoolean();
        com.soul.soa_additions.quest.model.RewardScope repeatScope = root.has("repeat_scope")
                ? com.soul.soa_additions.quest.model.RewardScope.fromString(root.get("repeat_scope").getAsString())
                : com.soul.soa_additions.quest.model.RewardScope.TEAM;
        List<String> exclusions = readStrings(root, "exclusions");
        return new Quest(id, chapterId, title, description, icon, visibility, optional, deps, depsAll, minDeps, tasks, rewards, modes, source, autoClaim, shape, posX, posY, showDeps, size, repeatable, repeatScope, exclusions);
    }

    private static List<String> readStrings(JsonObject o, String key) {
        List<String> out = new ArrayList<>();
        if (!o.has(key)) return out;
        JsonElement el = o.get(key);
        if (el.isJsonArray()) {
            for (JsonElement s : el.getAsJsonArray()) out.add(s.getAsString());
        } else if (el.isJsonPrimitive()) {
            out.add(el.getAsString());
        }
        return out;
    }

    private static Set<PackMode> readModes(JsonObject o) {
        if (!o.has("modes")) return EnumSet.allOf(PackMode.class);
        Set<PackMode> out = EnumSet.noneOf(PackMode.class);
        for (JsonElement e : o.getAsJsonArray("modes")) {
            out.add(PackMode.fromString(e.getAsString()));
        }
        return out.isEmpty() ? EnumSet.allOf(PackMode.class) : out;
    }
}
