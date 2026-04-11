package com.soul.soa_additions.network;

import com.soul.soa_additions.anticheat.AntiCheatHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Sent from client to server immediately after login. Carries the client's loaded mod list and
 * selected resource packs so the server can scan for forbidden entries (xray / baritone / etc.).
 * Each entry is a single string of form {@code id|name|description}.
 */
public record ClientModReportPacket(List<String> mods, List<String> resourcePacks) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(mods, FriendlyByteBuf::writeUtf);
        buf.writeCollection(resourcePacks, FriendlyByteBuf::writeUtf);
    }

    public static ClientModReportPacket decode(FriendlyByteBuf buf) {
        return new ClientModReportPacket(
                buf.readList(FriendlyByteBuf::readUtf),
                buf.readList(FriendlyByteBuf::readUtf)
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                AntiCheatHandler.handleClientReport(player, this);
            }
        });
        ctx.setPacketHandled(true);
    }
}
