package com.soul.soa_additions.tconstructevo.integration.botania;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Ports Botania integration (Mana-Infused, Aura Siphon, Gaia Wrath, Fae Voice
 * traits; Terrasteel and Elementium TConstruct materials).
 */
public final class BotaniaIntegration {
    private BotaniaIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_BOTANIA.get()) {
            TConstructEvoPlugin.LOG.info("    Botania integration disabled by config, skipping");
            return;
        }
        BotaniaModifiers.bootstrap();
    }
}
