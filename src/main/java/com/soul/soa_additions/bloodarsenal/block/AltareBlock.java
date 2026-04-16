package com.soul.soa_additions.bloodarsenal.block;

import com.soul.soa_additions.bloodarsenal.tile.AltareBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Altare Aenigmatica — a block that links to a Blood Magic altar and
 * automatically inserts recipe items. Has a 10-slot inventory
 * (9 input + 1 orb slot) and stores the linked altar position.
 *
 * <p>Ported from: arcaratus.bloodarsenal.block.BlockAltare</p>
 */
public class AltareBlock extends BaseEntityBlock {

    public AltareBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltareBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof AltareBlockEntity altare) altare.serverTick();
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AltareBlockEntity altare) {
                altare.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
