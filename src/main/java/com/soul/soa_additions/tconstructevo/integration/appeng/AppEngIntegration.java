package com.soul.soa_additions.tconstructevo.integration.appeng;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Applied Energistics 2 integration — ports Certus Quartz / Fluix material
 * stubs and wires ME-aware upgrades (the original mod's Quartz modifier).
 */
public final class AppEngIntegration {
    private AppEngIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_APPENG.get()) {
            TConstructEvoPlugin.LOG.info("    AE2 integration disabled by config, skipping");
            return;
        }
    }
}
