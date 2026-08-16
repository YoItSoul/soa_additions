package com.soul.soa_additions.anticheat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.ModConfigs;
import com.soul.soa_additions.network.ClientModReportPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The SOA anticheat. Detection paths:
 *
 * <ol>
 *   <li><b>Client report scan.</b> On login the client's {@link ClientModScanner} sends a
 *       {@link ClientModReportPacket} with its full mod list and all available resource packs.
 *       The server scans every entry for forbidden substrings (xray, baritone, known cheat
 *       clients). A match flags the player. Note: a missing report is NOT treated as tampering,
 *       because laggy or slow clients can legitimately send it late or drop the packet.
 *       <p>The scan is opt-in: the client sends nothing until the player ticks the cheat-scan box
 *       on {@code SoaConsentScreen}, and their answer arrives first as a
 *       {@link com.soul.soa_additions.network.ScanConsentPacket}. A refusal is answered by
 *       {@link #handleScanConsent} — on a server that requires the scan the player is turned away
 *       rather than let in unscanned, so opting out costs access, not detection.</p></li>
 *
 *   <li><b>Command heuristic.</b> Any command executed by a player with OP permissions (level
 *       &ge; 2) is treated as a cheat unless the root command is on a small whitelist of
 *       harmless OP-accessible commands (help, me, msg, list, etc.).</li>
 *
 *   <li><b>Server-side mod scan.</b> On server start, if a forbidden mod is installed on the
 *       server itself, a loud warning is printed (it cannot grant advancements without a player
 *       context, but alerts the admin).</li>
 *
 *   <li><b>Creative / spectator gamemode.</b> Entering creative or spectator flags the player.
 *       This catches tools like JEI's cheat-mode "inventory" path that inject items via
 *       {@code ServerboundSetCreativeModeSlotPacket} without ever executing a command, which
 *       would otherwise slip past the command heuristic. Also fires on login if the player is
 *       already in one of those modes.</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class AntiCheatHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_AntiCheat");

    /** Substrings that mark a mod or resource pack as forbidden. Match is case-insensitive. */
    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
            // XRay family
            "xray", "x-ray", "x_ray", "xraymod", "advancedxray",
            // Pathfinder / autominer
            "baritone",
            // Known cheat clients
            "wurst", "liquidbounce", "meteor client", "meteor-client",
            "impact client", "impact-client", "future client", "future-client",
            "inertia", "rusherhack", "rusher hack", "salhack", "sal hack",
            "nodus", "huzuni", "aristois", "flux client", "sigma client", "vape",
            // Common cheat feature names
            "killaura", "kill-aura", "kill_aura",
            "aimbot", "aim-bot", "aim_bot",
            "wallhack", "wall-hack", "wall_hack",
            "autoclicker", "auto-clicker", "auto_clicker",
            "freecam", "tracers", "esphack", "esp hack"
    );

    /** Commands safe for OPs to run without being flagged. Everything else is
     *  treated as a cheat — either blocked (default) or run-and-flagged if the
     *  player has opted into {@link CheaterModeOptIn}. List covers legitimate
     *  server-management commands that don't grant items, XP, position, or
     *  otherwise bypass progression. */
    private static final Set<String> SAFE_COMMANDS = Set.of(
            // chat / info
            "help", "me", "msg", "tell", "w", "trigger",
            "list", "seed", "teammsg", "tm", "say",
            // moderation
            "kick", "ban", "ban-ip", "banip", "pardon", "pardon-ip", "pardonip",
            "whitelist", "op", "deop",
            // server / datapack lifecycle (doesn't alter gameplay state)
            "reload", "datapack", "save-all", "saveall", "save-on", "saveon",
            "save-off", "saveoff", "stop", "publish",
            // diagnostics
            "debug", "perf",
            // player self-service — /unstuck (see UnstuckCommand) kills the
            // caller and does nothing else. That's a penalty, not a progression
            // bypass, so OPs can run it unflagged.
            "unstuck"
    );

    /**
     * Subpaths of {@code /soa} that are safe by themselves. Matching is
     * prefix-based on a space boundary so {@code "donor"} also covers
     * {@code "donor add"}, {@code "donor sync"}, etc. Anything NOT listed here
     * is treated as cheating — the big ones are {@code quests editmode} (live
     * quest rewrite), {@code quests claim} / {@code quests trigger} /
     * {@code quests resetprogress} (bypasses tasks/rewards/progression),
     * {@code quests import-ftb} / {@code quests edittarget} (quest content
     * mutation), and {@code packmode set} / {@code packmode force} (mode
     * switch past lock).
     */
    private static final Set<String> SAFE_SOA_SUBPATHS = Set.of(
            // opt-in bootstrap — must always run or nothing can ever opt in
            "quests cheatermode",
            // read-only / progression-neutral
            "quests overlay",     // sends a link to the web overlay
            "optimizer",          // profiler snapshot
            "export",             // dumps registry to files
            "team",               // team mgmt (typically non-op anyway)
            "donor", "donors",    // donor wall + sync — no item grants
            "packmode show",      // read-only
            "packmode lock",      // tightens progression rather than loosening
            "worldgen",           // read-only scan of generated chunks
            "materials",          // Smithery material catalog — read-only reference UI, grants nothing
            "anticheat"           // client-side consent control; grants nothing, and refusing costs access
    );

    /** Players whose session has already been flagged — prevents repeat logging. */
    private static final Set<UUID> FLAGGED_THIS_SESSION = ConcurrentHashMap.newKeySet();

    private AntiCheatHandler() {}

    // ────────────────────────────────────────────────────────────────────────────────
    // Server-side startup scan
    // ────────────────────────────────────────────────────────────────────────────────

    public static void scanServerInstalledMods() {
        for (IModInfo mod : ModList.get().getMods()) {
            String entry = mod.getModId() + "|" + mod.getDisplayName() + "|" + mod.getDescription();
            String keyword = findForbiddenKeyword(entry);
            if (keyword != null) {
                LOGGER.error("╔══════════════════════════════════════════════════════════");
                LOGGER.error("║ SOA ANTICHEAT: forbidden mod installed on THIS SERVER!");
                LOGGER.error("║   mod : {}", mod.getDisplayName());
                LOGGER.error("║   hit : {}", keyword);
                LOGGER.error("╚══════════════════════════════════════════════════════════");
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Client report handling (called from packet handler)
    // ────────────────────────────────────────────────────────────────────────────────

    /**
     * Handles the player's answer to the consent screen, which arrives before any report.
     *
     * <p>Consent is the player's to give; joining is the server's to grant. A refusal is not a cheat
     * detection — nothing is flagged, nothing is written — but on a server that requires the scan it
     * is not enough to play, so the connection is closed with instructions for allowing it and
     * coming back. That keeps the consent screen from doubling as an off switch for the anticheat.</p>
     */
    public static void handleScanConsent(ServerPlayer player, boolean accepted) {
        if (accepted) return;

        String name = player.getGameProfile().getName();
        if (isSingleplayerOwner(player)) {
            LOGGER.info("[soa anticheat] {} declined the scan in their own singleplayer world.", name);
            return;
        }
        if (!ModConfigs.REQUIRE_SCAN_CONSENT.get()) {
            LOGGER.warn("[soa anticheat] {} declined the scan; requireScanConsent is off, letting them in "
                    + "(handshake mod list is the only check that still applies).", name);
            return;
        }
        LOGGER.warn("[soa anticheat] {} declined the scan and this server requires it — disconnecting.", name);
        player.connection.disconnect(scanRequiredReason());
    }

    /** True for the host of a singleplayer world; LAN guests are on someone else's world. */
    private static boolean isSingleplayerOwner(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return server != null && !server.isDedicatedServer()
                && server.isSingleplayerOwner(player.getGameProfile());
    }

    /** Deliberately worded as a requirement, not an accusation — declining is not cheating. */
    private static Component scanRequiredReason() {
        return Component.empty()
                .append(Component.literal("Anticheat scan required\n\n")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                // Worded to fit both an explicit refusal and the rarer case of a player who never
                // saw the prompt (unwritable config dir, config not loaded in time).
                .append(Component.literal(
                                "This server runs the Souls of Avarice anticheat, and your game does not have "
                                + "your permission to check its installed mods and resource packs.\n\n")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal("You are not in trouble and nothing has been recorded against you.\n\n")
                        .withStyle(ChatFormatting.GREEN))
                // Point at the button, not the command: the player is staring at a disconnect screen
                // one click from the main menu, and telling them to load a singleplayer world to type
                // something is the long way round from exactly where they are standing.
                .append(Component.literal("To play here: go back to the main menu, click the ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("C").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(" button in the top-left corner, tick ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("\"Let servers check my mods for cheats\"")
                        .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(", confirm, and reconnect.\n\n")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(
                                "That screen lists exactly what is sent. In a world you can also use "
                                + "/soa anticheat allow.")
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static void handleClientReport(ServerPlayer player, ClientModReportPacket report) {
        if (alreadyFlagged(player)) return;

        // Content verdicts first: they survive a rename, so they are both the strongest evidence
        // and the most specific thing we can tell the player.
        for (String finding : report.findings()) {
            String[] parts = finding.split("\\|", 3);
            if (parts.length < 2) continue;
            String kind = parts[0];
            String evidence = parts.length > 2 ? parts[1] + " (" + parts[2] + ")" : parts[1];
            String category = switch (kind) {
                case "xray" -> "resource pack";
                case "fullbright" -> "resource pack";
                case "class" -> "cheat client";
                default -> kind;
            };
            // Fullbright overriding core shaders is suspicious, not proof — log it, don't act.
            if ("fullbright".equals(kind)) {
                LOGGER.info("[soa anticheat] {} ships core shaders (possible fullbright): {}",
                        player.getGameProfile().getName(), evidence);
                continue;
            }
            logDetection(player, category, evidence);
            CheatEnforcement.onCheatDetected(player, category, evidence);
            return;
        }

        String hit = findForbiddenIn(report.mods());
        if (hit != null) {
            logDetection(player, "mod", hit);
            CheatEnforcement.onCheatDetected(player, "mod", hit);
            return;
        }
        hit = findForbiddenIn(report.resourcePacks());
        if (hit != null) {
            logDetection(player, "resource pack", hit);
            CheatEnforcement.onCheatDetected(player, "resource pack", hit);
        }
    }

    /**
     * Checks the mod list Forge itself negotiated during the handshake.
     *
     * <p>Independent of {@link ClientModScanner}: that report is a packet our own code sends, so a
     * recompiled copy of this mod can simply lie in it. The handshake list is Forge's own
     * connection data, which the client also has to satisfy to connect at all — defeating both
     * costs meaningfully more than defeating one. Free, server-side, no protocol of ours involved.</p>
     */
    private static void checkHandshakeMods(ServerPlayer player) {
        if (alreadyFlagged(player)) return;
        try {
            if (player.connection == null) return;
            ConnectionData data = NetworkHooks.getConnectionData(player.connection.connection);
            if (data == null) return;   // singleplayer / local connection has none
            String hit = findForbiddenIn(data.getModList());
            if (hit != null) {
                logDetection(player, "mod", hit + " (forge handshake)");
                CheatEnforcement.onCheatDetected(player, "mod", hit);
            }
        } catch (Throwable t) {
            LOGGER.debug("Handshake mod-list check failed: {}", t.toString());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Login / silent-client detection
    // ────────────────────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Amnesty first, or the cross-check below would faithfully restore the very legacy flag
        // the amnesty exists to clear.
        CheaterManager.applyAmnestyOnce(player);

        // Cross-check the three cheater backends so that a stripped
        // advancement or edited NBT is re-applied before any gameplay starts.
        CheaterManager.crossCheckOnLogin(player);

        // Forge's own handshake data — does not depend on our scanner packet arriving, or on it
        // telling the truth.
        checkHandshakeMods(player);

        // Player may have logged in already in creative/spectator — the gamemode-change
        // event doesn't fire for the initial mode, so check it explicitly here.
        GameType mode = player.gameMode.getGameModeForPlayer();
        if (isCheatyGameMode(mode) && !alreadyFlagged(player)) {
            flag(player, "gamemode", "logged in in " + mode.getName() + " mode");
        }
    }

    @SubscribeEvent
    public static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isCheatyGameMode(event.getNewGameMode())) return;
        if (alreadyFlagged(player)) return;
        flag(player, "gamemode", "switched to " + event.getNewGameMode().getName() + " mode");
    }

    private static boolean isCheatyGameMode(GameType mode) {
        return mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CheaterManager.clearServer(event.getServer());
        com.soul.soa_additions.quest.telemetry.QuestTelemetry.clearSessionState();
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FLAGGED_THIS_SESSION.remove(player.getUUID());
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Command heuristic
    // ────────────────────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onCommandExecuted(CommandEvent event) {
        CommandSourceStack source = event.getParseResults().getContext().getSource();

        String raw = event.getParseResults().getReader().getString().trim();
        if (raw.startsWith("/")) raw = raw.substring(1);

        // Intercept any attempt to revoke the cheat advancement — from players
        // AND from the console. Cancel it, then log a tamper event naming the
        // executor so the player responsible (if any) is re-flagged.
        if (isCheatAdvancementRevoke(raw)) {
            event.setCanceled(true);
            String who = source.getEntity() instanceof ServerPlayer p
                    ? p.getGameProfile().getName() + " (" + p.getUUID() + ")"
                    : source.getTextName();
            LOGGER.warn("Blocked attempted revoke of cheats_are_fun by {}: {}", who, raw);
            if (source.getEntity() instanceof ServerPlayer executor) {
                flag(executor, "tamper", "attempted /advancement revoke of cheats_are_fun");
            }
            return;
        }

        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        if (!source.hasPermission(2)) return;

        // Bypass when another mod dispatches the command with the player as the
        // source (e.g. Chance Blocks running /summon on block break). Player-typed
        // commands always come from the chat packet handler; mod-dispatched ones
        // don't touch ServerGamePacketListenerImpl. Without this, gameplay
        // features that use the command system get cancelled as if the player had
        // typed them.
        if (!isPlayerTypedCommand()) return;

        // Classify /soa subcommands individually — most are cheating (editmode
        // rewrites quests, claim/trigger bypass tasks, packmode set jumps
        // difficulty), a few aren't (cheatermode, overlay, optimizer, team,
        // donor, read-only packmode queries).
        if (isSafeSoaSubcommand(raw)) return;

        String root = raw.split("\\s+", 2)[0];
        int colon = root.indexOf(':');
        if (colon >= 0) root = root.substring(colon + 1);
        root = root.toLowerCase(Locale.ROOT);

        if (SAFE_COMMANDS.contains(root)) return;

        // Already flagged players don't get the blocking UX — they're past
        // the point where protection helps.
        if (alreadyFlagged(player)) return;

        // If the player has explicitly opted into cheater mode, run the
        // command AND flag them. Otherwise cancel it and tell them how to
        // opt in. This prevents drive-by flagging from commands admins run
        // routinely but would never think of as "cheating" the run.
        if (CheaterModeOptIn.isEnabled(player)) {
            flag(player, "op command", raw);
            return;
        }

        event.setCanceled(true);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "[SOA] Blocked: '/" + root + "' would flag you as a cheater. "
                                + "Run '/soa quests cheatermode true' first to accept the flag and run cheat commands.")
                .withStyle(net.minecraft.ChatFormatting.RED));
    }

    /**
     * Match {@code advancement revoke <target> (only|from|through|until) soa_additions:cheats_are_fun}
     * and the shorthand {@code advancement revoke <target> everything} which
     * would strip the cheat advancement too. Case-insensitive; tolerant of
     * extra whitespace.
     */
    /** Strip an optional {@code modid:} prefix from the first token so
     *  {@code /soa_additions:soa ...} normalises to {@code /soa ...}. */
    private static String stripNamespaceOnFirstToken(String lower) {
        int firstSpace = lower.indexOf(' ');
        String first = firstSpace < 0 ? lower : lower.substring(0, firstSpace);
        int colon = first.indexOf(':');
        if (colon < 0) return lower;
        String rest = firstSpace < 0 ? "" : lower.substring(firstSpace);
        return first.substring(colon + 1) + rest;
    }

    /** True iff the command is {@code /soa <subpath>} where {@code subpath}
     *  starts with one of {@link #SAFE_SOA_SUBPATHS} on a word boundary. */
    private static boolean isSafeSoaSubcommand(String raw) {
        String lower = stripNamespaceOnFirstToken(
                raw.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim());
        if (!lower.startsWith("soa ")) return false;
        String rest = lower.substring(4);
        for (String path : SAFE_SOA_SUBPATHS) {
            if (rest.equals(path) || rest.startsWith(path + " ")) return true;
        }
        return false;
    }

    /**
     * True iff the currently-executing command was typed by a player into the
     * chat box, rather than dispatched programmatically by another mod.
     *
     * <p>The naive heuristic "is {@code ServerGamePacketListenerImpl} anywhere
     * in the call stack" is wrong — mods that run commands synchronously from
     * a player-triggered event (e.g. Chance Cubes scheduling a command on
     * cube-break with delay 0) inherit the packet handler in their call stack,
     * and would be flagged as cheating. The correct check is whether the
     * immediate caller of {@link Commands#performPrefixedCommand} is
     * {@code ServerGamePacketListenerImpl.performChatCommand} — that's the one
     * and only path for chat-typed commands.</p>
     *
     * <p>Walks down past our own handler, the Forge eventbus internals, and
     * the {@code Commands.performCommand} frame; the next non-{@code Commands}
     * frame is the dispatcher. If it's the chat handler, the player typed it.</p>
     */
    private static boolean isPlayerTypedCommand() {
        return StackWalker.getInstance().walk(frames -> {
            boolean pastCommandsFrames = false;
            for (java.util.Iterator<StackWalker.StackFrame> it = frames.iterator(); it.hasNext(); ) {
                String cn = it.next().getClassName();
                if (cn.equals("net.minecraft.commands.Commands")) {
                    pastCommandsFrames = true;
                    continue;
                }
                if (pastCommandsFrames) {
                    // First frame above the Commands.* frames is the dispatcher.
                    // Player-typed: ServerGamePacketListenerImpl.performChatCommand.
                    // Mod-dispatched: anything else (chanceCubes.util.RewardsUtil,
                    // a custom server tick task, a scheduled job, etc.).
                    return cn.equals("net.minecraft.server.network.ServerGamePacketListenerImpl");
                }
            }
            return false;
        });
    }

    private static boolean isCheatAdvancementRevoke(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        if (!lower.startsWith("advancement revoke ")) return false;
        if (lower.contains(CheaterManager.CHEAT_ADVANCEMENT_ID.toString())) return true;
        // "everything" wipes all advancements including ours.
        return lower.endsWith(" everything");
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Internals
    // ────────────────────────────────────────────────────────────────────────────────

    private static String findForbiddenIn(Iterable<String> entries) {
        for (String entry : entries) {
            String kw = findForbiddenKeyword(entry);
            if (kw != null) return entry + " (matched: " + kw + ")";
        }
        return null;
    }

    private static String findForbiddenKeyword(String entry) {
        String lower = entry.toLowerCase(Locale.ROOT);
        for (String kw : FORBIDDEN_KEYWORDS) {
            if (lower.contains(kw)) return kw;
        }
        return null;
    }

    private static boolean alreadyFlagged(ServerPlayer player) {
        if (FLAGGED_THIS_SESSION.contains(player.getUUID())) return true;
        return CheaterManager.isFlagged(player);
    }

    /**
     * Records a detection in the log without touching the player's flag state.
     *
     * <p>Detection is not commitment — see {@link CheatEnforcement}. The admin still wants the line
     * in the log, because "someone tried to join with xray and was turned away" is worth knowing
     * even though nothing was written against the account.</p>
     */
    private static void logDetection(ServerPlayer player, String category, String detail) {
        String address = player.connection == null ? "?" : player.connection.connection.getRemoteAddress().toString();
        LOGGER.warn("══════════ SOA CHEAT DETECTED ══════════");
        LOGGER.warn("  player   : {} ({})", player.getGameProfile().getName(), player.getUUID());
        LOGGER.warn("  address  : {}", address);
        LOGGER.warn("  category : {}", category);
        LOGGER.warn("  detail   : {}", detail);
        LOGGER.warn("════════════════════════════════════════");
    }

    private static void flag(ServerPlayer player, String category, String detail) {
        FLAGGED_THIS_SESSION.add(player.getUUID());
        String name = player.getGameProfile().getName();
        String address = player.connection == null ? "?" : player.connection.connection.getRemoteAddress().toString();
        LOGGER.warn("══════════ SOA ANTICHEAT FLAG ══════════");
        LOGGER.warn("  player   : {} ({})", name, player.getUUID());
        LOGGER.warn("  address  : {}", address);
        LOGGER.warn("  category : {}", category);
        LOGGER.warn("  detail   : {}", detail);
        LOGGER.warn("════════════════════════════════════════");
        CheaterManager.flag(player, category + ":" + detail);
    }
}
