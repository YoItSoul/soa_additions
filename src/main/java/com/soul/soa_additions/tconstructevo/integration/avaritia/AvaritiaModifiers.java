package com.soul.soa_additions.tconstructevo.integration.avaritia;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * RegistryObject holders for Avaritia-themed modifiers.
 */
public final class AvaritiaModifiers {

    public static final RegistryObject<Modifier> INFINITUM =
            TConEvoModifiers.register("infinitum", InfinitumModifier::new);

    public static final RegistryObject<Modifier> OMNIPOTENCE =
            TConEvoModifiers.register("omnipotence", OmnipotenceModifier::new);

    public static final RegistryObject<Modifier> CONDENSING =
            TConEvoModifiers.register("condensing", CondensingModifier::new);

    private AvaritiaModifiers() {}

    public static void bootstrap() {
        // Classload triggers registration.
    }
}
