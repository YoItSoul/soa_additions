package com.soul.soa_additions.donor;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable snapshot of a single donor's record.
 *
 * @param uuid      Minecraft player UUID
 * @param name      Display name (cached at time of donation)
 * @param tier      Donation tier
 * @param donatedAt ISO-8601 instant of the first donation
 * @param message   Optional personal message from the donor
 */
public record DonorData(
        UUID uuid,
        String name,
        Tier tier,
        Instant donatedAt,
        String message
) {

    /** Silver color used for Void / Infernium chat glow. */
    public static final int COL_SILVER = 0xFFC0C0C0;
    /** Gold color used for Ether chat glow. */
    public static final int COL_GOLD   = 0xFFFFD700;

    public enum Tier {
        VOID("Void",           COL_SILVER, "\u2764"),    // silver heart
        INFERNIUM("Infernium", COL_SILVER, "\u2B50"),    // silver star
        ETHER("Ether",         COL_GOLD,   "\u2728");    // gold sparkles

        public final String display;
        public final int color;
        public final String symbol;

        Tier(String display, int color, String symbol) {
            this.display = display;
            this.color = color;
            this.symbol = symbol;
        }

        /** Whether this tier gets the gold glow (Ether) vs silver. */
        public boolean isGold() { return this == ETHER; }

        public Tier next() { return values()[(ordinal() + 1) % values().length]; }

        public static Tier fromName(String name) {
            if (name == null) return VOID;
            for (Tier t : values()) {
                if (t.name().equalsIgnoreCase(name) || t.display.equalsIgnoreCase(name)) return t;
            }
            String lower = name.toLowerCase().trim();
            if (lower.contains("ether")) return ETHER;
            if (lower.contains("infernium")) return INFERNIUM;
            if (lower.contains("void")) return VOID;
            return VOID;
        }
    }
}
