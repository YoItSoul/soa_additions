package com.soul.soa_additions.mixin.mca;

import com.soul.soa_additions.compat.McaSpawnGuard;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stops MCA's villager spawn queue from force-loading chunks on the server thread.
 *
 * <p>{@code SpawnQueue.tick} runs off a {@code LevelTickEvent} and reaches
 * {@code WorldUtils.spawnEntity}, which calls {@code Level.getCurrentDifficultyAt} —
 * only to read the chunk's inhabited time, but it gets there through {@code getChunkAt},
 * a <em>loading</em> request that parks the server thread until the chunk finishes
 * generating. Measured at 43.4% of the server thread while travelling. See
 * {@link McaSpawnGuard} for the non-blocking replacement.</p>
 *
 * <p><strong>Why this targets the caller.</strong> The obvious target is
 * {@code WorldUtils.spawnEntity} itself, and that is what this mixin did until
 * 2026-08-12 — but {@code WorldUtils} is an <em>interface</em>, and Mixin 0.8.5's
 * {@code MixinApplicatorInterface#applyInjections} is an empty method: every injection
 * into an interface target is discarded without a word. The old mixin could never have
 * worked in any form. It did not even fail quietly in the end — it threw
 * {@code InvalidMixinException} for holding a non-private static method, which is the
 * only reason the dead injection was ever noticed. {@code VillagerFactory} is a plain
 * class, so a redirect on its call site applies normally.</p>
 *
 * <p>{@code require = 1} for the reason the old comment gave and then some: twice this
 * guard has silently done nothing while the mixin reported as applied. A startup crash
 * naming this mixin costs seconds to diagnose; silence costs an evening.</p>
 *
 * <p>{@code VillageManager.spawnBountyHunter} is the only other spawn path that goes
 * through {@code WorldUtils.spawnEntity} on the server. It is player-triggered and rare,
 * never appeared in a profile, and is deliberately left alone.</p>
 */
@Pseudo
@Mixin(targets = "forge.net.mca.entity.VillagerFactory", remap = false)
public abstract class VillagerFactorySpawnMixin {

    @Redirect(
            method = "spawn(Lnet/minecraft/world/entity/MobSpawnType;)Lforge/net/mca/entity/VillagerEntityMCA;",
            at = @At(
                    value = "INVOKE",
                    target = "Lforge/net/mca/util/WorldUtils;spawnEntity("
                            + "Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Mob;"
                            + "Lnet/minecraft/world/entity/MobSpawnType;)V"),
            require = 1, remap = false)
    private void soa$spawnWithoutBlocking(Level level, Mob mob, MobSpawnType reason) {
        if (McaSpawnGuard.spawnWithoutBlocking(level, mob, reason)) {
            return;
        }

        // Only reached off a ServerLevel, where the guard declines. Reproduces MCA's
        // own two-line body so behaviour there is unchanged.
        mob.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(mob.blockPosition()),
                reason, null, null);
        level.addFreshEntity(mob);
    }
}
