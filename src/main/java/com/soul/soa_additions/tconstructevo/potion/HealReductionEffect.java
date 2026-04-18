package com.soul.soa_additions.tconstructevo.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Ports {@code xyz.phanta.tconevo.potion.PotionHealReduction}. Reduces the
 * effective heal amount applied to the bearer; the multiplier comes from the
 * {@link com.soul.soa_additions.tconstructevo.TConEvoConfig#MORTAL_WOUNDS_HEAL_REDUCTION}
 * config so a server admin can tune Mortal Wounds without touching code.
 */
public final class HealReductionEffect extends MobEffect {
    public HealReductionEffect() {
        super(MobEffectCategory.HARMFUL, 0x6E2C2C);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
