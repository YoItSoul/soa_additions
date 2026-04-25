package com.soul.soa_additions.tr.compat.jade;

import com.soul.soa_additions.tr.ThaumicRemnants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin entry — registers {@link MonocleBlockProvider} for every block
 * so the monocle's overlay works regardless of what the player looks at. The
 * provider itself short-circuits when the player isn't wearing a monocle, so
 * the broad registration costs nothing at runtime in that state.
 */
@WailaPlugin
public final class JadeMonoclePlugin implements IWailaPlugin {

    public static final ResourceLocation MONOCLE_UID =
            new ResourceLocation(ThaumicRemnants.MODID, "monocle_aspects");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // No-op — purely client-side display.
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MonocleBlockProvider.INSTANCE, Block.class);
        registration.registerEntityComponent(MonocleEntityProvider.INSTANCE,
                net.minecraft.world.entity.Entity.class);
        // Skipping addConfig() — signature shifted between Jade builds within
        // the 11.13.2 display version, and providers run fine without it
        // (just no toggle in Jade's config GUI).
        ThaumicRemnants.LOG.info("Jade monocle plugin registered (block + entity providers)");
    }
}
