package com.soul.soa_additions.tconstructevo.integration.avaritia;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Bootstrap for Avaritia (Re-Avaritia on 1.20.1). Ports Neutronium / Infinity
 * materials, the Infinitum, Omnipotence, and Condensing traits. Only
 * classloaded when {@code avaritia} is present.
 */
public final class AvaritiaIntegration {
    private AvaritiaIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_AVARITIA.get()) {
            TConstructEvoPlugin.LOG.info("    Avaritia integration disabled by config, skipping");
            return;
        }
        AvaritiaModifiers.bootstrap();
    }
}
