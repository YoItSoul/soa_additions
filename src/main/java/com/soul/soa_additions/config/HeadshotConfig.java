package com.soul.soa_additions.config;

import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Server-side tunables for {@link com.soul.soa_additions.combat.HeadshotHandler}.
 *
 * <p>Helmet protection is derived dynamically from the armor item's own
 * {@code getDefense()} + {@code getMaterial().getToughness()} rather than a
 * hand-maintained list. Modpacks with hundreds of helmets get sensible
 * protection out of the box — a helmet that mod authors balanced to be strong
 * against normal damage is automatically strong against headshots too.</p>
 */
public final class HeadshotConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue REDUCTION_PER_ARMOR;
    public static final ForgeConfigSpec.DoubleValue REDUCTION_PER_TOUGHNESS;
    public static final ForgeConfigSpec.DoubleValue MAX_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue DURABILITY_MULT;
    public static final ForgeConfigSpec.IntValue NO_HELMET_BLINDNESS_SECONDS;
    public static final ForgeConfigSpec.IntValue NO_HELMET_BLINDNESS_AMPLIFIER;
    public static final ForgeConfigSpec.DoubleValue HEADSHOT_DING_VOLUME;
    public static final ForgeConfigSpec.DoubleValue HEADSHOT_DING_PITCH;
    public static final ForgeConfigSpec.DoubleValue THUMP_VOLUME;
    public static final ForgeConfigSpec.DoubleValue THUMP_PITCH;

    public record Profile(float damageTakenMult, float durabilityMult) {}

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("headshot");
        builder.comment("Headshot mechanics tuning.");

        REDUCTION_PER_ARMOR = builder
                .comment("Headshot damage reduction contributed per armor point on the helmet.")
                .defineInRange("reductionPerArmorPoint", 0.10D, 0.0D, 1.0D);
        REDUCTION_PER_TOUGHNESS = builder
                .comment("Headshot damage reduction contributed per toughness point on the helmet.")
                .defineInRange("reductionPerToughnessPoint", 0.08D, 0.0D, 1.0D);
        MAX_REDUCTION = builder
                .comment(
                        "Hard cap on a helmet's headshot reduction fraction. 0.85 leaves at least",
                        "15% of the raw headshot damage getting through regardless of how stacked",
                        "the helmet's stats are."
                )
                .defineInRange("maxReduction", 0.85D, 0.0D, 1.0D);

        DURABILITY_MULT = builder
                .comment(
                        "Durability points the helmet loses per 1.0 of raw headshot damage. The same",
                        "value applies to every helmet — cheaper helmets with smaller max-durability",
                        "pools naturally break faster than tougher ones."
                )
                .defineInRange("durabilityMultiplier", 1.5D, 0.0D, 20.0D);

        NO_HELMET_BLINDNESS_SECONDS = builder
                .comment("Seconds of blindness applied when a player is headshot without a helmet.")
                .defineInRange("noHelmetBlindnessSeconds", 2, 0, 60);
        NO_HELMET_BLINDNESS_AMPLIFIER = builder
                .comment("Blindness amplifier applied when a player is headshot without a helmet. 0 = level I.")
                .defineInRange("noHelmetBlindnessAmplifier", 0, 0, 4);

        HEADSHOT_DING_VOLUME = builder
                .comment("Volume of the attacker-only 'ding' sound on a successful headshot.")
                .defineInRange("dingVolume", 0.25D, 0.0D, 1.0D);
        HEADSHOT_DING_PITCH = builder
                .comment("Pitch of the attacker-only 'ding' sound on a successful headshot.")
                .defineInRange("dingPitch", 1.9D, 0.5D, 2.0D);

        THUMP_VOLUME = builder
                .comment("Volume of the low thump played when a player is headshot without a helmet.")
                .defineInRange("thumpVolume", 0.9D, 0.0D, 1.0D);
        THUMP_PITCH = builder
                .comment("Pitch of the low thump played when a player is headshot without a helmet.")
                .defineInRange("thumpPitch", 0.5D, 0.5D, 2.0D);

        builder.pop();
        SPEC = builder.build();
    }

    private HeadshotConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "soa_additions-headshot.toml");
    }

    /** Derive a protection profile from the helmet's own armor/toughness
     *  stats, clamped by {@link #MAX_REDUCTION}. The body-shot floor is
     *  enforced at application time, not here. */
    public static Profile profileFor(ArmorItem helmet) {
        int defense = helmet.getDefense();
        float toughness = helmet.getMaterial().getToughness();
        double reduction = Math.min(
                MAX_REDUCTION.get(),
                defense * REDUCTION_PER_ARMOR.get()
                        + toughness * REDUCTION_PER_TOUGHNESS.get()
        );
        reduction = Math.max(0.0D, reduction);
        return new Profile((float) (1.0D - reduction), DURABILITY_MULT.get().floatValue());
    }
}
