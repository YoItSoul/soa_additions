package com.soul.soa_additions.nyx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;

/** Harvest-moon fuelled growth crystal. Ticks on a randomTick, bonemeals nearby
 *  growable blocks, losing durability per successful growth. Recharges under a
 *  Harvest Moon if it can see the sky. */
public class CrystalBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);

    public CrystalBlock(Properties props) { super(props); }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public boolean isRandomlyTicking(BlockState state) { return true; }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CrystalBlockEntity tile)) return;
        if (tile.durability <= 0) return;

        int range = 5;
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = -range; x <= range; x++) for (int z = -range; z <= range; z++) for (int y = -1; y <= 1; y++) {
            BlockPos off = pos.offset(x, y, z);
            BlockState s = level.getBlockState(off);
            Block b = s.getBlock();
            if (b instanceof BonemealableBlock || b instanceof IPlantable) candidates.add(off);
        }
        for (BlockPos off : candidates) {
            if (tile.durability <= 0) break;
            BlockState before = level.getBlockState(off);
            if (before.getBlock() instanceof BonemealableBlock bm) {
                for (int i = 0; i < 5; i++) {
                    if (bm.isValidBonemealTarget(level, off, before, false)
                            && bm.isBonemealSuccess(level, rand, off, before)) {
                        bm.performBonemeal(level, rand, off, before);
                    }
                }
            }
            if (level.getBlockState(off) != before) {
                tile.durability--;
                tile.setChanged();
            }
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                com.soul.soa_additions.block.entity.ModBlockEntities.CRYSTAL.get(),
                CrystalBlockEntity::serverTick);
    }
}
