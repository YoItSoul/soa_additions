package com.soul.soa_additions.nyx.ench;

import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

/** Moon-phase-scaled protection variant. */
public class LunarShieldEnchantment extends Enchantment {

    public LunarShieldEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR,
                new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                        EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }

    @Override
    public int getMinCost(int level) { return 1 + (level - 1) * 11; }

    @Override
    public int getMaxCost(int level) { return this.getMinCost(level) + 20; }

    @Override
    public int getMaxLevel() { return 4; }

    @Override
    public int getDamageProtection(int level, DamageSource source) {
        return source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)
                ? 0
                : Mth.floor((level + 1) * NyxWorldData.getMoonPhaseMultiplier());
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && !(other instanceof ProtectionEnchantment);
    }
}
