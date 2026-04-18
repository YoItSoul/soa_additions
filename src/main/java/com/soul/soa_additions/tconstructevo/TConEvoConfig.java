package com.soul.soa_additions.tconstructevo;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Config for the TConstructEvo subsystem. Mirrors the spirit of 1.12.2's
 * {@code TconEvoConfig}: per-feature toggles and tuning knobs, with sensible
 * defaults tuned for a "factor in everything" modpack like Souls of Avarice.
 *
 * <p>Per the project convention, every gate that the original tconevo exposed
 * is preserved here so server owners can disable individual integrations or
 * trait categories without recompiling.</p>
 */
public final class TConEvoConfig {

    public static final ForgeConfigSpec SPEC;

    // ── General ──────────────────────────────────────────────────────────
    public static final ForgeConfigSpec.BooleanValue ENABLE_SCEPTRE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ARTIFACTS;
    public static final ForgeConfigSpec.DoubleValue ARTIFACT_DROP_CHANCE;

    // ── Combat tuning ────────────────────────────────────────────────────
    public static final ForgeConfigSpec.BooleanValue DISABLE_DAMAGE_CUTOFF;
    public static final ForgeConfigSpec.BooleanValue BREAK_UNBREAKABLE;
    public static final ForgeConfigSpec.IntValue BREAK_UNBREAKABLE_HARVEST_LEVEL;

    // ── Per-integration master toggles ──────────────────────────────────
    public static final ForgeConfigSpec.BooleanValue INTEG_DRACONIC;
    public static final ForgeConfigSpec.BooleanValue INTEG_AVARITIA;
    public static final ForgeConfigSpec.BooleanValue INTEG_BOTANIA;
    public static final ForgeConfigSpec.BooleanValue INTEG_MEKANISM;
    public static final ForgeConfigSpec.BooleanValue INTEG_ENDERIO;
    public static final ForgeConfigSpec.BooleanValue INTEG_THERMAL;
    public static final ForgeConfigSpec.BooleanValue INTEG_SOLARFLUX;
    public static final ForgeConfigSpec.BooleanValue INTEG_PROJECTE;
    public static final ForgeConfigSpec.BooleanValue INTEG_APPENG;
    public static final ForgeConfigSpec.BooleanValue INTEG_CURIOS;
    public static final ForgeConfigSpec.BooleanValue INTEG_BLOODMAGIC;
    public static final ForgeConfigSpec.BooleanValue INTEG_GAMESTAGES;
    public static final ForgeConfigSpec.BooleanValue INTEG_TOOLLEVELING;

    // ── Draconic specific ───────────────────────────────────────────────
    public static final ForgeConfigSpec.IntValue DRACONIC_ENERGY_CAPACITY_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue EVOLVED_BASE_RF_CAPACITY;
    public static final ForgeConfigSpec.IntValue EVOLVED_ENERGY_COST_TOOLS;
    public static final ForgeConfigSpec.DoubleValue DRACONIC_ARROW_SPEED_BONUS;
    public static final ForgeConfigSpec.DoubleValue PRIMORDIAL_CONVERSION_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue FLUX_BURN_FRACTION_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue FLUX_BURN_MIN_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue FLUX_BURN_MAX_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue FLUX_BURN_ENERGY_PER_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue DRACONIC_ATTACK_AOE_RADIUS_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue DRACONIC_ATTACK_AOE_DAMAGE_FRACTION;
    public static final ForgeConfigSpec.IntValue DRACONIC_DIG_AOE_RADIUS_PER_LEVEL;

