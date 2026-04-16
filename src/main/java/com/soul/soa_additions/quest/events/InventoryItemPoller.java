package com.soul.soa_additions.quest.events;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.net.QuestSyncPacket;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestNotifier;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.QuestStatus;
import com.soul.soa_additions.quest.progress.TaskProgress;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.task.ItemTask;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Absolute-value poller for {@link ItemTask} (non-consume variant). Unlike
 * event-driven counting, this catches every source of items the player can
 * acquire: ground pickups, crafting, container transfers, creative menu,
 * {@code /give}, trading, shift-click from loot — because it simply
 * reads the current inventory contents and writes them into task progress.
 *
 * <p>Consume-variant item tasks aren't handled here — those deduct on claim
 * and need delta semantics, so they stay on the event path. Polling replaces
 * only the "hold N of X" variant, which is the more common case.</p>
 *
 * <p>Cost: one inventory walk (~40 slots) + one hashmap build + one quest
 * tree walk per polled player per 10 ticks. Easily under a millisecond
 * even for large quest trees; profile if you notice pauses.</p>
 */
public final class InventoryItemPoller {

    private InventoryItemPoller() {}

    public static void poll(ServerPlayer player) {
        // Skip the entire poll — including inventory walk — when no item
        // tasks exist anywhere. This is the common case for most quests
        // most of the time.
        var refs = QuestRegistry.tasksOfType(ItemTask.TYPE);
        if (refs.isEmpty()) return;

        // Build a map of itemId → total count across the player's inventory.
        // Also keep the raw stack list so tag-based ItemTasks can scan them
        // without going back through the registry.
        Map<ResourceLocation, Integer> owned = new HashMap<>();
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
        var inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            stacks.add(stack);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            owned.merge(id, stack.getCount(), Integer::sum);
        }

        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData data = QuestProgressData.get(player.server);
        TeamQuestProgress tp = data.forTeam(team.id());

        boolean changed = false;
        long tick = player.server.getTickCount();
        java.util.Map<Quest, QuestStatus> seen = new java.util.HashMap<>();
        java.util.Set<Quest> dirty = null;

        for (QuestRegistry.TaskRef ref : refs) {
            Quest quest = ref.quest();
            QuestStatus status = seen.computeIfAbsent(quest, q -> QuestEvaluator.recompute(q, tp));
            if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) continue;

            ItemTask it = (ItemTask) ref.task();
            // Consume-variant stays on the event/claim path — polling would
            // over-count a player who holds items briefly.
            if (it.consume()) continue;

            int have;
            if (it.tag() != null || it.nbt() != null) {
                int sum = 0;
                for (ItemStack s : stacks) if (it.matches(s)) sum += s.getCount();
                have = sum;
            } else {
                have = owned.getOrDefault(it.item(), 0);
            }
            int capped = Math.min(have, it.target());
            QuestProgress qp = tp.get(quest.fullId());
            TaskProgress progress = qp.task(ref.taskIndex());
            if (progress.count() != capped) {
                progress.setCount(capped);
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
            // Push a fresh snapshot so the quest book GUI updates live.
            QuestSyncPacket.sendToTeam(player);
        }
    }
}
