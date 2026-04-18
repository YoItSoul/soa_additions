package com.soul.soa_additions.tconstructevo.integration.bloodmagic;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import com.soul.soa_additions.tconstructevo.TConEvoModifiers.Holder;
import slimeknights.tconstruct.library.modifiers.Modifier;

public final class BloodMagicModifiers {

    public static final Holder<Modifier> BLOODBOUND =
            TConEvoModifiers.register("bloodbound", BloodboundModifier::new);

    public static final Holder<Modifier> CRYSTALYS =
            TConEvoModifiers.register("crystalys", CrystalysModifier::new);

    public static final Holder<Modifier> SENTIENT =
            TConEvoModifiers.register("sentient", SentientModifier::new);

    public static final Holder<Modifier> WILLFUL =
            TConEvoModifiers.register("willful", WillfulModifier::new);

    private BloodMagicModifiers() {}

    public static void bootstrap() {
        // Classload triggers registration.
    }
}
