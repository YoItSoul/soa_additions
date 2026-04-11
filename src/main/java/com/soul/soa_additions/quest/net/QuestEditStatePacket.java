package com.soul.soa_additions.quest.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server→client toggle for quest edit mode. Sent whenever an op's edit flag
 * changes so the client GUI can show/hide the editor affordances (drag-to-move,
 * save indicators, etc.). Plain boolean payload — no chapter/quest scope.
 */
public record QuestEditStatePacket(boolean enabled) {

    public static void encode(QuestEditStatePacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.enabled);
    }

    public static QuestEditStatePacket decode(FriendlyByteBuf buf) {
        return new QuestEditStatePacket(buf.readBoolean());
    }

    public static void handle(QuestEditStatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientQuestState.setEditMode(pkt.enabled);
        }));
        c.setPacketHandled(true);
    }
}
