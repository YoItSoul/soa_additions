package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.network.ModNetworking;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.editor.EditModeTracker;
import com.soul.soa_additions.quest.editor.EditTarget;
import com.soul.soa_additions.quest.editor.FileQuestOverrideStorage;
import com.soul.soa_additions.quest.QuestLifecycleHandler;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Bidirectional packet for quest position edits.
 *
 * <p><b>C→S:</b> An op with edit mode active drags a quest node. The client
 * sends the new content-relative pixel coordinates; the server validates op
 * level + edit mode, rebuilds the chapter with the updated quest, persists
 * via {@link FileQuestOverrideStorage}, and then broadcasts the same packet
 * S→C to every player so concurrent viewers see the move without a relog.</p>
 *
 * <p><b>S→C:</b> Clients apply the new coordinates to a local override map
 * read alongside {@link com.soul.soa_additions.quest.layout.LayoutResult} by
 * the quest book screen. Positions land in-place; the screen doesn't
 * recompute layout.</p>
 */
public record QuestMovePacket(String chapterId, String questId, int x, int y) {

    public static void encode(QuestMovePacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.chapterId);
        buf.writeUtf(pkt.questId);
        buf.writeVarInt(pkt.x);
        buf.writeVarInt(pkt.y);
    }

    public static QuestMovePacket decode(FriendlyByteBuf buf) {
        return new QuestMovePacket(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(QuestMovePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        NetworkDirection dir = c.getDirection();
        c.enqueueWork(() -> {
            if (dir == NetworkDirection.PLAY_TO_SERVER) {
                handleServer(pkt, c.getSender());
            } else {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientApply.move(pkt));
            }
        });
        c.setPacketHandled(true);
    }

    private static void handleServer(QuestMovePacket pkt, ServerPlayer sender) {
        if (sender == null) return;
        if (!sender.hasPermissions(2)) return;
        if (!EditModeTracker.isActive(sender.getUUID())) return;

        Chapter chapter = QuestRegistry.chapter(pkt.chapterId).orElse(null);
        if (chapter == null) return;

        List<Quest> updated = new ArrayList<>(chapter.quests().size());
        boolean hit = false;
        for (Quest q : chapter.quests()) {
            if (q.id().equals(pkt.questId)) {
                updated.add(q.withPosition(pkt.x, pkt.y));
                hit = true;
            } else {
                updated.add(q);
            }
        }
        if (!hit) return;

        Chapter replacement = new Chapter(
                chapter.id(), chapter.title(), chapter.description(), chapter.icon(),
                chapter.sortOrder(), chapter.requiresChapters(), chapter.requiresQuests(),
                chapter.visibility(), chapter.modes(), updated, chapter.source(),
                chapter.parentChapter());
        QuestRegistry.updateChapter(replacement);

        FileQuestOverrideStorage storage = QuestLifecycleHandler.storage();
        if (storage != null) {
            EditTarget target = EditModeTracker.targetOf(sender.getUUID());
            if (storage.canWrite(replacement, target)) {
                storage.saveChapter(replacement, target);
            } else {
                // If the active target can't accept the write (e.g. author
                // mode in a shipped server), fall back to world override so
                // the move still survives a restart.
                storage.saveChapter(replacement, EditTarget.WORLD_OVERRIDE);
            }
        }

        // Rebroadcast to every player viewing the book so their override
        // maps update live. Senders included — echo confirms the write.
        MinecraftServer server = sender.getServer();
        if (server != null) {
            ModNetworking.CHANNEL.send(PacketDistributor.ALL.noArg(), pkt);
        }
        org.slf4j.LoggerFactory.getLogger("soa_additions/quest-editor")
                .info("Quest moved: {}/{} -> ({},{}) by {}",
                        pkt.chapterId, pkt.questId, pkt.x, pkt.y,
                        sender.getGameProfile().getName());
    }

    @OnlyIn(Dist.CLIENT)
    private static final class ClientApply {
        static void move(QuestMovePacket pkt) {
            ClientQuestEditState.applyMove(pkt);
        }
    }
}
