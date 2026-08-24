package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Health Boost — +4 max HP per amplifier level. Mirrors vanilla Health Boost
 * but registered separately so GC's golden_apple brew can target a SoA-owned
 * effect (avoids fighting vanilla's brewing recipe registration).
 */
public final class HealthBoostEffect extends MobEffect {

    /**
     * Vanilla Health Boost clamps current health when the bonus goes away; without it the entity
     * keeps the extra points until the next setHealth silently trims them, and the HUD overdraws
     * hearts past the end of the row in the meantime.
     */
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }

    public HealthBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF87D23);
        // UUID prefix 50AADD01 = "SOAADD01" leetspeak (all hex). Stable across
        // restarts so /attribute base set doesn't accumulate orphaned modifiers.
        addAttributeModifier(Attributes.MAX_HEALTH, "50aadd01-0001-4001-8001-000000000001",
                4.0, AttributeModifier.Operation.ADDITION);
    }
}
