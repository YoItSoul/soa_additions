package com.soul.soa_additions.quest.events;

import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.net.QuestDeltaPacket;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestNotifier;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.QuestStatus;
import com.soul.soa_additions.quest.progress.TaskProgress;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.task.ObserveTask;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Once-per-second observe-task poller. Designed for zero overhead when no
 * active quest references an observe task: a single quest-tree walk collects
 * the relevant tasks first, and only then is a raycast performed (and only
 * out to the largest reach any of the active tasks asks for).
 */
public final class ObserveTaskPoller {

    // Cap raycast reach so a single mis-typed ObserveTask with maxReach=256
    // cannot drag the entity-hit AABB across an entire chunk of sky.
    private static final double MAX_OBSERVE_REACH = 128.0;

    private ObserveTaskPoller() {}

    /** Per-poll bookkeeping for one (quest, taskIndex, task) triple. */
    private record Pending(Quest quest, int taskIndex, ObserveTask task, QuestStatus statusBefore) {}

    public static void poll(ServerPlayer player) {
        // Cheapest possible early-out: if no observe tasks exist anywhere in
        // the loaded quest tree, we do not even touch player team data.
        if (!QuestRegistry.hasTasksOfType(ObserveTask.TYPE)) return;

        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData data = QuestProgressData.get(player.server);
        TeamQuestProgress tp = data.forTeam(team.id());
        // Delta capture, taken lazily right before the first mutation — the
        // common no-change poll shouldn't clone the full progress table.
        QuestDeltaPacket.Capture delta = null;

        // Walk only observe-task entries via the index. Cache quest status so
        // quests with multiple observe tasks don't pay recompute per-ref.
        List<Pending> pending = null;
        boolean needBlock = false, needEntity = false;
        double maxReach = 0.0;
        java.util.Map<Quest, QuestStatus> seen = new java.util.HashMap<>();
        for (QuestRegistry.TaskRef ref : QuestRegistry.tasksOfType(ObserveTask.TYPE)) {
            Quest quest = ref.quest();
            QuestStatus status = seen.computeIfAbsent(quest, q -> QuestEvaluator.recompute(q, tp));
            if (status != QuestStatus.VISIBLE && status != QuestStatus.READY) continue;
            ObserveTask ot = (ObserveTask) ref.task();
            QuestProgress qp = tp.get(quest.fullId());
            if (qp.task(ref.taskIndex()).count() >= ot.target()) continue;
            if (pending == null) pending = new ArrayList<>(2);
            pending.add(new Pending(quest, ref.taskIndex(), ot, status));
            if (ot.isBlock()) needBlock = true;
            else if (ot.isEntity()) needEntity = true;
            if (ot.reach() > maxReach) maxReach = ot.reach();
        }
        if (pending == null) return;
        if (maxReach > MAX_OBSERVE_REACH) maxReach = MAX_OBSERVE_REACH;

        // One block raycast and (optionally) one entity raycast, sized to the
        // largest reach any pending task wants.
        ResourceLocation hitBlockId = null;
        ResourceLocation hitEntityId = null;
        if (needBlock || needEntity) {
            Vec3 eye = player.getEyePosition(1.0F);
            Vec3 look = player.getViewVector(1.0F);
            Vec3 end = eye.add(look.x * maxReach, look.y * maxReach, look.z * maxReach);
            if (needBlock) {
                // Only clip when the endpoint chunk is already loaded.
                // Level.clip() walks blocks via getBlockState() which forces
                // chunk loads on miss — that was attributing 100% of sampled
                // chunk loads to soa_additions. Bail to a no-op when the ray
                // would leave the loaded area; the player will just need to
                // be a bit closer.
                // `hasChunkAt` was NOT a safe pre-check. It resolves through
                // getChunk(x, z, FULL, false), and load=false only skips creating
                // a ticket — the call still runs managedBlock on the future of any
                // ChunkHolder that exists but has not finished generating. While a
                // player travels that is true almost constantly, and this poller
                // runs every player tick: it measured 58% of the whole server
                // thread, with ModernFix's watchdog logging 40s+ ticks.
                //
                // getChunkNow never waits. clip() can only touch the chunk columns
                // spanned by eye..end, so verifying those is sufficient, and the
                // hit block is read from its chunk rather than from the level.
                ServerLevel lvl = player.serverLevel();
                int cxA = SectionPos.blockToSectionCoord(Mth.floor(eye.x));
                int czA = SectionPos.blockToSectionCoord(Mth.floor(eye.z));
                int cxB = SectionPos.blockToSectionCoord(Mth.floor(end.x));
                int czB = SectionPos.blockToSectionCoord(Mth.floor(end.z));
                boolean rayLoaded = true;
                for (int cx = Math.min(cxA, cxB); cx <= Math.max(cxA, cxB) && rayLoaded; cx++) {
                    for (int cz = Math.min(czA, czB); cz <= Math.max(czA, czB); cz++) {
                        if (lvl.getChunkSource().getChunkNow(cx, cz) == null) {
                            rayLoaded = false;
                            break;
                        }
                    }
                }
                if (rayLoaded) {
                    BlockHitResult bhr = lvl.clip(new ClipContext(
                            eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                    if (bhr.getType() == HitResult.Type.BLOCK) {
                        LevelChunk hitChunk = lvl.getChunkSource().getChunkNow(
                                SectionPos.blockToSectionCoord(bhr.getBlockPos().getX()),
                                SectionPos.blockToSectionCoord(bhr.getBlockPos().getZ()));
                        if (hitChunk != null) {
                            BlockState state = hitChunk.getBlockState(bhr.getBlockPos());
                            hitBlockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                        }
                    }
                }
            }
            if (needEntity) {
                AABB box = player.getBoundingBox().expandTowards(look.scale(maxReach)).inflate(1.0);
                EntityHitResult ehr = ProjectileUtil.getEntityHitResult(
                        player, eye, end, box, e -> !e.isSpectator() && e.isPickable(), maxReach * maxReach);
                if (ehr != null) {
                    Entity hit = ehr.getEntity();
                    hitEntityId = ForgeRegistries.ENTITY_TYPES.getKey(hit.getType());
                }
            }
        }

        boolean changed = false;
        boolean anyBecameReady = false;
        long tick = player.server.getTickCount();
        for (Pending p : pending) {
            ResourceLocation hitId = p.task.isBlock() ? hitBlockId : hitEntityId;
            if (hitId == null || !hitId.equals(p.task.id())) continue;
            QuestProgress qp = tp.get(p.quest.fullId());
            TaskProgress prog = qp.task(p.taskIndex);
            int next = Math.min(p.task.target(), prog.count() + 1);
            if (next != prog.count()) {
                if (delta == null) delta = QuestDeltaPacket.Capture.of(player);
                prog.setCount(next);
                qp.touch(tick);
                QuestStatus after = QuestEvaluator.recompute(p.quest, tp);
                QuestNotifier.onTransition(player, p.quest, p.statusBefore, after);
                if (after == QuestStatus.READY) anyBecameReady = true;
                changed = true;
            }
        }

        if (anyBecameReady) {
            QuestEvaluator.recomputeAllAndAutoClaim(tp, player);
        }

        if (changed) {
            data.touch();
            delta.sendChanges(player);
        }
    }
}
