package com.soul.soa_additions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Server-side tunables for {@link com.soul.soa_additions.combat.HeadshotHandler}.
 *
 * <p>Defaults are GreedyCraft's shipped {@code config/iblis_headshots.cfg}
 * values, not the iblis mod's own defaults — GC halved the headshot and headgear
 * multipliers (4.0 -> 2.0), dropped body shots to 0.8, shortened the melee gate
 * to 12 blocks and turned particles off. Changing these changes combat feel, so
 * they live in config rather than as constants.</p>
 *
 * <p>Helmet protection is <em>not</em> configured here — it is derived from each
 * helmet's own armour attribute by
 * {@link com.soul.soa_additions.combat.HeadgearProtection}.</p>
 */
public final class HeadshotConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue HEADSHOT_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BODYSHOT_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue HEADGEAR_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MELEE_MIN_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue PLAYERS_HAVE_NO_HEADS;
    public static final ForgeConfigSpec.BooleanValue SHOW_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue PLAY_HIT_SOUND;
    public static final ForgeConfigSpec.DoubleValue HIT_SOUND_VOLUME;
    public static final ForgeConfigSpec.DoubleValue HIT_SOUND_PITCH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("headshot");
        builder.comment("Headshot mechanics tuning. Defaults match GreedyCraft's iblis-headshots config.");

        HEADSHOT_DAMAGE_MULTIPLIER = builder
                .comment(
                        "Damage multiplier for an unprotected headshot. A helmet scales this back",
                        "toward 1.0 in proportion to its headshot protection, so a headshot can",
                        "never do less than a body shot no matter how good the helmet is."
                )
                .defineInRange("headshotDamageMultiplier", 2.0D, 0.0D, 1_000_000.0D);

        BODYSHOT_DAMAGE_MULTIPLIER = builder
                .comment(
                        "Damage multiplier applied to a projectile hit that misses the head.",
                        "Below 1.0 this is what makes headshots feel worth aiming for."
                )
                .defineInRange("bodyshotDamageMultiplier", 0.8D, 0.0D, 1_000_000.0D);

        HEADGEAR_DAMAGE_MULTIPLIER = builder
                .comment(
                        "Durability the helmet loses per point of incoming damage on a headshot,",
                        "randomised between 1.0x and 1.5x of this value on each hit."
                )
                .defineInRange("headgearDamageMultiplier", 2.0D, 0.0D, 1_000_000.0D);

        MELEE_MIN_DISTANCE = builder
                .comment(
                        "Minimum distance in blocks at which a non-projectile attack can headshot.",
                        "At the default of 12 ordinary melee never headshots; the path exists for",
                        "hitscan-style ranged weapons that damage directly instead of firing an entity."
                )
                .defineInRange("meleeHeadshotMinDistance", 12.0D, 0.0D, 1_000_000.0D);

        PLAYERS_HAVE_NO_HEADS = builder
                .comment("If true, players can never be headshot. Mobs still can.")
                .define("playersHaveNoHeads", false);

        builder.comment("Cosmetic feedback. GreedyCraft ships headshots silent and invisible; these default to off.");
        SHOW_PARTICLES = builder
                .comment("Spawn crit particles at the head on a successful headshot.")
                .define("showParticles", false);
        PLAY_HIT_SOUND = builder
                .comment("Play an attacker-only confirmation sound on a successful headshot.")
                .define("playHitSound", false);
        HIT_SOUND_VOLUME = builder
                .comment("Volume of the attacker-only confirmation sound.")
                .defineInRange("hitSoundVolume", 0.25D, 0.0D, 1.0D);
        HIT_SOUND_PITCH = builder
                .comment("Pitch of the attacker-only confirmation sound.")
                .defineInRange("hitSoundPitch", 1.9D, 0.5D, 2.0D);

        builder.pop();
        SPEC = builder.build();
    }

    private HeadshotConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "soa_additions-headshot.toml");
    }

    /** Melee gate as a squared distance, so the hot path skips a sqrt. */
    public static double meleeMinDistanceSq() {
        double d = MELEE_MIN_DISTANCE.get();
        return d * d;
    }
}
