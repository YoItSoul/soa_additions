package com.soul.soa_additions.quest;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.ModConfigs;
import com.soul.soa_additions.quest.editor.EditModeTracker;
import com.soul.soa_additions.quest.editor.FileQuestOverrideStorage;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.net.QuestDefinitionSyncPacket;
import com.soul.soa_additions.quest.net.QuestSyncPacket;
import com.soul.soa_additions.quest.web.QuestWebServer;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        Path worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        Path gameDir = server.getServerDirectory().toPath();
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
        for (JsonObject root : storage.loadWorldEditRawJson()) {
            Chapter parsed = QuestLoader.parseChapterForWorldEdits(root);
            if (parsed != null) QuestRegistry.updateChapter(parsed);
        }

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
        QuestEvaluator.recomputeAll(tp);
        // Send the full quest definition tree so the client has every chapter
        // and quest the server knows about — including world edits, datapacks,
        // and programmatic quests that may differ from the client's JAR.
        QuestDefinitionSyncPacket.sendTo(player);
        QuestSyncPacket.sendTo(player);
        com.soul.soa_additions.quest.progress.QuestNotifier.replayCompleted(player, tp);
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
