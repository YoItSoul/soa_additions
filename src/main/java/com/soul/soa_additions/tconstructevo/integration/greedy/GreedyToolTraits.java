package com.soul.soa_additions.tconstructevo.integration.greedy;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import com.soul.soa_additions.tconstructevo.TConEvoModifiers.Holder;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Registry holder for the 31 GreedyCraft 1.12 tool traits ported as
 * marker modifiers. See class javadoc on each *Modifier for context.
 */
public final class GreedyToolTraits {

    public static final Holder<Modifier> POOPY =
            TConEvoModifiers.register("poopy", PoopyModifier::new);

    public static final Holder<Modifier> PINKY =
            TConEvoModifiers.register("pinky", PinkyModifier::new);

    public static final Holder<Modifier> COTLIFESTEAL =
            TConEvoModifiers.register("cotlifesteal", CotlifestealModifier::new);

    public static final Holder<Modifier> SUPERKNOCKBACK =
            TConEvoModifiers.register("superknockback", SuperknockbackModifier::new);

    public static final Holder<Modifier> GAMBLE =
            TConEvoModifiers.register("gamble", GambleModifier::new);

    public static final Holder<Modifier> RAGING =
            TConEvoModifiers.register("raging", RagingModifier::new);

    public static final Holder<Modifier> LEVELINGDAMAGE =
            TConEvoModifiers.register("levelingdamage", LevelingdamageModifier::new);

    public static final Holder<Modifier> LIGHTNING =
            TConEvoModifiers.register("lightning", LightningModifier::new);

    public static final Holder<Modifier> VISION =
            TConEvoModifiers.register("vision", VisionModifier::new);

    public static final Holder<Modifier> FORTIFIED =
            TConEvoModifiers.register("fortified", FortifiedModifier::new);

    public static final Holder<Modifier> RELIABLE =
            TConEvoModifiers.register("reliable", ReliableModifier::new);

    public static final Holder<Modifier> SACRIFICIAL =
            TConEvoModifiers.register("sacrificial", SacrificialModifier::new);

    public static final Holder<Modifier> HALLOWEEN =
            TConEvoModifiers.register("halloween", HalloweenModifier::new);

    public static final Holder<Modifier> MILKY =
            TConEvoModifiers.register("milky", MilkyModifier::new);

    public static final Holder<Modifier> GIANTSLAYER =
            TConEvoModifiers.register("giantslayer", GiantslayerModifier::new);

    public static final Holder<Modifier> CRYSTAL_FORCE =
            TConEvoModifiers.register("crystal_force", CrystalForceModifier::new);

    public static final Holder<Modifier> SPARTAN =
            TConEvoModifiers.register("spartan", SpartanModifier::new);

    public static final Holder<Modifier> KNOWLEDGEFUL =
            TConEvoModifiers.register("knowledgeful", KnowledgefulModifier::new);

    public static final Holder<Modifier> HOLD_GROUND =
            TConEvoModifiers.register("hold_ground", HoldGroundModifier::new);

    public static final Holder<Modifier> MOTION =
            TConEvoModifiers.register("motion", MotionModifier::new);

    public static final Holder<Modifier> EXECUTIONER =
            TConEvoModifiers.register("executioner", ExecutionerModifier::new);

    public static final Holder<Modifier> PENETRATION =
            TConEvoModifiers.register("penetration", PenetrationModifier::new);

    public static final Holder<Modifier> THRONY =
            TConEvoModifiers.register("throny", ThronyModifier::new);

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

    public static final Holder<Modifier> PERFECTIONIST =
            TConEvoModifiers.register("perfectionist", PerfectionistModifier::new);

    public static final Holder<Modifier> TIDAL_FORCE =
            TConEvoModifiers.register("tidal_force", TidalForceModifier::new);

    private GreedyToolTraits() {}

    /** Touch to trigger classload-time registration. */
    public static void bootstrap() {}
}
