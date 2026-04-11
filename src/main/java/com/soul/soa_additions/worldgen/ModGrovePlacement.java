package com.soul.soa_additions.worldgen;

import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for a flat grass area within 32 blocks of the world spawn and places a
 * {@link com.soul.soa_additions.block.custom.GroveSpawnBlock} there. Called once per world on
 * first server start by {@link ModWorldEvents}.
 */
public final class ModGrovePlacement {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_GrovePlacement");
    private static final int SEARCH_RADIUS = 32;
    private static final int MAX_ATTEMPTS = 50;
    private static final int REQUIRED_AIR_ABOVE = 10;

    private ModGrovePlacement() {}

    public static void placeShrineNearSpawn(ServerLevel level, BlockPos spawnPos) {
        RandomSource random = level.getRandom();
        LOGGER.info("Attempting to place shrine near spawn at: {}", spawnPos);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int offsetX = random.nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS;
            int offsetZ = random.nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS;
            BlockPos potentialPos = spawnPos.offset(offsetX, 0, offsetZ);

            level.getChunkSource().getChunk(potentialPos.getX() >> 4, potentialPos.getZ() >> 4, true);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, potentialPos.getX(), potentialPos.getZ());
            BlockPos surfacePos = new BlockPos(potentialPos.getX(), surfaceY, potentialPos.getZ());

            if (isValidGround(level, surfacePos) && hasEnoughAirAbove(level, surfacePos) && isFlatArea(level, surfacePos)) {
                LOGGER.info("Valid position found! Placing shrine at: {}", surfacePos);
                level.setBlock(surfacePos, ModBlocks.GROVE_SPAWN_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }

        LOGGER.warn("Failed to find a valid position to place the shrine near the spawn.");
    }

    private static boolean isValidGround(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK);
    }

    private static boolean hasEnoughAirAbove(ServerLevel level, BlockPos pos) {
        for (int i = 1; i <= REQUIRED_AIR_ABOVE; i++) {
            if (!level.getBlockState(pos.above(i)).isAir()) return false;
        }
        return true;
    }

    private static boolean isFlatArea(ServerLevel level, BlockPos pos) {
        int surfaceY = pos.getY();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                int heightHere = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() + x, pos.getZ() + z);
                if (heightHere != surfaceY) return false;
            }
        }
        return true;
    }
}
