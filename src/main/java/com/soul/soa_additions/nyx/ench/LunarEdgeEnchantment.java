package com.soul.soa_additions.nyx.ench;

import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/** Moon-phase-scaled sharpness variant. */
public class LunarEdgeEnchantment extends Enchantment {

    public LunarEdgeEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMinCost(int level) { return 1 + (level - 1) * 11; }

    @Override
    public int getMaxCost(int level) { return this.getMinCost(level) + 20; }

    @Override
    public int getMaxLevel() { return 5; }

    @Override
    public float getDamageBonus(int level, MobType type) {
        float base = 1.25f + Math.max(0, level - 1) * 0.5f;
        return NyxWorldData.getMoonPhaseMultiplier() * base;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && !(other instanceof DamageEnchantment);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof AxeItem || super.canEnchant(stack);
    }
}
