package com.soul.soa_additions.tconstructevo.integration.mekanism;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Ports Mekanism integration — Osmium / Refined Obsidian / Refined Glowstone
 * TConstruct materials and the related traits.
 */
public final class MekanismIntegration {
    private MekanismIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_MEKANISM.get()) {
            TConstructEvoPlugin.LOG.info("    Mekanism integration disabled by config, skipping");
            return;
        }
    }
}
