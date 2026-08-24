package com.soul.soa_additions.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;
import java.util.Set;

/**
 * 1:1 port of TAIGA 1.12.2 {@code Generator.generateMeteor} for the
 * overworld duranite/meteorite buried-meteor formations. Original call:
 * {@code generateMeteor(duraniteOre, blockMeteorite, rand, x, z, world,
 *  DURANITE_VAL=1, chance=6, minY=16, maxY=112)}.
 *
 * <p>Algorithm (verbatim from TAIGA Generator.java:183):
 * <ol>
 *   <li>Roll {@code chance/100} probability per call (6% per chunk).</li>
 *   <li>Pick a random hull radius {@code r ∈ [1,5]} and random (x,y,z) in chunk
 *       at {@code y ∈ [minY,maxY]}.</li>
 *   <li>If origin is in air with air below, descend until we hit a non-air block
 *       (the surface). Verify the surface block is dirt/grass/stone/sand/gravel/
 *       cobblestone/sandstone — else abort this attempt.</li>
 *   <li>Move the center DOWN by {@code random(r*2) + r + 1} so the meteor is
 *       buried inside the ground.</li>
 *   <li>Place an inner ore core: cubic-spherical fill of radius {@code t}
 *       (where {@code t = random(r-1)} if r>3 else 1) with {@code centerBlock}.</li>
 *   <li>Place an outer meteorite hull: cubic-spherical fill of radius {@code r}
 *       with {@code hullBlock}, except where ore was placed.</li>
 * </ol>
 *
 * <p>{@code MeteorWorldSaveData} (used in 1.12 to track meteor positions for
 * later teardown / radar features) is intentionally omitted — the feature is
 * pure terrain placement with no follow-up state.
 *
 * <p>Surface validation block list mirrors the 1.12 OreDictionary lookups
 * for {@code dirt/grass/stone/sand/gravel/cobblestone/sandstone}, expanded to
 * cover their 1.20.1 sub-variants and modded surface blocks of the same kind.
 */
public final class DuraniteMeteorFeature extends Feature<NoneFeatureConfiguration> {

    /** TAIGAConfiguration.DURANITE_VAL default = 1. Outer caller invokes once per
     *  chunk (placed_feature count=1) so this controls per-call attempts. */
    private static final int COUNT      = 1;
    /** TAIGA chance arg = 6 → 6% per attempt. */
    private static final int CHANCE_PCT = 6;
    private static final int Y_MIN = 16;
    private static final int Y_MAX = 112;

    private final ResourceLocation oreId    = new ResourceLocation("taiga", "duranite_ore");
    private final ResourceLocation hullId   = new ResourceLocation("taiga", "meteorite_block");

    public DuraniteMeteorFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel world = ctx.level();
        Random rand = new Random(ctx.random().nextLong());
        BlockPos origin = ctx.origin();
        int chunkX = origin.getX() & ~15;
        int chunkZ = origin.getZ() & ~15;

        Block oreBlock  = ForgeRegistries.BLOCKS.getValue(oreId);
        Block hullBlock = ForgeRegistries.BLOCKS.getValue(hullId);
        if (oreBlock == null || hullBlock == null) return false; // taiga thingpack absent
        BlockState ore  = oreBlock.defaultBlockState();
        BlockState hull = hullBlock.defaultBlockState();

        boolean placedAny = false;
        for (int i = 0; i < COUNT; i++) {
            if (rand.nextFloat() >= 0.01f * CHANCE_PCT) continue;

            int r = 1 + rand.nextInt(5);                         // [1,5]  hull radius
            int x = chunkX + rand.nextInt(16);
            int z = chunkZ + rand.nextInt(16);
            int y = Y_MIN + rand.nextInt(Math.max(1, Y_MAX - Y_MIN));

            BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos(x, y, z);

            // If origin is in air with air below, descend until we hit a non-air block.
            if (world.getBlockState(cPos).isAir() && world.getBlockState(cPos.below()).isAir()) {
                while (world.getBlockState(cPos.below()).isAir() && cPos.getY() >= Y_MIN) {
                    cPos.move(0, -1, 0);
                }
            }

            // Validate surface (block one below cPos).
            if (!isValidSurface(world.getBlockState(cPos.below()))) continue;

            // Bury the center by [r+1, r*3+1] blocks.
            int dy = rand.nextInt(r * 2) + r + 1;
            cPos.move(0, -dy, 0);

            // Inner ore core, radius t.
            int t = (r > 3) ? rand.nextInt(r - 1) : 1;
            BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
            for (int dx = -t; dx <= t; dx++) {
                for (int ey = -t; ey <= t; ey++) {
                    for (int dz = -t; dz <= t; dz++) {
                        if (Math.sqrt(dx * dx + ey * ey + dz * dz) > t) continue;
                        cur.set(cPos.getX() + dx, cPos.getY() + ey, cPos.getZ() + dz);
                        world.setBlock(cur, ore, 2);
                        placedAny = true;
                    }
                }
            }

            // Outer meteorite hull, radius r — skip cells already filled with ore.
            for (int dx = -r; dx <= r; dx++) {
                for (int ey = -r; ey <= r; ey++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.sqrt(dx * dx + ey * ey + dz * dz) > r) continue;
                        cur.set(cPos.getX() + dx, cPos.getY() + ey, cPos.getZ() + dz);
                        if (world.getBlockState(cur).is(oreBlock)) continue;
                        world.setBlock(cur, hull, 2);
                        placedAny = true;
                    }
                }
            }
        }
        return placedAny;
    }

    /**
     * 1:1 of the 1.12 OreDictionary lookups for
     * {@code dirt, grass, stone, sand, gravel, cobblestone, sandstone}, expanded
     * to vanilla 1.20.1 sub-variants. Modded look-alikes (mossy/cracked/etc.)
     * still pass via the {@code is(...)} BlockTag-equivalent membership.
     */
    private static final Set<Block> VALID_SURFACE_BLOCKS = Set.of(
            net.minecraft.world.level.block.Blocks.DIRT,
            net.minecraft.world.level.block.Blocks.COARSE_DIRT,
            net.minecraft.world.level.block.Blocks.ROOTED_DIRT,
            net.minecraft.world.level.block.Blocks.PODZOL,
            net.minecraft.world.level.block.Blocks.MYCELIUM,
            net.minecraft.world.level.block.Blocks.GRASS_BLOCK,
            net.minecraft.world.level.block.Blocks.STONE,
            net.minecraft.world.level.block.Blocks.ANDESITE,
            net.minecraft.world.level.block.Blocks.DIORITE,
            net.minecraft.world.level.block.Blocks.GRANITE,
            net.minecraft.world.level.block.Blocks.DEEPSLATE,
            net.minecraft.world.level.block.Blocks.TUFF,
            net.minecraft.world.level.block.Blocks.SAND,
            net.minecraft.world.level.block.Blocks.RED_SAND,
            net.minecraft.world.level.block.Blocks.GRAVEL,
            net.minecraft.world.level.block.Blocks.COBBLESTONE,
            net.minecraft.world.level.block.Blocks.MOSSY_COBBLESTONE,
            net.minecraft.world.level.block.Blocks.SANDSTONE,
            net.minecraft.world.level.block.Blocks.RED_SANDSTONE,
            net.minecraft.world.level.block.Blocks.SMOOTH_SANDSTONE,
            net.minecraft.world.level.block.Blocks.SMOOTH_RED_SANDSTONE
    );

    private static boolean isValidSurface(BlockState state) {
        return VALID_SURFACE_BLOCKS.contains(state.getBlock());
    }
}
