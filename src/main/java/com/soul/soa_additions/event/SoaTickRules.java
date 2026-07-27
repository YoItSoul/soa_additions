package com.soul.soa_additions.event;

import com.soul.soa_additions.quest.task.GameStageTask;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * GC-ported per-tick safety rules, moved out of KubeJS (soa_player_tick.js /
 * soa_world_events.js) in 3.58.3: the JS versions crossed the Java-&gt;Rhino
 * boundary once per entity per poll, which is measurable overhead in a world
 * with hundreds of loaded entities. Same behaviour, native cost.
 *
 * <ul>
 *   <li>All living entities, 1&nbsp;Hz (phase-spread by entity tickCount):
 *       motion clamped to ±8 blocks/tick; listed bosses yanked back below
 *       y=255 (GC anti-cheese for boss skybox parking).</li>
 *   <li>Players, 5&nbsp;Hz: dim night-vision strip (screen-flash bug),
 *       saturation duration cap (food gauge exploit), sub-5-tick effect sweep
 *       (visual carryover).</li>
 *   <li>Players, 1&nbsp;Hz: nether/end portal stage-gate warnings, Twilight
 *       Forest dark-leaves floor damage.</li>
 * </ul>
 */
public final class SoaTickRules {

    private static final double MOTION_LIMIT = 8.0;
    private static final double MAX_BOSS_Y = 255.0;
    private static final double BOSS_RESET_Y = 252.0;
    private static final String[] BOSS_TYPE_FRAGMENTS =
            {"wither", "ender_dragon", "frostmaw", "umvuthi", "alpha_yeti", "snow_queen"};

    private SoaTickRules() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide()) return;
        if (e.tickCount % 20 != 0) return; // 1 Hz per entity, phase-spread by spawn time

        Vec3 m = e.getDeltaMovement();
        double mx = Mth.clamp(m.x, -MOTION_LIMIT, MOTION_LIMIT);
        double my = Mth.clamp(m.y, -MOTION_LIMIT, MOTION_LIMIT);
        double mz = Mth.clamp(m.z, -MOTION_LIMIT, MOTION_LIMIT);
        if (mx != m.x || my != m.y || mz != m.z) {
            e.setDeltaMovement(mx, my, mz);
        }

        if (e.getY() > MAX_BOSS_Y) {
            String type = String.valueOf(EntityType.getKey(e.getType()));
            for (String frag : BOSS_TYPE_FRAGMENTS) {
                if (type.contains(frag)) {
                    e.setPos(e.getX(), BOSS_RESET_Y, e.getZ());
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;
        long time = player.level().getGameTime();
        if (time % 4 != 0) return; // 5 Hz is plenty for everything below

        // Strip dim night vision to prevent the screen-flash bug.
        MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
        if (nv != null && nv.getDuration() <= 200) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }

        boolean creative = player.isCreative();
        if (!creative) {
            // Cap saturation duration at 1t (food gauge max-out exploit).
            MobEffectInstance sat = player.getEffect(MobEffects.SATURATION);
            if (sat != null && sat.getDuration() > 1) {
                player.removeEffect(MobEffects.SATURATION);
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1,
                        sat.getAmplifier(), sat.isAmbient(), sat.isVisible()));
            }

            // Drop near-expired effects to clear visual carryover.
            List<MobEffect> expiring = null;
            for (MobEffectInstance inst : player.getActiveEffects()) {
                if (inst.getDuration() < 5) {
                    if (expiring == null) expiring = new ArrayList<>(2);
                    expiring.add(inst.getEffect());
                }
            }
            if (expiring != null) {
                for (MobEffect effect : expiring) player.removeEffect(effect);
            }
        }

        if (time % 20 != 0 || !(player instanceof ServerPlayer sp)) return;

        // Portal stage-gate warnings.
        BlockState here = player.level().getBlockState(player.blockPosition());
        if (here.is(Blocks.NETHER_PORTAL) && !GameStageTask.hasStage(sp, "twilight_shield")) {
            sp.sendSystemMessage(Component.translatable("greedycraft.event.nether.reject.message")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else if (here.is(Blocks.END_PORTAL) && !GameStageTask.hasStage(sp, "ender_charm")) {
            sp.sendSystemMessage(Component.translatable("greedycraft.event.end.reject.message")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }

        // Twilight Forest dark-leaves floor damage.
        if (!creative) {
            BlockState below = player.level().getBlockState(player.blockPosition().below());
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(below.getBlock());
            if (id != null && "twilightforest".equals(id.getNamespace())
                    && (id.getPath().equals("dark_leaves") || id.getPath().equals("dark_oak_leaves"))) {
                player.hurt(player.damageSources().hotFloor(), 2.0F);
            }
        }
    }
}
