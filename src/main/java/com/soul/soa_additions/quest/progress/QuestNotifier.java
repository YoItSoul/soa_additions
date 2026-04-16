package com.soul.soa_additions.quest.progress;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.i18n.QuestText;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Broadcasts "Quest completed: <title>!" chat messages when a quest
 * transitions VISIBLE -> READY. Centralized so every recompute site can
 * call the same one-liner and we only tweak formatting in one place.
 *
 * <p>Sent to every online member of the acting player's team so solo
 * players get it for themselves and team members see each other's
 * completions in real time.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestNotifier {

    // Key under player persistent data. Using the Forge PERSISTED_NBT_TAG
    // sub-compound so this survives death + full logout.
    private static final String NOTIFIED_KEY = "soa_quests_notified";

    // Per-session in-memory cache. The old code round-tripped the full NBT
    // list on every single markNotified call, which during a multi-quest
    // auto-claim sweep meant one NBT write per claimed quest. Now we mutate
    // the in-memory set, track a dirty flag per player, and flush once on
    // logout or server shutdown.
    private static final Map<UUID, Set<String>> CACHE = new ConcurrentHashMap<>();
    private static final Set<UUID> DIRTY = ConcurrentHashMap.newKeySet();

    private QuestNotifier() {}

    /**
     * If {@code before} was not READY and {@code after} is READY, broadcast
     * a completion message to the player's team.
     */
    public static void onTransition(ServerPlayer player, Quest quest, QuestStatus before, QuestStatus after) {
        if (after != QuestStatus.READY || before == QuestStatus.READY || before == QuestStatus.CLAIMED) return;

        MutableComponent msg = Component.literal("Quest completed: ")
                .withStyle(ChatFormatting.GREEN)
                .append(QuestText.questTitle(quest).copy().withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("!").withStyle(ChatFormatting.GREEN));

        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        boolean sent = false;
        if (!team.solo()) {
            for (UUID m : team.members()) {
                ServerPlayer mp = player.server.getPlayerList().getPlayer(m);
                if (mp != null) {
                    mp.sendSystemMessage(msg);
                    markNotified(mp, quest.fullId());
                    sent = true;
                }
            }
        }
        if (!sent) {
            player.sendSystemMessage(msg);
            markNotified(player, quest.fullId());
        }

        // Auto-claim is handled by the callers (ProgressService, pollers,
        // ClaimService) via QuestEvaluator.recomputeAllAndAutoClaim() — not
        // here. Triggering ClaimService.claim from inside a notification that
        // fires mid-recompute caused re-entrant auto-claim chains and was
        // redundant with the sweep the caller already runs.
    }

    private static Set<String> notifiedSet(ServerPlayer player) {
        return CACHE.computeIfAbsent(player.getUUID(), id -> loadFromNbt(player));
    }

    private static Set<String> loadFromNbt(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        Set<String> out = ConcurrentHashMap.newKeySet();
        if (!root.contains(NOTIFIED_KEY, Tag.TAG_LIST)) return out;
        ListTag list = root.getList(NOTIFIED_KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) out.add(list.getString(i));
        return out;
    }

    private static void markNotified(ServerPlayer player, String fullId) {
        Set<String> set = notifiedSet(player);
        if (set.add(fullId)) DIRTY.add(player.getUUID());
    }

    private static void flush(ServerPlayer player) {
        UUID id = player.getUUID();
        if (!DIRTY.remove(id)) return;
        Set<String> set = CACHE.get(id);
        if (set == null) return;
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        ListTag list = new ListTag();
        for (String qid : set) list.add(StringTag.valueOf(qid));
        persisted.put(NOTIFIED_KEY, list);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void replayCompleted(ServerPlayer player, TeamQuestProgress team) {
        Set<String> notified = notifiedSet(player);
        int count = 0;
        for (Chapter chapter : QuestRegistry.allChapters()) {
            for (Quest quest : chapter.quests()) {
                QuestProgress qp = team.peek(quest.fullId());
                if (qp == null) continue;
                QuestStatus s = qp.status();
                if (s != QuestStatus.READY && s != QuestStatus.CLAIMED) continue;
                // Skip quests this player has already been told about — only
                // brand-new completions (e.g. team-mate finished it while this
                // player was offline) should re-notify.
                if (!notified.add(quest.fullId())) continue;
                DIRTY.add(player.getUUID());

                MutableComponent msg = Component.literal("Quest completed: ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(QuestText.questTitle(quest).copy().withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("!").withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(msg);
                count++;
            }
        }
        if (count > 0) {
            player.sendSystemMessage(Component.literal("(" + count + " quest"
                    + (count == 1 ? "" : "s") + " already completed by your team)")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            flush(sp);
            CACHE.remove(sp.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (ServerPlayer sp : event.getServer().getPlayerList().getPlayers()) {
            flush(sp);
        }
        CACHE.clear();
        DIRTY.clear();
    }
}
