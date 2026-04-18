package com.soul.soa_additions.tconstructevo;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.tconstructevo.modifier.core.AccuracyModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.AftershockModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.AstralModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.BattleFurorModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.BlastingModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.CascadingModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.ChainLightningModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.CorruptingModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.CrystallineModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.CullingModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.DeadlyPrecisionModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.ElectricModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.EnergizedModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.ExecutorModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.FertilizingModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.FluxedModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.FootFleetModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.ImpactForceModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.JuggernautModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.LuminiferousModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.ModifiableModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.MortalWoundsModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.OpportunistModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.OverwhelmModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.PhotosyntheticModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.PhotovoltaicModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.PiezoelectricModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.PurgingModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.RejuvenatingModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.RelentlessModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.RuinationModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.SlimeyPinkModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.StaggeringModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.SunderingModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.ThundergodWrathModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.TrueStrikeModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.VampiricModifier;
import com.soul.soa_additions.tconstructevo.modifier.core.WarpingModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierManager;

import java.util.function.Supplier;

/**
 * Central DeferredRegister for every TConstructEvo modifier (the 1.20.1
 * successor to 1.12.2's {@code TraitXxx}/{@code ModifierXxx} classes).
 *
 * <p>TConstruct 3.x registers modifiers through its own {@code MODIFIERS} key,
 * exposed by {@link ModifierManager}. Sub-systems call {@link #register(String, Supplier)}
 * during plugin init; the DeferredRegister is attached to the mod event bus in
 * {@link #register(IEventBus)} so all modifiers are present by {@code RegisterEvent}.</p>
 */
public final class TConEvoModifiers {

    public static final DeferredRegister<Modifier> MODIFIERS =
            DeferredRegister.create(ModifierManager.REGISTRY_KEY, SoaAdditions.MODID);

    // ---------- Core (mod-agnostic) traits ----------
    public static final RegistryObject<VampiricModifier> VAMPIRIC =
            register("vampiric", VampiricModifier::new);
    public static final RegistryObject<ExecutorModifier> EXECUTOR =
            register("executor", ExecutorModifier::new);
    public static final RegistryObject<CullingModifier> CULLING =
            register("culling", CullingModifier::new);
    public static final RegistryObject<JuggernautModifier> JUGGERNAUT =
            register("juggernaut", JuggernautModifier::new);
    public static final RegistryObject<OverwhelmModifier> OVERWHELM =
            register("overwhelm", OverwhelmModifier::new);
    public static final RegistryObject<OpportunistModifier> OPPORTUNIST =
            register("opportunist", OpportunistModifier::new);
    public static final RegistryObject<ImpactForceModifier> IMPACT_FORCE =
            register("impact_force", ImpactForceModifier::new);
    public static final RegistryObject<CrystallineModifier> CRYSTALLINE =
            register("crystalline", CrystallineModifier::new);
    public static final RegistryObject<SunderingModifier> SUNDERING =
            register("sundering", SunderingModifier::new);
    public static final RegistryObject<CorruptingModifier> CORRUPTING =
            register("corrupting", CorruptingModifier::new);
    public static final RegistryObject<BattleFurorModifier> BATTLE_FUROR =
            register("battle_furor", BattleFurorModifier::new);
    public static final RegistryObject<LuminiferousModifier> LUMINIFEROUS =
            register("luminiferous", LuminiferousModifier::new);
    public static final RegistryObject<FootFleetModifier> FOOT_FLEET =
            register("foot_fleet", FootFleetModifier::new);
    public static final RegistryObject<MortalWoundsModifier> MORTAL_WOUNDS =
            register("mortal_wounds", MortalWoundsModifier::new);
    public static final RegistryObject<RejuvenatingModifier> REJUVENATING =
            register("rejuvenating", RejuvenatingModifier::new);
    public static final RegistryObject<TrueStrikeModifier> TRUE_STRIKE =
            register("true_strike", TrueStrikeModifier::new);
    public static final RegistryObject<RuinationModifier> RUINATION =
            register("ruination", RuinationModifier::new);
    public static final RegistryObject<DeadlyPrecisionModifier> DEADLY_PRECISION =
            register("deadly_precision", DeadlyPrecisionModifier::new);
    public static final RegistryObject<RelentlessModifier> RELENTLESS =
            register("relentless", RelentlessModifier::new);
    public static final RegistryObject<ThundergodWrathModifier> THUNDERGOD_WRATH =
            register("thundergod_wrath", ThundergodWrathModifier::new);
    public static final RegistryObject<PurgingModifier> PURGING =
            register("purging", PurgingModifier::new);
    public static final RegistryObject<StaggeringModifier> STAGGERING =
            register("staggering", StaggeringModifier::new);
    public static final RegistryObject<BlastingModifier> BLASTING =
            register("blasting", BlastingModifier::new);
    public static final RegistryObject<ChainLightningModifier> CHAIN_LIGHTNING =
            register("chain_lightning", ChainLightningModifier::new);
    public static final RegistryObject<AftershockModifier> AFTERSHOCK =
            register("aftershock", AftershockModifier::new);
    public static final RegistryObject<PhotosyntheticModifier> PHOTOSYNTHETIC =
            register("photosynthetic", PhotosyntheticModifier::new);
    public static final RegistryObject<FertilizingModifier> FERTILIZING =
            register("fertilizing", FertilizingModifier::new);
    public static final RegistryObject<CascadingModifier> CASCADING =
            register("cascading", CascadingModifier::new);
    public static final RegistryObject<ModifiableModifier> MODIFIABLE =
            register("modifiable", ModifiableModifier::new);
    public static final RegistryObject<EnergizedModifier> ENERGIZED =
            register("energized", EnergizedModifier::new);
    public static final RegistryObject<PiezoelectricModifier> PIEZOELECTRIC =
            register("piezoelectric", PiezoelectricModifier::new);
    public static final RegistryObject<PhotovoltaicModifier> PHOTOVOLTAIC =
            register("photovoltaic", PhotovoltaicModifier::new);
    public static final RegistryObject<FluxedModifier> FLUXED =
            register("fluxed", FluxedModifier::new);
    public static final RegistryObject<AccuracyModifier> ACCURACY =
            register("accuracy", AccuracyModifier::new);

    // Marker traits for materials whose source mods are absent from 1.20.1 but
    // still need their material JSONs to load. Behaviour is a no-op until the
    // respective mod is available and dedicated behaviour is wired in.
    public static final RegistryObject<AstralModifier> ASTRAL =
            register("astral", AstralModifier::new);
    public static final RegistryObject<ElectricModifier> ELECTRIC =
            register("electric", ElectricModifier::new);
    public static final RegistryObject<WarpingModifier> WARPING =
            register("warping", WarpingModifier::new);
    public static final RegistryObject<SlimeyPinkModifier> SLIMEY_PINK =
            register("slimey_pink", SlimeyPinkModifier::new);

    private TConEvoModifiers() {}

    public static <M extends Modifier> RegistryObject<M> register(String name, Supplier<M> factory) {
        return MODIFIERS.register("tconevo/" + name, factory);
    }

    public static void register(IEventBus modEventBus) {
        MODIFIERS.register(modEventBus);
    }
}
