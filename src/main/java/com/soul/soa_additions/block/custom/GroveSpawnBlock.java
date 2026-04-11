package com.soul.soa_additions.block.custom;

import com.soul.soa_additions.block.entity.GroveBlockEntity;
import com.soul.soa_additions.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * An ephemeral marker block that, on its first tick, generates the surrounding grove structure
 * and then replaces itself with a {@link GroveBoonBlock}.
 */
public class GroveSpawnBlock extends BaseEntityBlock {

    public GroveSpawnBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GroveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.GROVE_SPAWN_BLOCK_ENTITY.get(), GroveBlockEntity::tick);
    }
}
