package com.soul.soa_additions.network;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.net.QuestCheckmarkPacket;
import com.soul.soa_additions.quest.net.QuestClaimPacket;
import com.soul.soa_additions.quest.net.ChapterEditPacket;
import com.soul.soa_additions.quest.net.QuestDefinitionSyncPacket;
import com.soul.soa_additions.quest.net.QuestEditPacket;
import com.soul.soa_additions.quest.net.QuestEditStatePacket;
import com.soul.soa_additions.quest.net.QuestMovePacket;
import com.soul.soa_additions.quest.net.QuestSubmitPacket;
import com.soul.soa_additions.quest.net.QuestSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(SoaAdditions.MODID, "main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    private ModNetworking() {}

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(ClientModReportPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ClientModReportPacket::encode)
                .decoder(ClientModReportPacket::decode)
                .consumerMainThread(ClientModReportPacket::handle)
                .add();

        CHANNEL.messageBuilder(QuestSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(QuestSyncPacket::encode)
                .decoder(QuestSyncPacket::decode)
                .consumerMainThread(QuestSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(QuestClaimPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QuestClaimPacket::encode)
                .decoder(QuestClaimPacket::decode)
                .consumerMainThread(QuestClaimPacket::handle)
                .add();

        CHANNEL.messageBuilder(QuestCheckmarkPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QuestCheckmarkPacket::encode)
                .decoder(QuestCheckmarkPacket::decode)
                .consumerMainThread(QuestCheckmarkPacket::handle)
                .add();

        CHANNEL.messageBuilder(QuestSubmitPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QuestSubmitPacket::encode)
                .decoder(QuestSubmitPacket::decode)
                .consumerMainThread(QuestSubmitPacket::handle)
                .add();

        CHANNEL.messageBuilder(QuestEditStatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(QuestEditStatePacket::encode)
                .decoder(QuestEditStatePacket::decode)
                .consumerMainThread(QuestEditStatePacket::handle)
                .add();

        // Move packet is bidirectional — same class, no direction locked in
        // the builder, so the framework dispatches based on the receiving side.
        CHANNEL.messageBuilder(QuestMovePacket.class, id++)
                .encoder(QuestMovePacket::encode)
                .decoder(QuestMovePacket::decode)
                .consumerMainThread(QuestMovePacket::handle)
                .add();

        CHANNEL.messageBuilder(QuestEditPacket.class, id++)
                .encoder(QuestEditPacket::encode)
                .decoder(QuestEditPacket::decode)
                .consumerMainThread(QuestEditPacket::handle)
                .add();

        CHANNEL.messageBuilder(ChapterEditPacket.class, id++)
                .encoder(ChapterEditPacket::encode)
                .decoder(ChapterEditPacket::decode)
                .consumerMainThread(ChapterEditPacket::handle)
                .add();

        CHANNEL.messageBuilder(QuestDefinitionSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(QuestDefinitionSyncPacket::encode)
                .decoder(QuestDefinitionSyncPacket::decode)
                .consumerMainThread(QuestDefinitionSyncPacket::handle)
                .add();
    }
}
