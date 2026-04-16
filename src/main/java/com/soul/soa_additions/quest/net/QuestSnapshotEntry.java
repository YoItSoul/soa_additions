package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.TaskProgress;
import com.soul.soa_additions.quest.progress.QuestStatus;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Wire-format row for a single quest's progress. Kept minimal — the client
 * only needs status, the task counters, and the team-claim flag for display.
 * Player-claim sets aren't synced because the client only cares about "has
 * the local player claimed their share yet", which is a single boolean.
 */
public record QuestSnapshotEntry(
        String fullId,
        QuestStatus status,
        List<Integer> taskCounts,
        boolean teamClaimed,
        boolean localClaimed
) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(fullId);
        buf.writeEnum(status);
        buf.writeVarInt(taskCounts.size());
        for (int c : taskCounts) buf.writeVarInt(c);
        buf.writeBoolean(teamClaimed);
        buf.writeBoolean(localClaimed);
    }

    public static QuestSnapshotEntry decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        QuestStatus st = buf.readEnum(QuestStatus.class);
        int n = buf.readVarInt();
        List<Integer> counts = new ArrayList<>(n);
        for (int i = 0; i < n; i++) counts.add(buf.readVarInt());
        boolean teamClaimed = buf.readBoolean();
        boolean localClaimed = buf.readBoolean();
        return new QuestSnapshotEntry(id, st, counts, teamClaimed, localClaimed);
    }

    /** Build the wire row for a given viewer. Extracted so the full-snapshot
     *  and delta-diff paths can't drift in what they consider "the same row". */
    public static QuestSnapshotEntry from(QuestProgress qp, UUID viewer) {
        List<Integer> counts = new ArrayList<>(qp.tasks().size());
        for (TaskProgress t : qp.tasks()) counts.add(t.count());
        return new QuestSnapshotEntry(
                qp.fullId(),
                qp.status(),
                counts,
                qp.teamClaimed(),
                qp.hasClaimed(viewer)
        );
    }
}
