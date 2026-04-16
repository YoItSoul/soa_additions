package com.soul.soa_additions.quest.builder;

import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.QuestSource;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.quest.model.RewardScope;
import com.soul.soa_additions.quest.reward.CommandReward;
import com.soul.soa_additions.quest.reward.GrantStageReward;
import com.soul.soa_additions.quest.reward.ItemReward;
import com.soul.soa_additions.quest.reward.LockPackmodeReward;
import com.soul.soa_additions.quest.reward.XpReward;
import com.soul.soa_additions.quest.task.AdvancementTask;
import com.soul.soa_additions.quest.task.CheckmarkTask;
import com.soul.soa_additions.quest.task.CraftTask;
import com.soul.soa_additions.quest.task.CustomTriggerTask;
import com.soul.soa_additions.quest.task.DimensionTask;
import com.soul.soa_additions.quest.task.ItemTask;
import com.soul.soa_additions.quest.task.KillTask;
import com.soul.soa_additions.quest.task.MineTask;
import com.soul.soa_additions.quest.task.StatTask;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Fluent builder for programmatic quest authoring. Use from Java when a chapter
 * should be generated from code (auto-generated milestones, procedural content,
 * quick prototypes before JSON finalization).
 *
 * <pre>{@code
 * Quests.chapter("tutorial", "The Basics")
 *   .icon("minecraft:oak_log")
 *   .order(0)
 *   .quest("chop_wood", "Start Chopping")
 *     .desc("Get your first logs.")
 *     .task(Quests.item("minecraft:oak_log", 8))
 *     .reward(Quests.item("minecraft:crafting_table", 1))
 *     .reward(Quests.xp(20))
 *     .reward(Quests.lockPackmode())
 *   .end()
 *   .build();
 * }</pre>
 */
public final class Quests {

    private Quests() {}

    public static ChapterBuilder chapter(String id, String title) {
        return new ChapterBuilder(id, title);
    }

    // ---------- task shortcuts ----------
    public static QuestTask kill(String entity, int count) {
        return new KillTask(new ResourceLocation(entity), count);
    }
    public static QuestTask item(String item, int count) {
        return new ItemTask(new ResourceLocation(item), null, null, count, false);
    }
    public static QuestTask itemConsume(String item, int count) {
        return new ItemTask(new ResourceLocation(item), null, null, count, true);
    }
    public static QuestTask itemTag(String tag, int count) {
        return new ItemTask(null, new ResourceLocation(tag), null, count, false);
    }
    public static QuestTask itemTagConsume(String tag, int count) {
        return new ItemTask(null, new ResourceLocation(tag), null, count, true);
    }
    public static QuestTask advancement(String adv) {
        return new AdvancementTask(new ResourceLocation(adv));
    }
    public static QuestTask dimension(String dim) {
        return new DimensionTask(new ResourceLocation(dim));
    }
    public static QuestTask stat(String statType, String statValue, int threshold) {
        return new StatTask(new ResourceLocation(statType), new ResourceLocation(statValue), threshold);
    }
    public static QuestTask trigger(String id, int count, String label) {
        return new CustomTriggerTask(new ResourceLocation(id), count, label);
    }
    public static QuestTask checkmark(String text) { return new CheckmarkTask(text); }
    public static QuestTask craft(String item, int count) {
        return new CraftTask(new ResourceLocation(item), null, null, count);
    }
    public static QuestTask craftTag(String tag, int count) {
        return new CraftTask(null, new ResourceLocation(tag), null, count);
    }
    public static QuestTask mine(String block, int count) {
        return new MineTask(new ResourceLocation(block), count);
    }

    // ---------- reward shortcuts (default PLAYER scope) ----------
    public static QuestReward itemReward(String item, int count) {
        return new ItemReward(new ResourceLocation(item), count, RewardScope.PLAYER);
    }
    public static QuestReward xp(int amount) { return new XpReward(amount, false, RewardScope.PLAYER); }
    public static QuestReward levels(int amount) { return new XpReward(amount, true, RewardScope.PLAYER); }
    public static QuestReward command(String cmd) { return new CommandReward(cmd, RewardScope.PLAYER); }
    public static QuestReward grantStage(String stage) { return new GrantStageReward(stage, RewardScope.TEAM); }
    public static QuestReward lockPackmode() { return new LockPackmodeReward(); }

    // ---------- explicitly team-scoped variants ----------
    public static QuestReward teamItem(String item, int count) {
        return new ItemReward(new ResourceLocation(item), count, RewardScope.TEAM);
    }
    public static QuestReward teamXp(int amount) { return new XpReward(amount, false, RewardScope.TEAM); }
    public static QuestReward teamLevels(int amount) { return new XpReward(amount, true, RewardScope.TEAM); }
    public static QuestReward teamCommand(String cmd) { return new CommandReward(cmd, RewardScope.TEAM); }

