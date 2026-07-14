package com.soul.soa_additions.quest.progress;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.Predicate;

/**
 * Façade used by every task-event subscriber. Given "player did X, and here's
 * a predicate that says whether task T cares about X", it walks the player's
 * team progress, finds matching VISIBLE tasks, bumps their counters, and
 * re-evaluates. The predicate approach keeps event-type-specific matching
 * logic (entity type, item id, stat threshold, etc.) in the subscriber and
 * out of this hot path.
 *
 * <p>Efficiency: matching uses the {@link QuestRegistry} task-type index
 * (rebuilt on reload), so each event only touches tasks of its own type;
 * events with no matching tasks exit in microseconds. Sync uses the delta
 * protocol — only rows that actually changed go over the wire.</p>
 */
public final class ProgressService {

    private ProgressService() {}

    /**
     * Apply a delta to every task of the given {@code type} that matches.
     * Uses the {@link QuestRegistry} task-type index, so the cost is
     * proportional to the number of tasks of that type in the loaded quest
     * tree — not the size of the entire tree. The vast majority of events
     * (kill a zombie, break a block) hit zero matching tasks and exit in
     * a few microseconds.
     */
    public static void apply(ServerPlayer player, int delta, ResourceLocation type, Predicate<QuestTask> match) {
        if (delta <= 0) return;
        List<QuestRegistry.TaskRef> refs = QuestRegistry.tasksOfType(type);
        if (refs.isEmpty()) return;

        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData progressData = QuestProgressData.get(player.server);
        TeamQuestProgress teamProgress = progressData.forTeam(team.id());

        boolean anythingChanged = false;
        long tick = player.server.getTickCount();

        // Track quests we've already evaluated this call so a quest with N
        // matching tasks doesn't pay the recompute cost N times.
        java.util.Map<String, QuestStatus> statusCache = new java.util.HashMap<>();
        java.util.Set<Quest> changedQuests = null;
        // Delta capture, taken lazily right before the first mutation so the
        // (common) no-match path never pays the snapshot cost.
        com.soul.soa_additions.quest.net.QuestDeltaPacket.Capture capture = null;

        for (QuestRegistry.TaskRef ref : refs) {
            Quest quest = ref.quest();
            QuestStatus status = statusCache.computeIfAbsent(quest.fullId(),
                    id -> QuestEvaluator.recompute(quest, teamProgress));
            if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) continue;
            if (!match.test(ref.task())) continue;

            QuestProgress qp = teamProgress.get(quest.fullId());
            TaskProgress tp = qp.task(ref.taskIndex());
            if (tp.count() >= ref.task().target()) continue;
            if (capture == null) {
                capture = com.soul.soa_additions.quest.net.QuestDeltaPacket.Capture.of(player);
            }
            tp.add(delta);
            qp.touch(tick);
            if (changedQuests == null) changedQuests = new java.util.HashSet<>();
            changedQuests.add(quest);
            anythingChanged = true;
        }

        boolean anyBecameReady = false;
        if (changedQuests != null) {
            for (Quest quest : changedQuests) {
                QuestStatus before = statusCache.get(quest.fullId());
                QuestStatus after = QuestEvaluator.recompute(quest, teamProgress);
                QuestNotifier.onTransition(player, quest, before, after);
                if (after == QuestStatus.READY) anyBecameReady = true;
            }
        }

        // Auto-claim sweep: if any quest just became READY, run the full
        // recompute-and-auto-claim pass so autoClaim quests fire immediately
        // rather than waiting for the next login or manual claim.
        if (anyBecameReady) {
            QuestEvaluator.recomputeAllAndAutoClaim(teamProgress, player);
        }

        if (anythingChanged) {
            progressData.touch();
            // Delta, not full-book resend: on grind-heavy packs (one event per
            // zombie kill / block break) the full QuestSyncPacket per event was
            // the dominant network cost. Login/reset still use QuestSyncPacket.
            capture.sendChanges(player);
        }
    }
}
