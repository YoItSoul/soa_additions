package com.soul.soa_additions.nyx.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Meteor Bow — 1.12 Nyx stats: durability 2250, enchantability 18, and a hotter
 * velocity curve (~1.3x vanilla arrow speed at full draw). This is vanilla
 * {@link BowItem#releaseUsing} with the speed constant scaled.
 */
public class MeteorBowItem extends BowItem {

    private static final float VELOCITY_SCALE = 1.3f;

    public MeteorBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingTicks) {
        if (!(user instanceof Player player)) return;
        boolean creativeOrInfinity = player.getAbilities().instabuild
                || stack.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0;
        ItemStack ammo = player.getProjectile(stack);

        int charge = this.getUseDuration(stack) - remainingTicks;
        charge = ForgeEventFactory.onArrowLoose(stack, level, player, charge,
                !ammo.isEmpty() || creativeOrInfinity);
        if (charge < 0) return;
        if (ammo.isEmpty() && !creativeOrInfinity) return;
        if (ammo.isEmpty()) ammo = new ItemStack(Items.ARROW);

        float power = getPowerForTime(charge);
        if (power < 0.1f) return;

        boolean freeArrow = player.getAbilities().instabuild
                || (ammo.getItem() instanceof ArrowItem arrowItem && arrowItem.isInfinite(ammo, stack, player));
        if (!level.isClientSide()) {
            ArrowItem arrowItem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
            AbstractArrow arrow = arrowItem.createArrow(level, ammo, player);
            arrow = customArrow(arrow);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f,
                    power * 3.0f * VELOCITY_SCALE, 1.0f);
            if (power == 1.0f) arrow.setCritArrow(true);

            int powerEnch = stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
            if (powerEnch > 0) arrow.setBaseDamage(arrow.getBaseDamage() + powerEnch * 0.5 + 0.5);
            int punch = stack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
            if (punch > 0) arrow.setKnockback(punch);
            if (stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                arrow.setSecondsOnFire(100);
            }

            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            if (freeArrow || (player.getAbilities().instabuild
                    && (ammo.is(Items.SPECTRAL_ARROW) || ammo.is(Items.TIPPED_ARROW)))) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(arrow);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.ARROW_SHOOT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f,
                1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + power * 0.5f);
        if (!freeArrow && !player.getAbilities().instabuild) {
            ammo.shrink(1);
            if (ammo.isEmpty()) player.getInventory().removeItem(ammo);
        }
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
    }
}
