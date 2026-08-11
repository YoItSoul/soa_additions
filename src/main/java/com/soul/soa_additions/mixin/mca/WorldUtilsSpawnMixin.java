package com.soul.soa_additions.mixin.mca;

import com.soul.soa_additions.compat.ChunkLoadGuard;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops MCA's villager spawn queue from force-loading chunks on the server thread.
 *
 * <p>{@code SpawnQueue.tick} runs off a {@code LevelTickEvent} and calls
 * {@code VillagerFactory.spawn} → {@code WorldUtils.spawnEntity}, which reads the
 * level at the mob's position without checking whether that chunk is loaded. On a
 * miss it becomes a blocking chunk load — and in singleplayer that means running
 * the whole worldgen stack inline on the tick thread. Measured at 18.1% of the
 * server thread while travelling, with 40s and 80s ticks in the watchdog log.</p>
 *
 * <p>{@code WorldUtils} is an interface with static methods, so this mixin is
 * declared as an interface too. {@code @Pseudo} with a string target: MCA is not a
 * compile dependency and the mixin config sets {@code defaultRequire: 0}, so with
 * the mod absent this silently does not apply. Because that failure mode is silent,
 * {@link ChunkLoadGuard} logs once when it first defers a spawn — absence of that
 * line alongside continued blocking means the injection did not take.</p>
 */
@Pseudo
@Mixin(targets = "forge.net.mca.util.WorldUtils", remap = false)
public interface WorldUtilsSpawnMixin {

    // MUST be public: Mixin rejects an entire interface mixin if any method in it
    // is non-public (InvalidInterfaceMixinException). Declaring this `private
    // static` silently disabled the guard for three releases.
    @Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true, remap = false)
    static void soa$deferSpawnIntoUnloadedChunk(Level level, Mob mob, MobSpawnType reason, CallbackInfo ci) {
        if (!ChunkLoadGuard.loadedAt(level, mob.blockPosition(), "mca:spawnEntity")) {
            ci.cancel();
        }
    }
}
