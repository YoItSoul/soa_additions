package com.soul.soa_additions.quest.telemetry;

import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.PackModeData;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.Visibility;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.QuestStatus;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fires a {@link QuestTelemetry} event after every successful claim, and a
 * one-shot {@code quest_complete} event the first time a player's team
 * reaches 100% of the non-optional, visible quests for the current packmode.
 *
 * <p>"Total" counts every non-optional, non-hidden quest visible in the
 * current packmode. Optional quests don't block completion. "Completed"
 * counts rows in {@link QuestStatus#CLAIMED} — a quest with full tasks but
 * an unclaimed reward doesn't count until the player actually hits claim.
 * This matches the in-game progress bar users see in the quest book.</p>
 *
 * <p>The tracker is stateless other than {@link QuestTelemetry}'s own
 * per-session latches, so it's safe to call on every claim from anywhere.</p>
 */
public final class QuestCompletionTracker {

    private QuestCompletionTracker() {}

    /** Call after a successful {@code ClaimService.claim}. */
    public static void onClaim(ServerPlayer player, String fullQuestId) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Snapshot snap = snapshot(server, player);
        if (snap.total == 0) return;

        QuestTelemetry.postClaim(player, fullQuestId, snap.completed, snap.total);
        if (snap.completed >= snap.total) {
            QuestTelemetry.postComplete(player, snap.completed, snap.total);
        }
    }

    /** Snapshot of a player's team progress against the active packmode. */
    public record Snapshot(int completed, int total) {}

    public static Snapshot snapshot(MinecraftServer server, ServerPlayer player) {
        PackMode mode = PackModeData.get(server).mode();
        TeamData teams = TeamData.get(server);
        QuestTeam team = teams.teamOf(player);
        TeamQuestProgress tp = QuestProgressData.get(server).forTeam(team.id());

        int total = 0;
        int completed = 0;
        for (Chapter ch : QuestRegistry.chaptersFor(mode)) {
            if (ch.visibility() == Visibility.INVISIBLE) continue;
            for (Quest q : ch.quests()) {
                if (q.optional()) continue;
                if (q.visibility() == Visibility.INVISIBLE) continue;
                if (!q.availableIn(mode)) continue;
                total++;
                QuestProgress qp = tp.peek(q.fullId());
                if (qp != null && qp.status() == QuestStatus.CLAIMED) completed++;
            }
        }
        return new Snapshot(completed, total);
    }
}
