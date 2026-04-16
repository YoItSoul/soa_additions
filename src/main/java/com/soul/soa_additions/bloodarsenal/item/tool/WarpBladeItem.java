package com.soul.soa_additions.bloodarsenal.item.tool;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

/**
 * Warp Blade — unbreakable iron sword. Right-click throws a projectile.
 * 20-tick cooldown between throws.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.tool.ItemWarpBlade</p>
 */
public class WarpBladeItem extends SwordItem {

    public WarpBladeItem(Properties props) {
        super(Tiers.IRON, 3, -2.4f, props.defaultDurability(0));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            Arrow projectile = new Arrow(level, player);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.5f, 1.0f);
            projectile.pickup = Arrow.Pickup.CREATIVE_ONLY;
            projectile.setBaseDamage(6.0);
            level.addFreshEntity(projectile);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
