package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import com.soul.soa_additions.tconstructevo.integration.draconicevolution.recipe.DraconicFusionRecipes;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Bootstrap for the Draconic Evolution bridge. Wires Draconic materials into
 * TConstruct's material registry, registers the Evolved / DraconicEnergy /
 * DraconicArrowSpeed modifiers, and (once the recipe system lands) hooks the
 * fusion crafter so TConstruct tools are legal fusion inputs.
 *
 * <p>This class is only classloaded when {@code draconicevolution} is present,
 * so it is safe to import {@code com.brandon3055.draconicevolution.*} here.</p>
 */
public final class DraconicIntegration {
    private DraconicIntegration() {}

    public static void init(IEventBus modEventBus) {
        if (!TConEvoConfig.INTEG_DRACONIC.get()) {
            TConstructEvoPlugin.LOG.info("    Draconic integration disabled by config, skipping");
            return;
        }
        // Materials, part-stats and modifier RegistryObjects get wired here as
        // each is ported. Keeping the bootstrap minimal until actual content lands.
        DraconicModifiers.bootstrap();
        DraconicMaterials.bootstrap();
        DraconicFusionRecipes.register(modEventBus);
    }
}
