package com.soul.soa_additions.quest.progress;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.RewardScope;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Single entry point for "player X pressed the claim button on quest Y".
 * Encapsulates the scope fan-out: TEAM rewards are granted once per team and
 * PLAYER rewards are granted once per player, with both tracked on the same
 * {@link QuestProgress} row so partial claims (e.g. owner already took the
 * team item, teammate later hits claim for their own XP) work correctly.
 *
 * <p>A quest only transitions to {@link QuestStatus#CLAIMED} when the team
 * items have been distributed AND every online team member has taken their
 * player rewards. Offline members don't block the transition — their player
 * rewards are simply skipped; a future enhancement can queue them for next
 * login. For now, "be online when your team claims" is the rule.</p>
 */
public final class ClaimService {

    private ClaimService() {}

    /**
     * Attempt to claim a quest's rewards on behalf of {@code claimant}. Returns
     * a result describing what happened so the caller (command, GUI, packet)
     * can show appropriate feedback.
     */
    public static ClaimResult claim(ServerPlayer claimant, String fullQuestId) {
        Optional<Quest> maybeQuest = QuestRegistry.quest(fullQuestId);
        if (maybeQuest.isEmpty()) return ClaimResult.UNKNOWN_QUEST;
        Quest quest = maybeQuest.get();

        TeamData teams = TeamData.get(claimant.server);
        QuestTeam team = teams.teamOf(claimant);

        QuestProgressData progressData = QuestProgressData.get(claimant.server);
        TeamQuestProgress teamProgress = progressData.forTeam(team.id());

        // Re-evaluate so we aren't trusting a stale status from the client.
        QuestStatus status = QuestEvaluator.recompute(quest, teamProgress);
        if (status != QuestStatus.READY) {
            return status == QuestStatus.CLAIMED ? ClaimResult.ALREADY_CLAIMED : ClaimResult.NOT_READY;
        }

        QuestProgress qp = teamProgress.get(fullQuestId);

        // Snapshot which players still need their PLAYER-scope rewards BEFORE
        // the reward loop. Marking inside the loop would set hasClaimed after
        // the first reward, causing rewards 2..N for the same player to skip.
        java.util.List<ServerPlayer> pendingClaimants = new java.util.ArrayList<>();
        if (team.solo()) {
            if (!qp.hasClaimed(claimant.getUUID())) pendingClaimants.add(claimant);
        } else {
            for (ServerPlayer member : teams.onlineMembers(claimant.server, team.id())) {
                if (!qp.hasClaimed(member.getUUID())) pendingClaimants.add(member);
            }
        }

        boolean grantedAny = false;
        for (QuestReward reward : quest.rewards()) {
            if (reward.scope() == RewardScope.TEAM) {
                if (!qp.teamClaimed()) {
                    reward.grant(claimant);
                    grantedAny = true;
                }
            } else {
                // PLAYER scope — grant to every pending member. Marking is
                // deferred until after all rewards have fired (see below).
                for (ServerPlayer member : pendingClaimants) {
                    reward.grant(member);
                    grantedAny = true;
                }
            }
        }
        for (ServerPlayer member : pendingClaimants) {
            qp.markPlayerClaimed(member.getUUID());
        }
        qp.markTeamClaimed(); // team-level rewards definitively distributed
        qp.markEverClaimed(); // sticky flag — downstream deps stay satisfied
        if (quest.repeatable()) {
            // Repeatable quests don't enter CLAIMED; they walk back so the
            // player can run them again. Scope picks whether task progress is
            // wiped (TEAM) or only the claim markers are cleared (PLAYER).
            if (quest.repeatScope() == RewardScope.PLAYER) {
                qp.clearClaimMarkers();
            } else {
                qp.resetForRepeat();
            }
            // Evaluator decides the resulting status (READY if tasks still
            // complete, VISIBLE if not, LOCKED if deps somehow regressed).
            QuestEvaluator.recompute(quest, teamProgress);
        } else {
            qp.setStatus(QuestStatus.CLAIMED);
        }
        qp.touch(claimant.server.getTickCount());
        progressData.touch();

        // Newly CLAIMED dependencies may unlock downstream quests.
        // Use the auto-claim variant so any downstream quest with autoClaim
        // set fires immediately rather than waiting for a manual button press.
        QuestEvaluator.recomputeAllAndAutoClaim(teamProgress, claimant);

        claimant.sendSystemMessage(Component.literal("✔ Claimed: " + quest.title())
                .withStyle(ChatFormatting.GREEN));

        // Telemetry hook — fires a throttled quest_claim event and a one-shot
        // quest_complete when the team reaches 100% of non-optional quests.
        // Must run for every online team member so each player's own cheated
        // flag is evaluated individually (per-player clean-run eligibility).
        if (team.solo()) {
            com.soul.soa_additions.quest.telemetry.QuestCompletionTracker.onClaim(claimant, fullQuestId);
        } else {
            for (ServerPlayer member : teams.onlineMembers(claimant.server, team.id())) {
                com.soul.soa_additions.quest.telemetry.QuestCompletionTracker.onClaim(member, fullQuestId);
            }
        }

        return grantedAny ? ClaimResult.OK : ClaimResult.NOTHING_TO_GRANT;
    }

    public enum ClaimResult {
        OK,
        NOT_READY,
        ALREADY_CLAIMED,
        NOTHING_TO_GRANT,
        UNKNOWN_QUEST
    }
}
