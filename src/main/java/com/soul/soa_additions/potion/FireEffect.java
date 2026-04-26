package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fire — sets self on fire each cycle. PotionCore's effect was "fire damage
 * over time"; vanilla setSecondsOnFire is the cleanest equivalent.
 * Amplifier adds 4s of fire per cycle.
 */
public final class FireEffect extends MobEffect {
    public FireEffect() { super(MobEffectCategory.HARMFUL, 0xFF5722); }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 60 == 0; }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setSecondsOnFire(4 * (amplifier + 1));
    }
}
