package com.soul.soa_additions.quest.progress;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestTask;

import java.util.List;
import java.util.Optional;

/**
 * Pure function layer over {@link QuestRegistry} and {@link TeamQuestProgress}.
 * Nothing in here touches Forge state — given a team's progress and the live
 * quest definitions, it computes what the status of any quest <i>should</i> be
 * and mutates the progress row when reality disagrees.
 *
 * <p>Called from two places:
 * <ul>
 *   <li>Task event subscribers after bumping a counter — local to one quest.</li>
 *   <li>{@link #recomputeAll} after a claim, a reload, or a team merge — sweeps
 *       every quest to propagate newly-unlocked dependents.</li>
 * </ul>
 * </p>
 *
 * <p>Keeping evaluation pure means it's trivial to unit-test and there's no
 * risk of "progress drifted because two code paths had different ideas about
 * what LOCKED means" — there is only this one idea.</p>
 */
public final class QuestEvaluator {

    private QuestEvaluator() {}

    /** Re-entrancy guard for {@link #recomputeAllAndAutoClaim}. Command
     *  rewards can dispatch commands that complete tasks and re-enter the
     *  auto-claim sweep through the command dispatcher — without this flag
     *  the recursion is unbounded and blows the stack. */
    private static boolean autoClaimInProgress = false;

    /**
     * Recompute the status of a single quest against the team's current
     * progress. Writes back to the progress row if anything changed and
     * returns the new status.
     */
    public static QuestStatus recompute(Quest quest, TeamQuestProgress team) {
        QuestProgress qp = team.get(quest.fullId());

        // CLAIMED is terminal for non-repeatable quests. Repeatable quests
        // are deliberately walked back to VISIBLE/READY after ClaimService
        // resets their progress, so don't short-circuit on them.
        if (qp.status() == QuestStatus.CLAIMED && !quest.repeatable()) return QuestStatus.CLAIMED;

        // Mutual-exclusion lockout: if any exclusion target has ever been
        // claimed, this quest is permanently locked. Branching-path quests
        // use this to shut the door once the player commits.
        if (isExcluded(quest, team)) {
            qp.setStatus(QuestStatus.LOCKED);
            return QuestStatus.LOCKED;
        }

        boolean depsOk = dependenciesSatisfied(quest, team);
        if (!depsOk) {
            qp.setStatus(QuestStatus.LOCKED);
            return QuestStatus.LOCKED;
        }

        if (tasksComplete(quest, qp)) {
            qp.setStatus(QuestStatus.READY);
            return QuestStatus.READY;
        }

        qp.setStatus(QuestStatus.VISIBLE);
        return QuestStatus.VISIBLE;
    }

    /**
     * Walk every loaded chapter/quest and recompute. Used after bulk state
     * changes (reload, claim fan-out, team roster change) where multiple
     * dependent quests might transition at once. Cheap enough for book-sized
     * packs; if we ever hit perf issues we can switch to a reverse-dep index.
     */
    public static void recomputeAll(TeamQuestProgress team) {
        // Repeat until fixed point — unlocking a quest can unlock its dependents.
        boolean changed;
        int guard = 0;
        do {
            changed = false;
            for (Chapter c : QuestRegistry.allChapters()) {
                for (Quest q : c.quests()) {
                    QuestStatus before = team.get(q.fullId()).status();
                    QuestStatus after = recompute(q, team);
                    if (before != after) changed = true;
                }
            }
            if (++guard > 16) break; // deep dep chains shouldn't exceed this in practice
        } while (changed);
    }

