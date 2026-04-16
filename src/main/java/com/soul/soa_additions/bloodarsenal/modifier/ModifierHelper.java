package com.soul.soa_additions.bloodarsenal.modifier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Static helpers for reading/writing modifier data on {@link net.minecraft.world.item.ItemStack}s,
 * primarily used by Modifier Tomes and the Sanguine Infusion system.
 *
 * <p>NBT keys match the original Blood Arsenal 1.12 format for parity.</p>
 */
public final class ModifierHelper {

    private static final String TAG_KEY = "key";
    private static final String TAG_LEVEL = "level";
    private static final String TAG_READY = "readyToUpgrade";

    private ModifierHelper() {}

    // ── Key (modifier identifier) ────────────────────────────────────────

    public static String getKey(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return "";
        return stack.getTag().getString(TAG_KEY);
    }

    public static void setKey(ItemStack stack, String key) {
        stack.getOrCreateTag().putString(TAG_KEY, key);
    }

    // ── Level ────────────────────────────────────────────────────────────

    public static int getLevel(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return stack.getTag().getInt(TAG_LEVEL);
    }

    public static void setLevel(ItemStack stack, int level) {
        stack.getOrCreateTag().putInt(TAG_LEVEL, level);
    }

    // ── Ready to upgrade ─────────────────────────────────────────────────

    public static boolean isReadyToUpgrade(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        return stack.getTag().getBoolean(TAG_READY);
    }

    public static void setReadyToUpgrade(ItemStack stack, boolean ready) {
        stack.getOrCreateTag().putBoolean(TAG_READY, ready);
    }
}
