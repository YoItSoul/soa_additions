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
}
