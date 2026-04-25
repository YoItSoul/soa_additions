package com.soul.soa_additions.tr.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Block that wards its chunk against falling meteors. Backed by a tickless
 * {@link AstralWardBlockEntity} purely so {@code LevelChunk.getBlockEntities()}
 * gives us O(1) per-chunk presence checks — the meteor spawn loop runs every
 * second per player and a full chunk block-state scan would be too expensive.
 */
public class AstralWardBlock extends Block implements EntityBlock {

    public AstralWardBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstralWardBlockEntity(pos, state);
    }
}
