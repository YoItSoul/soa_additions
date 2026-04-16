package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.network.ModNetworking;
import com.soul.soa_additions.quest.QuestLifecycleHandler;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.QuestSource;
import com.soul.soa_additions.quest.editor.EditModeTracker;
import com.soul.soa_additions.quest.editor.EditTarget;
import com.soul.soa_additions.quest.editor.FileQuestOverrideStorage;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.NodeShape;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.Visibility;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.network.FriendlyByteBuf;
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
 * Bidirectional quest edit packet. Covers upsert (create or edit) and delete
 * operations — tasks and rewards are still JSON-only, so this packet
 * intentionally doesn't carry them. When a quest is upserted, tasks/rewards
 * on the server are preserved from the existing quest (or initialized empty
 * for brand-new quests); everything the GUI can edit flows through here.
 *
 * <p><b>C→S:</b> an op with edit mode active submits a form or deletes a
 * quest. The server re-validates permissions, mutates the chapter, saves via
 * {@link FileQuestOverrideStorage}, and rebroadcasts the same packet to every
 * client so viewers update live.</p>
 *
 * <p><b>S→C:</b> clients receive the echo and apply the same mutation to
 * their local {@link QuestRegistry} so the quest book reflects the change
 * without a reload. Existing tasks/rewards are preserved across upserts on
 * the client side using the same pattern as the server.</p>
 */
