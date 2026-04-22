package com.soul.soa_additions.worldgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;

import java.util.Random;

/**
 * 1:1 port of taiga 1.12.2 {@code Utils.generateCube} for the End-dimension
 * uru/obsidiorite cube formations. Original call:
 * {@code generateCube(true, uruOre, obsidiorite, rand, x, z, world, URU_VAL=1, chance=2, 0, 96, 3)}.
 *
 * Taiga signature: (fly, centerBlock, hullBlock, random, chunkX, chunkZ, world,
 *                   count, chance, minY, maxY, maxS).
 * Per chunk: {@code count} attempts, each succeeds with p = 0.01 * chance.
 * On success: random (x,z) inside chunk, y in [minY, maxY]; outer shell size ∈ [1,maxS],
 * inner core size ∈ [0,1]. Inner loop places ore only on air; outer loop places host
 * wherever air and not already ore.
 *
 * Density bumped over GC defaults (URU_VAL=1, chance=2 → 0.02/chunk) to
 * {@code COUNT}×{@code CHANCE}% per chunk so meteors are findable within a few hundred
 * blocks of End spawn, matching the pack's expected pacing.
 */
public final class UruObsidioriteFeature extends Feature<NoneFeatureConfiguration> {

    private static final Logger LOG = LogUtils.getLogger();

    private static final int COUNT  = 1;    // attempts per chunk
    private static final int CHANCE = 10;   // 10% per chunk (~1 per 10 chunks)
    private static final int MAX_S  = 3;    // outer shell size max (matches GC)
    private static final int Y_MIN  = 40;   // End main island floor
    private static final int Y_MAX  = 180;  // covers outer islands

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
        LOG.info("[soa_additions] uru_obsidiorite place() invoked at chunk ({}, {}) biome={}",
                chunkX >> 4, chunkZ >> 4, world.getBiome(origin).unwrapKey().map(k -> k.location().toString()).orElse("?"));

        BlockState oreState = ModBlocks.URU_ORE.get().defaultBlockState();
        BlockState hostState = ModBlocks.OBSIDIORITE.get().defaultBlockState();

        boolean placedAny = false;
        for (int i = 0; i < COUNT; i++) {
            if (rand.nextFloat() >= 0.01f * CHANCE) continue;
            LOG.info("[soa_additions] uru_obsidiorite feature roll hit at chunk ({}, {}), placing cluster", chunkX >> 4, chunkZ >> 4);

            int outer = 1 + rand.nextInt(MAX_S);   // [1, MAX_S]
            int inner = rand.nextInt(2);           // 0 or 1
            int x = chunkX + rand.nextInt(16);
            int z = chunkZ + rand.nextInt(16);
            int y = Y_MIN + rand.nextInt(Math.max(1, Y_MAX - Y_MIN));
            if (y < 1 || y > world.getMaxBuildHeight() - 1) continue;

            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            // Inner: ore core, only where air (matches GC's air-check).
            for (int dx = -inner; dx <= inner; dx++) {
                for (int dy = -inner; dy <= inner; dy++) {
                    for (int dz = -inner; dz <= inner; dz++) {
                        cursor.set(x + dx, y + dy, z + dz);
                        if (!world.getBlockState(cursor).isAir()) continue;
                        world.setBlock(cursor, oreState, 2);
                        placedAny = true;
                    }
                }
            }

            // Outer: obsidiorite shell, only where air and not already ore.
            for (int dx = -outer; dx <= outer; dx++) {
                for (int dy = -outer; dy <= outer; dy++) {
                    for (int dz = -outer; dz <= outer; dz++) {
                        cursor.set(x + dx, y + dy, z + dz);
                        BlockState existing = world.getBlockState(cursor);
                        if (!existing.isAir()) continue;
                        world.setBlock(cursor, hostState, 2);
                        placedAny = true;
                    }
                }
            }
        }
        return placedAny;
    }
}
