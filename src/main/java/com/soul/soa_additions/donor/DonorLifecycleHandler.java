package com.soul.soa_additions.donor;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Wires the donor registry into server lifecycle events. Loads on server
 * start, saves on stop, and syncs the full list to each player on login
 * (and to all players whenever the list changes).
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DonorLifecycleHandler {

    private DonorLifecycleHandler() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        DonorRegistry.init(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        DonorRegistry.shutdown();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            syncTo(sp);
        }
    }

    /** Send the full donor list to a single player. */
    public static void syncTo(ServerPlayer player) {
        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new DonorSyncPacket(DonorRegistry.all()));
    }

    /** Broadcast the donor list to all connected players. */
    public static void syncToAll() {
        ModNetworking.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new DonorSyncPacket(DonorRegistry.all()));
    }
}
