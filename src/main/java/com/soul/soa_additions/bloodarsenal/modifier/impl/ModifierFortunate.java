package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * CORE modifier — Fortune equivalent. Incompatible with Silky.
 */
public class ModifierFortunate extends Modifier {

    public ModifierFortunate() {
        super("fortunate", EnumModifierType.CORE, 3);
    }

    public static void applyToStack(ItemStack stack, int level) {
        stack.enchant(Enchantments.BLOCK_FORTUNE, level + 1);
    }
}
