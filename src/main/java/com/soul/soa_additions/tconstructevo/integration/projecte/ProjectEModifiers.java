package com.soul.soa_additions.tconstructevo.integration.projecte;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import com.soul.soa_additions.tconstructevo.TConEvoModifiers.Holder;
import slimeknights.tconstruct.library.modifiers.Modifier;

public final class ProjectEModifiers {

    public static final Holder<Modifier> ETERNAL_DENSITY =
            TConEvoModifiers.register("eternal_density", EternalDensityModifier::new);

    private ProjectEModifiers() {}

    public static void bootstrap() {
        // Classload triggers registration.
    }
}