    // ── Trait tuning shared across mods ─────────────────────────────────
    public static final ForgeConfigSpec.DoubleValue VAMPIRIC_LIFESTEAL_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue MORTAL_WOUNDS_HEAL_REDUCTION;
    public static final ForgeConfigSpec.IntValue REJUVENATING_REPAIR_SECONDS;
    public static final ForgeConfigSpec.IntValue ENERGIZED_CAPACITY_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue ENERGIZED_ENERGY_COST_TOOLS;
    public static final ForgeConfigSpec.IntValue PIEZOELECTRIC_RF_PER_DAMAGE;
    public static final ForgeConfigSpec.IntValue PHOTOVOLTAIC_RF_PER_LEVEL_SEC;
    public static final ForgeConfigSpec.IntValue FLUXED_CAPACITY_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue FLUXED_ENERGY_COST_TOOLS;
    public static final ForgeConfigSpec.DoubleValue ACCURACY_CHANCE_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ACCURACY_ATTR_MAX;

    // ── Avaritia specific ───────────────────────────────────────────────
    public static final ForgeConfigSpec.DoubleValue CONDENSING_DROP_PROBABILITY;
    public static final ForgeConfigSpec.BooleanValue OMNIPOTENCE_HITS_CREATIVE;

    // ── Blood Magic specific ────────────────────────────────────────────
    public static final ForgeConfigSpec.IntValue BLOODBOUND_TOOL_COST;
    public static final ForgeConfigSpec.DoubleValue CRYSTALYS_DROP_PROBABILITY;
    public static final ForgeConfigSpec.DoubleValue WILLFUL_DROP_PROBABILITY;
    public static final ForgeConfigSpec.DoubleValue WILLFUL_GEM_WILL_AMOUNT;
    public static final ForgeConfigSpec.DoubleValue SENTIENT_TIER_STEP;
    public static final ForgeConfigSpec.DoubleValue SENTIENT_DAMAGE_PER_TIER;

    // ── Botania specific ────────────────────────────────────────────────
    public static final ForgeConfigSpec.DoubleValue AURA_SIPHON_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FAE_VOICE_PIXIE_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FAE_VOICE_PIXIE_DAMAGE;
    public static final ForgeConfigSpec.IntValue MANA_INFUSED_COST;
    public static final ForgeConfigSpec.IntValue GAIA_WRATH_MANA_COST;

    // ── ProjectE specific ───────────────────────────────────────────────
    public static final ForgeConfigSpec.DoubleValue ETERNAL_DENSITY_EMC_PER_DAMAGE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("General toggles for the TConstructEvo content pack.").push("general");
        ENABLE_SCEPTRE = b.comment("Enable the Sceptre tool type and Magic Missile entity.")
                .define("enableSceptre", true);
        ENABLE_ARTIFACTS = b.comment("Enable the legendary artifact items and their loot table integration.")
                .define("enableArtifacts", true);
        ARTIFACT_DROP_CHANCE = b.comment("Global multiplier applied to every per-chest-table artifact drop chance. 1.0 = use the per-table values verbatim; 0.0 disables all loot drops (artifacts can still be obtained from creative).")
                .defineInRange("artifactDropChance", 1.0D, 0.0D, 1.0D);
        b.pop();

        b.comment("Combat tuning that mirrors the 1.12.2 coremod transforms still relevant on 1.20.1.").push("combat");
        DISABLE_DAMAGE_CUTOFF = b.comment(
                        "Disable TConstruct's damage cutoff so endgame tools can deal their full attack damage.",
                        "Without this, attack damage above ~30 is sharply reduced.")
                .define("disableDamageCutoff", true);
        BREAK_UNBREAKABLE = b.comment(
                        "Allow sufficiently powerful TConstruct tools to break otherwise unbreakable blocks (Draconic chest, etc.).",
                        "The tool's harvest level must meet or exceed `breakUnbreakableHarvestLevel`.")
                .define("breakUnbreakable", true);
        BREAK_UNBREAKABLE_HARVEST_LEVEL = b.comment("Minimum harvest tier (0=wood, 4=cobalt, 5=hepatizon, 6=manyullyn, 7+=draconic-tier) required to break unbreakable blocks.")
                .defineInRange("breakUnbreakableHarvestLevel", 7, 0, 99);
        b.pop();

