package com.soul.soa_additions.donor;

import java.util.HashMap;
import java.util.Map;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * Shared definition of the donor "shine" text styling.
 *
 * <p>A donor's name in chat is stamped with a marker font
 * ({@code soa_additions:donor_*}). Those fonts are pass-through
 * {@code reference} providers pointing at {@code minecraft:default}, so they
 * render with the exact glyphs of normal text and cost no extra atlas space —
 * their only job is to survive the network as a per-character flag that the
 * client can recognise.
 *
 * <p>The client mixin {@code DonorGradientMixin} spots that flag while the
 * glyph is being drawn and swaps the colour for {@link #shine} — a soft
 * highlight band that drifts across the text once every few seconds and then
 * rests. Without the mod (or if the mixin fails to apply) the text simply keeps
 * its {@linkplain Gradient#staticColor static} tier colour, so nothing breaks.
 */
public final class DonorStyles {

    private DonorStyles() {}

    /**
     * A two-stop colour ramp plus the marker font that selects it.
     *
     * @param font     marker font stamped onto the styled text
     * @param from     dark stop, 0xRRGGBB
     * @param to       bright stop, 0xRRGGBB
     * @param contrast how far the sweep swings either side of rest; 1.0 is the
     *                 restrained tier look, higher values travel most of the ramp
     */
    public record Gradient(ResourceLocation font, int from, int to, float contrast) {

        Gradient(ResourceLocation font, int from, int to) {
            this(font, from, to, 1.0F);
        }

        /** Flat colour used when the animation isn't running. */
        public int staticColor() {
            return lerpRgb(from, to, 0.55F);
        }
    }

    private static ResourceLocation font(String name) {
        return new ResourceLocation(SoaAdditions.MODID, "donor_" + name);
    }

    public static final Gradient VOID      = new Gradient(font("void"),      0x7B5FD4, 0xB8A4FF);
    public static final Gradient INFERNIUM = new Gradient(font("infernium"), 0xD4652F, 0xFFB061);
    public static final Gradient ETHER     = new Gradient(font("ether"),     0x3FBFB0, 0x9FF0E4);
    /**
     * Owner-only chrome ramp, dark through to white, swinging harder than the
     * tiers so it reads as light travelling over polished metal.
     *
     * <p>The dark stop is charcoal rather than true black: chat draws over a
     * translucent black backdrop, so black glyphs would drop out entirely
     * instead of looking dark.
     */
    public static final Gradient OWNER     = new Gradient(font("owner"),     0x2A2A2A, 0xFFFFFF, 1.55F);

    private static final Map<ResourceLocation, Gradient> BY_FONT = new HashMap<>();

    static {
        for (Gradient g : new Gradient[] { VOID, INFERNIUM, ETHER, OWNER }) {
            BY_FONT.put(g.font(), g);
        }
    }

    /** Ramp for a donor: the owner keeps their tier tag but shines chrome. */
    public static Gradient of(DonorData.Tier tier, boolean owner) {
        if (owner) return OWNER;
        return switch (tier) {
            case VOID -> VOID;
            case INFERNIUM -> INFERNIUM;
            case ETHER -> ETHER;
        };
    }

    /**
     * Ramp a marker font selects, or {@code null} for any other font.
     *
     * <p>Called once per glyph per frame, so the overwhelmingly common case —
     * text in the default font — short-circuits on an identity check before the
     * map is ever touched. {@link Style} holds the shared
     * {@code Style.DEFAULT_FONT} instance whenever no font was set explicitly.
     */
    public static Gradient gradientFor(ResourceLocation font) {
        if (font == null || font == Style.DEFAULT_FONT) return null;
        return BY_FONT.get(font);
    }

    // ---------------------------------------------------------------------
    // Animation
    // ---------------------------------------------------------------------

    /** Full cycle: roughly 1.8s of travel followed by ~4.4s of rest. */
    private static final float PERIOD_MS = 6200.0F;
    /** Phase advance per character — ~12 characters spans one highlight width. */
    private static final float PER_CHAR = 0.085F;
    /** Extra phase per character of run offset, so the name trails the tag. */
    private static final float PER_RUN = 0.02F;
    /** Resting brightness along the ramp when the band is elsewhere. */
    private static final float REST = 0.42F;
    private static final float HIGHLIGHT = 0.62F;
    private static final float SHADOW = 0.26F;

    /**
     * Colour for one glyph of a shining run.
     *
     * @param g        the tier ramp
     * @param offset   character offset within this run
     * @param runStart index the run began at, used to stagger separate runs
     * @return 0xRRGGBB
     */
    public static int shine(Gradient g, int offset, int runStart) {
        float t = (System.currentTimeMillis() % (long) PERIOD_MS) / PERIOD_MS;

        // The band starts off the left edge and travels well past the right,
        // so most of the cycle is spent at rest rather than mid-sweep.
        float band = -0.55F + t * 3.6F;
        float pos = offset * PER_CHAR + runStart * PER_RUN;
        float d = pos - band;

        // A dark lobe leading a bright one reads as light moving over metal,
        // rather than as a flat brightness pulse.
        float swing = gaussian(d, 0.17F) * HIGHLIGHT - gaussian(d + 0.26F, 0.20F) * SHADOW;
        float level = REST + swing * g.contrast();

        return lerpRgb(g.from(), g.to(), clamp01(level));
    }

    private static float gaussian(float x, float sigma) {
        return (float) Math.exp(-(x * x) / (2.0F * sigma * sigma));
    }

    private static float clamp01(float v) {
        return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
    }

    private static int lerpRgb(int from, int to, float f) {
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * f);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * f);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * f);
        return (r << 16) | (g << 8) | b;
    }
}
