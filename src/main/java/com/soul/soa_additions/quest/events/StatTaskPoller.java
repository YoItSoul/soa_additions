package com.soul.soa_additions.quest.events;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestNotifier;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.QuestStatus;
import com.soul.soa_additions.quest.progress.TaskProgress;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.task.StatTask;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Dedicated absolute-value poller for {@link StatTask}. Separate from the
 * generic {@link com.soul.soa_additions.quest.progress.ProgressService} path
 * because stat counters mirror the Minecraft stat directly rather than
 * incrementing on an event — the player's boots_walked stat jumps independent
 * of any event we see, so we read it and write the absolute into task
 * progress rather than bumping by a delta.
 */
public final class StatTaskPoller {

    private StatTaskPoller() {}

    public static void poll(ServerPlayer player) {
        if (!QuestRegistry.hasTasksOfType(StatTask.TYPE)) return;

        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData progressData = QuestProgressData.get(player.server);
        TeamQuestProgress teamProgress = progressData.forTeam(team.id());

        boolean changed = false;
        long tick = player.server.getTickCount();

        // Group refs by quest so each quest only pays one recompute pre/post.
        java.util.Map<Quest, QuestStatus> seen = new java.util.HashMap<>();
        java.util.Set<Quest> dirty = null;
        // Delta capture, taken lazily before the first mutation — most polls
        // find nothing changed and never pay the snapshot.
        com.soul.soa_additions.quest.net.QuestDeltaPacket.Capture capture = null;
        for (QuestRegistry.TaskRef ref : QuestRegistry.tasksOfType(StatTask.TYPE)) {
            Quest quest = ref.quest();
            QuestStatus status = seen.computeIfAbsent(quest, q -> QuestEvaluator.recompute(q, teamProgress));
            if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) continue;

            StatTask st = (StatTask) ref.task();
            int current = lookupStat(player, st.statType(), st.statValue());
            QuestProgress qp = teamProgress.get(quest.fullId());
            TaskProgress tp = qp.task(ref.taskIndex());
            // The counter is shared by the whole team, so it has to ratchet — the same contract
            // InventoryItemPoller.ratchet keeps. Writing each member's own absolute stat straight
            // in let the next member's poll drag it back down, flipping a READY quest to VISIBLE
            // and re-firing its notification every few seconds.
            int next = Math.min(current, st.target());
            if (next > tp.count()) {
                if (capture == null) {
                    capture = com.soul.soa_additions.quest.net.QuestDeltaPacket.Capture.of(player);
                }
                tp.setCount(next);
                qp.touch(tick);
                if (dirty == null) dirty = new java.util.HashSet<>();
                dirty.add(quest);
                changed = true;
            }
        }
        boolean anyBecameReady = false;
        if (dirty != null) {
            for (Quest quest : dirty) {
                QuestStatus before = seen.get(quest);
                QuestStatus after = QuestEvaluator.recompute(quest, teamProgress);
                QuestNotifier.onTransition(player, quest, before, after);
                if (after == QuestStatus.READY) anyBecameReady = true;
            }
        }
        if (anyBecameReady) {
            QuestEvaluator.recomputeAllAndAutoClaim(teamProgress, player);
        }
        if (changed) {
            progressData.touch();
            capture.sendChanges(player);
        }
    }

    private static int lookupStat(ServerPlayer player, ResourceLocation typeId, ResourceLocation valueId) {
        StatType<?> statType = ForgeRegistries.STAT_TYPES.getValue(typeId);
        if (statType == null) return 0;
        return lookupStatTyped(player, statType, valueId);
    }


    private static <T> int lookupStatTyped(ServerPlayer player, StatType<T> statType, ResourceLocation valueId) {
        Registry<T> valueRegistry = statType.getRegistry();
        T value = valueRegistry.get(valueId);
        if (value == null) return 0;
        Stat<T> stat = statType.get(value);
        if (stat == null) return 0;
        return player.getStats().getValue(stat);
    }
}
