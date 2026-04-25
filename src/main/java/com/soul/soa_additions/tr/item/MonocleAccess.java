package com.soul.soa_additions.tr.item;

import com.soul.soa_additions.tr.ThaumicRemnants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * Bridge for "is the monocle accessible to this player" — checks the player's
 * Curios inventory first (if Curios is installed), then falls back to the
 * regular inventory + off-hand. Mirrors {@code BACuriosHelper}.
 *
 * <p>Class-loading guard: any reference to {@code top.theillusivec4.curios.api}
 * is hidden inside the private helper, only invoked when {@code ModList} reports
 * Curios as loaded. Without Curios in the classpath this class is fully linkable.
 */
public final class MonocleAccess {

    private MonocleAccess() {}

    /** One-shot diagnostic — flips after the first call so we don't spam. */
    private static boolean diagPrinted = false;

    /** Per-side cache: re-check at most every {@link #CACHE_TICKS} game ticks
     *  rather than on every render frame. The monocle's equip state only
     *  changes when the player picks it up or moves it between slots — both
     *  rare events. Cache is keyed by player + game-time so it auto-expires
     *  and works for both client (LocalPlayer) and server (ServerPlayer)
     *  callers without needing dist-aware machinery. */
    private static final java.util.Map<java.util.UUID, CachedResult> CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int CACHE_TICKS = 10;

    private record CachedResult(long expiresAtTick, boolean value) {}

    public static boolean hasMonocle(Player player, Item monocleItem) {
        if (player == null || monocleItem == null) return false;

        long now = player.level().getGameTime();
        java.util.UUID id = player.getUUID();
        CachedResult hit = CACHE.get(id);
        if (hit != null && hit.expiresAtTick > now) {
            return hit.value;
        }

        boolean result = computeHasMonocle(player, monocleItem);
        CACHE.put(id, new CachedResult(now + CACHE_TICKS, result));
        return result;
    }

    /** Force the next hasMonocle() call to recompute. Call from monocle
     *  pickup/drop hooks if we ever add inventory-mutation listeners. */
    public static void invalidate(Player player) {
        if (player != null) CACHE.remove(player.getUUID());
    }

    private static boolean computeHasMonocle(Player player, Item monocleItem) {
        boolean curiosLoaded = ModList.get().isLoaded("curios");
        boolean curiosHit = false;
        Throwable curiosErr = null;
        if (curiosLoaded) {
            try {
                curiosHit = hasInCurios(player, monocleItem);
            } catch (Throwable t) {
                curiosErr = t;
            }
        }
        boolean inventoryHit = hasInPlayerInventory(player, monocleItem);
        boolean result = curiosHit || inventoryHit;
        if (!diagPrinted) {
            diagPrinted = true;
            ThaumicRemnants.LOG.info(
                    "[monocle] hasMonocle first-call: player={}, curiosLoaded={}, curiosHit={}, invHit={}, result={}, err={}",
                    player.getName().getString(), curiosLoaded, curiosHit, inventoryHit, result,
                    curiosErr == null ? "none" : curiosErr.toString());
        }
        return result;
    }

    public static void resetDiag() { diagPrinted = false; }

    private static boolean hasInPlayerInventory(Player player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == item) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && stack.getItem() == item) return true;
        }
        return false;
    }

    private static boolean hasInCurios(Player player, Item item) {
        var inv = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);
        if (inv.resolve().isEmpty()) return false;
        var handler = inv.resolve().get();
        for (var entry : handler.getCurios().entrySet()) {
            var stacks = entry.getValue().getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() == item) return true;
            }
        }
        return false;
    }
}
