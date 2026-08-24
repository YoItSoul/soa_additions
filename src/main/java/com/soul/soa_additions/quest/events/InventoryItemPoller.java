package com.soul.soa_additions.quest.events;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.net.QuestDeltaPacket;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestNotifier;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.QuestStatus;
import com.soul.soa_additions.quest.progress.TaskProgress;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.task.HarvestLevelTask;
import com.soul.soa_additions.quest.task.ItemTask;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Absolute-value poller for the inventory-derived task types — {@link ItemTask} (non-consume
 * variant) and {@link HarvestLevelTask}. Unlike event-driven counting, this catches every source
 * of items the player can acquire: ground pickups, crafting, container transfers, creative menu,
 * {@code /give}, trading, shift-click from loot — because it simply reads the current inventory
 * contents and writes them into task progress.
 *
 * <p>Consume-variant item tasks aren't handled here — those deduct on claim and need delta
 * semantics, so they stay on the event path. Polling replaces only the "hold N of X" variant,
 * which is the more common case.</p>
 *
 * <p>Cost is paid strictly in proportion to what the quest tree actually asks for. With no
 * inventory tasks loaded the whole method exits before walking a single slot. With only item
 * tasks loaded no gear probing happens; with only harvest-level tasks loaded the item-id tally
 * is never built. The expensive half is the harvest-level path, because Smithery stores a tool's
 * materials but not its level and so has to recompute the stat block to answer — that is done
 * once per stack per poll and shared across every harvest-level task in the tree, never once
 * per task-stack pair.</p>
 */
public final class InventoryItemPoller {

    private InventoryItemPoller() {}

    public static void poll(ServerPlayer player) {
        // Skip the entire poll — including the inventory walk — when no inventory-derived tasks
        // exist anywhere. This is the common case for most quests most of the time.
        var itemRefs = QuestRegistry.tasksOfType(ItemTask.TYPE);
        var levelRefs = QuestRegistry.tasksOfType(HarvestLevelTask.TYPE);
        if (itemRefs.isEmpty() && levelRefs.isEmpty()) return;

        // One walk, feeding whichever indexes are actually needed:
        //   owned   — itemId → total count, for plain id-matched ItemTasks
        //   stacks  — raw stacks, for tag/NBT ItemTasks that must inspect each one
        //   probes  — parallel to stacks, the gear facts every HarvestLevelTask needs
        boolean wantItems = !itemRefs.isEmpty();
        boolean wantLevels = !levelRefs.isEmpty();
        Map<ResourceLocation, Integer> owned = wantItems ? new HashMap<>() : Map.of();
        List<ItemStack> stacks = new ArrayList<>();
        List<HarvestLevelTask.Probe> probes = wantLevels ? new ArrayList<>() : List.of();

        var inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            stacks.add(stack);
            if (wantItems) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                owned.put(id, owned.getOrDefault(id, 0) + stack.getCount());
            }
            if (wantLevels) probes.add(HarvestLevelTask.Probe.of(stack));
        }

        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData data = QuestProgressData.get(player.server);
        TeamQuestProgress tp = data.forTeam(team.id());
        PollState state = new PollState(player, tp);

        for (QuestRegistry.TaskRef ref : itemRefs) {
            if (!state.active(ref)) continue;
            ItemTask it = (ItemTask) ref.task();
            // Consume-variant stays on the event/claim path — polling would over-count a
            // player who holds items briefly.
            if (it.consume()) continue;
            int have;
            if (it.tag() != null || it.nbt() != null) {
                int sum = 0;
                for (ItemStack s : stacks) if (it.matches(s)) sum += s.getCount();
                have = sum;
            } else {
                have = owned.getOrDefault(it.item(), 0);
            }
            state.ratchet(ref, have);
        }

        for (QuestRegistry.TaskRef ref : levelRefs) {
            if (!state.active(ref)) continue;
            HarvestLevelTask ht = (HarvestLevelTask) ref.task();
            int have = 0;
            for (int i = 0; i < probes.size(); i++) {
                if (ht.matches(probes.get(i))) have += stacks.get(i).getCount();
            }
            state.ratchet(ref, have);
        }

        state.finish(data);
    }

    /**
     * The mutable bookkeeping shared by both task loops: which quests have been evaluated this
     * poll, which ones changed, and the lazily-taken delta snapshot. Kept in one object so the
     * two loops can't drift in how they ratchet progress or notify transitions.
     */
    private static final class PollState {

        private final ServerPlayer player;
        private final TeamQuestProgress tp;
        private final long tick;
        private final Map<Quest, QuestStatus> seen = new HashMap<>();

        // Delta capture, taken lazily right before the first mutation — the common no-change
        // poll otherwise clones the full progress table per online player every 10 ticks for
        // nothing.
        private QuestDeltaPacket.Capture delta;
        private java.util.Set<Quest> dirty;
        private boolean changed;

        PollState(ServerPlayer player, TeamQuestProgress tp) {
            this.player = player;
            this.tp = tp;
            this.tick = player.server.getTickCount();
        }

        /** True when this task's quest is in a state where progress can still move. */
        boolean active(QuestRegistry.TaskRef ref) {
            QuestStatus status = seen.computeIfAbsent(ref.quest(), q -> QuestEvaluator.recompute(q, tp));
            return status == QuestStatus.VISIBLE || status == QuestStatus.READY;
        }

        /**
         * Ratchet only — never let polling regress a count. Once a "collect N" task has been
         * satisfied to some level, losing the items (death, crafting, dropping) must not
         * un-complete it. Otherwise a finished quest reverts to VISIBLE on the next inventory
         * loss.
         */
        void ratchet(QuestRegistry.TaskRef ref, int have) {
            int capped = Math.min(have, ref.task().target());
            QuestProgress qp = tp.get(ref.quest().fullId());
            TaskProgress progress = qp.task(ref.taskIndex());
            if (capped <= progress.count()) return;
            if (delta == null) delta = QuestDeltaPacket.Capture.of(player);
            progress.setCount(capped);
            qp.touch(tick);
            if (dirty == null) dirty = new java.util.HashSet<>();
            dirty.add(ref.quest());
            changed = true;
        }

        void finish(QuestProgressData data) {
            boolean anyBecameReady = false;
            if (dirty != null) {
                for (Quest quest : dirty) {
                    QuestStatus before = seen.get(quest);
                    QuestStatus after = QuestEvaluator.recompute(quest, tp);
                    QuestNotifier.onTransition(player, quest, before, after);
                    if (after == QuestStatus.READY) anyBecameReady = true;
                }
            }
            if (anyBecameReady) {
                QuestEvaluator.recomputeAllAndAutoClaim(tp, player);
            }
            if (changed) {
                data.touch();
                delta.sendChanges(player);
            }
        }
    }
}
