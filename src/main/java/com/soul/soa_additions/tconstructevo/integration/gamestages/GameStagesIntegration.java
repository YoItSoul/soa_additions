package com.soul.soa_additions.tconstructevo.integration.gamestages;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * GameStages integration — exposes stage-gated material/trait predicates so
 * pack authors can hide tconevo content behind progression stages.
 */
public final class GameStagesIntegration {
    private GameStagesIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_GAMESTAGES.get()) {
            TConstructEvoPlugin.LOG.info("    GameStages integration disabled by config, skipping");
            return;
        }
    }
}
