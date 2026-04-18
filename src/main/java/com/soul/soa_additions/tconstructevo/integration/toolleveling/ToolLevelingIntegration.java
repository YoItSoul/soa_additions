package com.soul.soa_additions.tconstructevo.integration.toolleveling;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Tinkers' Levelling Addon integration — makes the tconevo traits aware of
 * the addon's XP/level system (e.g. XP yield scaling on kills granted by
 * Crystalline / Culling / Relentless).
 */
public final class ToolLevelingIntegration {
    private ToolLevelingIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_TOOLLEVELING.get()) {
            TConstructEvoPlugin.LOG.info("    Tinkers' Levelling integration disabled by config, skipping");
            return;
        }
    }
}
