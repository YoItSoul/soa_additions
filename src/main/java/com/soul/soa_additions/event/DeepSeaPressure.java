package com.soul.soa_additions.event;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.potion.SoaPotions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Port of GreedyCraft's deep-sea pressure rule (onPlayerTick.zs, "Prevent
 * breathing in ocean with a door"): below y=40 in deep ocean, breathing from
 * anything that isn't actual water (door pockets, torch tricks, sealed air
 * spaces) suffocates you unless you have Water Breathing or the Drown effect.
 *
 * <p>GC ran the full check every tick; here it runs once per second per
 * player (staggered by entity id so players don't all check on the same
 * tick), with conditions ordered cheapest-first so the common cases exit on
 * a couple of primitive comparisons. GC's actual punishments were already
 * per-second (damage/blindness/message at worldTime%20==0), so the only
 * behavioral difference is cosmetic: the air bar refills briefly between
 * checks instead of being pinned at zero every tick. The 10 drown
 * damage/second — the real deterrent — is unchanged.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DeepSeaPressure {

    private static final int CHECK_PERIOD_TICKS = 20;  // GC punished at worldTime%20==0
    /** GC used y&lt;40 against 1.12 depths. 1.20 deviation (deliberate): SOA
     *  aqualite generates at y 5–40 and Terralith ocean floors run deeper
     *  than 1.12's, so the zone is raised to y&lt;50 (13 below sea level) to
     *  keep the whole aqualite band + typical floors inside it — the rule
     *  exists to stop door-pocket aqualite cheesing. */
    private static final double DEPTH_Y = 50.0;
    private static final int SAMPLE_OFFSET = 5;        // GC: 4 points at ±5 x/z
    private static final float DAMAGE_PER_SECOND = 10.0f;

    private DeepSeaPressure() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        // Stagger per player so a full server doesn't burst-check on one tick.
        if ((player.tickCount + player.getId()) % CHECK_PERIOD_TICKS != 0) return;
        Level level = player.level();
        if (level.isClientSide()) return;
        if (level.dimension() != Level.OVERWORLD) return;   // GC: dimension 0 only
        if (player.getY() >= DEPTH_Y) return;
        if (player.isCreative() || player.isSpectator()) return;

        // Head in actual water → vanilla drowning rules apply, we're done.
        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        BlockState eye = level.getBlockState(eyePos);
        if (eye.getFluidState().is(FluidTags.WATER)) return;

        // Surface guard: GC had a bug where swimming ON low-lying water still
        // counted as "underwater" from the raw y reading. If the player's eyes
        // are at/above the local motion-blocking surface (which includes the
        // water surface), they're floating on top of it, not sealed beneath it.
        if (player.getEyeY() >= level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                player.getBlockX(), player.getBlockZ()) - 1) {
            return;
        }

        if (player.hasEffect(MobEffects.WATER_BREATHING) || player.hasEffect(SoaPotions.DROWN.get())) return;

        // Biome sampling last — five registry lookups only for candidate
        // violators (below y40, dry head, no breathing effect).
        BlockPos pos = player.blockPosition();
        if (!isOcean(level, pos)
                || !isOcean(level, pos.offset(SAMPLE_OFFSET, 0, 0))
                || !isOcean(level, pos.offset(-SAMPLE_OFFSET, 0, 0))
                || !isOcean(level, pos.offset(0, 0, SAMPLE_OFFSET))
                || !isOcean(level, pos.offset(0, 0, -SAMPLE_OFFSET))) {
            return;
        }

        // ---- Violation: the pressure isolates your air pocket. (GC 1:1) ----
        player.setAirSupply(0);
        if (!eye.isAir() && !player.hasEffect(MobEffects.WITHER)) {
            // Head inside a door/trapdoor/solid — GC added Wither II on top.
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, false));
        }
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
        player.hurt(level.damageSources().drown(), DAMAGE_PER_SECOND);
        player.displayClientMessage(Component.empty()
                .append(Component.translatable("greedycraft.event.deep_sea.warning")
                        .withStyle(ChatFormatting.RED))
                .append(Component.literal(" "))
                .append(Component.translatable("greedycraft.event.deep_sea.message")
                        .withStyle(ChatFormatting.YELLOW)), true);
    }

    private static boolean isOcean(Level level, BlockPos pos) {
        return level.getBiome(pos).is(BiomeTags.IS_OCEAN);
    }
}
