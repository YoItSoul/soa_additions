package com.soul.soa_additions.compat.jade;

import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade entry point. Jade discovers this by scanning for {@link WailaPlugin}, so nothing in
 * {@code SoaAdditions} references it and the class never loads when Jade is absent — the same
 * arrangement the JEI and JER plugins use.
 */
@WailaPlugin
public class SoaJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MiningLevelProvider.INSTANCE, Block.class);
    }
}
