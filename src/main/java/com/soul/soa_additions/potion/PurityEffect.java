package com.soul.soa_additions.potion;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/** Purity — strips all negative effects every (40 / (amp+1)) ticks. */
public final class PurityEffect extends MobEffect {
    public PurityEffect() { super(MobEffectCategory.BENEFICIAL, 0xFFFFFF); }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % Math.max(2, 40 / (amplifier + 1)) == 0;
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        List<net.minecraft.world.effect.MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance e : entity.getActiveEffects()) {
            if (e.getEffect().getCategory() == MobEffectCategory.HARMFUL) toRemove.add(e.getEffect());
        }
        for (var e : toRemove) entity.removeEffect(e);
    }
}
