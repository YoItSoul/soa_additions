package com.soul.soa_additions.bloodarsenal.item.bauble;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * Safe bridge to Curios API for Blood Arsenal baubles.
 * Falls back to scanning player inventory when Curios is absent.
 */
public final class BACuriosHelper {

    private BACuriosHelper() {}

    /**
     * Finds an equipped item of the given type in Curios slots first,
     * then falls back to regular inventory.
     */
    public static ItemStack findEquipped(Player player, Item item) {
        if (ModList.get().isLoaded("curios")) {
            try {
                ItemStack found = findInCurios(player, item);
                if (!found.isEmpty()) return found;
            } catch (Throwable ignored) {
            }
        }
        return findInInventory(player, item);
    }

    /**
     * Checks if the player has the given item equipped (Curios or inventory).
     */
    public static boolean hasEquipped(Player player, Item item) {
        return !findEquipped(player, item).isEmpty();
    }

    private static ItemStack findInCurios(Player player, Item item) {
        var inv = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);
        if (inv.resolve().isEmpty()) return ItemStack.EMPTY;
        var handler = inv.resolve().get();
        for (var entry : handler.getCurios().entrySet()) {
            var stacks = entry.getValue().getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() == item) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findInInventory(Player player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
