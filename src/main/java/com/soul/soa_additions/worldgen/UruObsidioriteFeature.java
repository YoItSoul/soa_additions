package com.soul.soa_additions.worldgen;

import com.mojang.serialization.Codec;
import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

/**
 * 1:1 port of taiga 1.12.2 {@code Utils.generateCube} for the End-dimension
 * uru/obsidiorite cube formations (original call: {@code generateCube(true, uruOre, obsidiorite,
 * rand, x, z, world, 2, 2, 0, 96, 3)}).
 *
 * Arguments (taiga order): (floating, ore, host, rand, chunkX, chunkZ, world, chance, cubeSize, yMin, yMax, oreCount)
 * Per call: {@code oreCount} attempts; each succeeds with p = 0.01 * chance.
 * On success: pick random (x,z) offset in chunk + random y in [yMin,yMax]; size1 in [1,cubeSize], size2 in [0,1].
 * Floating shifts y upward by (nextInt(4)+2)*size1 blocks. Ore fills inner cube radius size2;
 * host fills outer cube radius size1 but only where the position is air AND not already ore.
 */
public final class UruObsidioriteFeature extends Feature<NoneFeatureConfiguration> {

    private static final int CHANCE = 2;     // URU_VAL
    private static final int CUBE_SIZE = 2;
    private static final int Y_MIN = 0;
    private static final int Y_MAX = 96;
    private static final int ORE_COUNT = 3;
    private static final boolean FLOATING = true;

    public UruObsidioriteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel world = ctx.level();
        Random rand = new Random(ctx.random().nextLong());
        BlockPos origin = ctx.origin();
        int chunkX = origin.getX() & ~15;
        int chunkZ = origin.getZ() & ~15;

        BlockState oreState = ModBlocks.URU_ORE.get().defaultBlockState();
        BlockState hostState = ModBlocks.OBSIDIORITE.get().defaultBlockState();

        boolean placedAny = false;
        for (int i = 0; i < ORE_COUNT; i++) {
            if (rand.nextFloat() >= 0.01f * CHANCE) continue;

            int x = chunkX + rand.nextInt(16);
            int z = chunkZ + rand.nextInt(16);
            int y = Y_MIN + rand.nextInt(Math.max(1, Y_MAX - Y_MIN));

            int size1 = 1 + rand.nextInt(CUBE_SIZE);          // Utils.nextInt(1, cubeSize) == 1..cubeSize
            int size2 = rand.nextInt(2);                      // 0 or 1

            if (FLOATING) {
                y += (rand.nextInt(4) + 2) * size1;
            }
            if (y < 1 || y > world.getMaxBuildHeight() - 1) continue;

            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            // Inner: ore cube radius size2
            for (int dx = -size2; dx <= size2; dx++) {
                for (int dy = -size2; dy <= size2; dy++) {
                    for (int dz = -size2; dz <= size2; dz++) {
                        cursor.set(x + dx, y + dy, z + dz);
                        if (world.hasChunk(cursor.getX() >> 4, cursor.getZ() >> 4)) {
                            world.setBlock(cursor, oreState, 2);
                            placedAny = true;
                        }
                    }
                }
            }

            // Outer: host shell radius size1, only air and not already ore
            for (int dx = -size1; dx <= size1; dx++) {
                for (int dy = -size1; dy <= size1; dy++) {
                    for (int dz = -size1; dz <= size1; dz++) {
                        cursor.set(x + dx, y + dy, z + dz);
                        if (!world.hasChunk(cursor.getX() >> 4, cursor.getZ() >> 4)) continue;
                        BlockState existing = world.getBlockState(cursor);
                        if (existing.isAir() && !existing.is(oreState.getBlock())) {
                            world.setBlock(cursor, hostState, 2);
                            placedAny = true;
                        }
                    }
                }
            }
        }
        return placedAny;
    }
}
