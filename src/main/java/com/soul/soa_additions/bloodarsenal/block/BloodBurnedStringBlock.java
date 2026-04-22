package com.soul.soa_additions.bloodarsenal.block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;

/**
 * Blood-burned string block — tripwire variant. Placed via the
 * matching block item; triggers vanilla tripwire hooks.
 * Ported from: arcaratus.bloodarsenal.block.BlockBloodBurnedString
 */
public class BloodBurnedStringBlock extends TripWireBlock {

    public BloodBurnedStringBlock(Properties props) {
        super((TripWireHookBlock) Blocks.TRIPWIRE_HOOK, props);
    }
}