public record QuestEditPacket(
        Op op,
        String chapterId,
        String questId,
        // UPSERT fields — unused for DELETE.
        String title,
        List<String> description,
        String icon,
        NodeShape shape,
        Visibility visibility,
        boolean optional,
        boolean autoClaim,
        boolean depsAll,
        int minDeps,
        List<String> dependencies,
        int posX,
        int posY,
        List<TaskDraft> tasks,
        boolean showDeps,
        List<RewardDraft> rewards,
        int size,
        boolean repeatable,
        com.soul.soa_additions.quest.model.RewardScope repeatScope,
        List<String> exclusions
) {

    public enum Op { UPSERT, DELETE }

    public static QuestEditPacket delete(String chapterId, String questId) {
        return new QuestEditPacket(Op.DELETE, chapterId, questId,
                "", List.of(), "", NodeShape.ICON, Visibility.NORMAL, false, false, true, -1, List.of(), -1, -1, List.of(), true, List.of(), Quest.DEFAULT_SIZE, false, com.soul.soa_additions.quest.model.RewardScope.TEAM, List.of());
    }

    public static void encode(QuestEditPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.op);
        buf.writeUtf(pkt.chapterId);
        buf.writeUtf(pkt.questId);
        if (pkt.op == Op.UPSERT) {
            buf.writeUtf(pkt.title);
            buf.writeVarInt(pkt.description.size());
            for (String l : pkt.description) buf.writeUtf(l);
            buf.writeUtf(pkt.icon);
            buf.writeEnum(pkt.shape);
            buf.writeEnum(pkt.visibility);
            buf.writeBoolean(pkt.optional);
            buf.writeBoolean(pkt.autoClaim);
            buf.writeBoolean(pkt.depsAll);
            buf.writeVarInt(pkt.minDeps);
            buf.writeVarInt(pkt.dependencies.size());
            for (String d : pkt.dependencies) buf.writeUtf(d);
            buf.writeVarInt(pkt.posX);
            buf.writeVarInt(pkt.posY);
            buf.writeVarInt(pkt.tasks.size());
            for (TaskDraft d : pkt.tasks) TaskDraft.encode(d, buf);
            buf.writeBoolean(pkt.showDeps);
            buf.writeVarInt(pkt.rewards.size());
            for (RewardDraft d : pkt.rewards) RewardDraft.encode(d, buf);
            buf.writeVarInt(pkt.size);
            buf.writeBoolean(pkt.repeatable);
            buf.writeEnum(pkt.repeatScope);
            buf.writeVarInt(pkt.exclusions.size());
            for (String s : pkt.exclusions) buf.writeUtf(s);
        }
    }

    public static QuestEditPacket decode(FriendlyByteBuf buf) {
        Op op = buf.readEnum(Op.class);
        String chapterId = buf.readUtf();
        String questId = buf.readUtf();
        if (op == Op.DELETE) return delete(chapterId, questId);
        String title = buf.readUtf();
        int n = buf.readVarInt();
        List<String> desc = new ArrayList<>(n);
        for (int i = 0; i < n; i++) desc.add(buf.readUtf());
        String icon = buf.readUtf();
        NodeShape shape = buf.readEnum(NodeShape.class);
        Visibility visibility = buf.readEnum(Visibility.class);
        boolean optional = buf.readBoolean();
        boolean autoClaim = buf.readBoolean();
        boolean depsAll = buf.readBoolean();
        int minDeps = buf.readVarInt();
        int dn = buf.readVarInt();
        List<String> deps = new ArrayList<>(dn);
        for (int i = 0; i < dn; i++) deps.add(buf.readUtf());
        int px = buf.readVarInt();
        int py = buf.readVarInt();
        int tn = buf.readVarInt();
        List<TaskDraft> tasks = new ArrayList<>(tn);
        for (int i = 0; i < tn; i++) tasks.add(TaskDraft.decode(buf));
        boolean showDeps = buf.readBoolean();
        int rn = buf.readVarInt();
        List<RewardDraft> rewards = new ArrayList<>(rn);
        for (int i = 0; i < rn; i++) rewards.add(RewardDraft.decode(buf));
        int size = buf.readVarInt();
        boolean repeatable = buf.readBoolean();
        com.soul.soa_additions.quest.model.RewardScope repeatScope = buf.readEnum(com.soul.soa_additions.quest.model.RewardScope.class);
        int exn = buf.readVarInt();
        List<String> excl = new ArrayList<>(exn);
        for (int i = 0; i < exn; i++) excl.add(buf.readUtf());
        return new QuestEditPacket(op, chapterId, questId, title, desc, icon, shape,
                visibility, optional, autoClaim, depsAll, minDeps, deps, px, py, tasks, showDeps, rewards, size, repeatable, repeatScope, excl);
    }

    public static void handle(QuestEditPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        NetworkDirection dir = c.getDirection();
        c.enqueueWork(() -> {
            if (dir == NetworkDirection.PLAY_TO_SERVER) {
                handleServer(pkt, c.getSender());
            } else {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientApply.run(pkt));
            }
        });
        c.setPacketHandled(true);
    }

    // ---------- server ----------

    private static void handleServer(QuestEditPacket pkt, ServerPlayer sender) {
        if (sender == null) return;
        if (!sender.hasPermissions(2)) return;
        if (!EditModeTracker.isActive(sender.getUUID())) return;

        Chapter chapter = QuestRegistry.chapter(pkt.chapterId).orElse(null);
        if (chapter == null) return;

        Chapter replacement = applyToChapter(chapter, pkt);
        if (replacement == null) return;

        // Reject upserts that would introduce a dependency cycle. Cycles
        // would freeze the affected quests in LOCKED forever, so the editor
        // surfaces them in red and the server refuses the save outright.
        if (pkt.op == Op.UPSERT && introducesCycle(replacement, pkt.questId)) {
            org.slf4j.LoggerFactory.getLogger("soa_additions/quest-editor")
                    .warn("Refusing UPSERT of {}/{} by {}: dependency cycle",
                            pkt.chapterId, pkt.questId, sender.getGameProfile().getName());
            return;
        }

        QuestRegistry.updateChapter(replacement);

        FileQuestOverrideStorage storage = QuestLifecycleHandler.storage();
        if (storage != null) {
            EditTarget target = EditModeTracker.targetOf(sender.getUUID());
            if (storage.canWrite(replacement, target)) {
                storage.saveChapter(replacement, target);
            } else {
                storage.saveChapter(replacement, EditTarget.WORLD_OVERRIDE);
            }
        }

        ModNetworking.CHANNEL.send(PacketDistributor.ALL.noArg(), pkt);

        org.slf4j.LoggerFactory.getLogger("soa_additions/quest-editor")
                .info("Quest {} {}/{} by {}",
                        pkt.op, pkt.chapterId, pkt.questId,
                        sender.getGameProfile().getName());
    }

    // ---------- shared mutation ----------

    /**
     * Apply the packet to a chapter and return the replacement. Shared
     * between server (persists + broadcasts) and client (applies broadcast to
     * local registry). Returns null if the mutation is a no-op (e.g. delete
     * targeting a missing quest).
     */
    public static Chapter applyToChapter(Chapter chapter, QuestEditPacket pkt) {
        List<Quest> existing = chapter.quests();
        List<Quest> updated = new ArrayList<>(existing.size() + 1);

        if (pkt.op == Op.DELETE) {
            boolean removed = false;
            for (Quest q : existing) {
                if (q.id().equals(pkt.questId)) { removed = true; continue; }
                // Scrub dangling dep references so the chapter doesn't end
                // up pointing at a quest that no longer exists.
                if (q.dependencies().contains(pkt.questId)) {
                    List<String> deps = new ArrayList<>(q.dependencies());
                    deps.remove(pkt.questId);
                    updated.add(new Quest(q.id(), q.chapterId(), q.title(), q.description(),
                            q.icon(), q.visibility(), q.optional(), deps, q.depsAll(), q.minDeps(),
                            q.tasks(), q.rewards(), q.modes(), q.source(), q.autoClaim(),
                            q.shape(), q.posX(), q.posY(), q.showDeps(), q.size(), q.repeatable(), q.repeatScope(), q.exclusions()));
                } else {
                    updated.add(q);
                }
            }
            if (!removed) return null;
        } else {
            // UPSERT: replace existing or append new. Tasks/rewards are
            // preserved from the existing quest — this packet doesn't ship
            // them because the GUI can't edit them yet.
            // Build the live task list from drafts. Drafts replace existing
            // tasks wholesale — the editor is the source of truth once it's
            // been opened, so dropping a row in the GUI must drop it on save.
            List<QuestTask> taskList = new ArrayList<>(pkt.tasks.size());
            for (TaskDraft d : pkt.tasks) taskList.add(d.toTask());

            // Reward drafts are the source of truth on save — the form ships
            // the full list, including round-tripped OTHER entries for types
            // it can't edit directly. Malformed drafts are silently dropped.
            List<QuestReward> rewardList = new ArrayList<>(pkt.rewards.size());
            for (RewardDraft d : pkt.rewards) {
                QuestReward built = d.toReward();
                if (built != null) rewardList.add(built);
            }

            boolean replaced = false;
            for (Quest q : existing) {
                if (q.id().equals(pkt.questId)) {
                    updated.add(new Quest(
                            pkt.questId, chapter.id(), pkt.title,
                            new ArrayList<>(pkt.description), pkt.icon,
                            pkt.visibility, pkt.optional,
                            new ArrayList<>(pkt.dependencies), pkt.depsAll, pkt.minDeps,
                            taskList, rewardList, q.modes(), q.source(),
                            pkt.autoClaim, pkt.shape, pkt.posX, pkt.posY, pkt.showDeps, pkt.size, pkt.repeatable, pkt.repeatScope, new ArrayList<>(pkt.exclusions)));
                    replaced = true;
                } else {
                    updated.add(q);
                }
            }
            if (!replaced) {
                // Brand-new quest — tasks and rewards both come from the form,
                // source = world edits so the loader persists via the override
                // path on future reloads.
                updated.add(new Quest(
                        pkt.questId, chapter.id(), pkt.title,
                        new ArrayList<>(pkt.description), pkt.icon,
                        pkt.visibility, pkt.optional,
                        new ArrayList<>(pkt.dependencies), pkt.depsAll, pkt.minDeps,
                        taskList, rewardList,
                        chapter.modes(), QuestSource.WORLD_EDITS,
                        pkt.autoClaim, pkt.shape, pkt.posX, pkt.posY, pkt.showDeps, pkt.size, pkt.repeatable, pkt.repeatScope, new ArrayList<>(pkt.exclusions)));
            }
        }

        return new Chapter(chapter.id(), chapter.title(), chapter.description(),
                chapter.icon(), chapter.sortOrder(), chapter.requiresChapters(),
                chapter.requiresQuests(), chapter.visibility(),
                chapter.modes(), updated, chapter.source(),
                chapter.parentChapter());
    }

    /** Walk dependencies starting from {@code questId} and report whether the
     *  graph in {@code chapter} contains a cycle that touches that quest. */
    private static boolean introducesCycle(Chapter chapter, String questId) {
        java.util.Map<String, java.util.List<String>> deps = new java.util.HashMap<>();
        for (Quest q : chapter.quests()) {
            java.util.List<String> resolved = new java.util.ArrayList<>();
            for (String d : q.dependencies()) {
                resolved.add(d.contains("/") ? d : chapter.id() + "/" + d);
            }
            deps.put(q.fullId(), resolved);
        }
        String start = chapter.id() + "/" + questId;
        java.util.Set<String> stackSet = new java.util.HashSet<>();
        return dfsHasCycle(deps, start, stackSet, new java.util.HashSet<>());
    }

    private static boolean dfsHasCycle(java.util.Map<String, java.util.List<String>> deps,
                                       String node, java.util.Set<String> onStack,
                                       java.util.Set<String> done) {
        if (onStack.contains(node)) return true;
        if (done.contains(node)) return false;
        onStack.add(node);
        for (String n : deps.getOrDefault(node, java.util.List.of())) {
            if (dfsHasCycle(deps, n, onStack, done)) return true;
        }
        onStack.remove(node);
        done.add(node);
        return false;
    }

    private static void applyToRegistry(QuestEditPacket pkt) {
        Chapter chapter = QuestRegistry.chapter(pkt.chapterId).orElse(null);
        if (chapter == null) return;
        Chapter updated = applyToChapter(chapter, pkt);
        if (updated != null) {
            QuestRegistry.updateChapter(updated);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static final class ClientApply {
        static void run(QuestEditPacket pkt) {
            applyToRegistry(pkt);
            com.soul.soa_additions.quest.client.QuestBookScreen.onChapterMutated(pkt.chapterId);
        }
    }
}
