package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Diamond Skin — +4 armor and +2 toughness per amplifier level. */
public final class DiamondSkinEffect extends MobEffect {
    public DiamondSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4ECDC4);
        addAttributeModifier(Attributes.ARMOR, "50aadd01-0002-4001-8001-000000000001",
                4.0, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "50aadd01-0002-4001-8002-000000000001",
                2.0, AttributeModifier.Operation.ADDITION);
    }
}
