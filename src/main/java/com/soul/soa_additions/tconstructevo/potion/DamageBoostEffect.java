package com.soul.soa_additions.tconstructevo.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Ports {@code xyz.phanta.tconevo.potion.PotionDamageBoost}. The +damage
 * multiplier is applied in {@link com.soul.soa_additions.tconstructevo.event.TConEvoEventHandler}
 * during {@code LivingHurtEvent}, so the effect class itself only carries
 * registry presence and amplifier data.
 */
public final class DamageBoostEffect extends MobEffect {
    /** +20% damage dealt per amplifier level. */
    public static final float MULT_PER_LEVEL = 0.20F;

    public DamageBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC4302F);
    }

    public static float multiplierFor(int amplifier) {
        return 1F + MULT_PER_LEVEL * (amplifier + 1);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
