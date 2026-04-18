package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * RegistryObject holders for the Draconic-themed TConstruct modifiers.
 * Concrete modifier classes live under the {@code modifier/} sub-package;
 * this class just pins the registry entries.
 */
public final class DraconicModifiers {

    public static final RegistryObject<Modifier> EVOLVED =
            TConEvoModifiers.register("evolved", EvolvedModifier::new);

    public static final RegistryObject<Modifier> DRACONIC_ENERGY =
            TConEvoModifiers.register("draconic_energy", DraconicEnergyModifier::new);

    public static final RegistryObject<Modifier> DRACONIC_ARROW_SPEED =
            TConEvoModifiers.register("draconic_arrow_speed", DraconicArrowSpeedModifier::new);

    public static final RegistryObject<Modifier> DRACONIC_ATTACK_DAMAGE =
            TConEvoModifiers.register("draconic_attack_damage", DraconicAttackDamageModifier::new);

    public static final RegistryObject<Modifier> DRACONIC_DIG_SPEED =
            TConEvoModifiers.register("draconic_dig_speed", DraconicDigSpeedModifier::new);

    public static final RegistryObject<Modifier> DRACONIC_ARROW_DAMAGE =
            TConEvoModifiers.register("draconic_arrow_damage", DraconicArrowDamageModifier::new);

    public static final RegistryObject<Modifier> DRACONIC_DRAW_SPEED =
            TConEvoModifiers.register("draconic_draw_speed", DraconicDrawSpeedModifier::new);

    public static final RegistryObject<Modifier> SOUL_REND =
            TConEvoModifiers.register("soul_rend", SoulRendModifier::new);

    public static final RegistryObject<Modifier> REAPING =
            TConEvoModifiers.register("reaping", ReapingModifier::new);

    public static final RegistryObject<Modifier> ENTROPIC =
            TConEvoModifiers.register("entropic", EntropicModifier::new);

    public static final RegistryObject<Modifier> PRIMORDIAL =
            TConEvoModifiers.register("primordial", PrimordialModifier::new);

    public static final RegistryObject<Modifier> FLUX_BURN =
            TConEvoModifiers.register("flux_burn", FluxBurnModifier::new);

    public static final RegistryObject<Modifier> DRACONIC_ATTACK_AOE =
            TConEvoModifiers.register("draconic_attack_aoe", DraconicAttackAoeModifier::new);

    public static final RegistryObject<Modifier> DRACONIC_DIG_AOE =
            TConEvoModifiers.register("draconic_dig_aoe", DraconicDigAoeModifier::new);

    private DraconicModifiers() {}

    public static void bootstrap() {
        // Class load triggers registration.
    }
}