    /**
     * {@link #recomputeAll} followed by an auto-claim sweep: any quest that
     * is now READY and has {@code autoClaim} set will be claimed on behalf of
     * {@code player}. Claiming can unlock further dependents, so we loop
     * until no more auto-claims fire (guarded to prevent runaway chains).
     */
    public static void recomputeAllAndAutoClaim(TeamQuestProgress team, net.minecraft.server.level.ServerPlayer player) {
        recomputeAll(team);

        // If we're already inside an auto-claim sweep (e.g. a CommandReward
        // dispatched a command that completed a task and re-entered here),
        // skip the sweep — the outermost invocation's loop will pick up any
        // newly-READY quests on its next iteration.
        if (autoClaimInProgress) return;

        autoClaimInProgress = true;
        try {
            int guard = 0;
            boolean claimed;
            do {
                claimed = false;
                for (Chapter c : QuestRegistry.allChapters()) {
                    for (Quest q : c.quests()) {
                        if (!q.autoClaim()) continue;
                        QuestProgress qp = team.peek(q.fullId());
                        if (qp != null && qp.status() == QuestStatus.READY) {
                            ClaimService.claim(player, q.fullId());
                            claimed = true;
                        }
                    }
                }
                if (++guard > 16) break;
            } while (claimed);
        } finally {
            autoClaimInProgress = false;
        }
    }

    // ---------- helpers ----------

    private static boolean isExcluded(Quest quest, TeamQuestProgress team) {
        List<String> ex = quest.exclusions();
        if (ex == null || ex.isEmpty()) return false;
        for (String id : ex) {
            String fullId = id.contains("/") ? id : quest.chapterId() + "/" + id;
            if (!id.contains("/") && QuestRegistry.quest(fullId).isEmpty()) {
                Optional<Quest> found = QuestRegistry.questByBareId(id);
                if (found.isPresent()) fullId = found.get().fullId();
            }
            QuestProgress p = team.peek(fullId);
            if (p != null && (p.status() == QuestStatus.CLAIMED || p.everClaimed())) {
                return true;
            }
        }
        return false;
    }

    private static boolean dependenciesSatisfied(Quest quest, TeamQuestProgress team) {
        List<String> deps = quest.dependencies();
        if (deps == null || deps.isEmpty()) return true;

        int satisfied = 0;
        for (String depId : deps) {
            // Dependency ids in JSON can be bare "quest_id" (same chapter) or
            // "chapter/quest_id" (cross-chapter). Normalize to full ids.
            String fullDep = depId.contains("/") ? depId : quest.chapterId() + "/" + depId;
            Optional<Quest> depQuest = QuestRegistry.quest(fullDep);
            if (depQuest.isEmpty() && !depId.contains("/")) {
                // Bare id not found in same chapter — search all chapters.
                // Handles cross-chapter deps stored without a chapter prefix.
                depQuest = QuestRegistry.questByBareId(depId);
                if (depQuest.isPresent()) fullDep = depQuest.get().fullId();
            }
            if (depQuest.isEmpty()) {
                // Missing dependency — treat as unsatisfied but don't hard-fail;
                // the loader has already logged it, and an orphaned dep shouldn't
                // brick the whole branch if the author removes a quest mid-save.
                continue;
            }
            QuestProgress depProg = team.peek(fullDep);
            boolean done = depProg != null
                    && (depProg.status() == QuestStatus.CLAIMED
                        || depProg.status() == QuestStatus.READY
                        || depProg.everClaimed());
            if (done) {
                satisfied++;
                // Short-circuit for OR mode when min_deps isn't set
                if (quest.minDeps() <= 0 && !quest.depsAll() && satisfied >= 1) return true;
                // Short-circuit for min_deps threshold
                if (quest.minDeps() > 0 && satisfied >= quest.minDeps()) return true;
            }
        }
        if (quest.minDeps() > 0) return satisfied >= quest.minDeps();
        return quest.depsAll() ? satisfied == deps.size() : satisfied >= 1;
    }

    private static boolean tasksComplete(Quest quest, QuestProgress qp) {
        List<QuestTask> tasks = quest.tasks();
        if (tasks.isEmpty()) return true; // checkmark-only quest — claim immediately
        for (int i = 0; i < tasks.size(); i++) {
            QuestTask t = tasks.get(i);
            TaskProgress tp = qp.task(i);
            if (tp.count() < t.target()) return false;
        }
        return true;
    }
}
