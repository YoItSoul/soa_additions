package com.soul.soa_additions.donor;

import java.time.Instant;
import java.util.Locale;
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

    /**
     * The pack owner.
     *
     * <p>Lives on the data record rather than in {@code DonorRegistry} so the
     * client can tell whether a name should shine chrome without reaching into
     * server-side state \u2014 the donor wall only ever sees {@link DonorData}.
     */
    public static final UUID OWNER_UUID = UUID.fromString("3686e026-4b14-4f9d-accb-678b61ee478d");

    /** Whether this is the pack owner, who shines chrome rather than tier colors. */
    public boolean isOwner() {
        return OWNER_UUID.equals(uuid);
    }

    /**
     * Representative tier colors, ARGB \u2014 these feed the GUI ({@code fill} and
     * {@code drawString} both want an opaque alpha byte) and the orb.
     *
     * <p>Chat does not use these; it ramps between the two stops held in
     * {@link DonorStyles}, of which each of these is the midpoint.
     */
    public static final int COL_VOID      = 0xFF9982E9;
    public static final int COL_INFERNIUM = 0xFFEC8B48;
    public static final int COL_ETHER     = 0xFF6FD8CA;

    public enum Tier {
        VOID("Void",           COL_VOID),
        INFERNIUM("Infernium", COL_INFERNIUM),
        ETHER("Ether",         COL_ETHER);

        public final String display;
        public final int color;

        Tier(String display, int color) {
            this.display = display;
            this.color = color;
        }

        /** Whether this is the top tier (Ether). */
        public boolean isTop() { return this == ETHER; }

        public Tier next() { return values()[(ordinal() + 1) % values().length]; }

        /**
         * Resolves a tier by name. The Ko-fi tiers carry these same three names,
         * so anything the supporters API sends lands on the exact-match pass.
         *
         * <p>Exact matches are tried before the substring pass, and the
         * substring pass guards "ether" specifically: "n<b>ether</b>ite"
         * contains it, so a naive scan would read any label mentioning a nether
         * material as the top tier.
         */
        public static Tier fromName(String name) {
            if (name == null) return VOID;
            String lower = name.toLowerCase(Locale.ROOT).trim();

            for (Tier t : values()) {
                if (t.name().equalsIgnoreCase(lower) || t.display.equalsIgnoreCase(lower)) return t;
            }

            // Decorated labels, e.g. "Ether Supporter" or "void tier".
            if (lower.contains("infernium")) return INFERNIUM;
            if (lower.contains("void")) return VOID;
            if (lower.contains("ether") && !lower.contains("netherite")) return ETHER;
            return VOID;
        }
    }
}
