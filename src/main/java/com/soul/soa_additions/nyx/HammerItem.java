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
 *
 * <p>Leap: hold right-click (bow-style, 20-tick max charge) and release while on the
 * ground looking up (pitch ≤ -10°) to launch along the look direction. Writes
 * {@code nyx:leap_start}, which powers the ground-slam and fall-damage cancel in
 * NyxEvents.</p>
 */
public class HammerItem extends PickaxeItem {

    private static final int MAX_CHARGE_TICKS = 20;

    public HammerItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
            net.minecraft.world.InteractionHand hand) {
        player.startUsingItem(hand);
        return net.minecraft.world.InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity user,
            int remainingTicks) {
        if (!(user instanceof Player player)) return;
        int charge = Math.min(MAX_CHARGE_TICKS, getUseDuration(stack) - remainingTicks);
        if (!player.onGround() || player.getXRot() > -10.0f) return;

        double strength = Math.min(Math.max(charge / 8.0, 1.0), 2.5);
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0, look.z);
        if (flat.lengthSqr() > 1.0e-4) flat = flat.normalize();
        player.setDeltaMovement(flat.x * strength, strength * 0.625, flat.z * strength);
        player.hurtMarked = true; // sync the velocity to the client

        if (!level.isClientSide()) {
            player.getPersistentData().putLong("nyx:leap_start", level.getGameTime());
            level.playSound(null, player.blockPosition(), NyxSounds.HAMMER_START.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.0f);
            if (level instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                        player.getX(), player.getY(), player.getZ(), 20, 0.3, 0.1, 0.3, 0.05);
            }
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        Level level = player.level();
        // Forge calls this on both sides. Breaking blocks and spending durability against the
        // client's copy of the world made neighbours vanish before the server agreed, and the
        // tool's damage bar drop by eight per swing.
        if (level.isClientSide) return false;
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
