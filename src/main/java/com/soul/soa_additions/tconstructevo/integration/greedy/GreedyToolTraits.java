package com.soul.soa_additions.tconstructevo.integration.greedy;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import com.soul.soa_additions.tconstructevo.TConEvoModifiers.Holder;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Registry holder for the 31 GreedyCraft 1.12 tool traits ported as
 * marker modifiers. See class javadoc on each *Modifier for context.
 */
public final class GreedyToolTraits {

    // Modifiers also registered by GreedyArmorTraits (apply to both tools and
    // armor) — alias here to avoid the "duplicate static modifier" registry
    // crash. Single registration site lives in GreedyArmorTraits.
    public static final Holder<Modifier> POOPY = GreedyArmorTraits.POOPY;

    public static final Holder<Modifier> PINKY =
            TConEvoModifiers.register("pinky", PinkyModifier::new);

    public static final Holder<Modifier> COTLIFESTEAL =
            TConEvoModifiers.register("cotlifesteal", CotlifestealModifier::new);

    public static final Holder<Modifier> SUPERKNOCKBACK =
            TConEvoModifiers.register("superknockback", SuperknockbackModifier::new);

    public static final Holder<Modifier> GAMBLE = GreedyArmorTraits.GAMBLE;

    public static final Holder<Modifier> RAGING =
            TConEvoModifiers.register("raging", RagingModifier::new);

    public static final Holder<Modifier> LEVELINGDAMAGE =
            TConEvoModifiers.register("levelingdamage", LevelingdamageModifier::new);

    public static final Holder<Modifier> LIGHTNING =
            TConEvoModifiers.register("lightning", LightningModifier::new);

    public static final Holder<Modifier> VISION = GreedyArmorTraits.VISION;

    public static final Holder<Modifier> FORTIFIED = GreedyArmorTraits.FORTIFIED;

    public static final Holder<Modifier> RELIABLE =
            TConEvoModifiers.register("reliable", ReliableModifier::new);

    public static final Holder<Modifier> SACRIFICIAL =
            TConEvoModifiers.register("sacrificial", SacrificialModifier::new);

    public static final Holder<Modifier> HALLOWEEN =
            TConEvoModifiers.register("halloween", HalloweenModifier::new);

    public static final Holder<Modifier> MILKY = GreedyArmorTraits.MILKY;

    public static final Holder<Modifier> GIANTSLAYER =
            TConEvoModifiers.register("giantslayer", GiantslayerModifier::new);

    public static final Holder<Modifier> CRYSTAL_FORCE = GreedyArmorTraits.CRYSTAL_FORCE;

    public static final Holder<Modifier> SPARTAN = GreedyArmorTraits.SPARTAN;

    public static final Holder<Modifier> KNOWLEDGEFUL = GreedyArmorTraits.KNOWLEDGEFUL;

    public static final Holder<Modifier> HOLD_GROUND = GreedyArmorTraits.HOLD_GROUND;

    public static final Holder<Modifier> MOTION = GreedyArmorTraits.MOTION;

    public static final Holder<Modifier> EXECUTIONER =
            TConEvoModifiers.register("executioner", ExecutionerModifier::new);

    public static final Holder<Modifier> PENETRATION =
            TConEvoModifiers.register("penetration", PenetrationModifier::new);

    public static final Holder<Modifier> THRONY = GreedyArmorTraits.THRONY;

    public static final Holder<Modifier> BANE_OF_NIGHT =
            TConEvoModifiers.register("bane_of_night", BaneOfNightModifier::new);

    public static final Holder<Modifier> BLOODLUST =
            TConEvoModifiers.register("bloodlust", BloodlustModifier::new);

    public static final Holder<Modifier> ASSASSIN =
            TConEvoModifiers.register("assassin", AssassinModifier::new);

    public static final Holder<Modifier> MADNESS =
            TConEvoModifiers.register("madness", MadnessModifier::new);

    public static final Holder<Modifier> MATTER_CONDENSING1 =
            TConEvoModifiers.register("matter_condensing1", MatterCondensing1Modifier::new);

    public static final Holder<Modifier> MATTER_CONDENSING2 =
            TConEvoModifiers.register("matter_condensing2", MatterCondensing2Modifier::new);

    public static final Holder<Modifier> PERFECTIONIST = GreedyArmorTraits.PERFECTIONIST;

    public static final Holder<Modifier> TIDAL_FORCE = GreedyArmorTraits.TIDAL_FORCE;

    private GreedyToolTraits() {}

    /** Touch to trigger classload-time registration. */
    public static void bootstrap() {}
}
