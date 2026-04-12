package com.soul.soa_additions.donor;

import com.soul.soa_additions.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * Safe bridge to the Curios API. All calls are guarded behind
 * {@link ModList#isLoaded} so the mod works without Curios installed.
 * When Curios is absent, {@link #hasDonorTokenEquipped} always returns
 * false (orbs simply won't spawn).
 */
public final class DonorCuriosHelper {

    private DonorCuriosHelper() {}

    /** Whether the player has a donor token in their Curios "donor" slot. */
    public static boolean hasDonorTokenEquipped(Player player) {
        if (!ModList.get().isLoaded("curios")) return false;
        try {
            return checkCuriosSlot(player);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean checkCuriosSlot(Player player) {
        var optional = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);
        if (optional.resolve().isEmpty()) return false;
        var handler = optional.resolve().get();
        var stacksOpt = handler.getStacksHandler("donor");
        if (stacksOpt.isEmpty()) return false;
        var stacks = stacksOpt.get().getStacks();
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.ORB_OF_AVARICE.get()) {
                return true;
            }
        }
        return false;
    }
}
