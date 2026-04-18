package com.soul.soa_additions.tconstructevo.integration.botania;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import com.soul.soa_additions.tconstructevo.TConEvoModifiers.Holder;
import slimeknights.tconstruct.library.modifiers.Modifier;

public final class BotaniaModifiers {

    public static final Holder<Modifier> AURA_SIPHON =
            TConEvoModifiers.register("aura_siphon", AuraSiphonModifier::new);

    public static final Holder<Modifier> FAE_VOICE =
            TConEvoModifiers.register("fae_voice", FaeVoiceModifier::new);

    public static final Holder<Modifier> GAIA_WRATH =
            TConEvoModifiers.register("gaia_wrath", GaiaWrathModifier::new);

    public static final Holder<Modifier> MANA_INFUSED =
            TConEvoModifiers.register("mana_infused", ManaInfusedModifier::new);

    private BotaniaModifiers() {}

    public static void bootstrap() {
        // Classload triggers registration.
    }
}
