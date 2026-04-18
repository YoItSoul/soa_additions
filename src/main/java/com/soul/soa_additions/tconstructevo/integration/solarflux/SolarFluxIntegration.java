package com.soul.soa_additions.tconstructevo.integration.solarflux;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Solar Flux Reborn integration — ports the Photovoltaic modifier
 * that passively charges energy-capacitive TConstruct tools in daylight.
 */
public final class SolarFluxIntegration {
    private SolarFluxIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_SOLARFLUX.get()) {
            TConstructEvoPlugin.LOG.info("    Solar Flux Reborn integration disabled by config, skipping");
            return;
        }
    }
}
