package com.soul.soa_additions.bloodarsenal;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Configuration for all Blood Arsenal content.
 * Values ported from BloodArsenal 1.12 ConfigHandler.
 */
public final class BAConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // ── General ──────────────────────────────────────────────────────────

    public static final ForgeConfigSpec.DoubleValue GLASS_DAGGER_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue GLASS_DAGGER_OF_SACRIFICE_LP_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue GLASS_SACRIFICIAL_DAGGER_LP;
    public static final ForgeConfigSpec.IntValue BLOOD_CAPACITOR_STORAGE;
    public static final ForgeConfigSpec.IntValue BLOOD_CAPACITOR_TRANSFER;
    public static final ForgeConfigSpec.IntValue TOOL_REPAIR_RATE;
    public static final ForgeConfigSpec.IntValue TOOL_REPAIR_COST;

    // ── Sigils ───────────────────────────────────────────────────────────

    public static final ForgeConfigSpec.IntValue SIGIL_SWIMMING_COST;
    public static final ForgeConfigSpec.IntValue SIGIL_ENDER_COST;
    public static final ForgeConfigSpec.IntValue SIGIL_LIGHTNING_COST;
    public static final ForgeConfigSpec.IntValue SIGIL_DIVINITY_COST;
    public static final ForgeConfigSpec.IntValue SIGIL_SENTIENCE_COST;

    // ── Baubles ──────────────────────────────────────────────────────────

    public static final ForgeConfigSpec.DoubleValue VAMPIRE_RING_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue SACRIFICE_AMULET_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue SELF_SACRIFICE_AMULET_MULTIPLIER;

    // ── Rituals ──────────────────────────────────────────────────────────

    public static final ForgeConfigSpec.BooleanValue ENABLE_SANGUINE_INFUSION;
    public static final ForgeConfigSpec.IntValue INFUSION_ACTIVATION_COST;
    public static final ForgeConfigSpec.IntValue INFUSION_REFRESH_COST;

    public static final ForgeConfigSpec.BooleanValue ENABLE_PURIFICATION;
    public static final ForgeConfigSpec.IntValue PURIFICATION_ACTIVATION_COST;
    public static final ForgeConfigSpec.IntValue PURIFICATION_REFRESH_COST;

    public static final ForgeConfigSpec.BooleanValue ENABLE_BLOOD_BURNER;
    public static final ForgeConfigSpec.IntValue BURNER_ACTIVATION_COST;
    public static final ForgeConfigSpec.IntValue BURNER_REFRESH_COST;

    public static final ForgeConfigSpec.IntValue MODIFIER_REMOVAL_COST;

    // ── Stasis Tools ─────────────────────────────────────────────────────

    public static final ForgeConfigSpec.IntValue STASIS_DRAIN_LP;
    public static final ForgeConfigSpec.IntValue STASIS_DRAIN_INTERVAL;

    static {
        BUILDER.push("general");
        GLASS_DAGGER_DAMAGE = BUILDER
                .comment("Amount of damage the Glass Sacrificial Dagger deals per use")
                .defineInRange("glassSacrificialDaggerHealth", 2.0, 0.0, 100.0);
        GLASS_DAGGER_OF_SACRIFICE_LP_MULTIPLIER = BUILDER
                .comment("LP multiplier for the Glass Dagger of Sacrifice")
                .defineInRange("glassDaggerOfSacrificeLPMultiplier", 2.0, 1.0, 1000.0);
        GLASS_SACRIFICIAL_DAGGER_LP = BUILDER
                .comment("Approximate LP given per use of the Glass Sacrificial Dagger (5x BM's default)")
                .defineInRange("glassSacrificialDaggerLP", 500, 0, 10000);
        BLOOD_CAPACITOR_STORAGE = BUILDER
                .comment("Maximum RF storage of the Blood Capacitor")
                .defineInRange("bloodCapacitorStorage", 10000, 1, Integer.MAX_VALUE);
        BLOOD_CAPACITOR_TRANSFER = BUILDER
                .comment("RF/tick transfer rate for the Blood Capacitor")
                .defineInRange("bloodCapacitorTransfer", 1000, 1, Integer.MAX_VALUE);
        TOOL_REPAIR_RATE = BUILDER
                .comment("Ticks between blood-infused tool auto-repair")
                .defineInRange("toolRepairRate", 100, 1, 6000);
        TOOL_REPAIR_COST = BUILDER
                .comment("LP cost per durability point repaired")
                .defineInRange("toolRepairCost", 10, 1, 10000);
        BUILDER.pop();

        BUILDER.push("sigils");
        SIGIL_SWIMMING_COST = BUILDER
                .comment("LP cost for the Swimming Sigil")
                .defineInRange("swimmingCost", 5000, 0, 1000000);
        SIGIL_ENDER_COST = BUILDER
                .comment("LP cost for the Ender Sigil")
                .defineInRange("enderCost", 5000, 0, 1000000);
        SIGIL_LIGHTNING_COST = BUILDER
                .comment("Base LP cost for the Lightning Sigil (multiplied by level)")
                .defineInRange("lightningCost", 800, 0, 1000000);
        SIGIL_DIVINITY_COST = BUILDER
                .comment("LP cost for the Divinity Sigil")
                .defineInRange("divinityCost", 100000, 0, 10000000);
        SIGIL_SENTIENCE_COST = BUILDER
                .comment("Base LP cost for the Sentience Sigil")
                .defineInRange("sentienceCost", 500, 0, 1000000);
        BUILDER.pop();

        BUILDER.push("baubles");
        VAMPIRE_RING_MULTIPLIER = BUILDER
                .comment("Healing multiplier for the Vampire Ring (healing = damage * this)")
                .defineInRange("vampireRingMultiplier", 0.5, 0.0, 100.0);
        SACRIFICE_AMULET_MULTIPLIER = BUILDER
                .comment("LP multiplier for the Sacrifice Amulet (LP = damage * this)")
                .defineInRange("sacrificeAmuletMultiplier", 20.0, 0.0, 100.0);
        SELF_SACRIFICE_AMULET_MULTIPLIER = BUILDER
                .comment("LP multiplier for the Self-Sacrifice Amulet (LP = damage * this)")
                .defineInRange("selfSacrificeAmuletMultiplier", 20.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("rituals");
        ENABLE_SANGUINE_INFUSION = BUILDER
                .comment("Enable the Sanguine Infusion ritual")
                .define("enableSanguineInfusion", true);
        INFUSION_ACTIVATION_COST = BUILDER
                .defineInRange("infusionActivationCost", 10000, 0, 1000000);
        INFUSION_REFRESH_COST = BUILDER
                .defineInRange("infusionRefreshCost", 100, 0, 1000000);

        ENABLE_PURIFICATION = BUILDER
                .comment("Enable the Purification ritual")
                .define("enablePurification", true);
        PURIFICATION_ACTIVATION_COST = BUILDER
                .defineInRange("purificationActivationCost", 10000, 0, 1000000);
        PURIFICATION_REFRESH_COST = BUILDER
                .defineInRange("purificationRefreshCost", 20, 0, 1000000);

        ENABLE_BLOOD_BURNER = BUILDER
                .comment("Enable the Blood Burner ritual")
                .define("enableBloodBurner", true);
        BURNER_ACTIVATION_COST = BUILDER
                .defineInRange("burnerActivationCost", 10000, 0, 1000000);
        BURNER_REFRESH_COST = BUILDER
                .defineInRange("burnerRefreshCost", 200, 0, 1000000);

        MODIFIER_REMOVAL_COST = BUILDER
                .comment("LP cost for the Modifier Removal ritual")
                .defineInRange("modifierRemovalCost", 25000, 0, 1000000);
        BUILDER.pop();

        BUILDER.push("stasisTools");
        STASIS_DRAIN_LP = BUILDER
                .comment("LP drained per interval while a stasis tool is active")
                .defineInRange("stasisDrainLP", 5, 0, 10000);
        STASIS_DRAIN_INTERVAL = BUILDER
                .comment("Ticks between stasis tool LP drain ticks")
                .defineInRange("stasisDrainInterval", 80, 1, 6000);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private BAConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "soa_additions/blood_arsenal.toml");
    }
}
