package com.soul.soa_additions.donor;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages spawning and despawning of donor orbs. Orbs are tied to having
 * the Donor Token equipped in the Curios "donor" slot — not just donor
 * status alone. A periodic tick check ensures the orb despawns when the
 * token is unequipped and spawns when it's equipped.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DonorOrbManager {

    /** Check interval in ticks (every 20 ticks = 1 second). */
    private static final int CHECK_INTERVAL = 20;

    // Owner UUID → live orb. Replaces the previous 200-block AABB scan that
    // ran every second per online player. Kept in sync by spawn/remove paths
    // and by EntityJoinLevelEvent/EntityLeaveLevelEvent for chunk reload cases.
    private static final Map<UUID, DonorOrbEntity> ORBS_BY_OWNER = new ConcurrentHashMap<>();

    private DonorOrbManager() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            sp.getServer().execute(() -> syncOrb(sp));
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
            sp.getServer().execute(() -> syncOrb(sp));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            sp.getServer().execute(() -> syncOrb(sp));
        }
    }

    /** Periodic check — picks up curio equip/unequip without needing a
     *  Curios-specific event listener (keeps the dependency optional). */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        if (server.getTickCount() % CHECK_INTERVAL != 0) return;

        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            syncOrb(sp);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof DonorOrbEntity orb) {
            orb.getOwnerUUID().ifPresent(id -> ORBS_BY_OWNER.put(id, orb));
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof DonorOrbEntity orb) {
            orb.getOwnerUUID().ifPresent(id -> ORBS_BY_OWNER.remove(id, orb));
        }
    }

    /** Ensure the orb state matches whether the donor token is equipped. */
    public static void syncOrb(ServerPlayer player) {
        boolean shouldHaveOrb = DonorRegistry.isDonor(player.getUUID())
                && DonorCuriosHelper.hasDonorTokenEquipped(player);
        boolean hasOrb = hasExistingOrb(player);

        if (shouldHaveOrb && !hasOrb) {
            spawnOrb(player);
        } else if (!shouldHaveOrb && hasOrb) {
            removeOrb(player);
        }
    }

    private static boolean hasExistingOrb(ServerPlayer player) {
        DonorOrbEntity orb = ORBS_BY_OWNER.get(player.getUUID());
        return orb != null && orb.isAlive();
    }

    /** Spawn a donor orb for the given player. Removes any existing orb first. */
    public static void spawnOrb(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        removeOrb(player);

        DonorData donor = DonorRegistry.get(player.getUUID()).orElse(null);
        if (donor == null) return;

        DonorOrbEntity orb = new DonorOrbEntity(ModEntities.DONOR_ORB.get(), level);
        orb.setOwnerUUID(player.getUUID());
        orb.setOrbColor(donor.tier().color);
        orb.randomizeOrbit();
        orb.setPos(player.getX() + 1, player.getY() + 2, player.getZ());

        level.addFreshEntity(orb);
        ORBS_BY_OWNER.put(player.getUUID(), orb);
    }

    /** Remove any donor orbs belonging to the given player. */
    public static void removeOrb(ServerPlayer player) {
        DonorOrbEntity orb = ORBS_BY_OWNER.remove(player.getUUID());
        if (orb != null && !orb.isRemoved()) {
            orb.discard();
        }
    }
}
