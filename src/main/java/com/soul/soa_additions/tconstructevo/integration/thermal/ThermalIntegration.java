package com.soul.soa_additions.tconstructevo.integration.thermal;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Thermal Series integration — covers Signalum, Lumium, Enderium, and the
 * redstone_arsenal "Fluxed Electrum" tier (the 1.12.2 redstonerepository
 * integration collapsed into Thermal on 1.20.1).
 */
public final class ThermalIntegration {
    private ThermalIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_THERMAL.get()) {
            TConstructEvoPlugin.LOG.info("    Thermal integration disabled by config, skipping");
            return;
        }
    }
}
