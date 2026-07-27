package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Burst — periodically knocks nearby entities away. PotionCore port. */
public final class BurstEffect extends MobEffect {
    public BurstEffect() { super(MobEffectCategory.BENEFICIAL, 0xFFA500); }

    @Override public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 40 == 0; }

    @Override public void applyEffectTick(LivingEntity e, int amplifier) {
        if (e.level().isClientSide) return;
        double r = 3.0 + amplifier;
        for (LivingEntity t : e.level().getEntitiesOfClass(LivingEntity.class, e.getBoundingBox().inflate(r))) {
            if (t == e) continue;
            t.knockback(1.0 + amplifier, e.getX() - t.getX(), e.getZ() - t.getZ());
        }
    }
}
