package com.soul.soa_additions.bloodarsenal.item.tool;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

/**
 * Blood-infused axe — auto-repairs from soul network LP.
 */
public class BloodInfusedAxeItem extends AxeItem {

    public BloodInfusedAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties props) {
        super(tier, attackDamage, attackSpeed, props);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        BloodInfusedToolItem.handleAutoRepair(stack, level, entity);
    }
}
