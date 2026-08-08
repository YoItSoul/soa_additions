package com.soul.soa_additions.anticheat;

import com.soul.soa_additions.config.ModConfigs;
import com.soul.soa_additions.network.CheatDetectedPacket;
import com.soul.soa_additions.network.ModNetworking;
import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.PackModeData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decides what happens when a cheat method is found on a player.
 *
 * <p>The rule this class exists to enforce is that <b>detection is not commitment</b>. Finding an
 * xray pack on someone's disk says nothing about intent — plenty of people install a pack years
 * ago, forget it, and never realise it is even active. Previously any detection wrote a permanent,
 * triple-persisted, tamper-checked flag, silently, with no way back and no notification; the flag
 * then went unused by anything except a telemetry field, so the honest player was branded forever
 * and the deliberate cheater was not inconvenienced at all.</p>
 *
 * <p>Now the flag means "played with a cheat active", and is only written when the player either
 * opts in or is let through by an admin who allows it. Someone who removes the pack and rejoins
 * leaves no trace, because nothing was written on the way out.</p>
 *
 * <dl>
 *   <dt>Opted in</dt><dd>They asked for this: flag and carry on.</dd>
 *   <dt>Singleplayer</dt><dd>Never disconnected — it is their own world. The client opens a screen
 *       offering to quit and fix it, or to enable cheating for that world.</dd>
 *   <dt>Server, {@code allowCheatingPlayers} on</dt><dd>Let them in, tell them, and flag: the cheat
 *       is active and the admin has said that is fine here.</dd>
 *   <dt>Server, default</dt><dd>Disconnect naming the offending file and how to come back. No flag —
 *       they never got to play with it.</dd>
 * </dl>
 */
public final class CheatEnforcement {

    private CheatEnforcement() {}

    /**
     * Detections that opened a choice screen and are waiting on an answer.
     *
     * <p>Accepting sets the opt-in, but the opt-in alone only affects the <em>next</em> detection —
     * and the detection that raised the screen has already been handled by then, so without this
     * the player clicked "yes" and nothing happened until they relogged. Holding the detection here
     * lets the answer apply to the thing they were actually asked about.
     *
     * <p>Server-side so the reason written to the log is the one the server detected, not whatever
     * a client sends back.
     */
    private static final Map<UUID, String> AWAITING_CHOICE = new ConcurrentHashMap<>();

    /**
     * Handles a detection.
     *
     * @return true when the player is allowed to keep playing
     */
    public static boolean onCheatDetected(ServerPlayer player, String category, String detail) {
        // Already opted in — this is a choice they already made, so record it and move on.
        if (CheaterModeOptIn.isEnabled(player)) {
            CheaterManager.flag(player, category + ":" + detail);
            send(player, CheatDetectedPacket.Mode.ALLOWED, category, detail);
            return true;
        }

        if (isSingleplayerOwner(player)) {
            // Casual has already spent everything the flag would take. The mode grants every
            // progression stage and brands the player 'iswuss', which the terminal checks and
            // refuses — so the run cannot be completed either way. Asking a casual player to
            // choose between "nothing is recorded" and "your world can never be completed" is
            // offering a trade they made when they picked the mode. Let them play.
            if (isCasual(player)) return true;

            AWAITING_CHOICE.put(player.getUUID(), category + ":" + detail);
            send(player, CheatDetectedPacket.Mode.CHOICE, category, detail);
            return true;
        }

        if (ModConfigs.ALLOW_CHEATING_PLAYERS.get()) {
            CheaterManager.flag(player, category + ":" + detail);
            send(player, CheatDetectedPacket.Mode.ALLOWED, category, detail);
            player.sendSystemMessage(allowedMessage(category, detail));
            return true;
        }

        // Packet first so the client can swap the vanilla disconnect screen for ours; if it does
        // not arrive in time the disconnect reason below carries the same information anyway.
        send(player, CheatDetectedPacket.Mode.BLOCKED, category, detail);
        player.connection.disconnect(disconnectReason(category, detail));
        return false;
    }

    /**
     * True for the host of a singleplayer world. LAN guests deliberately fall through to the server
     * path — the host's opt-in is theirs alone, and a guest with xray is on someone else's world.
     */
    /**
     * Applies the flag for the detection this player was asked about and accepted.
     *
     * <p>Called when cheater mode is switched on, so clicking "Yes, mark this world" and typing
     * {@code /soa quests cheatermode true} land in the same place. No pending detection means
     * nothing to apply — someone opting in ahead of time is not yet a cheater.
     */
    public static void acceptPendingChoice(ServerPlayer player) {
        String pending = AWAITING_CHOICE.remove(player.getUUID());
        if (pending != null) {
            CheaterManager.flag(player, pending);
        }
    }

    /** Drops any unanswered detection, so it can't be applied in a later session. */
    public static void forgetPendingChoice(ServerPlayer player) {
        AWAITING_CHOICE.remove(player.getUUID());
    }

    /**
     * True when this world is on Casual.
     *
     * <p>Deliberately not applied to servers: casual there is the admin's setting for everyone, and
     * an xray player still ruins the game for the people around them. Progression integrity is what
     * casual has already given up; fairness to other players is not.
     */
    private static boolean isCasual(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return server != null
                && PackModeData.get(server).mode() == PackMode.CASUAL;
    }

    private static boolean isSingleplayerOwner(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return server != null && !server.isDedicatedServer()
                && server.isSingleplayerOwner(player.getGameProfile());
    }

    private static void send(ServerPlayer player, CheatDetectedPacket.Mode mode, String category, String detail) {
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CheatDetectedPacket(mode, category, detail));
    }

    /** Multi-line, styled — the vanilla disconnect screen renders this verbatim if ours doesn't load. */
    public static Component disconnectReason(String category, String detail) {
        return Component.empty()
                .append(Component.literal("Cheat detected\n\n").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal("Souls of Avarice found a file that lets you cheat:\n")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal(detail + "\n\n").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("Delete the file and you can join straight back in — ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("nothing is recorded against you.\n")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(CheatCopy.NOTHING_RECORDED + "\n\n")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(CheatCopy.FILE_LOCATION + "\n")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(CheatCopy.DELETE_NOT_DISABLE)
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component allowedMessage(String category, String detail) {
        return Component.empty()
                .append(Component.literal("[SOA] ").withStyle(ChatFormatting.RED))
                .append(Component.literal("Cheat detected (" + category + ": " + detail + "). ")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal("This server allows it, so you may play — but you are now marked as playing modified.")
                        .withStyle(ChatFormatting.GRAY));
    }
}
