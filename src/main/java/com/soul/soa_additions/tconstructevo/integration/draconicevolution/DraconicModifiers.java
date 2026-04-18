package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import com.soul.soa_additions.tconstructevo.TConEvoModifiers.Holder;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Holders for the Draconic-themed TConstruct modifiers.
 * Concrete modifier classes live under the {@code modifier/} sub-package;
 * this class just pins the registry entries.
 */
public final class DraconicModifiers {

    public static final Holder<Modifier> EVOLVED =
            TConEvoModifiers.register("evolved", EvolvedModifier::new);

    public static final Holder<Modifier> DRACONIC_ENERGY =
            TConEvoModifiers.register("draconic_energy", DraconicEnergyModifier::new);

    public static final Holder<Modifier> DRACONIC_ARROW_SPEED =
            TConEvoModifiers.register("draconic_arrow_speed", DraconicArrowSpeedModifier::new);

    public static final Holder<Modifier> DRACONIC_ATTACK_DAMAGE =
            TConEvoModifiers.register("draconic_attack_damage", DraconicAttackDamageModifier::new);

    public static final Holder<Modifier> DRACONIC_DIG_SPEED =
            TConEvoModifiers.register("draconic_dig_speed", DraconicDigSpeedModifier::new);

    public static final Holder<Modifier> DRACONIC_ARROW_DAMAGE =
            TConEvoModifiers.register("draconic_arrow_damage", DraconicArrowDamageModifier::new);

    public static final Holder<Modifier> DRACONIC_DRAW_SPEED =
            TConEvoModifiers.register("draconic_draw_speed", DraconicDrawSpeedModifier::new);

    public static final Holder<Modifier> SOUL_REND =
            TConEvoModifiers.register("soul_rend", SoulRendModifier::new);

    public static final Holder<Modifier> REAPING =
            TConEvoModifiers.register("reaping", ReapingModifier::new);

    public static final Holder<Modifier> ENTROPIC =
            TConEvoModifiers.register("entropic", EntropicModifier::new);

    public static final Holder<Modifier> PRIMORDIAL =
            TConEvoModifiers.register("primordial", PrimordialModifier::new);

    public static final Holder<Modifier> FLUX_BURN =
            TConEvoModifiers.register("flux_burn", FluxBurnModifier::new);

    public static final Holder<Modifier> DRACONIC_ATTACK_AOE =
            TConEvoModifiers.register("draconic_attack_aoe", DraconicAttackAoeModifier::new);

    public static final Holder<Modifier> DRACONIC_DIG_AOE =
            TConEvoModifiers.register("draconic_dig_aoe", DraconicDigAoeModifier::new);

    private DraconicModifiers() {}

    public static void bootstrap() {
        // Class load triggers registration.
    }
}
