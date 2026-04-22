package com.soul.soa_additions.nyx;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 3x3 AoE pickaxe. Ported from 1.12 Nyx Hammer — breaks blocks on the plane perpendicular
 * to the player's look direction, centred on the original block being mined.
 */
public class HammerItem extends PickaxeItem {

    public HammerItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        Level level = player.level();
        HitResult hit = player.pick(5.0D, 0.0f, false);
        if (!(hit instanceof BlockHitResult bhr)) return false;
        Direction face = bhr.getDirection();
        BlockState center = level.getBlockState(pos);
        if (center.isAir() || !this.isCorrectToolForDrops(stack, center)) return false;

        for (BlockPos p : planeAround(pos, face)) {
            if (p.equals(pos)) continue;
            BlockState state = level.getBlockState(p);
            if (state.isAir()) continue;
            if (!this.isCorrectToolForDrops(stack, state)) continue;
            if (state.getDestroySpeed(level, p) < 0) continue;
            Block.dropResources(state, level, p, null, player, stack);
            level.destroyBlock(p, false, player);
            stack.hurtAndBreak(1, player, e -> {});
            if (stack.isEmpty()) break;
        }
        return false;
    }

    private static Iterable<BlockPos> planeAround(BlockPos center, Direction face) {
        int r = 1;
        BlockPos a, b;
        switch (face.getAxis()) {
            case Y -> { a = center.offset(-r, 0, -r); b = center.offset(r, 0, r); }
            case X -> { a = center.offset(0, -r, -r); b = center.offset(0, r, r); }
            default -> { a = center.offset(-r, -r, 0); b = center.offset(r, r, 0); }
        }
        return BlockPos.betweenClosed(a, b);
    }
}
