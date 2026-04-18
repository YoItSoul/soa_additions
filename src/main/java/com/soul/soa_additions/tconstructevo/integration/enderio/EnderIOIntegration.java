package com.soul.soa_additions.tconstructevo.integration.enderio;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Ports EnderIO integration — Vibrant Alloy / Energetic Alloy / Dark Steel
 * materials and related TConstruct traits.
 */
public final class EnderIOIntegration {
    private EnderIOIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_ENDERIO.get()) {
            TConstructEvoPlugin.LOG.info("    EnderIO integration disabled by config, skipping");
            return;
        }
    }
}
