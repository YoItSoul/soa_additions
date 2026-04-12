package com.soul.soa_additions.donor;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable snapshot of a single donor's record.
 *
 * @param uuid      Minecraft player UUID
 * @param name      Display name (cached at time of donation)
 * @param tier      Donation tier (e.g. SUPPORTER, CHAMPION, LEGEND)
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

    public enum Tier {
        SUPPORTER("Supporter",  0xFF55FF55, "\u2764"),   // green heart
        CHAMPION("Champion",    0xFFFFAA00, "\u2B50"),   // gold star
        LEGEND("Legend",        0xFFFF55FF, "\u2728");    // purple sparkles

        public final String display;
        public final int color;
        public final String symbol;

        Tier(String display, int color, String symbol) {
            this.display = display;
            this.color = color;
            this.symbol = symbol;
        }

        public Tier next() { return values()[(ordinal() + 1) % values().length]; }

        public static Tier fromName(String name) {
            for (Tier t : values()) {
                if (t.name().equalsIgnoreCase(name) || t.display.equalsIgnoreCase(name)) return t;
            }
            return SUPPORTER;
        }
    }
}
