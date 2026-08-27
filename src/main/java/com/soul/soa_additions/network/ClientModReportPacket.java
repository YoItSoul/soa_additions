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

    /** A client with thousands of mods is already unusual; nothing legitimate approaches this. */
    private static final int MAX_ENTRIES = 4096;
    private static final int MAX_ENTRY_LENGTH = 512;

    public void encode(FriendlyByteBuf buf) {
        writeBounded(buf, mods);
        writeBounded(buf, resourcePacks);
        writeBounded(buf, findings);
    }

    /**
     * Writes under the same bounds {@link #readBounded} enforces. Encode and decode share one
     * contract or the packet becomes a disconnect: mod entries carry mods.toml descriptions, which
     * routinely run past 512 characters, and an unclamped write of one of those met the read cap as
     * a DecoderException that kicked every consenting player on login. Truncation costs only the
     * tail of a description; the keyword scan still sees the id and name.
     */
    private static void writeBounded(FriendlyByteBuf buf, List<String> entries) {
        int count = Math.min(entries.size(), MAX_ENTRIES);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            String entry = entries.get(i);
            if (entry.length() > MAX_ENTRY_LENGTH) entry = entry.substring(0, MAX_ENTRY_LENGTH);
            buf.writeUtf(entry, MAX_ENTRY_LENGTH);
        }
    }

    public static ClientModReportPacket decode(FriendlyByteBuf buf) {
        return new ClientModReportPacket(readBounded(buf), readBounded(buf), readBounded(buf));
    }

    /**
     * Reads a length-prefixed string list without trusting the length.
     *
     * <p>{@code readList} hands the declared count straight to {@code newArrayListWithCapacity},
     * so a five-byte VarInt from a modified client asked the netty decode thread for an 8GB array
     * before a single element was read. This is a serverbound packet: the count is attacker
     * input, and the 32767-byte payload cap does not constrain it.</p>
     */
    private static List<String> readBounded(FriendlyByteBuf buf) {
        int declared = buf.readVarInt();
        if (declared < 0 || declared > MAX_ENTRIES) {
            throw new IllegalArgumentException("Mod report list too long: " + declared);
        }
        List<String> out = new java.util.ArrayList<>(declared);
        for (int i = 0; i < declared; i++) {
            out.add(buf.readUtf(MAX_ENTRY_LENGTH));
        }
        return out;
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
