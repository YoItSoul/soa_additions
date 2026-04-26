package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Health Boost — +4 max HP per amplifier level. Mirrors vanilla Health Boost
 * but registered separately so GC's golden_apple brew can target a SoA-owned
 * effect (avoids fighting vanilla's brewing recipe registration).
 */
public final class HealthBoostEffect extends MobEffect {
    public HealthBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF87D23);
        // UUID prefix 50AADD01 = "SOAADD01" leetspeak (all hex). Stable across
        // restarts so /attribute base set doesn't accumulate orphaned modifiers.
        addAttributeModifier(Attributes.MAX_HEALTH, "50aadd01-0001-4001-8001-000000000001",
                4.0, AttributeModifier.Operation.ADDITION);
    }
}
