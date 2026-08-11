package com.soul.soa_additions.mixin.sculkhorde;

import com.soul.soa_additions.compat.ChunkLoadGuard;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Stops Sculk Horde's ambient-sound scan from force-loading chunks.
 *
 * <p>{@code AmbientSFXSystem.getSurroundingBlockStatesAndPositions} raycasts out
 * from each player to pick ambient sounds, driven from a {@code LevelTickEvent}
 * ({@code ForgeEventSubscriber.WorldTickEvent} → {@code Gravemind.serverTick} →
 * {@code AmbientSFXSystem.serverTick} → {@code playAmbientSounds}). The rays call
 * {@code BlockGetter.clip} with no load check, so any ray reaching into an
 * unloaded chunk parks the server thread on a synchronous chunk load — 28.8% of
 * the whole server thread while travelling.</p>
 *
 * <p>The scan radius is the method's own parameter, so the guard covers exactly
 * the area the rays can reach. Returning an empty list means no ambient sound
 * that tick, which is inaudible as a miss and re-evaluated on the next one.</p>
 */
@Pseudo
@Mixin(targets = "com.github.sculkhorde.systems.AmbientSFXSystem", remap = false)
public abstract class AmbientSfxScanMixin {

    @Inject(method = "getSurroundingBlockStatesAndPositions", at = @At("HEAD"),
            cancellable = true, remap = false)
    private void soa$skipUnloadedAmbientScan(ServerPlayer player, int radius,
                                             CallbackInfoReturnable<List<?>> cir) {
        if (!ChunkLoadGuard.loadedAround(player.level(), player.blockPosition(), radius,
                "sculkhorde:getSurroundingBlockStatesAndPositions")) {
            cir.setReturnValue(List.of());
        }
    }
}
