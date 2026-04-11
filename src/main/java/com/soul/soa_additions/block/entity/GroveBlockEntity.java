package com.soul.soa_additions.block.entity;

import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * On its first tick this entity carves out the grove structure around its origin block — a
 * hollow grass sphere, a sky opening, lush ground flora and decorative stone brick pillars — then
 * swaps its own block for a {@link com.soul.soa_additions.block.custom.GroveBoonBlock}.
 */
public class GroveBlockEntity extends BlockEntity {

    private static final int BASE_RADIUS = 10;
    private static final int SPHERE_RADIUS = 13;
    private static final int SKY_OPENING_RADIUS = 3;
    private static final int LUSH_RADIUS = 30;
    private static final int PILLAR_RADIUS = 35;
    private static final double DIRT_CHANCE = 0.5D;
    private static final double ROOTED_DIRT_CHANCE = 0.2D;
    private static final double GLOWSTONE_CHANCE = 0.05D;

    public GroveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GROVE_SPAWN_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GroveBlockEntity entity) {
        if (level.isClientSide) return;
        generateSphericalStructure(level, pos);
        generateTreeRings(level, pos);
        createSkyOpening(level, pos);
        generateLushVegetation(level, pos);
        generatePillars(level, pos);
        level.setBlock(pos, ModBlocks.GROVE_BOON_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void generateSphericalStructure(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int x = -SPHERE_RADIUS; x <= SPHERE_RADIUS; x++) {
            for (int y = -SPHERE_RADIUS; y <= SPHERE_RADIUS; y++) {
                for (int z = -SPHERE_RADIUS; z <= SPHERE_RADIUS; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= SPHERE_RADIUS && distance >= BASE_RADIUS) {
                        mut.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                        setTerrainBlock(level, mut, distance >= 12.0D);
                    }
                }
            }
        }
    }

    private static void setTerrainBlock(Level level, BlockPos pos, boolean isOuterLayer) {
        BlockState blockState;
        if (isOuterLayer) {
            blockState = Blocks.GRASS_BLOCK.defaultBlockState();
        } else {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            if (rng.nextDouble() < GLOWSTONE_CHANCE) {
                blockState = Blocks.GLOWSTONE.defaultBlockState();
            } else if (rng.nextDouble() < ROOTED_DIRT_CHANCE) {
                blockState = Blocks.ROOTED_DIRT.defaultBlockState();
            } else if (rng.nextDouble() < DIRT_CHANCE) {
                blockState = Blocks.DIRT.defaultBlockState();
            } else {
                blockState = Blocks.COARSE_DIRT.defaultBlockState();
            }
        }
        level.setBlock(pos, blockState, Block.UPDATE_ALL);
    }

    private static void generateTreeRings(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int angle = 0; angle < 360; angle += 5) {
            double radian = Math.toRadians(angle);
            int xOffset = (int) Math.round(Math.cos(radian) * BASE_RADIUS);
            int zOffset = (int) Math.round(Math.sin(radian) * BASE_RADIUS);
            mut.set(pos.getX() + xOffset, pos.getY(), pos.getZ() + zOffset);
            level.setBlock(mut, Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_ALL);
            int yOffset = (int) Math.round(Math.cos(radian) * BASE_RADIUS);
            mut.set(pos.getX(), pos.getY() + yOffset, pos.getZ() + zOffset);
            level.setBlock(mut, Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void createSkyOpening(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int x = -SKY_OPENING_RADIUS; x <= SKY_OPENING_RADIUS; x++) {
            for (int z = -SKY_OPENING_RADIUS; z <= SKY_OPENING_RADIUS; z++) {
                if (Math.sqrt(x * x + z * z) > SKY_OPENING_RADIUS) continue;
                for (int y = 0; y <= SPHERE_RADIUS; y++) {
                    mut.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    level.setBlock(mut, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = 9; z <= SPHERE_RADIUS; z++) {
                    mut.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    level.setBlock(mut, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void generateLushVegetation(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int x = -LUSH_RADIUS; x <= LUSH_RADIUS; x++) {
            for (int y = -LUSH_RADIUS; y <= LUSH_RADIUS; y++) {
                for (int z = -LUSH_RADIUS; z <= LUSH_RADIUS; z++) {
                    if (Math.sqrt(x * x + y * y + z * z) > LUSH_RADIUS) continue;
                    mut.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    generateVegetationAt(level, mut, rng);
                }
            }
        }
    }

    private static void generateVegetationAt(Level level, BlockPos pos, ThreadLocalRandom rng) {
        BlockState below = level.getBlockState(pos.below());
        BlockState current = level.getBlockState(pos);
        if (!current.isAir()) return;
        if (isValidGroundBlock(below) && rng.nextInt(4) == 0) {
            level.setBlock(pos, getRandomFlora(level), Block.UPDATE_ALL);
        } else if (below.is(Blocks.DIRT) && rng.nextInt(5) == 0) {
            level.setBlock(pos, Blocks.GRASS.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static boolean isValidGroundBlock(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT);
    }

    private static BlockState getRandomFlora(Level level) {
        HolderSet.Named<Block> flowers = level.registryAccess().registryOrThrow(Registries.BLOCK)
                .getTag(BlockTags.FLOWERS).orElseThrow(() -> new IllegalStateException("Flower tag is missing!"));
        HolderSet.Named<Block> saplings = level.registryAccess().registryOrThrow(Registries.BLOCK)
                .getTag(BlockTags.SAPLINGS).orElseThrow(() -> new IllegalStateException("Sapling tag is missing!"));
        List<Holder<Block>> combined = new ArrayList<>(flowers.stream().toList());
        combined.addAll(saplings.stream().toList());
        if (combined.isEmpty()) return Blocks.POPPY.defaultBlockState();
        return combined.get(ThreadLocalRandom.current().nextInt(combined.size())).value().defaultBlockState();
    }

    private static void generatePillars(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int x = -PILLAR_RADIUS; x <= PILLAR_RADIUS; x++) {
            for (int z = -PILLAR_RADIUS; z <= PILLAR_RADIUS; z++) {
                if (Math.sqrt(x * x + z * z) > PILLAR_RADIUS) continue;
                if (rng.nextInt(32) != 0) continue;
                mut.set(pos.getX() + x, pos.getY(), pos.getZ() + z);
                if (canPlacePillar(level, mut)) {
                    generatePillar(level, mut.immutable(), rng);
                }
            }
        }
    }

    private static boolean canPlacePillar(Level level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        if (!level.getBlockState(pos).isAir()) return false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockState neighbor = level.getBlockState(pos.offset(dx, 0, dz));
                if (neighbor.is(Blocks.STONE_BRICKS)
                        || neighbor.is(Blocks.CRACKED_STONE_BRICKS)
                        || neighbor.is(Blocks.CHISELED_STONE_BRICKS)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void generatePillar(Level level, BlockPos pos, ThreadLocalRandom rng) {
        int pillarHeight = rng.nextInt(7) + 2;
        for (int y = 0; y < pillarHeight; y++) {
            BlockState pillarBlock = rng.nextBoolean()
                    ? Blocks.STONE_BRICKS.defaultBlockState()
                    : Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            level.setBlock(pos.above(y), pillarBlock, Block.UPDATE_ALL);
        }
        level.setBlock(pos.above(pillarHeight),
                Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
    }
}
