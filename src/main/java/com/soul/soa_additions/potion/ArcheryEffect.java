package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Archery — flat +arrow damage. Pure marker effect; the actual damage
 * boost is applied by {@link com.soul.soa_additions.potion.SoaPotionEvents
 * via ProjectileImpactEvent / LivingHurtEvent for any arrow whose owner
 * has this effect active.
 */
public final class ArcheryEffect extends MobEffect {
    public ArcheryEffect() { super(MobEffectCategory.BENEFICIAL, 0x8FBC8F); }
    public static float multiplierFor(int amplifier) { return 1.0F + 0.25F * (amplifier + 1); }
}
