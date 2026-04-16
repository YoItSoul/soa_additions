package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * CORE modifier — Silk Touch equivalent.
 * Incompatible with Fortunate and Smelting.
 */
public class ModifierSilky extends Modifier {

    public ModifierSilky() {
        super("silky", EnumModifierType.CORE, 1);
    }

    public static void applyToStack(ItemStack stack, int level) {
        stack.enchant(Enchantments.SILK_TOUCH, 1);
    }
}
