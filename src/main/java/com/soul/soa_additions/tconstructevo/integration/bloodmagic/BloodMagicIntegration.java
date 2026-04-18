package com.soul.soa_additions.tconstructevo.integration.bloodmagic;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Blood Magic integration — separate from the existing
 * {@code com.soul.soa_additions.bloodarsenal} package. This file ports the
 * tconevo Blood Magic traits (Sentient, Bloodbound, Crystalys, Willful) to
 * TConstruct modifiers; Blood Arsenal items themselves stay in the original
 * sub-package.
 */
public final class BloodMagicIntegration {
    private BloodMagicIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_BLOODMAGIC.get()) {
            TConstructEvoPlugin.LOG.info("    Blood Magic integration disabled by config, skipping");
            return;
        }
        BloodMagicModifiers.bootstrap();
    }
}
