package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Solid Core — +0.5 knockback resistance per amp (cap at 1.0). */
public final class SolidCoreEffect extends MobEffect {
    public SolidCoreEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x37474F);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                "50aadd01-0006-4001-8001-000000000001",
                0.5, AttributeModifier.Operation.ADDITION);
    }
}
