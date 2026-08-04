package com.soul.soa_additions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Tunables for the Cyclic Fishing Net &times; Aquaculture integration
 * ({@link com.soul.soa_additions.cyclicaqua.CyclicAquaFisher}).
 *
 * <p>Cyclic's own {@code config/cyclic.toml} still owns the two knobs it always
 * owned — {@code [cyclic.blocks.fisher] radius} (how far the net samples) and
 * {@code chance} (the per-tick base catch chance). Everything here layers on top
 * of that base chance rather than replacing it, so tuning the net's raw speed is
 * still done in Cyclic's config.</p>
 *
 * <p>COMMON, not CLIENT: every value is read on the logical server inside the
 * block-entity tick. A client never evaluates any of it.</p>
 */
public final class CyclicFisherConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue BASE_LUCK_BONUS;
    public static final ForgeConfigSpec.DoubleValue LURE_RATE_BONUS_PER_LEVEL;
    public static final ForgeConfigSpec.BooleanValue ALLOW_OPEN_WATER_LOOT;
    public static final ForgeConfigSpec.DoubleValue MENDING_DAMAGE_SKIP_CHANCE;
    public static final ForgeConfigSpec.BooleanValue DROP_EXPERIENCE;
    public static final ForgeConfigSpec.BooleanValue POST_ITEM_FISHED_EVENT;
    public static final ForgeConfigSpec.BooleanValue ALLOW_HOOK_FLUIDS;

    public static final ForgeConfigSpec.BooleanValue WITHER_CATCH_ENABLED;
    public static final ForgeConfigSpec.DoubleValue WITHER_CATCH_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<String> WITHER_CATCH_STAGE;
    public static final ForgeConfigSpec.ConfigValue<String> WITHER_CATCH_ROD_NAME;
    public static final ForgeConfigSpec.DoubleValue WITHER_CATCH_NET_PLAYER_RADIUS;
    public static final ForgeConfigSpec.BooleanValue WITHER_CATCH_HAND_FISHING;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("cyclic_fisher");
        builder.comment(
                "Cyclic Fishing Net x Aquaculture integration.",
                "Only has any effect when BOTH mods are installed; with either one missing the",
                "net keeps Cyclic's stock behaviour and nothing in this file is read."
        );

        ENABLED = builder
                .comment(
                        "Master switch. When false the Fishing Net falls back to Cyclic's own",
                        "logic: no hooks, no bait, no open-water loot, and Mending repairs the",
                        "rod faster than fishing damages it (an infinite-durability rod)."
                )
                .define("enabled", true);

        BASE_LUCK_BONUS = builder
                .comment(
                        "Flat luck added on top of Luck of the Sea and the hook's luck modifier.",
                        "Cyclic hardcodes +1 here; hand-casting has no such bonus, so the default",
                        "of 0 matches what a player rolls with the same rod. Raising this shifts",
                        "the roll away from junk (and away from Aquaculture's lootboxes, which",
                        "live in the junk table) and toward treasure."
                )
                .defineInRange("baseLuckBonus", 0, 0, 64);

        LURE_RATE_BONUS_PER_LEVEL = builder
                .comment(
                        "Catch-rate bonus per level of lure speed (Lure enchant + bait + the",
                        "Neptunium rod's built-in +1, capped at 5 like hand-casting).",
                        "Hand-casting spends lure on a bite timer the net does not have, so it is",
                        "remapped onto the catch chance: chance = cyclic_chance * (1 + lure * this).",
                        "At the default 0.2 a fully-lured rod fishes twice as fast."
                )
                .defineInRange("lureRateBonusPerLevel", 0.2D, 0.0D, 10.0D);

        ALLOW_OPEN_WATER_LOOT = builder
                .comment(
                        "Allow the open-water-only loot entries (vanilla treasure and Aquaculture's",
                        "Neptune's Bounty / neptunium nuggets) to roll. The net still has to pass",
                        "vanilla's real open-water test at the block it sampled: a clean 5x5 column",
                        "of source water, no blocks, no lily pads, for the layers y-1 through y+2.",
                        "A net in a 1x1 hole or a covered pool fails it and gets fish and junk only.",
                        "Set false to keep treasure and neptunium strictly hand-caught."
                )
                .define("allowOpenWaterLoot", true);

        MENDING_DAMAGE_SKIP_CHANCE = builder
                .comment(
                        "Chance for a Mending rod to skip the durability hit on a catch.",
                        "Cyclic's stock behaviour repairs the rod faster than it damages it, so a",
                        "Mending rod never wears out. Here Mending never repairs — it only skips",
                        "damage — so 0.5 means a 450-durability diamond rod lasts ~900 catches",
                        "instead of forever. 0.0 makes Mending worthless in the net."
                )
                .defineInRange("mendingDamageSkipChance", 0.5D, 0.0D, 0.95D);

        DROP_EXPERIENCE = builder
                .comment(
                        "Drop the 1-6 XP orbs per caught item that hand-casting awards.",
                        "Turn off if you would rather the net not double as an XP farm."
                )
                .define("dropExperience", true);

        POST_ITEM_FISHED_EVENT = builder
                .comment(
                        "Fire Forge's ItemFishedEvent for net catches. Aquaculture's own listener",
                        "(the fish-weight roll) is always invoked directly regardless of this",
                        "setting, so leaving it false loses nothing from Aquaculture itself.",
                        "Off by default because third-party listeners generally assume a real",
                        "angler behind the hook, and the net has no player."
                )
                .define("postItemFishedEvent", false);

        ALLOW_HOOK_FLUIDS = builder
                .comment(
                        "Let the net fish whatever fluid the equipped hook declares, instead of",
                        "water only. No hook in Aquaculture 2.5.7 actually declares lava (the",
                        "Nether Star hook registers WATER, which looks like an upstream bug), so",
                        "today this changes nothing; it exists so the net follows automatically",
                        "if a lava-capable hook ever appears."
                )
                .define("allowHookFluids", true);

        builder.pop();

        builder.push("wither_catch");
        builder.comment(
                "Ol' Withy. A rod renamed to the magic words, wearing a Nether Star Hook, in the",
                "hands of someone who has already proven themselves, occasionally hooks something",
                "that is not a fish. Requires Aquaculture; the stage check additionally requires",
                "GameStages (with GameStages absent the stage can never be held, so it never fires)."
        );

        WITHER_CATCH_ENABLED = builder
                .comment("Master switch for the Ol' Withy catch.")
                .define("enabled", true);

        WITHER_CATCH_CHANCE = builder
                .comment(
                        "Flat chance, rolled once per successful catch, that the catch is a Wither",
                        "instead of loot. Not affected by luck, lure, bait or open water."
                )
                .defineInRange("chance", 0.001D, 0.0D, 1.0D);

        WITHER_CATCH_STAGE = builder
                .comment("GameStages stage the angler must hold.")
                .define("requiredStage", "wither_slayer");

        WITHER_CATCH_ROD_NAME = builder
                .comment("Required custom rod name. Matched case-insensitively, exact text otherwise.")
                .define("requiredRodName", "Ol' Withy");

        WITHER_CATCH_NET_PLAYER_RADIUS = builder
                .comment(
                        "A Fishing Net has no angler, so it borrows the nearest player within this",
                        "many blocks and checks that player's stages. Nobody in range means no roll."
                )
                .defineInRange("netPlayerRadius", 32.0D, 1.0D, 256.0D);

        WITHER_CATCH_HAND_FISHING = builder
                .comment(
                        "Also fire when a player hand-casts with Ol' Withy, not just from the net.",
                        "The rod, the hook and the stage are all the player's own in that case."
                )
                .define("applyToHandFishing", true);

        builder.pop();
        SPEC = builder.build();
    }

    private CyclicFisherConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "soa_additions-cyclic_fisher.toml");
    }
}
