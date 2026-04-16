package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.quest.progress.ClaimService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client→server claim request. Server re-validates status via
 * {@link ClaimService#claim} (never trusts the client's view of READY) and
 * responds with a {@link QuestDeltaPacket} covering only the quests whose
 * state changed (claimed + any auto-claimed downstream).
 */
public record QuestClaimPacket(String fullQuestId) {

    public static void encode(QuestClaimPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.fullQuestId);
    }

    public static QuestClaimPacket decode(FriendlyByteBuf buf) {
        return new QuestClaimPacket(buf.readUtf());
    }

    public static void handle(QuestClaimPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player == null) return;
            QuestDeltaPacket.Capture delta = QuestDeltaPacket.Capture.of(player);
            ClaimService.claim(player, pkt.fullQuestId);
            delta.sendChanges(player);
        });
        c.setPacketHandled(true);
    }
}
