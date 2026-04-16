package com.soul.soa_additions.bloodarsenal.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

/**
 * Sickle — harvests crops/plants in an AOE around the broken block.
 * Range depends on tier: wooden=1 (3x3), iron=2 (5x5).
 * Blood-infused variants auto-repair from soul network.
 */
public class SickleItem extends DiggerItem {

    private final int range;
    private final boolean autoRepair;

    public SickleItem(Tier tier, float attackDamage, float attackSpeed, int range, boolean autoRepair, Properties props) {
        super(attackDamage, attackSpeed, tier, BlockTags.CROPS, props);
        this.range = range;
        this.autoRepair = autoRepair;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isHarvestable(state)) {
            return this.speed * 2.0f;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, net.minecraft.world.entity.LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);

        if (!level.isClientSide() && miner instanceof Player player && isHarvestable(state)) {
            harvestArea(stack, level, pos, player);
        }

        return result;
    }

    private void harvestArea(ItemStack stack, Level level, BlockPos center, Player player) {
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                if (x == 0 && z == 0) continue; // center already broken
                BlockPos target = center.offset(x, 0, z);
                BlockState targetState = level.getBlockState(target);

                if (isHarvestable(targetState)) {
                    // Drop items and break
                    Block block = targetState.getBlock();
                    if (level instanceof ServerLevel serverLevel) {
                        block.playerDestroy(level, player, target, targetState, null, stack);
                        level.destroyBlock(target, false);
                    }
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
    }

    private boolean isHarvestable(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CropBlock
                || block instanceof IPlantable
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.FLOWERS);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (autoRepair) {
            BloodInfusedToolItem.handleAutoRepair(stack, level, entity);
        }
    }
}
