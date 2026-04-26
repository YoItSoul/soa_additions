package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Flight — grants creative-style flight while active. Restored on
 * remove() to match vanilla creative-mode rules. The persistent
 * abilities flag is re-checked each tick to handle re-login carry-over.
 */
public final class FlightEffect extends MobEffect {
    public FlightEffect() { super(MobEffectCategory.BENEFICIAL, 0xCFE2F3); }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;  // refresh once per second
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player p) || p.getAbilities().instabuild) return;
        if (!p.getAbilities().mayfly) {
            p.getAbilities().mayfly = true;
            p.onUpdateAbilities();
        }
    }
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attrs, int amplifier) {
        super.removeAttributeModifiers(entity, attrs, amplifier);
        if (entity instanceof Player p && !p.getAbilities().instabuild) {
            p.getAbilities().mayfly = false;
            p.getAbilities().flying = false;
            p.onUpdateAbilities();
        }
    }
}
