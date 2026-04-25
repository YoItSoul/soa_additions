package com.soul.soa_additions.tr.network;

import com.soul.soa_additions.tr.ThaumicRemnants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Dedicated network channel for Thaumic Remnants packets. Kept separate from
 * the soa_additions main channel so Thaumic Remnants can be lifted out into
 * its own jar later without packet-id collisions.
 */
public final class TrNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ThaumicRemnants.MODID, "main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    private TrNetworking() {}

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(KnownAspectsSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(KnownAspectsSyncPacket::encode)
                .decoder(KnownAspectsSyncPacket::decode)
                .consumerMainThread(KnownAspectsSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(ScannedTargetsSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ScannedTargetsSyncPacket::encode)
                .decoder(ScannedTargetsSyncPacket::decode)
                .consumerMainThread(ScannedTargetsSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(MonocleScanRequestPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MonocleScanRequestPacket::encode)
                .decoder(MonocleScanRequestPacket::decode)
                .consumerMainThread(MonocleScanRequestPacket::handle)
                .add();
    }
}
