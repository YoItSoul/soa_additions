package com.soul.soa_additions.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Hands an item to a player, falling back to a drop when the inventory is full.
 *
 * <p>The point of routing this through one place is the ordering hazard, not
 * the drop itself: callers that shrink the triggering item <em>before</em>
 * delivering let {@link net.minecraft.world.entity.player.Inventory#add} pick
 * the now-free hand slot, which vanilla's
 * {@code ServerPlayerGameMode#useItem} then overwrites with
 * {@link ItemStack#EMPTY} — the reward silently disappears. Deliver first,
 * shrink second. See {@code RewardTicketItem} for the full write-up.</p>
 *
 * <p>The fallback is a plain Q-drop: thrown out in front of the player with
 * the usual pickup delay, exactly like pressing the drop key.</p>
 */
public final class ItemDelivery {

    private ItemDelivery() {}

    /**
     * Add {@code stack} to the player's inventory, or drop it in front of them
     * if there's no room.
     *
     * @return true if it went into the inventory, false if it was dropped
     */
    public static boolean give(Player player, ItemStack stack) {
        if (stack.isEmpty()) return true;

        if (player.getInventory().add(stack)) return true;

        drop(player, stack);
        return false;
    }

    /** Q-drop the stack: thrown in front of the player, standard pickup delay. */
    public static void drop(Player player, ItemStack stack) {
        if (stack.isEmpty() || player.level().isClientSide) return;
        player.drop(stack, false);
    }
}
