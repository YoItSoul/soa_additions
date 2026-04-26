package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Explode — marker effect; the explosion is fired in
 * {@link SoaPotionEvents#onLivingDeath. Amplifier scales explosion radius
 * (3.0F + amp). Non-griefing (no block damage).
 */
public final class ExplodeEffect extends MobEffect {
    public ExplodeEffect() { super(MobEffectCategory.HARMFUL, 0x424242); }
    public static float radiusFor(int amplifier) { return 3.0F + amplifier; }
}
