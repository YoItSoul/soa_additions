package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Iron Skin — +2 armor per amplifier level. */
public final class IronSkinEffect extends MobEffect {
    public IronSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC0C0C0);
        addAttributeModifier(Attributes.ARMOR, "50aadd01-0003-4001-8001-000000000001",
                2.0, AttributeModifier.Operation.ADDITION);
    }
}
