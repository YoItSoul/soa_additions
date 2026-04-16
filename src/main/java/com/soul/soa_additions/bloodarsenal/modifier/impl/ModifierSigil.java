package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * ABILITY modifier — wraps a sigil item in the tool's NBT.
 * Right-click toggles the sigil. Ticks the wrapped sigil every 100 ticks.
 * The sigil item is stored in NBT as "itemStack" with an "activated" boolean.
 */
public class ModifierSigil extends Modifier {

    private static final String TAG_SIGIL_STACK = "sigilItemStack";
    private static final String TAG_SIGIL_ACTIVE = "sigilActivated";

    public ModifierSigil() {
        super("sigil", EnumModifierType.ABILITY, 1);
    }

    @Override
    public void onRightClick(Level level, Player player, ItemStack stack, int modLevel) {
        if (level.isClientSide()) return;

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_SIGIL_STACK)) return;

        // Toggle sigil activation
        boolean active = tag.getBoolean(TAG_SIGIL_ACTIVE);
        tag.putBoolean(TAG_SIGIL_ACTIVE, !active);
    }

    @Override
    public void onUpdate(Level level, Player player, ItemStack stack, int slotIndex, int modLevel) {
        if (level.isClientSide()) return;

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_SIGIL_STACK) || !tag.getBoolean(TAG_SIGIL_ACTIVE)) return;

        // Tick the wrapped sigil every 100 ticks
        if (level.getGameTime() % 100 == 0) {
            ItemStack sigilStack = ItemStack.of(tag.getCompound(TAG_SIGIL_STACK));
            if (!sigilStack.isEmpty()) {
                sigilStack.getItem().inventoryTick(sigilStack, level, player, slotIndex, false);
                // Save updated sigil state back
                tag.put(TAG_SIGIL_STACK, sigilStack.save(new CompoundTag()));
            }
        }
    }

    @Override
    public void writeSpecialNBT(CompoundTag tag, int modLevel) {
        // Sigil data is managed directly on the stack tag
    }

    /** Set the wrapped sigil on a tool stack */
    public static void setSigilStack(ItemStack toolStack, ItemStack sigilStack) {
        CompoundTag tag = toolStack.getOrCreateTag();
        tag.put(TAG_SIGIL_STACK, sigilStack.save(new CompoundTag()));
        tag.putBoolean(TAG_SIGIL_ACTIVE, false);
    }

    /** Get the wrapped sigil from a tool stack */
    public static ItemStack getSigilStack(ItemStack toolStack) {
        CompoundTag tag = toolStack.getTag();
        if (tag == null || !tag.contains(TAG_SIGIL_STACK)) return ItemStack.EMPTY;
        return ItemStack.of(tag.getCompound(TAG_SIGIL_STACK));
    }
}
