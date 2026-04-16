package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.google.common.collect.Multimap;
import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * HEAD modifier — increases attack damage.
 * Damage formula: 6 + 2 * (level+1)^1.75
 */
public class ModifierSharpness extends Modifier {

    private static final UUID SHARPNESS_UUID = UUID.fromString("a3f2b1c4-5d6e-7f80-9a1b-2c3d4e5f6a7b");

    public ModifierSharpness() {
        super("sharpness", EnumModifierType.HEAD, 5);
    }

    @Override
    public void getAttributeModifiers(Multimap<Attribute, AttributeModifier> modifiers, int modLevel) {
        double damage = 6.0 + 2.0 * Math.pow(modLevel + 1, 1.75);
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(SHARPNESS_UUID,
                "Modifier Sharpness", damage, AttributeModifier.Operation.ADDITION));
    }
}
