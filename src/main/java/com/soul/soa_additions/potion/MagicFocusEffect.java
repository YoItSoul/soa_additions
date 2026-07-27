package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Magic Focus — +2 attack damage per amplifier level (PotionCore magic-damage port;
 *  1.20 has no vanilla magic-damage attribute, so boosts dealt damage as the proxy). */
public final class MagicFocusEffect extends MobEffect {
    public MagicFocusEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9932CC);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "50aadd01-0021-4001-8001-000000000001",
                2.0, AttributeModifier.Operation.ADDITION);
    }
}
