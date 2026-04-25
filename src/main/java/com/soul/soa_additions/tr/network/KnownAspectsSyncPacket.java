package com.soul.soa_additions.tr.network;

import com.soul.soa_additions.tr.knowledge.ClientKnownAspects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Server → client: replaces the client's known-aspects set with the server's
 * authoritative copy. Sent on login, respawn, dimension change, and after any
 * server-side discover/forget call.
 *
 * <p>Wire format: varint count, then N namespaced ids. The full set is sent
 * each time rather than deltas — even fully unlocked the set is &lt;48
 * entries, so the byte cost is negligible.
 */
public final class KnownAspectsSyncPacket {

    private final Set<ResourceLocation> known;

    public KnownAspectsSyncPacket(Set<ResourceLocation> known) {
        this.known = known;
    }

    public static void encode(KnownAspectsSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.known.size());
        for (ResourceLocation id : pkt.known) buf.writeResourceLocation(id);
    }

    public static KnownAspectsSyncPacket decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        Set<ResourceLocation> set = new LinkedHashSet<>(n);
        for (int i = 0; i < n; i++) set.add(buf.readResourceLocation());
        return new KnownAspectsSyncPacket(set);
    }

    public static void handle(KnownAspectsSyncPacket pkt, Supplier<NetworkEvent.Context> ctxRef) {
        NetworkEvent.Context ctx = ctxRef.get();
        ctx.enqueueWork(() -> {
            ClientKnownAspects.replace(pkt.known);
            com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                    "[monocle/cli] known-aspects sync received: {} known", pkt.known.size());
        });
        ctx.setPacketHandled(true);
    }
}
