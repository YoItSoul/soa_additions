package com.soul.soa_additions.cyclicaqua;

import com.soul.soa_additions.compat.GameStagesCompat;
import com.soul.soa_additions.config.CyclicFisherConfig;
import com.teammetallurgy.aquaculture.api.fishing.Hook;
import com.teammetallurgy.aquaculture.api.fishing.Hooks;
import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Ol' Withy.
 *
 * <p>A rod renamed {@code Ol' Withy} (case-insensitive), wearing a Nether Star
 * Hook, in the hands of someone holding the {@code wither_slayer} stage, has a
 * flat 0.1% chance per catch of pulling up a Wither instead of loot. Luck, lure,
 * bait and open water do not move that number.</p>
 *
 * <p>Both ways of fishing are covered. Hand-casting goes through
 * {@link #onItemFished}, which cancels the catch so the Wither replaces the
 * drops. The Cyclic Fishing Net goes through {@link #tryNetCatch}; a block has
 * no angler, so it borrows the nearest player within a configurable radius and
 * reads that player's stages — nobody in range means no roll.</p>
 *
 * <p>Registered on the Forge bus only when Aquaculture is present, since the
 * hook check is an Aquaculture concept.</p>
 */
public final class WitherCatch {

    private WitherCatch() {}

    /**
     * Hand-casting. Fires after Aquaculture has built the drops but before it
     * spawns them, so cancelling here means the Wither is the catch.
     */
    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!CyclicFisherConfig.WITHER_CATCH_HAND_FISHING.get() || !enabled()) {
            return;
        }
        // Null angler: a machine-fired event, not a cast. The Fishing Net has its
        // own path (tryNetCatch) and only reaches this bus at all when the
        // postItemFishedEvent option is on, so ignore anything player-less here
        // rather than rolling twice for the same catch.
        Player player = event.getEntity();
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack rod = findWithyRod(player);
        if (rod == null || !roll(level)) {
            return;
        }
        if (!hasStage(player)) {
            return;
        }
        event.setCanceled(true);
        summon(level, event.getHookEntity().position());
    }

    /**
     * Fishing Net. Called once per successful catch, before the loot is
     * dropped; returning true means the caller should skip the drops.
     */
    static boolean tryNetCatch(ServerLevel level, BlockPos center, ItemStack rod, Hook hook) {
        if (!enabled() || hook != Hooks.NETHER_STAR || !isWithy(rod) || !roll(level)) {
            return false;
        }
        Vec3 origin = Vec3.atCenterOf(center);
        double radius = CyclicFisherConfig.WITHER_CATCH_NET_PLAYER_RADIUS.get();
        Player angler = level.getNearestPlayer(origin.x, origin.y, origin.z, radius, false);
        if (angler == null || !hasStage(angler)) {
            return false;
        }
        summon(level, origin);
        return true;
    }

    private static boolean enabled() {
        return CyclicFisherConfig.WITHER_CATCH_ENABLED.get();
    }

    private static boolean roll(ServerLevel level) {
        return level.getRandom().nextDouble() < CyclicFisherConfig.WITHER_CATCH_CHANCE.get();
    }

    private static boolean hasStage(Player player) {
        return GameStagesCompat.hasStage(player, CyclicFisherConfig.WITHER_CATCH_STAGE.get());
    }

    /** The rod in either hand, if it is named right and wears a Nether Star Hook. */
    private static ItemStack findWithyRod(Player player) {
        for (ItemStack held : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (isWithy(held) && AquaFishingRodItem.getHookType(held) == Hooks.NETHER_STAR) {
                return held;
            }
        }
        return null;
    }

    private static boolean isWithy(ItemStack rod) {
        return !rod.isEmpty()
                && rod.hasCustomHoverName()
                && rod.getHoverName().getString().trim()
                        .equalsIgnoreCase(CyclicFisherConfig.WITHER_CATCH_ROD_NAME.get());
    }

    private static void summon(ServerLevel level, Vec3 pos) {
        WitherBoss wither = EntityType.WITHER.create(level);
        if (wither == null) {
            return;
        }
        wither.moveTo(pos.x, pos.y + 1.0D, pos.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        // The full 220-tick charge-up and blue shell, exactly as a built Wither.
        wither.makeInvulnerable();
        level.addFreshEntity(wither);
    }
}
