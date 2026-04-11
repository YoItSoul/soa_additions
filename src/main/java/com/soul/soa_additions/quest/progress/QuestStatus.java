package com.soul.soa_additions.quest.progress;

/**
 * Lifecycle of a single quest for a single team.
 *
 * <pre>
 *   LOCKED  → dependencies not yet satisfied, not shown (or shown greyed)
 *   VISIBLE → dependencies satisfied, task counters can advance
 *   READY   → all tasks complete, player can claim rewards
 *   CLAIMED → rewards distributed, terminal
 * </pre>
 *
 * <p>The split between {@code READY} and {@code CLAIMED} exists so claiming is
 * an explicit player action — some rewards (stage grants, packmode locks) are
 * irreversible and should never fire automatically. It also gives us a clean
 * place to fan out per-player rewards across a team.</p>
 */
public enum QuestStatus {
    LOCKED,
    VISIBLE,
    READY,
    CLAIMED
}