    // ---------- builder classes ----------

    public static final class ChapterBuilder {
        private final String id;
        private final String title;
        private List<String> description = new ArrayList<>();
        private String icon = "minecraft:writable_book";
        private int sortOrder = 1000;
        private List<String> requiresChapters = new ArrayList<>();
        private Set<PackMode> modes = EnumSet.allOf(PackMode.class);
        private String parentChapter = "";
        private final List<QuestBuilder> quests = new ArrayList<>();

        ChapterBuilder(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public ChapterBuilder desc(String... lines) {
            description = new ArrayList<>(Arrays.asList(lines));
            return this;
        }
        public ChapterBuilder icon(String icon) { this.icon = icon; return this; }
        public ChapterBuilder order(int n) { this.sortOrder = n; return this; }
        public ChapterBuilder requires(String... chapterIds) {
            requiresChapters = new ArrayList<>(Arrays.asList(chapterIds));
            return this;
        }
        public ChapterBuilder modes(PackMode... m) {
            modes = EnumSet.copyOf(Arrays.asList(m));
            return this;
        }
        public ChapterBuilder parent(String parentChapterId) {
            this.parentChapter = parentChapterId;
            return this;
        }

        public QuestBuilder quest(String id, String title) {
            QuestBuilder qb = new QuestBuilder(this, id, title);
            quests.add(qb);
            return qb;
        }

        public Chapter build() {
            List<Quest> qs = new ArrayList<>();
            for (QuestBuilder qb : quests) qs.add(qb.build(id, modes));
            return new Chapter(id, title, description, icon, sortOrder, requiresChapters,
                    new ArrayList<String>(), com.soul.soa_additions.quest.model.Visibility.NORMAL,
                    modes, qs, QuestSource.PROGRAMMATIC, parentChapter);
        }
    }

    public static final class QuestBuilder {
        private final ChapterBuilder parent;
        private final String id;
        private final String title;
        private List<String> description = new ArrayList<>();
        private String icon = "minecraft:paper";
        private com.soul.soa_additions.quest.model.Visibility visibility = com.soul.soa_additions.quest.model.Visibility.NORMAL;
        private boolean optional;
        private List<String> dependencies = new ArrayList<>();
        private boolean depsAll = true;
        private final List<QuestTask> tasks = new ArrayList<>();
        private final List<QuestReward> rewards = new ArrayList<>();
        private Set<PackMode> modes;
        private boolean autoClaim;
        private com.soul.soa_additions.quest.model.NodeShape shape = com.soul.soa_additions.quest.model.NodeShape.ICON;
        private int posX = -1;
        private int posY = -1;

        QuestBuilder(ChapterBuilder parent, String id, String title) {
            this.parent = parent;
            this.id = id;
            this.title = title;
        }

        public QuestBuilder desc(String... lines) {
            description = new ArrayList<>(Arrays.asList(lines));
            return this;
        }
        public QuestBuilder icon(String icon) { this.icon = icon; return this; }
        public QuestBuilder hidden() { this.visibility = com.soul.soa_additions.quest.model.Visibility.HIDDEN_UNTIL_DEPS; return this; }
        public QuestBuilder visibility(com.soul.soa_additions.quest.model.Visibility v) { this.visibility = v; return this; }
        public QuestBuilder optional() { this.optional = true; return this; }
        public QuestBuilder requires(String... questIds) {
            dependencies = new ArrayList<>(Arrays.asList(questIds));
            return this;
        }
        public QuestBuilder anyOf() { this.depsAll = false; return this; }
        public QuestBuilder modes(PackMode... m) {
            modes = EnumSet.copyOf(Arrays.asList(m));
            return this;
        }
        public QuestBuilder task(QuestTask t) { tasks.add(t); return this; }
        public QuestBuilder reward(QuestReward r) { rewards.add(r); return this; }
        public QuestBuilder autoClaim() { this.autoClaim = true; return this; }
        public QuestBuilder shape(com.soul.soa_additions.quest.model.NodeShape s) { this.shape = s; return this; }
        public QuestBuilder pos(int x, int y) { this.posX = x; this.posY = y; return this; }

        /** Finish this quest and return to the parent chapter builder. */
        public ChapterBuilder end() { return parent; }

        Quest build(String chapterId, Set<PackMode> chapterModes) {
            Set<PackMode> effective = modes != null ? EnumSet.copyOf(modes) : EnumSet.copyOf(chapterModes);
            effective.retainAll(chapterModes);
            return new Quest(id, chapterId, title, description, icon, visibility, optional,
                    dependencies, depsAll, -1, tasks, rewards, effective, QuestSource.PROGRAMMATIC, autoClaim, shape, posX, posY, true, Quest.DEFAULT_SIZE, false, com.soul.soa_additions.quest.model.RewardScope.TEAM, java.util.List.of());
        }
    }
}
