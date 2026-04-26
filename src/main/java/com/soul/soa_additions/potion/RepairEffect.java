package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Repair — held + worn items tick toward repair. 1 durability per
 * (40 / (amplifier+1)) ticks. Higher amplifier = faster repair.
 */
public final class RepairEffect extends MobEffect {
    public RepairEffect() { super(MobEffectCategory.BENEFICIAL, 0x4A90E2); }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = Math.max(2, 40 / (amplifier + 1));
        return duration % interval == 0;
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Repair the most-damaged held / worn item by 1 point.
        EquipmentSlot best = null;
        int maxDmg = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem()) continue;
            int dmg = stack.getDamageValue();
            if (dmg > maxDmg) { maxDmg = dmg; best = slot; }
        }
        if (best != null) {
            ItemStack stack = entity.getItemBySlot(best);
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
}
