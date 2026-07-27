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
        int pad = GENERATION_DISTANCE;
        int sizeX = 16 + 2 * pad;
        int sizeZ = 16 + 2 * pad;
        int ySize = (maxY + pad) - minY + 1;

        boolean[] reach = new boolean[sizeX * ySize * sizeZ];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int y = 0; y < ySize; y++) {
                    cursor.set(chunkMinX - pad + x, minY + y, chunkMinZ - pad + z);
                    BlockState s = level.getBlockState(cursor);
                    if (s.isAir() || !s.getFluidState().isEmpty()) {
                        reach[idx(x, y, z, ySize, sizeZ)] = true;
                    }
                }
            }
        }

        // Chebyshev-distance dilation: after N passes, reach[] covers everything
        // within box distance N of a safe block.
        for (int pass = 0; pass < GENERATION_DISTANCE; pass++) {
            boolean[] next = reach.clone();
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    for (int y = 0; y < ySize; y++) {
                        if (next[idx(x, y, z, ySize, sizeZ)]) continue;
                        neighborScan:
                        for (int dx = -1; dx <= 1; dx++) {
                            int nx = x + dx;
                            if (nx < 0 || nx >= sizeX) continue;
                            for (int dz = -1; dz <= 1; dz++) {
                                int nz = z + dz;
                                if (nz < 0 || nz >= sizeZ) continue;
                                for (int dy = -1; dy <= 1; dy++) {
                                    int ny = y + dy;
                                    if (ny < 0 || ny >= ySize) continue;
                                    if (reach[idx(nx, ny, nz, ySize, sizeZ)]) {
                                        next[idx(x, y, z, ySize, sizeZ)] = true;
                                        break neighborScan;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            reach = next;
        }

        Block hard = ModBlocks.HARDENED_STONE.get();
        BlockState hardState = hard.defaultBlockState();
        Set<Block> untouchable = resolveUntouchable(cfg.untouchable());

        boolean placedAny = false;
        if (!loggedFirstRun) {
            loggedFirstRun = true;
            com.mojang.logging.LogUtils.getLogger().info(
                    "[soa_additions] Hardened stone seal feature running (first chunk this session: {} {} in {})",
                    chunkMinX >> 4, chunkMinZ >> 4, level.getLevel().dimension().location());
        }
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (reach[idx(x + pad, y - minY, z + pad, ySize, sizeZ)]) continue;
                    cursor.set(chunkMinX + x, y, chunkMinZ + z);
                    BlockState s = level.getBlockState(cursor);
                    if (s.isAir() || !s.getFluidState().isEmpty()) continue;
                    Block b = s.getBlock();
                    if (b == hard || s.is(Blocks.BEDROCK) || untouchable.contains(b)) continue;
                    if (s.hasBlockEntity()) continue;
                    level.setBlock(cursor, hardState, 2);
                    placedAny = true;
                }
            }
        }
        return placedAny;
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
}
