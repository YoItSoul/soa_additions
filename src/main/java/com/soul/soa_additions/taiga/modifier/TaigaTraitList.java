package com.soul.soa_additions.taiga.modifier;

import com.soul.soa_additions.taiga.modifier.core.*;

/**
 * Binds the 35 ported TAIGA custom-trait classes to {@link TaigaModifiers} ids.
 * Registration actually fires during {@code ModifierRegistrationEvent}; this
 * class only queues factories.
 *
 * <p>Ids match 1.12 TAIGA trait names so existing material JSONs keyed to
 * {@code taiga:<trait>} resolve without rename.</p>
 */
public final class TaigaTraitList {

    public static final TaigaModifiers.Holder<BrightModifier>      BRIGHT       = TaigaModifiers.register("bright",      BrightModifier::new);
    public static final TaigaModifiers.Holder<DarkModifier>        DARK         = TaigaModifiers.register("dark",        DarkModifier::new);
    public static final TaigaModifiers.Holder<HeroicModifier>      HEROIC       = TaigaModifiers.register("heroic",      HeroicModifier::new);
    public static final TaigaModifiers.Holder<SoulEaterModifier>   SOULEATER    = TaigaModifiers.register("souleater",   SoulEaterModifier::new);
    public static final TaigaModifiers.Holder<CongenialModifier>   CONGENIAL    = TaigaModifiers.register("congenial",   CongenialModifier::new);
    public static final TaigaModifiers.Holder<BerserkModifier>     BERSERK      = TaigaModifiers.register("berserk",     BerserkModifier::new);

    public static final TaigaModifiers.Holder<ResonanceModifier>   RESONANCE    = TaigaModifiers.register("resonance",   ResonanceModifier::new);
    public static final TaigaModifiers.Holder<UnstableModifier>    UNSTABLE     = TaigaModifiers.register("unstable",    UnstableModifier::new);
    public static final TaigaModifiers.Holder<BlindModifier>       BLIND        = TaigaModifiers.register("blind",       BlindModifier::new);
    public static final TaigaModifiers.Holder<GlimmerModifier>     GLIMMER      = TaigaModifiers.register("glimmer",     GlimmerModifier::new);
    public static final TaigaModifiers.Holder<ArcaneModifier>      ARCANE       = TaigaModifiers.register("arcane",      ArcaneModifier::new);
    public static final TaigaModifiers.Holder<HollowModifier>      HOLLOW       = TaigaModifiers.register("hollow",      HollowModifier::new);
    public static final TaigaModifiers.Holder<CurvatureModifier>   CURVATURE    = TaigaModifiers.register("curvature",   CurvatureModifier::new);
    public static final TaigaModifiers.Holder<PortedModifier>      PORTED       = TaigaModifiers.register("ported",      PortedModifier::new);

    public static final TaigaModifiers.Holder<CascadeModifier>     CASCADE      = TaigaModifiers.register("cascade",     CascadeModifier::new);
    public static final TaigaModifiers.Holder<FractureModifier>    FRACTURE     = TaigaModifiers.register("fracture",    FractureModifier::new);
    public static final TaigaModifiers.Holder<FragileModifier>     FRAGILE      = TaigaModifiers.register("fragile",     FragileModifier::new);
    public static final TaigaModifiers.Holder<MutateModifier>      MUTATE       = TaigaModifiers.register("mutate",      MutateModifier::new);
    public static final TaigaModifiers.Holder<MeltingModifier>     MELTING      = TaigaModifiers.register("melting",     MeltingModifier::new);

    public static final TaigaModifiers.Holder<PulverizingModifier> PULVERIZING  = TaigaModifiers.register("pulverizing", PulverizingModifier::new);
    public static final TaigaModifiers.Holder<SuperHeavyModifier>  SUPERHEAVY   = TaigaModifiers.register("superheavy",  SuperHeavyModifier::new);
    public static final TaigaModifiers.Holder<SoftyModifier>       SOFTY        = TaigaModifiers.register("softy",       SoftyModifier::new);

    public static final TaigaModifiers.Holder<CursedModifier>      CURSED       = TaigaModifiers.register("cursed",      CursedModifier::new);
    public static final TaigaModifiers.Holder<NatureBoundModifier> NATUREBOUND  = TaigaModifiers.register("naturebound", NatureBoundModifier::new);
    public static final TaigaModifiers.Holder<DecayModifier>       DECAY        = TaigaModifiers.register("decay",       DecayModifier::new);

    public static final TaigaModifiers.Holder<CrushingModifier>    CRUSHING     = TaigaModifiers.register("crushing",    CrushingModifier::new);
    public static final TaigaModifiers.Holder<AnalysingModifier>   ANALYSING    = TaigaModifiers.register("analysing",   AnalysingModifier::new);
    public static final TaigaModifiers.Holder<GarishlyModifier>    GARISHLY     = TaigaModifiers.register("garishly",    GarishlyModifier::new);
    public static final TaigaModifiers.Holder<DiffuseTaigaModifier> DIFFUSE     = TaigaModifiers.register("diffuse",     DiffuseTaigaModifier::new);

    public static final TaigaModifiers.Holder<SlaughteringModifier> SLAUGHTERING = TaigaModifiers.register("slaughtering", SlaughteringModifier::new);
    public static final TaigaModifiers.Holder<DissolvingModifier>  DISSOLVING   = TaigaModifiers.register("dissolving",  DissolvingModifier::new);
    public static final TaigaModifiers.Holder<ReviveModifier>      REVIVE       = TaigaModifiers.register("revive",      ReviveModifier::new);

    public static final TaigaModifiers.Holder<CatcherModifier>     CATCHER      = TaigaModifiers.register("catcher",     CatcherModifier::new);
    public static final TaigaModifiers.Holder<TantrumModifier>     TANTRUM      = TaigaModifiers.register("tantrum",     TantrumModifier::new);
    public static final TaigaModifiers.Holder<WhirlModifier>       WHIRL        = TaigaModifiers.register("whirl",       WhirlModifier::new);

    private TaigaTraitList() {}

    public static void bootstrap() {
        // static init of the fields above enqueues the 35 holders
    }
}
