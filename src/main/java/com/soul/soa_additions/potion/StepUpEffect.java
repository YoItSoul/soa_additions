package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.common.ForgeMod;

/** Step Up — +0.5 block step height per amplifier level (cap at 1.5). */
public final class StepUpEffect extends MobEffect {
    public StepUpEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
        addAttributeModifier(ForgeMod.STEP_HEIGHT_ADDITION.get(),
                "50aadd01-0007-4001-8001-000000000001",
                0.5, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
    }
}
