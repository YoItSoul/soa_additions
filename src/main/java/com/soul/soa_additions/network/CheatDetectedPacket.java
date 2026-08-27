package com.soul.soa_additions.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → client: a cheat method was detected on this player, and here is what happens next.
 *
 * <p>Sent instead of silently flagging. The old behaviour recorded a permanent mark in three
 * backends and told the player nothing, so someone who installed an xray pack without knowing the
 * pack had an anti-cheat could never find out, let alone undo it. Every detection now produces a
 * message naming the exact mod or pack that tripped it.</p>
 */
public record CheatDetectedPacket(Mode mode, String category, String detail) {

    /** What the server is doing about the detection — decides which screen the client shows. */
    public enum Mode {
        /** Singleplayer: the player chooses to fix it or to enable cheating for this world. */
        CHOICE,
        /** Server: a disconnect follows immediately; show the blocked screen with fix instructions. */
        BLOCKED,
        /** Server has allowCheatingPlayers on: play continues, but say so plainly. */
        ALLOWED
    }

    public static void encode(CheatDetectedPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.mode);
        buf.writeUtf(clamp(msg.category, 64), 64);
        buf.writeUtf(clamp(msg.detail, 512), 512);
    }

    /** Scanner evidence strings have no length contract, and writeUtf past the cap throws — the
     *  detection must reach the player even with its tail cut off. */
    private static String clamp(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }

    public static CheatDetectedPacket decode(FriendlyByteBuf buf) {
        return new CheatDetectedPacket(buf.readEnum(Mode.class), buf.readUtf(64), buf.readUtf(512));
    }

    public static void handle(CheatDetectedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.soul.soa_additions.anticheat.client.CheatScreens.onDetected(msg)));
        ctx.get().setPacketHandled(true);
    }
}
