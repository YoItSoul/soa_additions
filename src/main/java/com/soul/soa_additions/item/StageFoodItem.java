package com.soul.soa_additions.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class StageFoodItem extends StageItem {

    private final int eatTime;
    private final boolean isDrink;

    public StageFoodItem(Properties props, boolean foil, int eatTime, boolean isDrink, String... tooltip) {
        super(props, foil, tooltip);
        this.eatTime = eatTime;
        this.isDrink = isDrink;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return eatTime;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isDrink ? UseAnim.DRINK : UseAnim.EAT;
    }
}
