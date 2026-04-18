package com.soul.soa_additions.tconstructevo.integration.avaritia;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import com.soul.soa_additions.tconstructevo.TConEvoModifiers.Holder;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Holders for Avaritia-themed modifiers.
 */
public final class AvaritiaModifiers {

    public static final Holder<Modifier> INFINITUM =
            TConEvoModifiers.register("infinitum", InfinitumModifier::new);

    public static final Holder<Modifier> OMNIPOTENCE =
            TConEvoModifiers.register("omnipotence", OmnipotenceModifier::new);

    public static final Holder<Modifier> CONDENSING =
            TConEvoModifiers.register("condensing", CondensingModifier::new);

    private AvaritiaModifiers() {}

    public static void bootstrap() {
        // Classload triggers registration.
    }
}
