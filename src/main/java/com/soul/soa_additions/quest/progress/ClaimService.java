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

        boolean grantedAny = false;
        for (QuestReward reward : quest.rewards()) {
            if (reward.scope() == RewardScope.TEAM) {
                if (!qp.teamClaimed()) {
                    reward.grant(claimant);
                    grantedAny = true;
                }
            } else {
                // PLAYER scope — fan out across every online team member who
                // hasn't personally claimed yet.
                for (ServerPlayer member : teams.onlineMembers(claimant.server, team.id())) {
                    if (!qp.hasClaimed(member.getUUID())) {
                        reward.grant(member);
                        qp.markPlayerClaimed(member.getUUID());
                        grantedAny = true;
                    }
                }
                // Solo-team case: team has no persistent record, so onlineMembers
                // returns empty. Grant to claimant directly.
                if (team.solo() && !qp.hasClaimed(claimant.getUUID())) {
                    reward.grant(claimant);
                    qp.markPlayerClaimed(claimant.getUUID());
                    grantedAny = true;
                }
            }
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
        QuestEvaluator.recomputeAll(teamProgress);

        claimant.sendSystemMessage(Component.literal("✔ Claimed: " + quest.title())
                .withStyle(ChatFormatting.GREEN));

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
