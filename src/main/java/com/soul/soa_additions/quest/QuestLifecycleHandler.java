package com.soul.soa_additions.quest;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.ModConfigs;
import com.soul.soa_additions.quest.editor.EditModeTracker;
import com.soul.soa_additions.quest.editor.FileQuestOverrideStorage;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.net.QuestDefinitionSyncPacket;
import com.soul.soa_additions.quest.net.QuestSyncPacket;
import com.soul.soa_additions.quest.progress.ClaimService;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.QuestStatus;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.reward.LockPackmodeReward;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import com.soul.soa_additions.quest.web.QuestWebServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-lifecycle glue for the quest system. Responsibilities:
 *
 * <ul>
 *   <li>On server start, build a {@link FileQuestOverrideStorage} for the
 *       current world and wire it into {@link QuestRegistry} as the world-edits
 *       source. This is what lets the editor's per-world override files
 *       layer onto the base quest content.</li>
 *   <li>On player login, recompute their team's full progress snapshot so
 *       any changes that happened while they were offline (or from a reload)
 *       are reflected immediately in their quest book.</li>
 *   <li>On server stop, clear the transient edit-mode tracker so nobody
 *       starts the next session with a stale edit flag.</li>
 * </ul>
 *
 * <p>Registered via {@code Mod.EventBusSubscriber} so the whole thing wires
 * itself up without explicit bus registration in the mod main class.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestLifecycleHandler {

    private QuestLifecycleHandler() {}

    private static FileQuestOverrideStorage storage;

    public static FileQuestOverrideStorage storage() { return storage; }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();

        // Dedicated servers must have a pack mode configured. If the config
        // is empty, pause startup and prompt the admin via the console.
        if (server.isDedicatedServer()) {
            String cfg = ModConfigs.SERVER_PACKMODE.get();
            if (cfg == null || cfg.isBlank()) {
                PackMode chosen = promptForPackMode();
                // Write back to the config so the prompt only happens once.
                ModConfigs.SERVER_PACKMODE.set(chosen.lower());
                org.slf4j.LoggerFactory.getLogger("soa_additions/packmode")
                        .info("Pack mode set via console prompt: {}", chosen.lower());
            } else {
                PackMode parsed = PackMode.fromString(cfg);
                if (!parsed.name().equalsIgnoreCase(cfg.trim())) {
                    // Invalid value — prompt instead of crashing.
                    org.slf4j.LoggerFactory.getLogger("soa_additions/packmode")
                            .warn("Invalid serverPackMode '{}' in config, prompting for correction", cfg);
                    PackMode chosen = promptForPackMode();
                    ModConfigs.SERVER_PACKMODE.set(chosen.lower());
                }
            }
        }

        Path worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        // Use FMLPaths.GAMEDIR rather than server.getServerDirectory(): the
        // latter returns `new File(".")` (process CWD) which varies by launcher
        // and on integrated servers can point at the wrong place. FMLPaths is
        // always the launcher-resolved instance root.
        Path gameDir = net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get();
        storage = new FileQuestOverrideStorage(worldDir, gameDir);

        // Plug the world-edit source into the registry. Loader re-parses on
        // each reload, so this supplier returning fresh JSON is enough.
        QuestRegistry.setWorldEditsSource(() -> {
            List<Chapter> chapters = new ArrayList<>();
            for (JsonObject root : storage.loadWorldEditRawJson()) {
                Chapter parsed = QuestLoader.parseChapterForWorldEdits(root);
                if (parsed != null) chapters.add(parsed);
            }
            return chapters;
        });

        // Datapack reload fired before this event, so the initial QuestLoader.apply()
        // ran against an empty world-edits supplier. Layer the world-edit chapters
        // in now that the storage is wired.
        List<JsonObject> worldEditRaw = storage.loadWorldEditRawJson();
        int loadedChapters = 0;
        int loadedQuests = 0;
        int loadedRewards = 0;
        for (JsonObject root : worldEditRaw) {
            Chapter parsed = QuestLoader.parseChapterForWorldEdits(root);
            if (parsed != null) {
                QuestRegistry.updateChapter(parsed);
                loadedChapters++;
                loadedQuests += parsed.quests().size();
                for (Quest q : parsed.quests()) loadedRewards += q.rewards().size();
            }
        }
        org.slf4j.LoggerFactory.getLogger("soa_additions/quest")
                .info("[SOA Quests] World-edit layer: {} raw files, {} chapters, {} quests, {} rewards",
                        worldEditRaw.size(), loadedChapters, loadedQuests, loadedRewards);

        // Touch packmode data so createdAt is stamped on first boot of a world.
        PackModeData.get(server);

        org.slf4j.LoggerFactory.getLogger("soa_additions/quest")
                .info("Quest system online (world={}, packmode={})",
                        worldDir.getFileName(), PackModeData.get(server).mode());

        // Start the quest web overlay server if enabled
        if (ModConfigs.ENABLE_QUEST_WEB_OVERLAY.get()) {
            QuestWebServer.start(server, ModConfigs.QUEST_WEB_OVERLAY_PORT.get());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        grantStarterBookOnce(player);
        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData data = QuestProgressData.get(player.server);
        TeamQuestProgress tp = data.forTeam(team.id());

        // If the pack mode is server-enforced, auto-complete any quest that
        // carries a lock_packmode reward so players skip the selection step.
        if (PackModeData.get(player.server).serverEnforced()) {
            autoCompletePackmodeQuests(tp, player);
        }

        QuestEvaluator.recomputeAllAndAutoClaim(tp, player);
        // Send the full quest definition tree so the client has every chapter
        // and quest the server knows about — including world edits, datapacks,
        // and programmatic quests that may differ from the client's JAR.
        QuestDefinitionSyncPacket.sendTo(player);
        QuestSyncPacket.sendTo(player);
        com.soul.soa_additions.quest.progress.QuestNotifier.replayCompleted(player, tp);
    }

    /**
     * Force-complete any quest with a {@code lock_packmode} reward when the
     * server admin has pre-set the pack mode. Fills all task counters to their
     * target and claims the quest so the player's quest tree starts unlocked
     * past the mode-selection step.
     */
    private static void autoCompletePackmodeQuests(TeamQuestProgress tp, ServerPlayer player) {
        for (Chapter chapter : QuestRegistry.allChapters()) {
            for (Quest quest : chapter.quests()) {
                boolean hasLockReward = false;
                for (QuestReward reward : quest.rewards()) {
                    if (reward instanceof LockPackmodeReward) {
                        hasLockReward = true;
                        break;
                    }
                }
                if (!hasLockReward) continue;

                QuestProgress qp = tp.get(quest.fullId());
                if (qp.status() == QuestStatus.CLAIMED) continue; // already done

                // Fill all task counters to their target so the quest evaluates
                // as complete regardless of task type.
                var tasks = quest.tasks();
                for (int i = 0; i < tasks.size(); i++) {
                    qp.task(i).setCount(tasks.get(i).target());
                }
                // Claim the quest through the normal flow so rewards fire and
                // downstream deps unlock.
                QuestEvaluator.recompute(quest, tp);
                if (qp.status() == QuestStatus.READY) {
                    ClaimService.claim(player, quest.fullId());
                }
            }
        }
    }

    /**
     * Blocks startup and reads from stdin until the admin enters a valid pack
     * mode. The dedicated server console is attached to stdin, so this works
     * as a normal interactive prompt in the server terminal.
     */
    private static PackMode promptForPackMode() {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("soa_additions/packmode");
        log.warn("========================================================");
        log.warn("  PACK MODE NOT SET");
        log.warn("  Enter a pack mode to continue: casual, adventure, expert");
        log.warn("========================================================");
        // Also print to stdout so it's visible even if the log format buries it.
        System.out.println();
        System.out.println("============================================================");
        System.out.println("  [SOA] Pack mode is not configured for this server.");
        System.out.println("  Please enter a pack mode to continue startup.");
        System.out.println("  Options: casual, adventure, expert");
        System.out.println("============================================================");
        System.out.print("  > ");
        System.out.flush();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    // stdin closed (e.g. headless/docker with no tty) — fall
                    // back to ADVENTURE so the server doesn't hang forever.
                    log.warn("stdin closed before a mode was entered, defaulting to ADVENTURE");
                    return PackMode.ADVENTURE;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    System.out.print("  > ");
                    System.out.flush();
                    continue;
                }
                PackMode mode = PackMode.fromString(line);
                if (mode.name().equalsIgnoreCase(line)) {
                    System.out.println("  Pack mode set to: " + mode.lower());
                    System.out.println();
                    return mode;
                }
                System.out.println("  Invalid mode '" + line + "'. Options: casual, adventure, expert");
                System.out.print("  > ");
                System.out.flush();
            }
        } catch (Exception e) {
            log.error("Failed to read pack mode from console, defaulting to ADVENTURE", e);
            return PackMode.ADVENTURE;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        QuestWebServer.revokeToken(player);
    }

    /** Give the player a quest book the first time they join this world.
     *  Uses Forge's persistent NBT sub-compound so the flag survives logout
     *  and dimension changes — re-rolling a new world starts a fresh flag,
     *  which is exactly what we want. */
    private static void grantStarterBookOnce(ServerPlayer player) {
        var persisted = player.getPersistentData()
                .getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean("soa_quest_book_granted")) return;
        persisted.putBoolean("soa_quest_book_granted", true);
        player.getPersistentData().put(
                net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, persisted);

        var book = new net.minecraft.world.item.ItemStack(
                com.soul.soa_additions.item.ModItems.QUEST_BOOK.get());
        if (!player.getInventory().add(book)) {
            // Inventory was full somehow — drop at the player's feet so the
            // book is never silently lost on first login.
            player.drop(book, false);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        QuestWebServer.stop();
        EditModeTracker.clearAll();
        storage = null;
    }
}
