package com.soul.soa_additions.nyx.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Drinkable lunar water bottle. Clears all negative potion effects and grants
 *  Regeneration II for 5s. Consumed on drink (returns a glass bottle). */
public class LunarWaterBottleItem extends Item {

    public LunarWaterBottleItem(Properties props) { super(props); }

    public static boolean applyLunarWater(LivingEntity entity) {
        boolean did = false;
        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance eff : entity.getActiveEffects()) {
            if (!eff.getEffect().isBeneficial()) {
                toRemove.add(eff.getEffect());
                did = true;
            }
        }
        for (MobEffect e : toRemove) entity.removeEffect(e);
        if (entity.getEffect(MobEffects.REGENERATION) == null) {
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            did = true;
        }
        return did;
    }

    @Override public int getUseDuration(ItemStack stack) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!level.isClientSide) applyLunarWater(user);
        Player player = user instanceof Player p ? p : null;
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            if (player != null) player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }

    private static final class ItemUtils {
        static InteractionResultHolder<ItemStack> startUsingInstantly(Level level, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
    }
}
