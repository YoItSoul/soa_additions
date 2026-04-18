package com.soul.soa_additions.tconstructevo.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Ports {@code xyz.phanta.tconevo.potion.PotionDamageReduction}. Incoming
 * damage is multiplied down in the shared hurt-event handler based on the
 * amplifier, identical to the 1.12.2 scaling (20 % reduction per level,
 * capped at 80 %).
 */
public final class DamageReductionEffect extends MobEffect {
    public static final float REDUCTION_PER_LEVEL = 0.20F;
    public static final float MAX_REDUCTION = 0.80F;

    public DamageReductionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4F7CAC);
    }

    public static float incomingMultiplierFor(int amplifier) {
        float reduction = Math.min(MAX_REDUCTION, REDUCTION_PER_LEVEL * (amplifier + 1));
        return 1F - reduction;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
