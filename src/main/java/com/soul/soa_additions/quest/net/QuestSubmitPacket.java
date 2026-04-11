package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Client→server "submit items for a consume ItemTask". Consume-variant item
 * tasks can't use the inventory poller (the poller would over-count a player
 * who briefly holds items), so they need an explicit submit action. This
 * packet scans the player's inventory for the requested item, removes up to
 * the amount still needed to complete the task, and bumps the counter.
 *
 * <p>Server re-validates everything: the quest must be VISIBLE/READY, the
 * task index must exist, and the task must actually be a consume ItemTask.
 * Partial submits are fine — submitting 3 wheat toward an 8-wheat task just
 * advances the counter to 3 and waits for the next click.</p>
 */
public record QuestSubmitPacket(String fullQuestId, int taskIndex) {

    public static void encode(QuestSubmitPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.fullQuestId);
        buf.writeVarInt(pkt.taskIndex);
    }

    public static QuestSubmitPacket decode(FriendlyByteBuf buf) {
        return new QuestSubmitPacket(buf.readUtf(), buf.readVarInt());
    }

    public static void handle(QuestSubmitPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player == null) return;

            Optional<Quest> maybe = QuestRegistry.quest(pkt.fullQuestId);
            if (maybe.isEmpty()) return;
            Quest quest = maybe.get();
            if (pkt.taskIndex < 0 || pkt.taskIndex >= quest.tasks().size()) return;
            if (!(quest.tasks().get(pkt.taskIndex) instanceof ItemTask it)) return;
            if (!it.consume()) return; // non-consume uses the poller

            TeamData teams = TeamData.get(player.server);
            QuestTeam team = teams.teamOf(player);
            QuestProgressData data = QuestProgressData.get(player.server);
            TeamQuestProgress tp = data.forTeam(team.id());

            QuestStatus status = QuestEvaluator.recompute(quest, tp);
            if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) return;

            QuestProgress qp = tp.get(quest.fullId());
            TaskProgress taskProg = qp.task(pkt.taskIndex);
            int remaining = it.target() - taskProg.count();
            if (remaining <= 0) return;

            int removed = removeFromInventory(player.getInventory(), it, remaining);
            if (removed <= 0) return;

            taskProg.add(removed);
            qp.touch(player.server.getTickCount());
            QuestStatus after = QuestEvaluator.recompute(quest, tp);
            QuestNotifier.onTransition(player, quest, status, after);
            data.touch();

            QuestSyncPacket.sendToTeam(player);
        });
        c.setPacketHandled(true);
    }

    /** Remove up to {@code wanted} items matching the task's item-or-tag filter. */
    private static int removeFromInventory(Inventory inv, ItemTask task, int wanted) {
        int removed = 0;
        for (int i = 0; i < inv.getContainerSize() && removed < wanted; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (!task.matches(stack)) continue;
            int take = Math.min(stack.getCount(), wanted - removed);
            stack.shrink(take);
            if (stack.isEmpty()) inv.setItem(i, ItemStack.EMPTY);
            removed += take;
        }
        if (removed > 0) inv.setChanged();
        return removed;
    }
}