        b.comment("Master toggles per-integration. Setting any of these to false skips that integration entirely even if the target mod is loaded.").push("integrations");
        INTEG_DRACONIC = b.define("draconicEvolution", true);
        INTEG_AVARITIA = b.define("avaritia", true);
        INTEG_BOTANIA = b.define("botania", true);
        INTEG_MEKANISM = b.define("mekanism", true);
        INTEG_ENDERIO = b.define("enderio", true);
        INTEG_THERMAL = b.define("thermal", true);
        INTEG_SOLARFLUX = b.define("solarflux", true);
        INTEG_PROJECTE = b.define("projecte", true);
        INTEG_APPENG = b.define("appliedEnergistics2", true);
        INTEG_CURIOS = b.define("curios", true);
        INTEG_BLOODMAGIC = b.define("bloodmagic", true);
        INTEG_GAMESTAGES = b.define("gamestages", true);
        INTEG_TOOLLEVELING = b.define("tinkersLevelling", true);
        b.pop();

        b.comment("Draconic Evolution integration tuning.").push("draconic");
        DRACONIC_ENERGY_CAPACITY_PER_LEVEL = b.comment("RF capacity granted per level of the Draconic Energy modifier.")
                .defineInRange("energyCapacityPerLevel", 1_000_000, 1, Integer.MAX_VALUE);
        EVOLVED_BASE_RF_CAPACITY = b.comment("Base RF capacity contributed by one level of the Evolved trait. Tier doubles each additional level.")
                .defineInRange("evolvedBaseRfCapacity", 8_000_000, 1, Integer.MAX_VALUE);
        EVOLVED_ENERGY_COST_TOOLS = b.comment("RF drained per point of durability damage absorbed by the Evolved trait.")
                .defineInRange("evolvedEnergyCostTools", 500, 0, Integer.MAX_VALUE);
        DRACONIC_ARROW_SPEED_BONUS = b.comment("Flat bonus to the tool's velocity stat per level of the Draconic Arrow Speed modifier.")
                .defineInRange("draconicArrowSpeedBonus", 1.0D, 0.0D, 100.0D);
        PRIMORDIAL_CONVERSION_PER_LEVEL = b.comment("Fraction of melee damage that the Primordial modifier converts into bonus armour-bypassing chaos damage per level.")
                .defineInRange("primordialConversionPerLevel", 0.04D, 0.0D, 1.0D);
        FLUX_BURN_FRACTION_PER_LEVEL = b.comment("Fraction of each equipped RF-storing item's energy that Flux Burn drains on hit per level.")
                .defineInRange("fluxBurnFractionPerLevel", 0.01D, 0.0D, 1.0D);
        FLUX_BURN_MIN_PER_LEVEL = b.comment("Lower bound on the RF Flux Burn drains from a single item per level, even if the fractional amount is smaller.")
                .defineInRange("fluxBurnMinPerLevel", 256, 0, Integer.MAX_VALUE);
        FLUX_BURN_MAX_PER_LEVEL = b.comment("Upper bound on the RF Flux Burn drains from a single item per level; 0 disables the cap.")
                .defineInRange("fluxBurnMaxPerLevel", 320_000, 0, Integer.MAX_VALUE);
        FLUX_BURN_ENERGY_PER_DAMAGE = b.comment("RF drained per point of bonus damage Flux Burn inflicts. Lower = more damage per drained RF.")
                .defineInRange("fluxBurnEnergyPerDamage", 16_000.0D, 1.0D, 1e12D);
        DRACONIC_ATTACK_AOE_RADIUS_PER_LEVEL = b.comment("Radius (in blocks) around the primary target that Draconic Attack AoE sweeps per level. Tier 4 (Chaotic) at default gives 3-block reach.")
                .defineInRange("draconicAttackAoeRadiusPerLevel", 0.75D, 0.0D, 16.0D);
        DRACONIC_ATTACK_AOE_DAMAGE_FRACTION = b.comment("Fraction of the primary hit's damage dealt to each AoE-swept secondary target.")
                .defineInRange("draconicAttackAoeDamageFraction", 0.8D, 0.0D, 1.0D);
        DRACONIC_DIG_AOE_RADIUS_PER_LEVEL = b.comment("Block-break AoE cube half-extent added per level of Draconic Dig AoE. Tier 4 at default sweeps a 9×9×9 cube around the primary block.")
                .defineInRange("draconicDigAoeRadiusPerLevel", 1, 0, 8);
        b.pop();

