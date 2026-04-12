package com.soul.soa_additions.donor;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Manages spawning and despawning of donor orbs. Orbs are transient — they
 * are spawned when a donor logs in and discarded when they log out or lose
 * donor status. They are never persisted to disk.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DonorOrbManager {

    private DonorOrbManager() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            if (DonorRegistry.isDonor(sp.getUUID())) {
                // Small delay to let the player fully load in
                sp.getServer().execute(() -> spawnOrb(sp));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            removeOrb(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            if (DonorRegistry.isDonor(sp.getUUID())) {
                // Respawn orb in the new dimension
                sp.getServer().execute(() -> spawnOrb(sp));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            if (DonorRegistry.isDonor(sp.getUUID())) {
                sp.getServer().execute(() -> spawnOrb(sp));
            }
        }
    }

    /** Spawn a donor orb for the given player. Removes any existing orb first. */
    public static void spawnOrb(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // Remove existing orb first
        removeOrb(player);

        DonorData donor = DonorRegistry.get(player.getUUID()).orElse(null);
        if (donor == null) return;

        DonorOrbEntity orb = new DonorOrbEntity(ModEntities.DONOR_ORB.get(), level);
        orb.setOwnerUUID(player.getUUID());
        orb.setOrbColor(donor.tier().color);
        orb.randomizeOrbit();
        orb.setPos(player.getX() + 1, player.getY() + 2, player.getZ());

        level.addFreshEntity(orb);
    }

    /** Remove any donor orbs belonging to the given player. */
    public static void removeOrb(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // Search nearby for orbs belonging to this player
        AABB searchBox = player.getBoundingBox().inflate(100);
        List<DonorOrbEntity> orbs = level.getEntitiesOfClass(
                DonorOrbEntity.class, searchBox,
                orb -> orb.getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false));
        for (DonorOrbEntity orb : orbs) {
            orb.discard();
        }
    }
}
