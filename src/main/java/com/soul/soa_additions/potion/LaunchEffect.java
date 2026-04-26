package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Launch — periodic upward velocity boost (every 60t). Amplifier scales
 * upward velocity (0.5 + 0.25*amp).
 */
public final class LaunchEffect extends MobEffect {
    public LaunchEffect() { super(MobEffectCategory.NEUTRAL, 0xFF9800); }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 60 == 0; }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        var v = entity.getDeltaMovement();
        entity.setDeltaMovement(v.x, 0.5 + 0.25 * amplifier, v.z);
        entity.hasImpulse = true;
    }
}
