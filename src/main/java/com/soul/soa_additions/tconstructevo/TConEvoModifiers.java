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
import com.soul.soa_additions.tconstructevo.modifier.core.DiffuseModifier;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Central registry for every TConstructEvo modifier (the 1.20.1 successor to
 * 1.12.2's {@code TraitXxx}/{@code ModifierXxx} classes).
 *
 * <p>TConstruct 3.x does <b>not</b> expose its modifier registry as a Forge
 * {@code ForgeRegistry}, so {@link net.minecraftforge.registries.DeferredRegister}
 * crashes on mod construction with "Unable to find registry with key
 * tconstruct:modifiers". Instead, modifiers are added during
 * {@link ModifierManager.ModifierRegistrationEvent} — a mod-bus event fired from
 * {@code ModifierManager.fireRegistryEvent()} during {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}.
 * Each sub-system calls {@link #register(String, Supplier)} during plugin init
 * to queue a pending entry; {@link #register(IEventBus)} attaches the listener
 * that drains the queue by calling {@code event.registerStatic(...)}.</p>
 */
public final class TConEvoModifiers {

    /** Queued entries awaiting {@link ModifierManager.ModifierRegistrationEvent}. */
    private static final List<Holder<?>> PENDING = new ArrayList<>();

    // ---------- Core (mod-agnostic) traits ----------
    public static final Holder<VampiricModifier> VAMPIRIC =
            register("vampiric", VampiricModifier::new);
    public static final Holder<ExecutorModifier> EXECUTOR =
            register("executor", ExecutorModifier::new);
    public static final Holder<CullingModifier> CULLING =
            register("culling", CullingModifier::new);
    public static final Holder<JuggernautModifier> JUGGERNAUT =
            register("juggernaut", JuggernautModifier::new);
    public static final Holder<OverwhelmModifier> OVERWHELM =
            register("overwhelm", OverwhelmModifier::new);
    public static final Holder<OpportunistModifier> OPPORTUNIST =
            register("opportunist", OpportunistModifier::new);
    public static final Holder<ImpactForceModifier> IMPACT_FORCE =
            register("impact_force", ImpactForceModifier::new);
    public static final Holder<CrystallineModifier> CRYSTALLINE =
            register("crystalline", CrystallineModifier::new);
    public static final Holder<SunderingModifier> SUNDERING =
            register("sundering", SunderingModifier::new);
    public static final Holder<CorruptingModifier> CORRUPTING =
            register("corrupting", CorruptingModifier::new);
    public static final Holder<BattleFurorModifier> BATTLE_FUROR =
            register("battle_furor", BattleFurorModifier::new);
    public static final Holder<LuminiferousModifier> LUMINIFEROUS =
            register("luminiferous", LuminiferousModifier::new);
    public static final Holder<FootFleetModifier> FOOT_FLEET =
            register("foot_fleet", FootFleetModifier::new);
    public static final Holder<MortalWoundsModifier> MORTAL_WOUNDS =
            register("mortal_wounds", MortalWoundsModifier::new);
    public static final Holder<RejuvenatingModifier> REJUVENATING =
            register("rejuvenating", RejuvenatingModifier::new);
    public static final Holder<TrueStrikeModifier> TRUE_STRIKE =
            register("true_strike", TrueStrikeModifier::new);
    public static final Holder<RuinationModifier> RUINATION =
            register("ruination", RuinationModifier::new);
    public static final Holder<DeadlyPrecisionModifier> DEADLY_PRECISION =
            register("deadly_precision", DeadlyPrecisionModifier::new);
    public static final Holder<RelentlessModifier> RELENTLESS =
            register("relentless", RelentlessModifier::new);
    public static final Holder<ThundergodWrathModifier> THUNDERGOD_WRATH =
            register("thundergod_wrath", ThundergodWrathModifier::new);
    public static final Holder<PurgingModifier> PURGING =
            register("purging", PurgingModifier::new);
    public static final Holder<StaggeringModifier> STAGGERING =
            register("staggering", StaggeringModifier::new);
    public static final Holder<BlastingModifier> BLASTING =
            register("blasting", BlastingModifier::new);
    public static final Holder<ChainLightningModifier> CHAIN_LIGHTNING =
            register("chain_lightning", ChainLightningModifier::new);
    public static final Holder<AftershockModifier> AFTERSHOCK =
            register("aftershock", AftershockModifier::new);
    public static final Holder<PhotosyntheticModifier> PHOTOSYNTHETIC =
            register("photosynthetic", PhotosyntheticModifier::new);
    public static final Holder<FertilizingModifier> FERTILIZING =
            register("fertilizing", FertilizingModifier::new);
    public static final Holder<CascadingModifier> CASCADING =
            register("cascading", CascadingModifier::new);
    public static final Holder<ModifiableModifier> MODIFIABLE =
            register("modifiable", ModifiableModifier::new);
    public static final Holder<EnergizedModifier> ENERGIZED =
            register("energized", EnergizedModifier::new);
    public static final Holder<PiezoelectricModifier> PIEZOELECTRIC =
            register("piezoelectric", PiezoelectricModifier::new);
    public static final Holder<PhotovoltaicModifier> PHOTOVOLTAIC =
            register("photovoltaic", PhotovoltaicModifier::new);
    public static final Holder<FluxedModifier> FLUXED =
            register("fluxed", FluxedModifier::new);
    public static final Holder<AccuracyModifier> ACCURACY =
            register("accuracy", AccuracyModifier::new);
    public static final Holder<DiffuseModifier> DIFFUSE =
            register("diffuse", DiffuseModifier::new);

    // Marker traits for materials whose source mods are absent from 1.20.1 but
    // still need their material JSONs to load. Behaviour is a no-op until the
    // respective mod is available and dedicated behaviour is wired in.
    public static final Holder<AstralModifier> ASTRAL =
            register("astral", AstralModifier::new);
    public static final Holder<ElectricModifier> ELECTRIC =
            register("electric", ElectricModifier::new);
    public static final Holder<WarpingModifier> WARPING =
            register("warping", WarpingModifier::new);
    public static final Holder<SlimeyPinkModifier> SLIMEY_PINK =
            register("slimey_pink", SlimeyPinkModifier::new);

    private TConEvoModifiers() {}

    public static <M extends Modifier> Holder<M> register(String name, Supplier<M> factory) {
        Holder<M> holder = new Holder<>(
                new ResourceLocation("tconevo",  name), factory);
        PENDING.add(holder);
        return holder;
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(TConEvoModifiers::onModifierRegistration);
    }

    private static void onModifierRegistration(ModifierManager.ModifierRegistrationEvent event) {
        for (Holder<?> holder : PENDING) {
            holder.bindAndRegister(event);
        }
    }

    /**
     * Holds a pending modifier entry and, after
     * {@link ModifierManager.ModifierRegistrationEvent} fires, caches the
     * registered instance for {@link #get()}. Mimics the subset of
     * {@link net.minecraftforge.registries.RegistryObject} that callers
     * actually need ({@code get}, {@code getId}).
     */
    public static final class Holder<M extends Modifier> implements Supplier<M> {
        private final ResourceLocation id;
        private final Supplier<M> factory;
        private M instance;

        private Holder(ResourceLocation id, Supplier<M> factory) {
            this.id = id;
            this.factory = factory;
        }

        public ResourceLocation getId() { return id; }

        @Override public M get() {
            if (instance == null) {
                throw new IllegalStateException(
                        "Modifier " + id + " accessed before ModifierRegistrationEvent fired");
            }
            return instance;
        }

        @SuppressWarnings("unchecked")
        private void bindAndRegister(ModifierManager.ModifierRegistrationEvent event) {
            instance = factory.get();
            event.registerStatic(new ModifierId(id), instance);
        }
    }
}
