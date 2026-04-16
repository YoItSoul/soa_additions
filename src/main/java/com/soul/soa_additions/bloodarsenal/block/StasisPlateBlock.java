package com.soul.soa_additions.bloodarsenal.block;

import com.soul.soa_additions.bloodarsenal.BABlockEntities;
import com.soul.soa_additions.bloodarsenal.tile.StasisPlateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Stasis Plate — a flat block (3 pixels tall) used as a pedestal for
 * Sanguine Infusion crafting. Holds 1 item in its block entity.
 *
 * <p>Ported from: arcaratus.bloodarsenal.block.BlockStasisPlate</p>
 */
public class StasisPlateBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 3, 16);

    public StasisPlateBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StasisPlateBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof StasisPlateBlockEntity plate)) return InteractionResult.PASS;
        if (plate.isInStasis()) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        ItemStack stored = plate.getStoredItem();

        if (stored.isEmpty() && !held.isEmpty()) {
            // Place item on plate
            plate.setStoredItem(held.split(1));
            return InteractionResult.CONSUME;
        } else if (!stored.isEmpty()) {
            // Remove item from plate
            if (!player.addItem(stored.copy())) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stored.copy());
            }
            plate.setStoredItem(ItemStack.EMPTY);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof StasisPlateBlockEntity plate) {
                ItemStack stored = plate.getStoredItem();
                if (!stored.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stored);
                }
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
