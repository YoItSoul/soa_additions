package com.soul.soa_additions.bloodarsenal.item.tool;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

/**
 * Blood-infused sword — auto-repairs from soul network LP.
 * Wooden variant: wood tier, enchantability 18.
 * Iron variant: iron tier, UNCOMMON rarity.
 */
public class BloodInfusedSwordItem extends SwordItem {

    public BloodInfusedSwordItem(Tier tier, int attackDamage, float attackSpeed, Properties props) {
        super(tier, attackDamage, attackSpeed, props);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        BloodInfusedToolItem.handleAutoRepair(stack, level, entity);
    }
}
