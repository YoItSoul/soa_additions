package com.soul.soa_additions.quest.model;

import java.util.Locale;

/**
 * Who receives a quest reward.
 *
 * <p><b>PLAYER</b> — reward is granted to the individual player who clicks claim.
 * Each teammate claims independently and each gets their own copy. Good for
 * personal gear, XP, private consumables.</p>
 *
 * <p><b>TEAM</b> — reward is granted to every team member simultaneously the
 * first time any teammate claims, and that claim consumes it for the whole team.
 * Good for shared milestones (pack-mode unlocks, stage grants, shared currency).</p>
 *
 * <p>A reward on a solo player always acts as PLAYER regardless of field value —
 * solo players are a team of one, so the distinction collapses.</p>
 */
public enum RewardScope {
    PLAYER,
    TEAM;

    public String lower() { return name().toLowerCase(Locale.ROOT); }

    public static RewardScope fromString(String s) {
        if (s == null) return PLAYER;
        try { return valueOf(s.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { return PLAYER; }
    }
}
