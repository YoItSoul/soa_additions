package com.soul.soa_additions.tconstructevo.integration.botania;

import com.soul.soa_additions.tconstructevo.TConEvoModifiers;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.modifiers.Modifier;

public final class BotaniaModifiers {

    public static final RegistryObject<Modifier> AURA_SIPHON =
            TConEvoModifiers.register("aura_siphon", AuraSiphonModifier::new);

    public static final RegistryObject<Modifier> FAE_VOICE =
            TConEvoModifiers.register("fae_voice", FaeVoiceModifier::new);

    public static final RegistryObject<Modifier> GAIA_WRATH =
            TConEvoModifiers.register("gaia_wrath", GaiaWrathModifier::new);

    public static final RegistryObject<Modifier> MANA_INFUSED =
            TConEvoModifiers.register("mana_infused", ManaInfusedModifier::new);

    private BotaniaModifiers() {}

    public static void bootstrap() {
        // Classload triggers registration.
    }
}
