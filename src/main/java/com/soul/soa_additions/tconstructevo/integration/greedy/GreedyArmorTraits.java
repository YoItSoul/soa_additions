package com.soul.soa_additions.tconstructevo.integration.greedy;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import com.soul.soa_additions.tconstructevo.TConEvoModifiers.Holder;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Registry holder for the 27 GreedyCraft 1.12 armor traits ported as
 * marker modifiers. See class javadoc on each *Modifier for context.
 */
public final class GreedyArmorTraits {

    public static final Holder<Modifier> WARM =
            TConEvoModifiers.register("warm", WarmModifier::new);

    public static final Holder<Modifier> FORTIFIED =
            TConEvoModifiers.register("fortified", FortifiedModifier::new);

    public static final Holder<Modifier> INFERNO =
            TConEvoModifiers.register("inferno", InfernoModifier::new);

    public static final Holder<Modifier> CRYONIC =
            TConEvoModifiers.register("cryonic", CryonicModifier::new);

    public static final Holder<Modifier> KNOWLEDGEFUL =
            TConEvoModifiers.register("knowledgeful", KnowledgefulModifier::new);

    public static final Holder<Modifier> VISION =
            TConEvoModifiers.register("vision", VisionModifier::new);

    public static final Holder<Modifier> TIDAL_FORCE =
            TConEvoModifiers.register("tidal_force", TidalForceModifier::new);

    public static final Holder<Modifier> SPARTAN =
            TConEvoModifiers.register("spartan", SpartanModifier::new);

    public static final Holder<Modifier> CRYSTAL_FORCE =
            TConEvoModifiers.register("crystal_force", CrystalForceModifier::new);

    public static final Holder<Modifier> SECOND_LIFE =
            TConEvoModifiers.register("second_life", SecondLifeModifier::new);

    public static final Holder<Modifier> PERFECTIONIST =
            TConEvoModifiers.register("perfectionist", PerfectionistModifier::new);

    public static final Holder<Modifier> GAMBLE =
            TConEvoModifiers.register("gamble", GambleModifier::new);

    public static final Holder<Modifier> FIRST_GUARD =
            TConEvoModifiers.register("first_guard", FirstGuardModifier::new);

    public static final Holder<Modifier> LEVELINGDEFENSE =
            TConEvoModifiers.register("levelingdefense", LevelingdefenseModifier::new);

    public static final Holder<Modifier> LUCKY =
            TConEvoModifiers.register("lucky", LuckyModifier::new);

    public static final Holder<Modifier> PURIFYING =
            TConEvoModifiers.register("purifying", PurifyingModifier::new);

    public static final Holder<Modifier> MILKY =
            TConEvoModifiers.register("milky", MilkyModifier::new);

    public static final Holder<Modifier> POOPY =
            TConEvoModifiers.register("poopy", PoopyModifier::new);

    public static final Holder<Modifier> TRUE_DEFENSE =
            TConEvoModifiers.register("true_defense", TrueDefenseModifier::new);

    public static final Holder<Modifier> HOLD_GROUND =
            TConEvoModifiers.register("hold_ground", HoldGroundModifier::new);

    public static final Holder<Modifier> MOTION =
            TConEvoModifiers.register("motion", MotionModifier::new);

    public static final Holder<Modifier> KUNGFU =
            TConEvoModifiers.register("kungfu", KungfuModifier::new);

    public static final Holder<Modifier> THRONY =
            TConEvoModifiers.register("throny", ThronyModifier::new);

    public static final Holder<Modifier> ENDURANCE =
            TConEvoModifiers.register("endurance", EnduranceModifier::new);

    public static final Holder<Modifier> VACCINE =
            TConEvoModifiers.register("vaccine", VaccineModifier::new);

    public static final Holder<Modifier> STRONG_VACCINE =
            TConEvoModifiers.register("strong_vaccine", StrongVaccineModifier::new);

    public static final Holder<Modifier> WARP_DRAIN =
            TConEvoModifiers.register("warp_drain", WarpDrainModifier::new);

    private GreedyArmorTraits() {}

    /** Touch to trigger classload-time registration. */
    public static void bootstrap() {}
}
