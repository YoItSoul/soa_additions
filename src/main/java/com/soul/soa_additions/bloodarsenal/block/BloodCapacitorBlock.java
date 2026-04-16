package com.soul.soa_additions.bloodarsenal.block;

import com.soul.soa_additions.bloodarsenal.tile.BloodCapacitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Blood Capacitor — stores Forge Energy (RF) and auto-pushes to adjacent
 * blocks when not powered by redstone.
 *
 * <p>Ported from: arcaratus.bloodarsenal.block.BlockBloodCapacitor</p>
 */
public class BloodCapacitorBlock extends BaseEntityBlock {

    public BloodCapacitorBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodCapacitorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof BloodCapacitorBlockEntity cap) cap.serverTick();
        };
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BloodCapacitorBlockEntity cap) {
            return cap.getRedstoneSignal();
        }
        return 0;
    }
}
