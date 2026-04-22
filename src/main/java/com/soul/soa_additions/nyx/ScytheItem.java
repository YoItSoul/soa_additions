package com.soul.soa_additions.nyx;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

/**
 * 4x4x4 AoE harvester of crops and leaves.
 * Ported from 1.12 Nyx Scythe which checks IPlantable or isLeaves for targets.
 */
public class ScytheItem extends HoeItem {

    private static final int RADIUS = 4;

    public ScytheItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        Level level = player.level();
        BlockState center = level.getBlockState(pos);
        if (!isPlantOrLeaves(center)) {
            return false;
        }
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(p);
                    if (isPlantOrLeaves(state)) {
                        Block.dropResources(state, level, p, null, player, stack);
                        level.destroyBlock(p, false, player);
                        stack.hurtAndBreak(1, player, e -> {});
                        if (stack.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isPlantOrLeaves(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof IPlantable) return true;
        return state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.CROPS);
    }
}
