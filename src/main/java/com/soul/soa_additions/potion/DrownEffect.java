package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Drown — steadily depletes air, drowning you even on land. PotionCore port. */
public final class DrownEffect extends MobEffect {
    public DrownEffect() { super(MobEffectCategory.HARMFUL, 0x1E3A5F); }

    @Override public boolean isDurationEffectTick(int duration, int amplifier) { return true; }

    @Override public void applyEffectTick(LivingEntity e, int amplifier) {
        if (e.level().isClientSide) return;
        e.setAirSupply(e.getAirSupply() - (2 + amplifier));
        if (e.getAirSupply() <= -20) {
            e.setAirSupply(0);
            e.hurt(e.damageSources().drown(), 2.0f);
        }
    }
}
