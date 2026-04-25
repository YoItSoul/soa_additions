package com.soul.soa_additions.tr.network;

import com.soul.soa_additions.tr.knowledge.ClientScannedTargets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Server → client: replaces the client's scanned-targets sets (blocks + items
 * + entities) with the server's authoritative copies. Sent on login, respawn,
 * dimension change, and after any server-side scan/unscan call.
 */
public final class ScannedTargetsSyncPacket {

    private final Set<ResourceLocation> blocks;
    private final Set<ResourceLocation> items;
    private final Set<ResourceLocation> entities;

    public ScannedTargetsSyncPacket(Set<ResourceLocation> blocks,
                                     Set<ResourceLocation> items,
                                     Set<ResourceLocation> entities) {
        this.blocks = blocks;
        this.items = items;
        this.entities = entities;
    }

    public static void encode(ScannedTargetsSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.blocks.size());
        for (ResourceLocation id : pkt.blocks) buf.writeResourceLocation(id);
        buf.writeVarInt(pkt.items.size());
        for (ResourceLocation id : pkt.items) buf.writeResourceLocation(id);
        buf.writeVarInt(pkt.entities.size());
        for (ResourceLocation id : pkt.entities) buf.writeResourceLocation(id);
    }

    public static ScannedTargetsSyncPacket decode(FriendlyByteBuf buf) {
        int nb = buf.readVarInt();
        Set<ResourceLocation> blocks = new HashSet<>(nb);
        for (int i = 0; i < nb; i++) blocks.add(buf.readResourceLocation());
        int ni = buf.readVarInt();
        Set<ResourceLocation> items = new HashSet<>(ni);
        for (int i = 0; i < ni; i++) items.add(buf.readResourceLocation());
        int ne = buf.readVarInt();
        Set<ResourceLocation> entities = new HashSet<>(ne);
        for (int i = 0; i < ne; i++) entities.add(buf.readResourceLocation());
        return new ScannedTargetsSyncPacket(blocks, items, entities);
    }

    public static void handle(ScannedTargetsSyncPacket pkt, Supplier<NetworkEvent.Context> ctxRef) {
        NetworkEvent.Context ctx = ctxRef.get();
        ctx.enqueueWork(() -> {
            ClientScannedTargets.replace(pkt.blocks, pkt.items, pkt.entities);
            com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                    "[monocle/cli] sync received: {} blocks, {} items, {} entities",
                    pkt.blocks.size(), pkt.items.size(), pkt.entities.size());
        });
        ctx.setPacketHandled(true);
    }
}
