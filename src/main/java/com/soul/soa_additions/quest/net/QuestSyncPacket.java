package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.PackModeData;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Full server→client snapshot of a team's quest progress. Sent on login and
 * on every claim/team-event that mutates state. Not a delta protocol yet —
 * the whole book gets resent. Delta sync is an optimization worth doing if
 * and only if snapshot cost shows up in profiles; for a pack-sized quest
 * tree it's bytes, not kilobytes.
 *
 * <p>Includes the current packmode so the client can filter the chapter
 * list without having to issue a second request at login.</p>
 */
public record QuestSyncPacket(PackMode packMode, List<QuestSnapshotEntry> entries) {

    public static void encode(QuestSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.packMode);
        buf.writeVarInt(pkt.entries.size());
        for (QuestSnapshotEntry e : pkt.entries) e.encode(buf);
    }

    public static QuestSyncPacket decode(FriendlyByteBuf buf) {
        PackMode mode = buf.readEnum(PackMode.class);
        int n = buf.readVarInt();
        List<QuestSnapshotEntry> entries = new ArrayList<>(n);
        for (int i = 0; i < n; i++) entries.add(QuestSnapshotEntry.decode(buf));
        return new QuestSyncPacket(mode, entries);
    }

    public static void handle(QuestSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientQuestState.apply(pkt));
        ctx.get().setPacketHandled(true);
    }

    // ---------- server-side build ----------

    public static QuestSyncPacket build(ServerPlayer player) {
        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData data = QuestProgressData.get(player.server);
        TeamQuestProgress tp = data.forTeam(team.id());

        List<QuestSnapshotEntry> out = new ArrayList<>(tp.size());
        for (QuestProgress qp : tp.all()) {
            List<Integer> counts = new ArrayList<>(qp.tasks().size());
            for (var t : qp.tasks()) counts.add(t.count());
            out.add(new QuestSnapshotEntry(
                    qp.fullId(),
                    qp.status(),
                    counts,
                    qp.teamClaimed(),
                    qp.hasClaimed(player.getUUID())
            ));
        }
        return new QuestSyncPacket(PackModeData.get(player.server).mode(), out);
    }

    public static void sendTo(ServerPlayer player) {
        com.soul.soa_additions.network.ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                build(player)
        );
        // Push live update to any connected web overlay clients
        com.soul.soa_additions.quest.web.QuestWebServer.pushUpdate(player);
    }

    /** Send the same snapshot to every online team member — used after claims. */
    public static void sendToTeam(ServerPlayer actor) {
        TeamData teams = TeamData.get(actor.server);
        QuestTeam team = teams.teamOf(actor);
        if (team.solo()) {
            sendTo(actor);
            return;
        }
        for (ServerPlayer p : teams.onlineMembers(actor.server, team.id())) {
            sendTo(p);
        }
    }
}
