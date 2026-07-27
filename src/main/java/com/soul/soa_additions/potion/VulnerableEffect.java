package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Vulnerable — -8 armor per amplifier level (take more damage). PotionCore port. */
public final class VulnerableEffect extends MobEffect {
    public VulnerableEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
        addAttributeModifier(Attributes.ARMOR, "50aadd01-0020-4001-8001-000000000001",
                -8.0, AttributeModifier.Operation.ADDITION);
    }
}
