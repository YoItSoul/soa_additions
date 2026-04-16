package com.soul.soa_additions.donor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHandler::open));
        ctx.get().setPacketHandled(true);
    }

    /** Isolated so the JVM never resolves DonorWallScreen / Screen on the
     *  server — this inner class is only loaded on Dist.CLIENT. */
    @OnlyIn(Dist.CLIENT)
    private static final class ClientHandler {
        static void open() {
            var mc = net.minecraft.client.Minecraft.getInstance();
            mc.setScreen(new DonorWallScreen());
        }
    }
}
