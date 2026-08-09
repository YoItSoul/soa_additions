package com.soul.soa_additions.smithery;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → client signal to open the Smithery material catalog screen.
 * Carries no data — the screen reads the material registry locally.
 *
 * <p>Mirrors {@code DonorWallOpenPacket}. It exists so {@code /soa materials}
 * can live on the <em>server</em> dispatcher as well as the client one: a
 * client-only command never appears in the command tree the server sends, so
 * whenever the client-side dispatcher misses it the command reads as unknown
 * with no tab-completion. Registering it server-side makes it visible and
 * runnable for every player regardless.</p>
 */
public record MaterialsCatalogOpenPacket() {

    public static void encode(MaterialsCatalogOpenPacket pkt, FriendlyByteBuf buf) {
        // no data
    }

    public static MaterialsCatalogOpenPacket decode(FriendlyByteBuf buf) {
        return new MaterialsCatalogOpenPacket();
    }

    public static void handle(MaterialsCatalogOpenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHandler::open));
        ctx.get().setPacketHandled(true);
    }

    /** Isolated so the JVM never resolves MaterialsCatalogScreen / Screen on a
     *  dedicated server — this inner class only loads on Dist.CLIENT. */
    @OnlyIn(Dist.CLIENT)
    private static final class ClientHandler {
        static void open() {
            var mc = net.minecraft.client.Minecraft.getInstance();
            mc.setScreen(new com.soul.soa_additions.smithery.client.MaterialsCatalogScreen());
        }
    }
}
