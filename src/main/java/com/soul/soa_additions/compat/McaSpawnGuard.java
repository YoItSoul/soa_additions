package com.soul.soa_additions.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Non-blocking replacement for MCA's {@code WorldUtils.spawnEntity}.
 *
 * <p>MCA's version is two calls:</p>
 * <pre>
 *   mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), reason, null, null);
 *   level.addFreshEntity(mob);
 * </pre>
 *
 * <p>{@code Level.getCurrentDifficultyAt} only wants the chunk's inhabited time, but it
 * gets there via {@code getChunkAt}, which is {@code getChunk(x, z, FULL, true)} — a
 * <em>loading</em> request that resolves through {@code ServerChunkCache.getChunkBlocking}
 * and parks the server thread until the chunk finishes generating. Under
 * {@code SpawnQueue.tick} that ran to 43.4% of the server thread while travelling
 * (40.4% of it parked in {@code Unsafe.park}), with server ticks at p95 427ms and
 * p99 6820ms.</p>
 *
 * <p><strong>Why the previous HEAD guard was not enough.</strong> {@link ChunkLoadGuard}
 * was already wired into this call site and never once deferred across whole sessions,
 * while three other guard sites did — so the pre-check kept passing and the load
 * happened anyway. Predicting the block is unreliable here; this class removes it
 * instead. When the chunk is resident we read {@code inhabitedTime} straight off the
 * chunk object we already hold and never re-enter {@code getChunkAt}.</p>
 *
 * <p>The not-resident branch keeps the old behaviour exactly — drop the spawn — so this
 * is not a behavioural change, only a removal of the blocking path.</p>
 */
public final class McaSpawnGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_ChunkGuard");

    private static final AtomicLong HANDLED = new AtomicLong();
    private static final AtomicLong SKIPPED = new AtomicLong();
    private static volatile boolean announced;

    private McaSpawnGuard() {}

    /**
     * Performs MCA's spawn without any chunk-loading call.
     *
     * @return {@code true} when this method has fully handled the spawn and the caller
     *         must cancel; {@code false} to let MCA's own body run unchanged.
     */
    // Forge wants ForgeEventFactory.onFinalizeSpawn here, but routing through it would fire MobSpawnEvent.FinalizeSpawn where the original does not, letting other mods veto these spawns.
    @SuppressWarnings("deprecation")
    public static boolean spawnWithoutBlocking(Level level, Mob mob, MobSpawnType reason) {
        if (!(level instanceof ServerLevel serverLevel)) {
            // Client/other level: not our concern, and getChunkNow is main-thread only.
            return false;
        }

        BlockPos pos = mob.blockPosition();
        LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()));

        if (chunk == null) {
            // Same outcome as the previous guard: spawning here would force a load.
            SKIPPED.incrementAndGet();
            announce();
            return true;
        }

        // Resident chunk: build the difficulty from the chunk in hand. This is what
        // Level.getCurrentDifficultyAt computes, minus the getChunkAt round trip.
        DifficultyInstance difficulty = new DifficultyInstance(
                serverLevel.getDifficulty(),
                serverLevel.getDayTime(),
                chunk.getInhabitedTime(),
                serverLevel.getMoonBrightness());

        mob.finalizeSpawn(serverLevel, difficulty, reason, null, null);
        serverLevel.addFreshEntity(mob);

        HANDLED.incrementAndGet();
        announce();
        return true;
    }

    /** Spawns handled locally without a chunk load. */
    public static long handled() {
        return HANDLED.get();
    }

    /** Spawns dropped because the target chunk was not resident. */
    public static long skipped() {
        return SKIPPED.get();
    }

    /**
     * A {@code @Pseudo} mixin against an absent or renamed target fails silently by
     * design, so this line is the only runtime proof the injection actually applied.
     */
    private static void announce() {
        if (!announced) {
            announced = true;
            LOGGER.info("Guard active for mca:spawnEntity — villager spawns no longer "
                    + "force-load chunks on the server thread (logs once per session; "
                    + "see /soa optimizer for counts).");
        }
    }
}
