package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

/**
 * Magic Shield — applies a stacking Absorption sub-effect every second.
 * Grants 4 absorption HP per amp+1 level on tick.
 */
public final class MagicShieldEffect extends MobEffect {
    public MagicShieldEffect() { super(MobEffectCategory.BENEFICIAL, 0x9C27B0); }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 20 == 0; }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Only top-up if absorption is below the cap for this amp.
        float cap = 4.0F * (amplifier + 1);
        if (entity.getAbsorptionAmount() < cap) {
            entity.setAbsorptionAmount(Math.min(cap, entity.getAbsorptionAmount() + 1.0F));
        }
    }

    /**
     * Absorption is raw persisted state that nothing else decays, so the grant has to be paid
     * back when the effect ends — vanilla's AbsorptionMobEffect pairs them for the same reason.
     * Without this the hearts survived expiry, relogs and world reloads: free armour forever.
     */
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        entity.setAbsorptionAmount(Math.max(0.0F, entity.getAbsorptionAmount() - 4.0F * (amplifier + 1)));
    }
}
