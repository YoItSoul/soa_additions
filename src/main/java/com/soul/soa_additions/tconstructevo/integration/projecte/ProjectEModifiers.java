package com.soul.soa_additions.tconstructevo.integration.projecte;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.modifiers.Modifier;

public final class ProjectEModifiers {

    public static final RegistryObject<Modifier> ETERNAL_DENSITY =
            TConEvoModifiers.register("eternal_density", EternalDensityModifier::new);

    private ProjectEModifiers() {}

    public static void bootstrap() {
        // Classload triggers registration.
    }
}
