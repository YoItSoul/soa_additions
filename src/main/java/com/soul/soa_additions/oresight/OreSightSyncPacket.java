package com.soul.soa_additions.oresight;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Server → owning client sync of the player's {@link OreSightTracker} state.
 * Sent on add/expire so the client renderer can keep its scan set in lockstep.
 *
 * <p>Wire format: {@code long masterExpiry}, then varint count, then
 * [string block_id, long expiry] pairs. {@code masterExpiry} != 0 enables
 * "scan all ores" mode on the client.</p>
 */
public final class OreSightSyncPacket {

    private final Map<Block, Long> entries;
    private final long masterExpiry;

    public OreSightSyncPacket(Map<Block, Long> entries, long masterExpiry) {
        this.entries = entries;
        this.masterExpiry = masterExpiry;
    }

    public static void encode(OreSightSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.masterExpiry);
        buf.writeVarInt(pkt.entries.size());
        for (Map.Entry<Block, Long> e : pkt.entries.entrySet()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(e.getKey());
            buf.writeResourceLocation(id == null ? new ResourceLocation("minecraft", "air") : id);
            buf.writeLong(e.getValue());
        }
    }

    public static OreSightSyncPacket decode(FriendlyByteBuf buf) {
        long master = buf.readLong();
        int n = buf.readVarInt();
        Map<Block, Long> map = new LinkedHashMap<>(n);
        for (int i = 0; i < n; i++) {
            ResourceLocation id = buf.readResourceLocation();
            long expiry = buf.readLong();
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            if (block != null) map.put(block, expiry);
        }
        return new OreSightSyncPacket(map, master);
    }

    public static void handle(OreSightSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // DistExecutor guard (same idiom as every other client-bound
                // packet here) so a dedicated server can never class-load the
                // client renderer, even if this packet's direction changes.
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                        net.minecraftforge.api.distmarker.Dist.CLIENT,
                        () -> () -> com.soul.soa_additions.oresight.client.OreSightClient
                                .applySync(pkt.entries, pkt.masterExpiry)));
        ctx.get().setPacketHandled(true);
    }

    public Map<Block, Long> entries() { return entries; }
    public long masterExpiry() { return masterExpiry; }
}
