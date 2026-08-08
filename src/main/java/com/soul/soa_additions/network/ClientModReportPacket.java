package com.soul.soa_additions.network;

import com.soul.soa_additions.anticheat.AntiCheatHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Sent from client to server after login. Carries the client's loaded mod list, its resource packs,
 * and — the part a rename cannot defeat — what those packs actually <em>contain</em>.
 *
 * <p>Mods and packs are {@code id|name|description} strings, which only ever described what
 * something calls itself: renaming a folder defeated the whole scan. {@code findings} carries
 * verdicts from {@link com.soul.soa_additions.anticheat.client.PackContentScanner} instead, in the
 * form {@code <kind>|<pack id>|<evidence>} — a pack that makes stone invisible is an xray pack
 * whatever it is named.</p>
 */
public record ClientModReportPacket(List<String> mods, List<String> resourcePacks, List<String> findings) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(mods, FriendlyByteBuf::writeUtf);
        buf.writeCollection(resourcePacks, FriendlyByteBuf::writeUtf);
        buf.writeCollection(findings, FriendlyByteBuf::writeUtf);
    }

    public static ClientModReportPacket decode(FriendlyByteBuf buf) {
        return new ClientModReportPacket(
                buf.readList(FriendlyByteBuf::readUtf),
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
