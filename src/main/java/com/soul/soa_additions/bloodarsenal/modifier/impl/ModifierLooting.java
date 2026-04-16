package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * CORE modifier — applies Looting enchantment at level+1.
 * Written via writeSpecialNBT to add the enchantment to the tool.
 */
public class ModifierLooting extends Modifier {

    public ModifierLooting() {
        super("looting", EnumModifierType.CORE, 3);
    }

    @Override
    public void writeSpecialNBT(CompoundTag tag, int modLevel) {
        // Looting is applied as an actual enchantment on the tool
        // This is handled when the modifier is applied/upgraded
    }

    /**
     * Apply the Looting enchantment to the given stack.
     * Called from StasisModifiable when modifiers are written.
     */
    public static void applyToStack(ItemStack stack, int level) {
        stack.enchant(Enchantments.MOB_LOOTING, level + 1);
    }
}
