package com.soul.soa_additions.nyx;

import com.soul.soa_additions.SoaAdditions;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

/** Forge config spec mirror of Nyx's original Configuration. */
public final class NyxConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENCHANTMENTS;
    public static final ForgeConfigSpec.BooleanValue LUNAR_WATER;
    public static final ForgeConfigSpec.BooleanValue ADD_POTION_EFFECTS;
    public static final ForgeConfigSpec.IntValue ADDITIONAL_MOBS_CHANCE;
    public static final ForgeConfigSpec.BooleanValue LUNAR_EDGE_XP;
    public static final ForgeConfigSpec.BooleanValue DISALLOW_DAY_ENCHANTING;
    public static final ForgeConfigSpec.DoubleValue METEOR_SHARD_GUARDIAN_CHANCE;
    public static final ForgeConfigSpec.BooleanValue FALLING_STARS;
    public static final ForgeConfigSpec.DoubleValue FALLING_STAR_RARITY;
    public static final ForgeConfigSpec.BooleanValue FULL_MOON;
    public static final ForgeConfigSpec.BooleanValue BLOOD_MOON_SLEEPING;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_SPAWN_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue BLOOD_MOON_VANISH;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_SPAWN_RADIUS;
    public static final ForgeConfigSpec.BooleanValue BLOOD_MOON_ON_FULL;
    public static final ForgeConfigSpec.BooleanValue HARVEST_MOON_ON_FULL;
    public static final ForgeConfigSpec.IntValue HARVEST_MOON_GROW_AMOUNT;
    public static final ForgeConfigSpec.IntValue HARVEST_MOON_GROW_INTERVAL;
    public static final ForgeConfigSpec.BooleanValue MOON_EVENT_TINT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> LUNAR_WATER_TICKS;
    public static final ForgeConfigSpec.DoubleValue METEOR_CHANCE;
    public static final ForgeConfigSpec.DoubleValue METEOR_CHANCE_NIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> METEOR_GATE_DIMENSION;
    public static final ForgeConfigSpec.DoubleValue METEOR_CHANCE_AFTER_GATE;
    public static final ForgeConfigSpec.DoubleValue METEOR_CHANCE_AFTER_GATE_NIGHT;
    public static final ForgeConfigSpec.DoubleValue METEOR_CHANCE_STAR_SHOWER;
    public static final ForgeConfigSpec.DoubleValue METEOR_CHANCE_END;
    public static final ForgeConfigSpec.IntValue METEOR_SPAWN_RADIUS;
    public static final ForgeConfigSpec.BooleanValue METEORS;
    public static final ForgeConfigSpec.IntValue METEOR_DISALLOW_RADIUS;
    public static final ForgeConfigSpec.IntValue METEOR_DISALLOW_TIME;
    public static final ForgeConfigSpec.BooleanValue EVENT_NOTIFICATIONS;
    public static final ForgeConfigSpec.IntValue CRYSTAL_DURABILITY;
    public static final ForgeConfigSpec.IntValue HAMMER_DAMAGE;
    public static final ForgeConfigSpec.IntValue HARVEST_MOON_START_NIGHT;
    public static final ForgeConfigSpec.IntValue HARVEST_MOON_GRACE;
    public static final ForgeConfigSpec.DoubleValue HARVEST_MOON_CHANCE;
    public static final ForgeConfigSpec.IntValue HARVEST_MOON_INTERVAL;
    public static final ForgeConfigSpec.IntValue STAR_SHOWER_START_NIGHT;
    public static final ForgeConfigSpec.IntValue STAR_SHOWER_GRACE;
    public static final ForgeConfigSpec.DoubleValue STAR_SHOWER_CHANCE;
    public static final ForgeConfigSpec.IntValue STAR_SHOWER_INTERVAL;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_START_NIGHT;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_GRACE;
    public static final ForgeConfigSpec.DoubleValue BLOOD_MOON_CHANCE;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_INTERVAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_DIMENSIONS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENCHANTING_WHITELIST_DIMENSIONS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_DUPLICATION_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue IS_MOB_DUPLICATION_WHITELIST;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("general");
        ALLOWED_DIMENSIONS = b.defineList("allowedDimensions",
                List.of("minecraft:overworld"),
                o -> o instanceof String);
        LUNAR_WATER = b.define("lunarWater", true);
        METEOR_SHARD_GUARDIAN_CHANCE = b.defineInRange("meteorShardGuardianChance", 0.05, 0.0, 1.0);
        MOB_DUPLICATION_BLACKLIST = b.defineList("mobDuplicationBlacklist", List.of(), o -> o instanceof String);
        IS_MOB_DUPLICATION_WHITELIST = b.define("isMobDuplicationWhitelist", false);
        MOON_EVENT_TINT = b.define("moonEventTint", true);
        LUNAR_WATER_TICKS = b.defineList("lunarWaterTicks",
                List.of(1200, -1, 4800, 4800, 3600, 3600, 2400, 2400, 600, -1),
                o -> o instanceof Integer);
        EVENT_NOTIFICATIONS = b.define("eventNotifications", true);
        b.pop();

        b.push("fullMoon");
        FULL_MOON = b.define("fullMoon", true);
        ADD_POTION_EFFECTS = b.define("addPotionEffects", true);
        ADDITIONAL_MOBS_CHANCE = b.defineInRange("additionalMobsChance", 5, 0, 1000);
        b.pop();

        b.push("enchantments");
        ENCHANTMENTS = b.define("enchantments", true);
        LUNAR_EDGE_XP = b.define("lunarEdgeXp", true);
        DISALLOW_DAY_ENCHANTING = b.define("disallowDayEnchanting", true);
        ENCHANTING_WHITELIST_DIMENSIONS = b.defineList("enchantingWhitelistDimensions",
                List.of("minecraft:the_nether", "minecraft:the_end"),
                o -> o instanceof String);
        b.pop();

        b.push("harvestMoon");
        HARVEST_MOON_ON_FULL = b.define("harvestMoonOnFull", true);
        HARVEST_MOON_GROW_AMOUNT = b.defineInRange("harvestMoonGrowAmount", 15, 0, 100);
        HARVEST_MOON_GROW_INTERVAL = b.defineInRange("harvestMoonGrowInterval", 10, 1, 100);
        HARVEST_MOON_CHANCE = b.defineInRange("harvestMoonChance", 0.05, 0.0, 1.0);
        HARVEST_MOON_START_NIGHT = b.defineInRange("harvestMoonStartNight", 0, 0, 1000);
        HARVEST_MOON_INTERVAL = b.defineInRange("harvestMoonInterval", 0, 0, 1000);
        HARVEST_MOON_GRACE = b.defineInRange("harvestMoonGracePeriod", 0, 0, 1000);
        b.pop();

        b.push("fallingStars");
        FALLING_STARS = b.define("fallingStars", true);
        FALLING_STAR_RARITY = b.defineInRange("fallingStarRarity", 0.01, 0.0, 1.0);
        STAR_SHOWER_CHANCE = b.defineInRange("starShowersChance", 0.05, 0.0, 1.0);
        STAR_SHOWER_START_NIGHT = b.defineInRange("starShowersStartNight", 0, 0, 1000);
        STAR_SHOWER_INTERVAL = b.defineInRange("starShowersInterval", 0, 0, 1000);
        STAR_SHOWER_GRACE = b.defineInRange("starShowersGracePeriod", 0, 0, 1000);
        b.pop();

        b.push("bloodMoon");
        BLOOD_MOON_SLEEPING = b.define("bloodMoonSleeping", false);
        BLOOD_MOON_SPAWN_MULTIPLIER = b.defineInRange("bloodMoonSpawnMultiplier", 2, 1, 1000);
        BLOOD_MOON_VANISH = b.define("bloodMoonVanish", true);
        BLOOD_MOON_SPAWN_RADIUS = b.defineInRange("bloodMoonSpawnRadius", 20, 0, 128);
        BLOOD_MOON_ON_FULL = b.define("bloodMoonOnFull", true);
        BLOOD_MOON_CHANCE = b.defineInRange("bloodMoonChance", 0.05, 0.0, 1.0);
        BLOOD_MOON_START_NIGHT = b.defineInRange("bloodMoonStartNight", 0, 0, 1000);
        BLOOD_MOON_INTERVAL = b.defineInRange("bloodMoonInterval", 0, 0, 1000);
        BLOOD_MOON_GRACE = b.defineInRange("bloodMoonGracePeriod", 0, 0, 1000);
        b.pop();

        b.push("meteors");
        METEORS = b.define("meteors", true);
        METEOR_CHANCE = b.defineInRange("meteorChance", 1.4E-4, 0.0, 1.0);
        METEOR_CHANCE_NIGHT = b.defineInRange("meteorChanceNight", 0.0024, 0.0, 1.0);
        METEOR_GATE_DIMENSION = b.define("meteorGateDimension", "minecraft:the_nether");
        METEOR_CHANCE_AFTER_GATE = b.defineInRange("meteorChanceAfterGate", 2.0E-4, 0.0, 1.0);
        METEOR_CHANCE_AFTER_GATE_NIGHT = b.defineInRange("meteorChanceAfterGateNight", 0.003, 0.0, 1.0);
        METEOR_CHANCE_STAR_SHOWER = b.defineInRange("meteorChanceStarShower", 0.0075, 0.0, 1.0);
        METEOR_CHANCE_END = b.defineInRange("meteorChanceEnd", 0.003, 0.0, 1.0);
        METEOR_SPAWN_RADIUS = b.defineInRange("meteorSpawnRadius", 1000, 1, 10000);
        METEOR_DISALLOW_RADIUS = b.defineInRange("meteorDisallowRadius", 16, 0, 256);
        METEOR_DISALLOW_TIME = b.defineInRange("meteorDisallowTime", 12000, 1, 240000);
        CRYSTAL_DURABILITY = b.defineInRange("crystalDurability", 1000, 1, 1000000);
        HAMMER_DAMAGE = b.defineInRange("hammerDamage", 15, 1, 1000);
        b.pop();

        SPEC = b.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, SoaAdditions.MODID + "-nyx.toml");
    }

    private NyxConfig() {}
}