        b.comment("Per-trait tuning. These match the original tconevo numbers unless noted.").push("traits");
        VAMPIRIC_LIFESTEAL_PER_LEVEL = b.comment("Fraction of damage dealt healed back per level of the Vampiric trait.")
                .defineInRange("vampiricLifestealPerLevel", 0.20D, 0.0D, 5.0D);
        MORTAL_WOUNDS_HEAL_REDUCTION = b.comment("Healing-received multiplier applied while a victim suffers Mortal Wounds (1.0 = unchanged, 0.0 = no healing).")
                .defineInRange("mortalWoundsHealReduction", 0.25D, 0.0D, 1.0D);
        REJUVENATING_REPAIR_SECONDS = b.comment("Seconds between auto-repair ticks granted by the Rejuvenating trait at level 1.")
                .defineInRange("rejuvenatingRepairSeconds", 8, 1, 600);
        ENERGIZED_CAPACITY_PER_LEVEL = b.comment("RF capacity added by one level of the Energized trait (contributes to TConstruct's max_energy stat).")
                .defineInRange("energizedCapacityPerLevel", 1_600_000, 1, Integer.MAX_VALUE);
        ENERGIZED_ENERGY_COST_TOOLS = b.comment("RF drained from the Energized pool per point of durability damage prevented (higher = more expensive).")
                .defineInRange("energizedEnergyCostTools", 50, 0, Integer.MAX_VALUE);
        PIEZOELECTRIC_RF_PER_DAMAGE = b.comment("RF produced per point of melee damage dealt at level 1 of the Piezoelectric trait; scales linearly with level.")
                .defineInRange("piezoelectricRfPerDamage", 36, 0, Integer.MAX_VALUE);
        PHOTOVOLTAIC_RF_PER_LEVEL_SEC = b.comment("RF generated per second per level of the Photovoltaic trait at full sunlight; scales linearly with sky light.")
                .defineInRange("photovoltaicRfPerLevelPerSecond", 640, 0, Integer.MAX_VALUE);
        FLUXED_CAPACITY_PER_LEVEL = b.comment(
                        "RF capacity added by one level of the Fluxed modifier.",
                        "1.12.2 tconevo read this value from the energy cell used to apply the modifier; this port uses a flat per-level config value instead since Tinkers' 3.x modifier-application flow no longer surfaces the applied item stack.")
                .defineInRange("fluxedCapacityPerLevel", 1_000_000, 1, Integer.MAX_VALUE);
        FLUXED_ENERGY_COST_TOOLS = b.comment("RF drained per point of durability damage prevented by the Fluxed modifier.")
                .defineInRange("fluxedEnergyCostTools", 320, 0, Integer.MAX_VALUE);
        ACCURACY_CHANCE_PER_LEVEL = b.comment("Fraction of accuracy added per level of the Accuracy modifier (0.05 = 5% per level). Consumed against the hypothetical tconevo.evasion attribute once it lands.")
                .defineInRange("accuracyChancePerLevel", 0.05D, 0.0D, 1.0D);
        ACCURACY_ATTR_MAX = b.comment("Upper bound on the tconevo.accuracy entity attribute, above the base value of 1.0. Sum of all sources is clamped here.")
                .defineInRange("accuracyAttrMax", 1.0D, 0.0D, 10.0D);
        b.pop();

