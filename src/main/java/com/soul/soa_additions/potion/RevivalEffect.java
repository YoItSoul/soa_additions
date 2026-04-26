package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Revival — marker effect; the auto-revive happens in
 * {@link SoaPotionEvents#onLivingDeath which intercepts the death event,
 * heals the entity to half-HP, applies Regen IV + Resistance V, and
 * consumes the effect.
 */
public final class RevivalEffect extends MobEffect {
    public RevivalEffect() { super(MobEffectCategory.BENEFICIAL, 0xE91E63); }
}
