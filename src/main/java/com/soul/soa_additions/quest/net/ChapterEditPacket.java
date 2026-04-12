package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.network.ModNetworking;
import com.soul.soa_additions.quest.QuestLifecycleHandler;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.QuestSource;
import com.soul.soa_additions.quest.editor.EditModeTracker;
import com.soul.soa_additions.quest.editor.FileQuestOverrideStorage;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.Visibility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

/**
 * Bidirectional chapter-level edit packet. Covers create, delete, reorder,
 * rename, and full property edit — operations that don't fit the per-quest
 * {@link QuestEditPacket}. Persisted via {@link FileQuestOverrideStorage}
 * and rebroadcast to all clients so chapter list state stays in sync without
 * a relog.
 */
public record ChapterEditPacket(
        Op op,
        String chapterId,
        String title,
        List<String> orderedIds,
        // Fields used only by EDIT:
        List<String> description,
        String icon,
        Visibility visibility,
        List<String> requiresChapters,
        List<String> requiresQuests,
        String parentChapter
) {

    public enum Op { ADD, DELETE, REORDER, RENAME, EDIT }

    public static ChapterEditPacket add(String id, String title, String parentChapter) {
        return new ChapterEditPacket(Op.ADD, id, title, List.of(),
                List.of(), "", Visibility.NORMAL, List.of(), List.of(), parentChapter == null ? "" : parentChapter);
    }
    public static ChapterEditPacket delete(String id) {
        return new ChapterEditPacket(Op.DELETE, id, "", List.of(),
                List.of(), "", Visibility.NORMAL, List.of(), List.of(), "");
    }
    public static ChapterEditPacket reorder(List<String> orderedIds) {
        return new ChapterEditPacket(Op.REORDER, "", "", orderedIds,
                List.of(), "", Visibility.NORMAL, List.of(), List.of(), "");
    }
    public static ChapterEditPacket rename(String id, String newTitle) {
        return new ChapterEditPacket(Op.RENAME, id, newTitle, List.of(),
                List.of(), "", Visibility.NORMAL, List.of(), List.of(), "");
    }
    public static ChapterEditPacket edit(String id, String title, List<String> description,
                                         String icon, Visibility visibility,
                                         List<String> requiresChapters,
                                         List<String> requiresQuests,
                                         String parentChapter) {
        return new ChapterEditPacket(Op.EDIT, id, title, List.of(),
                description, icon, visibility, requiresChapters, requiresQuests, parentChapter);
    }

    public static void encode(ChapterEditPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.op);
        buf.writeUtf(pkt.chapterId);
        buf.writeUtf(pkt.title);
        buf.writeVarInt(pkt.orderedIds.size());
        for (String id : pkt.orderedIds) buf.writeUtf(id);
        // EDIT fields — always written so decode is uniform.
        buf.writeVarInt(pkt.description.size());
        for (String line : pkt.description) buf.writeUtf(line);
        buf.writeUtf(pkt.icon);
        buf.writeEnum(pkt.visibility);
        buf.writeVarInt(pkt.requiresChapters.size());
        for (String id : pkt.requiresChapters) buf.writeUtf(id);
        buf.writeVarInt(pkt.requiresQuests.size());
        for (String id : pkt.requiresQuests) buf.writeUtf(id);
        buf.writeUtf(pkt.parentChapter);
    }

    public static ChapterEditPacket decode(FriendlyByteBuf buf) {
        Op op = buf.readEnum(Op.class);
        String chapterId = buf.readUtf();
        String title = buf.readUtf();
        int n = buf.readVarInt();
        List<String> ids = new ArrayList<>(n);
        for (int i = 0; i < n; i++) ids.add(buf.readUtf());
        int descN = buf.readVarInt();
        List<String> desc = new ArrayList<>(descN);
        for (int i = 0; i < descN; i++) desc.add(buf.readUtf());
        String icon = buf.readUtf();
        Visibility vis = buf.readEnum(Visibility.class);
        int reqChN = buf.readVarInt();
        List<String> reqCh = new ArrayList<>(reqChN);
        for (int i = 0; i < reqChN; i++) reqCh.add(buf.readUtf());
        int reqQN = buf.readVarInt();
        List<String> reqQ = new ArrayList<>(reqQN);
        for (int i = 0; i < reqQN; i++) reqQ.add(buf.readUtf());
        String parent = buf.readUtf();
        return new ChapterEditPacket(op, chapterId, title, ids, desc, icon, vis, reqCh, reqQ, parent);
    }

    public static void handle(ChapterEditPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        NetworkDirection dir = c.getDirection();
        c.enqueueWork(() -> {
            if (dir == NetworkDirection.PLAY_TO_SERVER) {
                handleServer(pkt, c.getSender());
            } else {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applyToRegistry(pkt));
            }
        });
        c.setPacketHandled(true);
    }

    // ---------- shared mutation ----------

    private static void applyToRegistry(ChapterEditPacket pkt) {
        switch (pkt.op) {
            case ADD -> {
                if (!QuestRegistry.chapter(pkt.chapterId).isPresent()) {
                    Chapter fresh = blankChapter(pkt.chapterId, pkt.title, pkt.parentChapter);
                    QuestRegistry.updateChapter(fresh);
                }
            }
            case DELETE -> QuestRegistry.removeChapter(pkt.chapterId);
            case REORDER -> QuestRegistry.reorderChapters(pkt.orderedIds);
            case RENAME -> {
                Chapter cur = QuestRegistry.chapter(pkt.chapterId).orElse(null);
                if (cur == null) return;
                QuestRegistry.updateChapter(new Chapter(
                        cur.id(), pkt.title, cur.description(), cur.icon(),
                        cur.sortOrder(), cur.requiresChapters(), cur.requiresQuests(),
                        cur.visibility(), cur.modes(), cur.quests(), cur.source(),
                        cur.parentChapter()));
            }
            case EDIT -> {
                Chapter cur = QuestRegistry.chapter(pkt.chapterId).orElse(null);
                if (cur == null) return;
                QuestRegistry.updateChapter(new Chapter(
                        cur.id(), pkt.title, pkt.description, pkt.icon,
                        cur.sortOrder(), pkt.requiresChapters, pkt.requiresQuests,
                        pkt.visibility, cur.modes(), cur.quests(), cur.source(),
                        pkt.parentChapter));
            }
        }
        com.soul.soa_additions.quest.client.QuestBookScreen.onChapterMutated(pkt.chapterId);
    }

    private static Chapter blankChapter(String id, String title, String parentChapter) {
        return new Chapter(
                id, title, new ArrayList<>(), "minecraft:writable_book",
                1000, new ArrayList<>(), new ArrayList<>(),
                Visibility.NORMAL, EnumSet.allOf(PackMode.class),
                new ArrayList<Quest>(), QuestSource.WORLD_EDITS,
                parentChapter == null ? "" : parentChapter);
    }

    // ---------- server ----------

    private static void handleServer(ChapterEditPacket pkt, ServerPlayer sender) {
        if (sender == null) return;
        if (!sender.hasPermissions(2)) return;
        if (!EditModeTracker.isActive(sender.getUUID())) return;

        FileQuestOverrideStorage storage = QuestLifecycleHandler.storage();

        switch (pkt.op) {
            case ADD -> {
                if (QuestRegistry.chapter(pkt.chapterId).isPresent()) return;
                Chapter fresh = blankChapter(pkt.chapterId, pkt.title, pkt.parentChapter);
                QuestRegistry.updateChapter(fresh);
                if (storage != null) storage.saveChapter(fresh, EditModeTracker.targetOf(sender.getUUID()));
            }
            case DELETE -> {
                if (QuestRegistry.chapter(pkt.chapterId).isEmpty()) return;
                QuestRegistry.removeChapter(pkt.chapterId);
                if (storage != null) storage.deleteChapterOverride(pkt.chapterId);
            }
            case RENAME -> {
                Chapter cur = QuestRegistry.chapter(pkt.chapterId).orElse(null);
                if (cur == null) return;
                String title = pkt.title == null ? "" : pkt.title.trim();
                if (title.isEmpty()) return;
                Chapter renamed = new Chapter(
                        cur.id(), title, cur.description(), cur.icon(),
                        cur.sortOrder(), cur.requiresChapters(), cur.requiresQuests(),
                        cur.visibility(), cur.modes(), cur.quests(), cur.source(),
                        cur.parentChapter());
                QuestRegistry.updateChapter(renamed);
                if (storage != null) storage.saveChapter(renamed, EditModeTracker.targetOf(sender.getUUID()));
            }
            case EDIT -> {
                Chapter cur = QuestRegistry.chapter(pkt.chapterId).orElse(null);
                if (cur == null) return;
                Chapter edited = new Chapter(
                        cur.id(), pkt.title, pkt.description, pkt.icon,
                        cur.sortOrder(), pkt.requiresChapters, pkt.requiresQuests,
                        pkt.visibility, cur.modes(), cur.quests(), cur.source(),
                        pkt.parentChapter);
                QuestRegistry.updateChapter(edited);
                if (storage != null) storage.saveChapter(edited, EditModeTracker.targetOf(sender.getUUID()));
            }
            case REORDER -> {
                QuestRegistry.reorderChapters(pkt.orderedIds);
                if (storage != null) {
                    int order = 10;
                    for (String id : pkt.orderedIds) {
                        Chapter cur = QuestRegistry.chapter(id).orElse(null);
                        if (cur == null) continue;
                        if (cur.sortOrder() != order) {
                            Chapter renumbered = new Chapter(
                                    cur.id(), cur.title(), cur.description(), cur.icon(),
                                    order, cur.requiresChapters(), cur.requiresQuests(),
                                    cur.visibility(), cur.modes(), cur.quests(), cur.source(),
                                    cur.parentChapter());
                            QuestRegistry.updateChapter(renumbered);
                            storage.saveChapter(renumbered, EditModeTracker.targetOf(sender.getUUID()));
                        }
                        order += 10;
                    }
                }
            }
        }

        ModNetworking.CHANNEL.send(PacketDistributor.ALL.noArg(), pkt);

        org.slf4j.LoggerFactory.getLogger("soa_additions/quest-editor")
                .info("Chapter {} {} by {}", pkt.op, pkt.chapterId.isEmpty() ? "(reorder)" : pkt.chapterId,
                        sender.getGameProfile().getName());
    }
}
