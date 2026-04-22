package com.soul.soa_additions.nyx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

/** Transparent, non-replaceable block whose only purpose is to keep a fallen-star ItemEntity
 *  alive when it lands. The tracker TE sets itself back to air once the stars despawn. */
public class StarAirBlock extends AirBlock {

    public StarAirBlock(Properties props) { super(props); }

    @Override
    public boolean isPathfindable(BlockState state, net.minecraft.world.level.BlockGetter world,
                                  BlockPos pos, PathComputationType type) {
        return true;
    }
}
