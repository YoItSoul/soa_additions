package com.soul.soa_additions.anticheat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Per-player "I accept being flagged as a cheater for the commands I run"
 * opt-in. Stored on Forge-persisted player NBT so it survives death and
 * logout, but not across world transfers (which is intentional — each world
 * is a fresh clean-run context).
 *
 * <p>By default, an OP running a non-safe command is <em>blocked</em> rather
 * than flagged. The player has to explicitly enable this flag via
 * {@code /soa quests cheatermode true} to acknowledge that subsequent
 * cheat-worthy commands will flag them. This protects legitimate admin
 * tasks like {@code /reload} from drive-by flagging without forcing admins
 * to give up the ability to cheat-test when they actually want to.</p>
 */
public final class CheaterModeOptIn {

    private static final String NBT_KEY = "soa_cheatermode_opt_in";

    private CheaterModeOptIn() {}

    public static boolean isEnabled(ServerPlayer player) {
        return persistedSubTag(player).getBoolean(NBT_KEY);
    }

    public static void setEnabled(ServerPlayer player, boolean enabled) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        if (enabled) {
            persisted.putBoolean(NBT_KEY, true);
        } else {
            persisted.remove(NBT_KEY);
        }
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static CompoundTag persistedSubTag(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }
}
