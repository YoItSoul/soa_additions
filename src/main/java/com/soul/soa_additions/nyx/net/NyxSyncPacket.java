package com.soul.soa_additions.nyx.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → client sync of the active lunar event ("" = none) and tracked meteor
 * landing sites (for the Meteor Finder pointer). Sent periodically per level.
 */
public record NyxSyncPacket(String eventName, List<BlockPos> landingSites) {

    public static void encode(NyxSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.eventName);
        buf.writeVarInt(pkt.landingSites.size());
        for (BlockPos pos : pkt.landingSites) buf.writeBlockPos(pos);
    }

    public static NyxSyncPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        int n = buf.readVarInt();
        List<BlockPos> sites = new ArrayList<>(n);
        for (int i = 0; i < n; i++) sites.add(buf.readBlockPos());
        return new NyxSyncPacket(name, sites);
    }

    public static void handle(NyxSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                        () -> () -> com.soul.soa_additions.nyx.client.NyxClientState.accept(pkt)));
        ctx.get().setPacketHandled(true);
    }
}
