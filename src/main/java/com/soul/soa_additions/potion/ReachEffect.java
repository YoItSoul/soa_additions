package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.common.ForgeMod;

/** Reach — +1 block per amplifier level on both block and entity reach. */
public final class ReachEffect extends MobEffect {
    public ReachEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB39DDB);
        // Forge's BLOCK_REACH and ENTITY_REACH are the 1.20.1 targets.
        addAttributeModifier(ForgeMod.BLOCK_REACH.get(), "50aadd01-0004-4001-8001-000000000001",
                1.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
        addAttributeModifier(ForgeMod.ENTITY_REACH.get(), "50aadd01-0004-4001-8001-000000000002",
                1.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
    }
}
