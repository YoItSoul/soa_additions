package com.soul.soa_additions.lostcities;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Port of GreedyCraft's Lost City entry mechanic (1.12 Lost Cities bed
 * "portal", documented in GC's guide book, patchouli entry misc/lost_city):
 * a bed sitting on two diamond blocks with six skulls ringing it — right
 * click the bed to teleport to the Lost City dimension. Works in reverse
 * from inside the city (GC: "a return portal will not be generated", the
 * player builds another one there), matching 1.12 Lost Cities behavior.
 *
 * Structure (any bed, any skull type; the glass ring in the guide is
 * decorative and not validated, same as 1.12):
 *   - bed head + foot each directly on a diamond block
 *   - skulls at both bed ends and on both sides of each bed half (6 total)
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class LostCityBedTeleport {

    private static final ResourceKey<Level> LOST_CITY =
            ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                    new ResourceLocation("lostcities", "lostcity"));

    private LostCityBedTeleport() {}

    @SubscribeEvent
    public static void onBedRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isShiftKeyDown()) return;

        BlockState state = level.getBlockState(event.getPos());
        if (!(state.getBlock() instanceof BedBlock)) return;

        // Resolve head + foot positions regardless of which half was clicked.
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BedPart part = state.getValue(BlockStateProperties.BED_PART);
        BlockPos head = part == BedPart.HEAD ? event.getPos() : event.getPos().relative(facing);
        BlockPos foot = part == BedPart.FOOT ? event.getPos() : event.getPos().relative(facing.getOpposite());

        if (!level.getBlockState(head.below()).is(Blocks.DIAMOND_BLOCK)) return;
        if (!level.getBlockState(foot.below()).is(Blocks.DIAMOND_BLOCK)) return;

        Direction cw = facing.getClockWise();
        Direction ccw = facing.getCounterClockWise();
        BlockPos[] skulls = {
                head.relative(facing), foot.relative(facing.getOpposite()),
                head.relative(cw), head.relative(ccw),
                foot.relative(cw), foot.relative(ccw)
        };
        for (BlockPos p : skulls) {
            if (!(level.getBlockState(p).getBlock() instanceof AbstractSkullBlock)) return;
        }

        // Valid portal — decide destination: overworld ⇄ lost city.
        ResourceKey<Level> from = level.dimension();
        ResourceKey<Level> destKey = from.equals(LOST_CITY) ? Level.OVERWORLD : LOST_CITY;
        if (!from.equals(LOST_CITY) && !from.equals(Level.OVERWORLD)) return; // only bridge these two
        ServerLevel dest = player.server.getLevel(destKey);
        if (dest == null) return; // lostcities absent — leave the bed alone

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        BlockPos anchor = new BlockPos(head.getX(), 0, head.getZ());
        int y = dest.getHeight(Heightmap.Types.MOTION_BLOCKING, anchor.getX(), anchor.getZ()) + 1;
        // Force-load the target chunk so the heightmap answer is real.
        dest.getChunk(anchor.getX() >> 4, anchor.getZ() >> 4);
        y = Math.max(y, dest.getHeight(Heightmap.Types.MOTION_BLOCKING, anchor.getX(), anchor.getZ()) + 1);

        player.teleportTo(dest, anchor.getX() + 0.5, y, anchor.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        dest.playSound(null, anchor, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.5f, 1.2f);
        player.displayClientMessage(Component.translatable(
                destKey == LOST_CITY ? "soa_additions.lostcity.enter" : "soa_additions.lostcity.leave"), true);
    }
}
