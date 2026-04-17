package com.soul.soa_additions.anticheat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.network.ClientModReportPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
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
 *       because laggy or slow clients can legitimately send it late or drop the packet.</li>
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
            "debug", "perf"
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
            "packmode lock"       // tightens progression rather than loosening
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

    public static void handleClientReport(ServerPlayer player, ClientModReportPacket report) {
        if (alreadyFlagged(player)) return;

        String hit = findForbiddenIn(report.mods());
        if (hit != null) {
            flag(player, "mod", hit);
            return;
        }
        hit = findForbiddenIn(report.resourcePacks());
        if (hit != null) {
            flag(player, "resource pack", hit);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Login / silent-client detection
    // ────────────────────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Cross-check the three cheater backends first so that a stripped
        // advancement or edited NBT is re-applied before any gameplay starts.
        CheaterManager.crossCheckOnLogin(player);

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
     * True iff the currently-executing command was typed by a player (routed
     * through {@code ServerGamePacketListenerImpl}), rather than dispatched
     * programmatically by another mod. Used to skip the OP-command block when
     * a mod runs a command with the player as the source.
     */
    private static boolean isPlayerTypedCommand() {
        return StackWalker.getInstance().walk(frames ->
                frames.anyMatch(f -> f.getClassName().equals(
                        "net.minecraft.server.network.ServerGamePacketListenerImpl")));
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
