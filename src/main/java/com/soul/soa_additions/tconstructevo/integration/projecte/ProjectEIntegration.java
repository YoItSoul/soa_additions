package com.soul.soa_additions.tconstructevo.integration.projecte;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * ProjectE integration — ports the Eternal Density trait (auto-compacts drops)
 * and Red/Dark Matter TConstruct materials.
 */
public final class ProjectEIntegration {
    private ProjectEIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_PROJECTE.get()) {
            TConstructEvoPlugin.LOG.info("    ProjectE integration disabled by config, skipping");
            return;
        }
        ProjectEModifiers.bootstrap();
    }
}
