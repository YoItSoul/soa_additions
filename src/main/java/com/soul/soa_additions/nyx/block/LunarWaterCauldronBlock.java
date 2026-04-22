package com.soul.soa_additions.nyx.block;

import com.soul.soa_additions.nyx.NyxBlocks;
import com.soul.soa_additions.nyx.NyxItems;
import com.soul.soa_additions.nyx.item.LunarWaterBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Lunar-water cauldron. Takes the place of a LayeredCauldronBlock holding lunar water,
 *  right-clicking with a bucket extracts a lunar-water bucket (level 3 → empty), and
 *  right-clicking with a glass bottle extracts a lunar-water bottle (level - 1). */
public class LunarWaterCauldronBlock extends LayeredCauldronBlock {

    public LunarWaterCauldronBlock(Properties props) {
        super(props, LayeredCauldronBlock.RAIN, java.util.Map.of());
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return InteractionResult.PASS;
        int level = state.getValue(LEVEL);
        if (stack.getItem() == Items.GLASS_BOTTLE && level > 0) {
            if (!world.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    player.awardStat(Stats.USE_CAULDRON);
                    stack.shrink(1);
                    ItemStack bottle = new ItemStack(NyxItems.LUNAR_WATER_BOTTLE.get());
                    if (stack.isEmpty()) player.setItemInHand(hand, bottle);
                    else if (!player.getInventory().add(bottle)) player.drop(bottle, false);
                }
                world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                lowerFillLevel(state, world, pos);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static void lowerFillLevel(BlockState state, Level world, BlockPos pos) {
        int level = state.getValue(LEVEL) - 1;
        if (level <= 0) world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        else world.setBlockAndUpdate(pos, state.setValue(LEVEL, level));
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        int i = state.getValue(LEVEL);
        float f = pos.getY() + (6.0f + 3.0f * i) / 16.0f;
        if (!world.isClientSide && i > 0 && entity.getBoundingBox().minY <= f) {
            boolean did = false;
            if (entity.isOnFire()) {
                entity.clearFire();
                did = true;
            }
            if (entity instanceof LivingEntity le && LunarWaterBottleItem.applyLunarWater(le)) {
                did = true;
            }
            if (did) lowerFillLevel(state, world, pos);
        }
    }

    @Override public boolean isFull(BlockState state) { return state.getValue(LEVEL) == 3; }

    /** Keep rain refilling disabled; lunar water is only filled via the tracker entity. */
    @Override public void handlePrecipitation(BlockState state, Level world, BlockPos pos,
                                              net.minecraft.world.level.biome.Biome.Precipitation p) { }
}
