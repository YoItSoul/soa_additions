package com.soul.soa_additions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Client-side color config for the quest book GUI. Colors are stored as hex
 * strings (with optional alpha) in the TOML so users can edit them in-place
 * without having to reason about signed-int encodings. Default values mirror
 * what the screen used before the config was introduced, with the background
 * swapped to 50% transparent black per the "can see the game behind the book"
 * requirement.
 *
 * <p>Reads are cheap — {@link #argb(ForgeConfigSpec.ConfigValue, int)} parses
 * the string each call, so a typo in the config just falls back to the default
 * rather than crashing the screen.</p>
 */
public final class QuestBookConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> BACKGROUND;
    public static final ForgeConfigSpec.ConfigValue<String> BACKGROUND_GRADIENT;
    public static final ForgeConfigSpec.ConfigValue<String> LEFT_PANE;
    public static final ForgeConfigSpec.ConfigValue<String> LEFT_PANE_SELECTED;
    public static final ForgeConfigSpec.ConfigValue<String> LEFT_PANE_HOVER;
    public static final ForgeConfigSpec.ConfigValue<String> BORDER;
    public static final ForgeConfigSpec.ConfigValue<String> SEPARATOR;
    public static final ForgeConfigSpec.ConfigValue<String> TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> TEXT_DIM;
    public static final ForgeConfigSpec.ConfigValue<String> TEXT_MUTED;
    public static final ForgeConfigSpec.ConfigValue<String> ACCENT;
    public static final ForgeConfigSpec.ConfigValue<String> HEADING;

    public static final ForgeConfigSpec.ConfigValue<String> NODE_OUTLINE_IDLE;
    public static final ForgeConfigSpec.ConfigValue<String> NODE_OUTLINE_HOVER;

    public static final ForgeConfigSpec.ConfigValue<String> STATUS_LOCKED;
    public static final ForgeConfigSpec.ConfigValue<String> STATUS_VISIBLE;
    public static final ForgeConfigSpec.ConfigValue<String> STATUS_READY;
    public static final ForgeConfigSpec.ConfigValue<String> STATUS_CLAIMED;

    public static final ForgeConfigSpec.ConfigValue<String> EDGE_NORMAL;
    public static final ForgeConfigSpec.ConfigValue<String> EDGE_OR_GROUP;

    public static final ForgeConfigSpec.ConfigValue<String> DETAIL_BACKGROUND;
    public static final ForgeConfigSpec.ConfigValue<String> DETAIL_HEADER;
    public static final ForgeConfigSpec.ConfigValue<String> DETAIL_BORDER;
    public static final ForgeConfigSpec.ConfigValue<String> DETAIL_SHADOW;

    public static final ForgeConfigSpec.ConfigValue<String> TOOLTIP_BACKGROUND;
    public static final ForgeConfigSpec.ConfigValue<String> CLAIMED_TICK_BG;
    public static final ForgeConfigSpec.ConfigValue<String> CLAIMED_TICK_FG;
    public static final ForgeConfigSpec.ConfigValue<String> CLAIM_BUTTON;
    public static final ForgeConfigSpec.ConfigValue<String> SUBMIT_BUTTON;
    public static final ForgeConfigSpec.ConfigValue<String> CHECKMARK_BOX;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("questBook");
        b.comment(
                "Quest book colors. Values are ARGB hex with an optional alpha channel.",
                "Format: 0xAARRGGBB — e.g. 0x80000000 is 50% transparent black,",
                "0xFF6B8CFF is a solid cornflower blue. Missing alpha implies fully opaque.");

        b.push("palette");
        BACKGROUND         = b.comment("Main book background (set to 0x80000000 for 50% see-through black).")
                .define("background", "0x80000000");
        BACKGROUND_GRADIENT = b.comment("Gradient bottom color. Use the same as 'background' to disable the gradient.")
                .define("backgroundGradient", "0x80000000");
        LEFT_PANE          = b.define("leftPane", "0xC01A1F33");
        LEFT_PANE_SELECTED = b.define("leftPaneSelected", "0xFF222842");
        LEFT_PANE_HOVER    = b.define("leftPaneHover", "0xFF1F253E");
        BORDER             = b.define("border", "0xFF3A4264");
        SEPARATOR          = b.define("separator", "0xFF2A3050");
        TEXT               = b.define("text", "0xFFE6E9F5");
        TEXT_DIM           = b.define("textDim", "0xFF8A91AE");
        TEXT_MUTED         = b.define("textMuted", "0xFFBFC5DC");
        ACCENT             = b.define("accent", "0xFF6B8CFF");
        HEADING            = b.define("heading", "0xFF6B8CFF");
        b.pop();

        b.push("nodes");
        NODE_OUTLINE_IDLE  = b.define("outlineIdle", "0xFF0A0D18");
        NODE_OUTLINE_HOVER = b.define("outlineHover", "0xFFFFFFFF");
        STATUS_LOCKED      = b.define("statusLocked", "0xFF3A3F55");
        STATUS_VISIBLE     = b.define("statusVisible", "0xFF3E6FB5");
        STATUS_READY       = b.define("statusReady", "0xFF4EB02E");
        STATUS_CLAIMED     = b.define("statusClaimed", "0xFFC9A227");
        CLAIMED_TICK_BG    = b.define("claimedTickBackground", "0xFF1A1F33");
        CLAIMED_TICK_FG    = b.define("claimedTickForeground", "0xFF66FF66");
        b.pop();

        b.push("edges");
        EDGE_NORMAL        = b.define("normal", "0xFF5A6391");
        EDGE_OR_GROUP      = b.define("orGroup", "0xFFB59A3A");
        b.pop();

        b.push("detail");
        DETAIL_BACKGROUND  = b.define("background", "0xF00E1220");
        DETAIL_HEADER      = b.define("header", "0xFF222842");
        DETAIL_BORDER      = b.define("border", "0xFF3A4264");
        DETAIL_SHADOW      = b.define("shadow", "0x80000000");
        TOOLTIP_BACKGROUND = b.define("tooltipBackground", "0xF00A0D18");
        CLAIM_BUTTON       = b.define("claimButton", "0xFF4EB02E");
        SUBMIT_BUTTON      = b.define("submitButton", "0xFF3E6FB5");
        CHECKMARK_BOX      = b.define("checkmarkBox", "0xFF3A4264");
        b.pop();

        b.pop();
        SPEC = b.build();
    }

    private QuestBookConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "soa_additions-questbook.toml");
    }

    /**
     * Parse a hex color string from config. Accepts optional "0x"/"#" prefix
     * and either 6 hex digits (RGB — opaque) or 8 (ARGB). Returns the caller's
     * fallback on any parse failure so a malformed config can't brick the GUI.
     */
    public static int argb(ForgeConfigSpec.ConfigValue<String> cv, int fallback) {
        try {
            String s = cv.get();
            if (s == null) return fallback;
            s = s.trim();
            if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
            else if (s.startsWith("#")) s = s.substring(1);
            long v = Long.parseLong(s, 16);
            // 6-digit → promote to opaque
            if (s.length() <= 6) v |= 0xFF000000L;
            return (int) v;
        } catch (Throwable t) {
            return fallback;
        }
    }
}
