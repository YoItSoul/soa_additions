package com.soul.soa_additions.anticheat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.network.ClientModReportPacket;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
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
 *       clients). A match flags the player.</li>
 *
 *   <li><b>Silent-client detection.</b> Every joining player is entered into an expected-report
 *       table with a deadline. If the report does not arrive within
 *       {@link #REPORT_DEADLINE_TICKS} ticks the player is flagged. This catches cheaters who
 *       strip, no-op, or never ship the client scanner — the only legitimate clients that do not
 *       report are ones whose anticheat has been tampered with.</li>
 *
 *   <li><b>Command heuristic.</b> Any command executed by a player with OP permissions (level
 *       &ge; 2) is treated as a cheat unless the root command is on a small whitelist of
 *       harmless OP-accessible commands (help, me, msg, list, etc.).</li>
 *
 *   <li><b>Server-side mod scan.</b> On server start, if a forbidden mod is installed on the
 *       server itself, a loud warning is printed (it cannot grant advancements without a player
 *       context, but alerts the admin).</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class AntiCheatHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_AntiCheat");

    private static final ResourceLocation CHEAT_ADVANCEMENT_ID =
            new ResourceLocation(SoaAdditions.MODID, "cheats_are_fun");

    /** How long a client has to submit its report after login before being flagged. */
    private static final int REPORT_DEADLINE_TICKS = 200; // 10 seconds

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

    /** Commands safe for OPs to run without being flagged. Everything else is treated as a cheat. */
    private static final Set<String> SAFE_COMMANDS = Set.of(
            "help", "me", "msg", "tell", "w", "trigger",
            "list", "seed", "teammsg", "tm", "pardon"
    );

    /** Players who still owe the server a client report. Maps UUID → server tick deadline. */
    private static final Map<UUID, Long> PENDING_REPORTS = new ConcurrentHashMap<>();

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
        PENDING_REPORTS.remove(player.getUUID());

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
        MinecraftServer server = player.getServer();
        if (server == null) return;
        long deadline = server.getTickCount() + REPORT_DEADLINE_TICKS;
        PENDING_REPORTS.put(player.getUUID(), deadline);
        LOGGER.debug("Awaiting client report from {} by tick {}", player.getGameProfile().getName(), deadline);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PENDING_REPORTS.remove(player.getUUID());
        FLAGGED_THIS_SESSION.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (PENDING_REPORTS.isEmpty()) return;

        long now = event.getServer().getTickCount();
        PENDING_REPORTS.entrySet().removeIf(entry -> {
            if (now < entry.getValue()) return false;
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player != null && !alreadyFlagged(player)) {
                flag(player, "silent client",
                        "no anticheat report received within " + REPORT_DEADLINE_TICKS + " ticks — client scanner likely tampered with");
            }
            return true;
        });
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Command heuristic
    // ────────────────────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onCommandExecuted(CommandEvent event) {
        CommandSourceStack source = event.getParseResults().getContext().getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        if (!source.hasPermission(2)) return;

        String raw = event.getParseResults().getReader().getString().trim();
        if (raw.startsWith("/")) raw = raw.substring(1);
        String root = raw.split("\\s+", 2)[0];
        int colon = root.indexOf(':');
        if (colon >= 0) root = root.substring(colon + 1);
        root = root.toLowerCase(Locale.ROOT);

        if (SAFE_COMMANDS.contains(root)) return;

        if (!alreadyFlagged(player)) {
            flag(player, "op command", raw);
        }
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
        return FLAGGED_THIS_SESSION.contains(player.getUUID()) || hasCheatAdvancement(player);
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
        grantCheatAdvancement(player);
    }

    private static boolean hasCheatAdvancement(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        Advancement advancement = server.getAdvancements().getAdvancement(CHEAT_ADVANCEMENT_ID);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static void grantCheatAdvancement(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Advancement advancement = server.getAdvancements().getAdvancement(CHEAT_ADVANCEMENT_ID);
        if (advancement == null) {
            LOGGER.error("Cheat advancement {} is missing!", CHEAT_ADVANCEMENT_ID);
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) return;
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
