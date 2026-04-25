package com.soul.soa_additions.tr.aura;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.core.AspectMap;
import com.soul.soa_additions.tr.core.AspectStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge event hooks that move aspects from destroyed blocks and slain
 * entities into the chunk's aura pool. The world-driven side of the design:
 * scanning teaches you which targets carry which aspects; harvesting and
 * killing them builds the ambient aspect pool you'll later draw against
 * for auramancy.
 *
 * <p>Roll model (per aspect on the source's composition):
 * <ul>
 *   <li><b>Block break:</b> 25% chance to impart {@code max(1, amount/2)}.</li>
 *   <li><b>Entity death:</b> 50% chance + full amount on player kills, 25% +
 *       half amount on environmental kills. Any death contributes — same
 *       "any block can contribute" semantics, no hostile-only gate. The
 *       randomness + low per-roll yield is the throttle, not the entity type.</li>
 * </ul>
 * Cap behaviour from {@link ChunkAura.Data#add} silently truncates if the
 * chunk is already saturated; no error, no spam.
 *
 * <p>Logs at INFO level whenever a death produces a non-empty impart so the
 * mechanic is observable during testing without dropping into a debugger.
 * Empty-impart deaths (no aspect entry, all rolls failed, or chunk saturated)
 * log at DEBUG so they don't spam INFO during normal gameplay.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AuraImpartHandler {

    private static final float BLOCK_BREAK_CHANCE = 0.25f;
    private static final float MOB_KILL_PLAYER_CHANCE = 0.50f;
    private static final float MOB_KILL_ENVIRONMENT_CHANCE = 0.25f;

    private AuraImpartHandler() {}

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) return;
        Level level = (Level) event.getLevel();
        if (!(level instanceof ServerLevel sl)) return;
        // No creative gate — any break contributes, same rule as any death
        // contributing. The 25% per-aspect roll + half-amount yield is the
        // throttle; gameplay-mode filtering would be inconsistent with the
        // rest of the impart pipeline.
        Block block = event.getState().getBlock();
        List<AspectStack> composition = AspectMap.forBlock(block);
        if (composition.isEmpty()) return;
        ChunkPos cp = new ChunkPos(event.getPos());
        LevelChunk chunk = sl.getChunkSource().getChunk(cp.x, cp.z, false);
        if (chunk == null) return;  // shouldn't happen — block break implies loaded chunk
        ChunkAura.Data aura = ChunkAura.of(chunk);
        if (aura == ChunkAura.empty()) return;
        for (AspectStack as : composition) {
            if (sl.random.nextFloat() < BLOCK_BREAK_CHANCE) {
                int delta = Math.max(1, as.amount() / 2);
                aura.add(as.aspect(), delta);
            }
        }
        chunk.setUnsaved(true);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel sl)) return;

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        List<AspectStack> composition = EntityAspectMap.forEntity(entity);
        if (composition.isEmpty()) {
            ThaumicRemnants.LOG.debug("[aura] {} died but has no aspect composition; nothing imparted",
                    entityId);
            return;
        }

        ChunkPos cp = new ChunkPos(entity.blockPosition());
        LevelChunk chunk = sl.getChunkSource().getChunk(cp.x, cp.z, false);
        if (chunk == null) return;
        ChunkAura.Data aura = ChunkAura.of(chunk);
        if (aura == ChunkAura.empty()) return;

        boolean playerKill = event.getSource().getEntity() instanceof Player;
        float chance = playerKill ? MOB_KILL_PLAYER_CHANCE : MOB_KILL_ENVIRONMENT_CHANCE;

        // Track what actually got imparted (after roll + cap clamping) so the
        // log line tells the truth: "rolled Bestia 2 but the chunk was at cap
        // so only 0 added" matters for debugging the impart pipeline.
        List<String> imparted = new ArrayList<>();
        for (AspectStack as : composition) {
            if (sl.random.nextFloat() < chance) {
                int requested = playerKill ? as.amount() : Math.max(1, as.amount() / 2);
                int actual = aura.add(as.aspect(), requested);
                if (actual > 0) {
                    imparted.add(as.aspect().id().getPath() + "+" + actual);
                }
            }
        }

        if (imparted.isEmpty()) {
            ThaumicRemnants.LOG.debug("[aura] {} died at chunk {} ({}-kill) but no aspect imparted (all rolls failed or chunk capped)",
                    entityId, cp, playerKill ? "player" : "environment");
        } else {
            chunk.setUnsaved(true);
            ThaumicRemnants.LOG.info("[aura] {} died at chunk {} ({}-kill) → imparted {}",
                    entityId, cp, playerKill ? "player" : "environment",
                    String.join(", ", imparted));
        }
    }
}
