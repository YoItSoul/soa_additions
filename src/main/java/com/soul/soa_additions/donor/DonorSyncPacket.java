package com.soul.soa_additions.donor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server → client full donor list sync. Sent on login and whenever the
 * donor list changes. Clients store this in {@link ClientDonorCache} for
 * chat formatting and the donor wall GUI.
 */
public record DonorSyncPacket(List<DonorData> donors) {

    public static void encode(DonorSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.donors.size());
        for (DonorData d : pkt.donors) {
            buf.writeUUID(d.uuid());
            buf.writeUtf(d.name());
            buf.writeEnum(d.tier());
            buf.writeLong(d.donatedAt().toEpochMilli());
            buf.writeUtf(d.message());
        }
    }

    public static DonorSyncPacket decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<DonorData> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            UUID uuid = buf.readUUID();
            String name = buf.readUtf();
            DonorData.Tier tier = buf.readEnum(DonorData.Tier.class);
            Instant donated = Instant.ofEpochMilli(buf.readLong());
            String message = buf.readUtf();
            list.add(new DonorData(uuid, name, tier, donated, message));
        }
        return new DonorSyncPacket(list);
    }

    public static void handle(DonorSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientDonorCache.apply(pkt.donors)));
        ctx.get().setPacketHandled(true);
    }
}