        b.comment("Avaritia-specific trait tuning.").push("avaritia");
        CONDENSING_DROP_PROBABILITY = b.comment("Per-level probability of the Condensing trait dropping an Avaritia neutron pile on a successful kill or effective block break.")
                .defineInRange("condensingDropProbability", 0.005D, 0.0D, 1.0D);
        OMNIPOTENCE_HITS_CREATIVE = b.comment("Whether the Omnipotence trait can damage players in creative mode (invuln-bypass path). Mirrors 1.12.2 behaviour.")
                .define("omnipotenceHitsCreative", false);
        b.pop();

        b.comment("Blood Magic-specific trait tuning.").push("bloodmagic");
        BLOODBOUND_TOOL_COST = b.comment("Life Points drained per point of durability damage absorbed by the Bloodbound trait.")
                .defineInRange("bloodboundToolCost", 100, 0, Integer.MAX_VALUE);
        CRYSTALYS_DROP_PROBABILITY = b.comment("Probability that Crystalys drops a Weak Blood Shard on a successful hostile kill.")
                .defineInRange("crystalysDropProbability", 0.05D, 0.0D, 1.0D);
        WILLFUL_DROP_PROBABILITY = b.comment("Probability that Willful drops a Petty Tartaric Gem on a successful kill.")
                .defineInRange("willfulDropProbability", 0.05D, 0.0D, 1.0D);
        WILLFUL_GEM_WILL_AMOUNT = b.comment("Demon Will amount the dropped Petty Gem is pre-filled with.")
                .defineInRange("willfulGemWillAmount", 2.0D, 0.0D, 1000.0D);
        SENTIENT_TIER_STEP = b.comment("Demon Will amount required to advance one Sentient tier. Tier = floor(totalWill / step), capped at 8.")
                .defineInRange("sentientTierStep", 16.0D, 1.0D, 1e6D);
        SENTIENT_DAMAGE_PER_TIER = b.comment("Flat bonus attack damage added per Sentient tier to a held weapon whose wielder has demon will in their inventory.")
                .defineInRange("sentientDamagePerTier", 1.0D, 0.0D, 100.0D);
        b.pop();

        b.comment("Botania-specific trait tuning. All values feed Botania's ManaItemHandler / entity APIs directly.").push("botania");
        AURA_SIPHON_MULTIPLIER = b.comment("Mana dispatched to the wielder's mana-storage items per point of melee damage dealt (Aura Siphon).")
                .defineInRange("auraSiphonMultiplier", 40.0D, 0.0D, 1e6D);
        FAE_VOICE_PIXIE_CHANCE = b.comment("Per-hit chance that Fae Voice summons a Pixie to strike the target.")
                .defineInRange("faeVoicePixieChance", 0.25D, 0.0D, 1.0D);
        FAE_VOICE_PIXIE_DAMAGE = b.comment("Damage dealt by a Fae Voice-summoned Pixie.")
                .defineInRange("faeVoicePixieDamage", 4.0D, 0.0D, 1000.0D);
        MANA_INFUSED_COST = b.comment("Mana drained per point of durability absorbed by Mana-Infused, and per auto-repair tick. Routed through ManaItemHandler.requestManaExactForTool so Botania's own discount/proficiency logic applies.")
                .defineInRange("manaInfusedCost", 60, 0, Integer.MAX_VALUE);
        GAIA_WRATH_MANA_COST = b.comment("Mana charged into the burst fired by Gaia Wrath on left-click-empty. Also used as starting mana / min-mana-loss threshold for the projectile.")
                .defineInRange("gaiaWrathManaCost", 6000, 0, Integer.MAX_VALUE);
        b.pop();

        b.comment("ProjectE-specific trait tuning.").push("projecte");
        ETERNAL_DENSITY_EMC_PER_DAMAGE = b.comment("EMC produced per level of Eternal Density per point of melee damage dealt; distributed into any IItemEmcHolder item in the wielder's inventory.")
                .defineInRange("eternalDensityEmcPerDamage", 4.0D, 0.0D, 1e9D);
        b.pop();

        SPEC = b.build();
    }

    private TConEvoConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "soa_additions-tconstructevo.toml");
    }
}
