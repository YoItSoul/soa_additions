package com.soul.soa_additions.bloodarsenal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Glass Shards — a thin hazardous block that damages entities walking on it.
 * Ported from: arcaratus.bloodarsenal.block.BlockGlassShards
 */
public class GlassShardsBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 3, 16); // 0.1875 blocks tall

    public GlassShardsBlock(Properties props) {
        super(props);
    }

    @Override
    // Deprecated only to steer callers to the BlockState overload; overriding it is the intended use.
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().cactus(), 1.0f);
        }
    }
}
