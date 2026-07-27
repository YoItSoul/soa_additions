package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;

/** Love — nearby adult animals periodically enter love mode. PotionCore port. */
public final class LoveEffect extends MobEffect {
    public LoveEffect() { super(MobEffectCategory.BENEFICIAL, 0xFF69B4); }

    @Override public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 40 == 0; }

    @Override public void applyEffectTick(LivingEntity e, int amplifier) {
        if (e.level().isClientSide) return;
        double r = 4.0 + amplifier;
        for (Animal a : e.level().getEntitiesOfClass(Animal.class, e.getBoundingBox().inflate(r))) {
            if (a.getAge() == 0 && !a.isInLove()) a.setInLove(null);
        }
    }
}
