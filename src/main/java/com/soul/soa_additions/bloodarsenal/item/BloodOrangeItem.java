package com.soul.soa_additions.bloodarsenal.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Blood Orange — food item that restores 4 hunger, 2.0 saturation,
 * and has a 50% chance to grant Regeneration II for 80 ticks (4 seconds).
 * Always edible regardless of hunger level.
 * <p>Ported from: arcaratus.bloodarsenal.item.ItemBloodOrange</p>
 */
public class BloodOrangeItem extends Item {

    private static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(2.0f)
            .alwaysEat()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 80, 1), 0.5f)
            .build();

    public BloodOrangeItem() {
        super(new Item.Properties().food(FOOD));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.soa_additions.ba_blood_orange.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}
