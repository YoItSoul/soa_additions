package com.soul.soa_additions.network;

import com.soul.soa_additions.anticheat.AntiCheatHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent from client to server on login, before anything else, carrying the player's answer to the
 * anticheat consent screen.
 *
 * <p>Exists so that "declined" and "not sent yet" are different states on the server. Without it a
 * refusal is indistinguishable from a slow client, which would force the server to either punish
 * lag or ignore refusals — and ignoring refusals turns the consent screen into an off switch for
 * the anticheat. With it, a refusal is an explicit answer the server can act on: see
 * {@code requireScanConsent}.</p>
 *
 * <p>Carries the answer only. No mod data of any kind travels in this packet, so a player who has
 * declined sends nothing about their installation.</p>
 */
public record ScanConsentPacket(boolean accepted) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(accepted);
    }

    public static ScanConsentPacket decode(FriendlyByteBuf buf) {
        return new ScanConsentPacket(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                AntiCheatHandler.handleScanConsent(player, accepted);
            }
        });
        ctx.setPacketHandled(true);
    }
}
