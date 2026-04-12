package com.soul.soa_additions.donor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → client signal to open the donor wall GUI screen.
 * Carries no data — the client uses {@link ClientDonorCache} for content.
 */
public record DonorWallOpenPacket() {

    public static void encode(DonorWallOpenPacket pkt, FriendlyByteBuf buf) {
        // no data
    }

    public static DonorWallOpenPacket decode(FriendlyByteBuf buf) {
        return new DonorWallOpenPacket();
    }

    public static void handle(DonorWallOpenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    mc.setScreen(new DonorWallScreen());
                }));
        ctx.get().setPacketHandled(true);
    }
}
