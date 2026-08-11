package com.soul.soa_additions.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Port of YUNG's Law (1.12-only mod) replacement mode, as configured in GC
 * {@code config/YungsLaw-1_12_2.cfg}: below {@code max_y}, every block whose
 * distance from a "safe block" (air or any liquid) exceeds GENERATION_DISTANCE
 * is replaced with Hardened Stone (hardness 125 / resistance 1000 / diamond
 * tool). Caves stay traversable with a protective shell of normal stone;
 * tunneling through solid rock hits the seal. Runs as a per-chunk feature at
 * the top_layer_modification step (after ores — sealing ores far from caves is
 * the anti-xray point, same as GC).
 *
 * GC values: Generation Distance 3, liquids safe, Max Altitude 50 (overworld)
 * / 32 (Twilight Forest, with mazestone + underbrick untouchable), bedrock
 * untouchable. GC also enabled it in Lost Cities and dim 111 — no SOA analog.
 * Distance metric is Chebyshev (box), computed by box dilation of the safe
 * mask; block entities are additionally skipped as a 1.20 safety.
 *
 * <h2>Cost</h2>
 * This runs on every chunk, so the shape of the work matters. Two things keep
 * it cheap:
 * <ul>
 *   <li><b>One world read per cell.</b> The mask pass records, for the inner
 *       16x16 column, whether each block is eligible for replacement — so the
 *       placement pass never re-reads the world. That removes ~29k
 *       {@code getBlockState} calls per chunk.</li>
 *   <li><b>Separable dilation.</b> A Chebyshev ball is an axis-aligned box, and
 *       box dilation is separable, so three linear sweeps (one per axis) give
 *       the identical result to N neighbourhood passes. That is O(3n) instead
 *       of O(N * 27n) — roughly 80x fewer inner operations at radius 3 — and it
 *       allocates two buffers instead of cloning per pass.</li>
 * </ul>
 * The output is bit-for-bit the same as the naive form; only the cost changed.
 */
public final class HardenedStoneSealFeature extends Feature<HardenedStoneSealFeature.Config> {

