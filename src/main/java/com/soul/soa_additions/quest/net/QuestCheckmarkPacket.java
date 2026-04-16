package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.QuestStatus;
import com.soul.soa_additions.quest.progress.TaskProgress;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.task.CheckmarkTask;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Client→server "the player clicked a checkmark task". Separate from the
 * generic claim packet because checkmark tasks are the only task type that
 * completes by player action rather than observed events — there's no event
 * source the server can listen to, so the intent has to come from the UI.
 *
 * <p>Server re-validates that (a) the quest is currently {@link QuestStatus#VISIBLE},
 * (b) the task at the requested index actually exists on that quest, and
 * (c) it is in fact a {@link CheckmarkTask}. Clients that craft a malicious
 * packet asking to check off a kill task get a silent no-op.</p>
 */
public record QuestCheckmarkPacket(String fullQuestId, int taskIndex) {

    public static void encode(QuestCheckmarkPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.fullQuestId);
        buf.writeVarInt(pkt.taskIndex);
    }

    public static QuestCheckmarkPacket decode(FriendlyByteBuf buf) {
        return new QuestCheckmarkPacket(buf.readUtf(), buf.readVarInt());
    }

    public static void handle(QuestCheckmarkPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player == null) return;

            Optional<Quest> maybe = QuestRegistry.quest(pkt.fullQuestId);
            if (maybe.isEmpty()) return;
            Quest quest = maybe.get();
            if (pkt.taskIndex < 0 || pkt.taskIndex >= quest.tasks().size()) return;
            if (!(quest.tasks().get(pkt.taskIndex) instanceof CheckmarkTask)) return;

            TeamData teams = TeamData.get(player.server);
            QuestTeam team = teams.teamOf(player);
            QuestProgressData data = QuestProgressData.get(player.server);
            TeamQuestProgress tp = data.forTeam(team.id());

            QuestStatus status = QuestEvaluator.recompute(quest, tp);
            if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) return;

            QuestProgress qp = tp.get(quest.fullId());
            TaskProgress task = qp.task(pkt.taskIndex);
            if (task.count() >= 1) return; // already checked
            task.setCount(1);
            qp.touch(player.server.getTickCount());
            QuestStatus after = QuestEvaluator.recompute(quest, tp);
            com.soul.soa_additions.quest.progress.QuestNotifier.onTransition(player, quest, status, after);
            if (after == QuestStatus.READY) {
                QuestEvaluator.recomputeAllAndAutoClaim(tp, player);
            }
            data.touch();

            QuestSyncPacket.sendToTeam(player);
        });
        c.setPacketHandled(true);
    }
}
