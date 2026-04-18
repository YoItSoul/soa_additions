package com.soul.soa_additions.tconstructevo.integration.curios;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Curios integration — ports the 1.12.2 Baubles-based artifacts (Phase Boots,
 * Light Shoes, Guardian Angel, Crystal Sceptre, etc.) to their 1.20.1
 * Curios-slot equivalents.
 */
public final class CuriosIntegration {
    private CuriosIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_CURIOS.get()) {
            TConstructEvoPlugin.LOG.info("    Curios integration disabled by config, skipping");
            return;
        }
    }
}
