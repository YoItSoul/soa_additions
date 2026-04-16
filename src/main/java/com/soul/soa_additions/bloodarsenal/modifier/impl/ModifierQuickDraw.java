package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.google.common.collect.Multimap;
import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * HANDLE modifier — increases attack speed.
 * Speed bonus = (level+1) * 2 / 4.
 * Tracker increments every 200 ticks of holding.
 */
public class ModifierQuickDraw extends Modifier {

    private static final UUID QUICKDRAW_UUID = UUID.fromString("c5d4e3f2-1a0b-9c8d-7e6f-5a4b3c2d1e0f");

    public ModifierQuickDraw() {
        super("quick_draw", EnumModifierType.HANDLE, 5);
    }

    @Override
    public void getAttributeModifiers(Multimap<Attribute, AttributeModifier> modifiers, int modLevel) {
        double speedBonus = (modLevel + 1) * 2 / 4;
        modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(QUICKDRAW_UUID,
                "Modifier QuickDraw", speedBonus, AttributeModifier.Operation.ADDITION));
    }
}
