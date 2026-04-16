package com.soul.soa_additions.quest.net;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Incremental server→client update carrying only quest rows whose wire-visible
 * state changed since a previously captured snapshot. Mirrors the row format
 * of {@link QuestSyncPacket} so the client merges deltas into the same local
 * cache, but avoids resending the full book after every task tick / claim.
 *
 * <p>Use via {@link Capture}: snapshot before mutating progress, mutate,
 * then {@link Capture#sendChanges} to emit per-viewer deltas. Login /
 * reconnect / /soa reset still use {@link QuestSyncPacket} — those need a
 * full replace of the client cache.</p>
 */
public record QuestDeltaPacket(List<QuestSnapshotEntry> entries) {

    public static void encode(QuestDeltaPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.entries.size());
        for (QuestSnapshotEntry e : pkt.entries) e.encode(buf);
    }

    public static QuestDeltaPacket decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<QuestSnapshotEntry> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) out.add(QuestSnapshotEntry.decode(buf));
        return new QuestDeltaPacket(out);
    }

    public static void handle(QuestDeltaPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                        net.minecraftforge.api.distmarker.Dist.CLIENT,
                        () -> () -> ClientQuestState.applyDelta(pkt)));
        ctx.get().setPacketHandled(true);
    }

    /**
     * Pre-mutation snapshot of the wire-visible row for every quest in a
     * team, taken once per online viewer. After mutating progress, call
     * {@link #sendChanges} to diff the current state against the captured
     * snapshot and emit one delta packet per viewer (rows unchanged for a
     * given viewer are omitted; viewers with no changes get no packet).
     *
     * <p>Per-viewer because {@code localClaimed} differs between team members
     * — if only one player took their share, the shared quest row is the
     * same but that one player's delta still needs to fire.</p>
     */
    public static final class Capture {
        private final Map<UUID, Map<String, QuestSnapshotEntry>> byViewer = new HashMap<>();
        private final UUID teamId;

        private Capture(UUID teamId) { this.teamId = teamId; }

        public static Capture of(ServerPlayer actor) {
            TeamData teams = TeamData.get(actor.server);
            QuestTeam team = teams.teamOf(actor);
            QuestProgressData data = QuestProgressData.get(actor.server);
            TeamQuestProgress tp = data.forTeam(team.id());
            Capture c = new Capture(team.id());
            if (team.solo()) {
                c.byViewer.put(actor.getUUID(), snapshotFor(tp, actor.getUUID()));
            } else {
                for (ServerPlayer p : teams.onlineMembers(actor.server, team.id())) {
                    c.byViewer.put(p.getUUID(), snapshotFor(tp, p.getUUID()));
                }
            }
            return c;
        }

        public void sendChanges(ServerPlayer actor) {
            TeamData teams = TeamData.get(actor.server);
            QuestTeam team = teams.teamOf(actor);
            // If the actor changed teams between capture and send (rare —
            // only /soa team join/leave), fall back to a full sync rather
            // than emit a wrong-team delta.
            if (!team.id().equals(teamId)) {
                QuestSyncPacket.sendToTeam(actor);
                return;
            }
            QuestProgressData data = QuestProgressData.get(actor.server);
            TeamQuestProgress tp = data.forTeam(team.id());

            if (team.solo()) {
                sendTo(actor, tp);
            } else {
                for (ServerPlayer p : teams.onlineMembers(actor.server, team.id())) {
                    sendTo(p, tp);
                }
            }
            com.soul.soa_additions.quest.web.QuestWebServer.pushUpdate(actor);
        }

        private void sendTo(ServerPlayer viewer, TeamQuestProgress tp) {
            Map<String, QuestSnapshotEntry> before = byViewer.get(viewer.getUUID());
            List<QuestSnapshotEntry> changed = new ArrayList<>();
            for (QuestProgress qp : tp.all()) {
                QuestSnapshotEntry now = QuestSnapshotEntry.from(qp, viewer.getUUID());
                QuestSnapshotEntry prev = before == null ? null : before.get(qp.fullId());
                if (!now.equals(prev)) changed.add(now);
            }
            if (changed.isEmpty()) return;
            com.soul.soa_additions.network.ModNetworking.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> viewer),
                    new QuestDeltaPacket(changed)
            );
        }

        private static Map<String, QuestSnapshotEntry> snapshotFor(TeamQuestProgress tp, UUID viewer) {
            Map<String, QuestSnapshotEntry> out = new HashMap<>(tp.size());
            for (QuestProgress qp : tp.all()) {
                out.put(qp.fullId(), QuestSnapshotEntry.from(qp, viewer));
            }
            return out;
        }
    }
}
