package com.soul.soa_additions.mixin.lodestone;

import com.soul.soa_additions.compat.ChunkLoadGuard;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

/**
 * Stops Lodestone's block-entity scan from force-loading chunks.
 *
 * <p>{@code BlockHelper.getBlockEntitiesStream} walks every position in a box and
 * reads the level at each, with no load check. Callers (Malum and friends) invoke
 * it from tick paths, so a box overlapping an unloaded chunk parks the server
 * thread on a synchronous chunk load — 4.0% of the server thread while travelling.</p>
 *
 * <p>Every public overload funnels into the {@code (Class, Level, AABB)} form, so
 * guarding that one covers them all. Returning an empty stream is the correct
 * degradation: block entities in unloaded chunks are not ticking and could not
 * have been legitimately interacted with anyway.</p>
 */
@Pseudo
@Mixin(targets = "team.lodestar.lodestone.helpers.BlockHelper", remap = false)
public abstract class BlockEntitiesStreamMixin {

    @Inject(
            method = "getBlockEntitiesStream(Ljava/lang/Class;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/AABB;)Ljava/util/stream/Stream;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void soa$skipUnloadedScan(Class<?> type, Level level, AABB box,
                                             CallbackInfoReturnable<Stream<?>> cir) {
        if (!ChunkLoadGuard.loadedIn(level, box, "lodestone:getBlockEntitiesStream")) {
            cir.setReturnValue(Stream.empty());
        }
    }
}
