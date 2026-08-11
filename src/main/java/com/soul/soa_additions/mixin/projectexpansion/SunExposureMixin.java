package com.soul.soa_additions.mixin.projectexpansion;

import com.soul.soa_additions.compat.ChunkLoadGuard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops Project Expansion's sun-exposure check from force-loading chunks.
 *
 * <p>{@code ServerEvents.handleSunExposure} runs from a {@code ServerTickEvent},
 * per player, and calls {@code ServerPlayer.pick(10.0, ...)} — a ten-block raycast
 * — then {@code Level.getBlockState} on whatever it hit. Neither is guarded, so a
 * ray crossing into an unloaded chunk parks the server thread on a synchronous
 * chunk load. Measured at 15.9% of the whole server thread while travelling.</p>
 *
 * <p>Ten blocks can cross at most one chunk boundary in each direction, so
 * verifying the 3x3 chunk area around the player fully covers the ray. Skipping a
 * tick costs nothing: the check re-runs next tick, and sun exposure accumulates
 * over seconds.</p>
 */
@Pseudo
@Mixin(targets = "cool.furry.mc.forge.projectexpansion.events.ServerEvents", remap = false)
public abstract class SunExposureMixin {

    @Inject(method = "handleSunExposure", at = @At("HEAD"), cancellable = true, remap = false)
    private static void soa$skipUnloadedSunCheck(TickEvent.ServerTickEvent event, ServerPlayer player, CallbackInfo ci) {
        if (!ChunkLoadGuard.loadedAround(player.level(), player.blockPosition(), 16,
                "projectexpansion:handleSunExposure")) {
            ci.cancel();
        }
    }
}
