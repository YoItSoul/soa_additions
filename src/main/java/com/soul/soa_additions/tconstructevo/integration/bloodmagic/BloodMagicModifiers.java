package com.soul.soa_additions.tconstructevo.integration.bloodmagic;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.modifiers.Modifier;

public final class BloodMagicModifiers {

    public static final RegistryObject<Modifier> BLOODBOUND =
            TConEvoModifiers.register("bloodbound", BloodboundModifier::new);

    public static final RegistryObject<Modifier> CRYSTALYS =
            TConEvoModifiers.register("crystalys", CrystalysModifier::new);

    public static final RegistryObject<Modifier> SENTIENT =
            TConEvoModifiers.register("sentient", SentientModifier::new);

    public static final RegistryObject<Modifier> WILLFUL =
            TConEvoModifiers.register("willful", WillfulModifier::new);

    private BloodMagicModifiers() {}

    public static void bootstrap() {
        // Classload triggers registration.
    }
}
