package com.soul.soa_additions.quest.progress;

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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Broadcasts "Quest completed: <title>!" chat messages when a quest
 * transitions VISIBLE -> READY. Centralized so every recompute site can
 * call the same one-liner and we only tweak formatting in one place.
 *
 * <p>Sent to every online member of the acting player's team so solo
 * players get it for themselves and team members see each other's
 * completions in real time.</p>
 */
public final class QuestNotifier {

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

        // Auto-claim rewards if the quest is flagged that way. Done after the
        // notification so the "Quest completed" line lands before the
        // "✔ Claimed" line that ClaimService emits.
        if (quest.autoClaim()) {
            ClaimService.claim(player, quest.fullId());
        }
    }

    /**
     * Replay "Quest completed: <title>!" messages to a single player for
     * every quest their team has already completed (status READY or
     * CLAIMED). Used on login and after joining an existing team so a
     * catching-up player sees everything the team has finished.
     */
    // Key under player persistent data. Using the Forge PERSISTED_NBT_TAG
    // sub-compound so this survives death + full logout.
    private static final String NOTIFIED_KEY = "soa_quests_notified";

    private static Set<String> loadNotified(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        Set<String> out = new HashSet<>();
        if (!root.contains(NOTIFIED_KEY, Tag.TAG_LIST)) return out;
        ListTag list = root.getList(NOTIFIED_KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) out.add(list.getString(i));
        return out;
    }

    private static void markNotified(ServerPlayer player, String fullId) {
        Set<String> set = loadNotified(player);
        if (set.add(fullId)) saveNotified(player, set);
    }

    private static void saveNotified(ServerPlayer player, Set<String> ids) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        ListTag list = new ListTag();
        for (String id : ids) list.add(StringTag.valueOf(id));
        persisted.put(NOTIFIED_KEY, list);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void replayCompleted(ServerPlayer player, TeamQuestProgress team) {
        Set<String> notified = loadNotified(player);
        int count = 0;
        boolean dirty = false;
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
                dirty = true;

                MutableComponent msg = Component.literal("Quest completed: ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(QuestText.questTitle(quest).copy().withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("!").withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(msg);
                count++;
            }
        }
        if (dirty) saveNotified(player, notified);
        if (count > 0) {
            player.sendSystemMessage(Component.literal("(" + count + " quest"
                    + (count == 1 ? "" : "s") + " already completed by your team)")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