    public record Config(int maxY, Optional<String> dimension, List<String> untouchable) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("max_y").forGetter(Config::maxY),
                Codec.STRING.optionalFieldOf("dimension").forGetter(Config::dimension),
                Codec.STRING.listOf().optionalFieldOf("untouchable", List.of()).forGetter(Config::untouchable)
        ).apply(inst, Config::new));
    }

    private static final int GENERATION_DISTANCE = 3;  // GC "Generation Distance"

    /** One-time boot proof that the feature actually runs — the biome-modifier
     *  path bug (data/forge/... vs data/<ns>/forge/...) failed silently, so
     *  log the first placement per game session to make regressions visible. */
    private static volatile boolean loggedFirstRun = false;

    public HardenedStoneSealFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> ctx) {
        WorldGenLevel level = ctx.level();
        Config cfg = ctx.config();

        if (cfg.dimension().isPresent()
                && !level.getLevel().dimension().location().toString().equals(cfg.dimension().get())) {
            return false;
        }

        BlockPos origin = ctx.origin();
        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;
        int minY = level.getMinBuildHeight();
        int maxY = Math.min(cfg.maxY(), level.getMaxBuildHeight() - 1);
        if (maxY <= minY) return false;

        // Safe-mask region: the chunk plus GENERATION_DISTANCE padding on x/z,
        // and up to maxY + distance so caves just above the cap still protect
        // blocks below it.
        final int pad = GENERATION_DISTANCE;
        final int sizeX = 16 + 2 * pad;
        final int sizeZ = 16 + 2 * pad;
        final int ySize = (maxY + pad) - minY + 1;
        final int innerY = maxY - minY + 1;

        Block hard = ModBlocks.HARDENED_STONE.get();
        BlockState hardState = hard.defaultBlockState();
        Set<Block> untouchable = resolveUntouchable(cfg.untouchable());

        boolean[] safe = new boolean[sizeX * ySize * sizeZ];
        // Eligibility for the inner 16x16 column, captured during the same read
        // so the placement pass needs no further world access.
        boolean[] eligible = new boolean[16 * innerY * 16];

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = 0; x < sizeX; x++) {
            boolean innerX = x >= pad && x < pad + 16;
            for (int z = 0; z < sizeZ; z++) {
                boolean inner = innerX && z >= pad && z < pad + 16;
                for (int y = 0; y < ySize; y++) {
                    cursor.set(chunkMinX - pad + x, minY + y, chunkMinZ - pad + z);
                    BlockState s = level.getBlockState(cursor);
                    boolean isSafe = s.isAir() || !s.getFluidState().isEmpty();
                    if (isSafe) {
                        safe[idx(x, y, z, ySize, sizeZ)] = true;
                    } else if (inner && y < innerY) {
                        Block b = s.getBlock();
                        if (b != hard && !s.is(Blocks.BEDROCK)
                                && !untouchable.contains(b) && !s.hasBlockEntity()) {
                            eligible[innerIdx(x - pad, y, z - pad, innerY)] = true;
                        }
                    }
                }
            }
        }

        boolean[] reach = dilateBox(safe, sizeX, ySize, sizeZ, GENERATION_DISTANCE);

        if (!loggedFirstRun) {
            loggedFirstRun = true;
            com.mojang.logging.LogUtils.getLogger().info(
                    "[soa_additions] Hardened stone seal feature running (first chunk this session: {} {} in {})",
                    chunkMinX >> 4, chunkMinZ >> 4, level.getLevel().dimension().location());
        }

        boolean placedAny = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < innerY; y++) {
                    if (!eligible[innerIdx(x, y, z, innerY)]) continue;
                    if (reach[idx(x + pad, y, z + pad, ySize, sizeZ)]) continue;
                    cursor.set(chunkMinX + x, minY + y, chunkMinZ + z);
                    level.setBlock(cursor, hardState, 2);
                    placedAny = true;
                }
            }
        }
        return placedAny;
    }

    /**
     * Chebyshev dilation by {@code r}, done as three separable linear sweeps.
     * A Chebyshev ball is the box [-r,r]^3, and box dilation factorises per
     * axis, so this is exactly equivalent to r rounds of 27-neighbour dilation.
     */
    private static boolean[] dilateBox(boolean[] src, int sizeX, int ySize, int sizeZ, int r) {
        int strideX = ySize * sizeZ;
        int strideY = sizeZ;
        boolean[] a = src;
        boolean[] b = new boolean[src.length];

        // X axis: lines indexed by (y, z)
        for (int y = 0; y < ySize; y++)
            for (int z = 0; z < sizeZ; z++)
                dilateLine(a, b, y * sizeZ + z, strideX, sizeX, r);
        // src is dead after this point, so it becomes the second scratch buffer.
        boolean[] t = a; a = b; b = t;

        // Y axis: lines indexed by (x, z)
        for (int x = 0; x < sizeX; x++)
            for (int z = 0; z < sizeZ; z++)
                dilateLine(a, b, x * strideX + z, strideY, ySize, r);
        t = a; a = b; b = t;

        // Z axis: lines indexed by (x, y)
        for (int x = 0; x < sizeX; x++)
            for (int y = 0; y < ySize; y++)
                dilateLine(a, b, x * strideX + y * strideY, 1, sizeZ, r);
        return b;
    }

    /** Writes into {@code dst} whether each cell is within {@code r} of a set cell in {@code src}. */
    private static void dilateLine(boolean[] src, boolean[] dst, int start, int stride, int n, int r) {
        int d = Integer.MAX_VALUE;
        for (int i = 0, p = start; i < n; i++, p += stride) {
            if (src[p]) d = 0;
            else if (d != Integer.MAX_VALUE) d++;
            dst[p] = d <= r;
        }
        d = Integer.MAX_VALUE;
        for (int i = n - 1, p = start + (n - 1) * stride; i >= 0; i--, p -= stride) {
            if (src[p]) d = 0;
            else if (d != Integer.MAX_VALUE) d++;
            if (d <= r) dst[p] = true;
        }
    }

    private static Set<Block> resolveUntouchable(List<String> ids) {
        Set<Block> out = new HashSet<>();
        for (String id : ids) {
            Block b = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(id));
            if (b != null && b != Blocks.AIR) out.add(b);
        }
        return out;
    }

    private static int idx(int x, int y, int z, int ySize, int sizeZ) {
        return (x * ySize + y) * sizeZ + z;
    }

    private static int innerIdx(int x, int y, int z, int innerY) {
        return (x * innerY + y) * 16 + z;
    }
}
