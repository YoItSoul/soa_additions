package com.soul.soa_additions.quest.progress;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.quest.task.EmcTask;
import com.soul.soa_additions.quest.task.EnergyTask;
import com.soul.soa_additions.quest.task.ItemTask;
import com.soul.soa_additions.quest.task.ManaTask;
import com.soul.soa_additions.quest.task.XpTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read/credit helpers for the Task Collector block: "how much of resource X do
 * this player's currently-active tasks still need?" Mirrors the visibility
 * rules of {@link ProgressService#apply} (only VISIBLE/READY quests count) so
 * the collector never swallows resources it can't credit.
 *
 * <p>Amounts are in raw resource units (FE, XP points, mana, EMC). Only
 * {@link EnergyTask} scales its progress counter ({@link EnergyTask#UNIT});
 * the other resource tasks are 1:1.</p>
 */
public final class CollectorService {

    private CollectorService() {}

    /** A task the collector can service right now. */
    public record ActiveTask(String questId, int taskIndex, QuestTask task) {}

    /** Raw FE-per-point for a task type (1 for everything except energy). */
    public static long unitOf(ResourceLocation type) {
        return type.equals(EnergyTask.TYPE) ? EnergyTask.UNIT : 1L;
    }

    private static TeamQuestProgress teamProgress(ServerPlayer player) {
        var teams = com.soul.soa_additions.quest.team.TeamData.get(player.server);
        var team = teams.teamOf(player);
        return QuestProgressData.get(player.server).forTeam(team.id());
    }

    /** Sum of raw resource still needed across the player's active tasks of {@code type}. */
    public static long remainingRaw(ServerPlayer player, ResourceLocation type) {
        var refs = QuestRegistry.tasksOfType(type);
        if (refs.isEmpty()) return 0;
        TeamQuestProgress tp = teamProgress(player);
        long unit = unitOf(type);
        long total = 0;
        Map<String, QuestStatus> statusCache = new HashMap<>();
        for (QuestRegistry.TaskRef ref : refs) {
            Quest quest = ref.quest();
            QuestStatus status = statusCache.computeIfAbsent(quest.fullId(),
                    id -> QuestEvaluator.recompute(quest, tp));
            if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) continue;
            QuestProgress qp = tp.get(quest.fullId());
            TaskProgress prog = qp.task(ref.taskIndex());
            long left = (long) ref.task().target() - prog.count();
            if (left > 0) total += left * unit;
        }
        return total;
    }

    /**
     * Credit progress points ({@code units}, already divided by the type's
     * unit) to the player's active tasks of {@code type}.
     */
    public static void creditUnits(ServerPlayer player, long units, ResourceLocation type) {
        if (units <= 0) return;
        int delta = (int) Math.min(Integer.MAX_VALUE, units);
        ProgressService.apply(player, delta, type, t -> true);
    }

    /** Items still needed across active consume-variant item tasks matching {@code stack}. */
    public static long remainingItems(ServerPlayer player, ItemStack stack) {
        var refs = QuestRegistry.tasksOfType(ItemTask.TYPE);
        if (refs.isEmpty() || stack.isEmpty()) return 0;
        TeamQuestProgress tp = teamProgress(player);
        long total = 0;
        Map<String, QuestStatus> statusCache = new HashMap<>();
        for (QuestRegistry.TaskRef ref : refs) {
            if (!(ref.task() instanceof ItemTask it) || !it.consume() || !it.matches(stack)) continue;
            Quest quest = ref.quest();
            QuestStatus status = statusCache.computeIfAbsent(quest.fullId(),
                    id -> QuestEvaluator.recompute(quest, tp));
            if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) continue;
            QuestProgress qp = tp.get(quest.fullId());
            long left = (long) it.target() - qp.task(ref.taskIndex()).count();
            if (left > 0) total += left;
        }
        return total;
    }

    /** Credit consumed items to active consume item-tasks that match {@code stack}. */
    public static void creditItems(ServerPlayer player, int count, ItemStack stack) {
        if (count <= 0) return;
        ItemStack probe = stack.copy();
        ProgressService.apply(player, count, ItemTask.TYPE,
                t -> t instanceof ItemTask it && it.consume() && it.matches(probe));
    }

    /**
     * Everything the collector can service for this player, in stable order —
     * resource tasks (energy/xp/mana/emc) then consume item tasks. Used for
     * the on-block display selection cycle.
     */
    public static List<ActiveTask> activeTasks(ServerPlayer player) {
        List<ActiveTask> out = new ArrayList<>();
        TeamQuestProgress tp = teamProgress(player);
        Map<String, QuestStatus> statusCache = new HashMap<>();
        for (ResourceLocation type : List.of(EnergyTask.TYPE, XpTask.TYPE, ManaTask.TYPE, EmcTask.TYPE, ItemTask.TYPE)) {
            for (QuestRegistry.TaskRef ref : QuestRegistry.tasksOfType(type)) {
                if (ref.task() instanceof ItemTask it && !it.consume()) continue;
                Quest quest = ref.quest();
                QuestStatus status = statusCache.computeIfAbsent(quest.fullId(),
                        id -> QuestEvaluator.recompute(quest, tp));
                if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) continue;
                QuestProgress qp = tp.get(quest.fullId());
                if (qp.task(ref.taskIndex()).count() >= ref.task().target()) continue;
                out.add(new ActiveTask(quest.fullId(), ref.taskIndex(), ref.task()));
            }
        }
        return out;
    }
}
