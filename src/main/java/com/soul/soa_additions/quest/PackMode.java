package com.soul.soa_additions.quest;

import java.util.Locale;

/**
 * The three supported packmodes. Drives quest filtering, loot, difficulty tiers,
 * recipes, and item stages. Locked at world creation (or on first quest completion
 * that carries a {@code lock_packmode} reward).
 */
public enum PackMode {
    CASUAL,
    ADVENTURE,
    EXPERT;

    public String lower() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static PackMode fromString(String s) {
        if (s == null) return ADVENTURE;
        try { return valueOf(s.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { return ADVENTURE; }
    }

    /**
     * Strict parse: null when the string names no mode.
     *
     * <p>{@link #fromString} answers ADVENTURE for a typo, which is right for callers that just
     * need a mode and wrong for the three that have to tell a typo from a deliberate "adventure".
     * Each of those used to re-derive this by comparing the result's name back against the input.</p>
     */
    public static PackMode parseStrict(String s) {
        if (s == null) return null;
        try { return valueOf(s.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { return null; }
    }
}
