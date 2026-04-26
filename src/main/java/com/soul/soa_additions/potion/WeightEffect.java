package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

/**
 * Weight — heavier gravity. Negative jump-strength + entity gravity scaler.
 * GC PotionCore's "weight" pulled players/mobs harder toward the ground.
 */
public final class WeightEffect extends MobEffect {
    public WeightEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A148C);
        // Forge's ENTITY_GRAVITY (1.20.1) is the right knob. Multiply by 1.5x per amp.
        addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(),
                "50aadd01-0005-4001-8001-000000000001",
                0.04, AttributeModifier.Operation.ADDITION);  // base gravity is 0.08
    }
}
