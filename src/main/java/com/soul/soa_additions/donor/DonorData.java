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

    /** Silver color used for Supporter / Netherite / Void Metal chat glow. */
    public static final int COL_SILVER = 0xFFC0C0C0;
    /** Gold color used for Draconium chat glow. */
    public static final int COL_GOLD   = 0xFFFFD700;

    public enum Tier {
        SUPPORTER("Supporter",    COL_SILVER, "\u2764"),    // silver heart
        NETHERITE("Netherite",    COL_SILVER, "\u2B50"),    // silver star
        VOID_METAL("Void Metal",  COL_SILVER, "\u2B50"),    // silver star
        DRACONIUM("Draconium",    COL_GOLD,   "\u2728");    // gold sparkles

        public final String display;
        public final int color;
        public final String symbol;

        Tier(String display, int color, String symbol) {
            this.display = display;
            this.color = color;
            this.symbol = symbol;
        }

        /** Whether this tier gets the gold glow (Draconium) vs silver. */
        public boolean isGold() { return this == DRACONIUM; }

        public Tier next() { return values()[(ordinal() + 1) % values().length]; }

        public static Tier fromName(String name) {
            if (name == null) return SUPPORTER;
            for (Tier t : values()) {
                if (t.name().equalsIgnoreCase(name) || t.display.equalsIgnoreCase(name)) return t;
            }
            // Handle common variations
            String lower = name.toLowerCase().trim();
            if (lower.contains("draconium")) return DRACONIUM;
            if (lower.contains("void")) return VOID_METAL;
            if (lower.contains("netherite")) return NETHERITE;
            return SUPPORTER;
        }
    }
}
