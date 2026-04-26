package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Extension — extends every other beneficial effect's duration by 1t per tick.
 * GC PotionCore's tooltip claimed this "extends OTHER potions you have." We
 * replicate with a per-tick scan over activeEffects.
 */
public final class ExtensionEffect extends MobEffect {
    public ExtensionEffect() { super(MobEffectCategory.BENEFICIAL, 0xC9A227); }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return true; }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        for (MobEffectInstance other : entity.getActiveEffects()) {
            if (other.getEffect() == this) continue;
            if (!other.getEffect().isBeneficial()) continue;
            // Nudge duration up by 1 tick per amplifier (so we don't immediately
            // drain when the holder also has Extension I/II).
            other.update(new MobEffectInstance(other.getEffect(),
                    other.getDuration() + (amplifier + 1),
                    other.getAmplifier(), other.isAmbient(), other.isVisible()));
        }
    }
}
