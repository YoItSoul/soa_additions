package com.soul.soa_additions.worldgen;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code /soa worldgen scan [radius]} — counts how many of our ore blocks
 * (anything from {@code soa_additions:} or {@code taiga:} that ends in
 * {@code _ore} or matches the special meteor/basalt/eezo blocks) sit in
 * loaded chunks around the executor. Useful for verifying that placed-feature
 * + biome-modifier wiring is actually firing in newly-generated terrain
 * without alt-tabbing to MCEdit or a NBT scanner.
 *
 * <p>Skips already-scanned chunks via {@code ChunkAccess.getStatus()} —
 * unloaded / partial chunks aren't counted (so the player should fly out to
 * the perimeter first to make sure the area is generated). Output reports
 * total counts, per-chunk averages, and the expected per-chunk count derived
 * from the placed-feature data table baked into this class — drift between
 * scanned vs expected highlights worldgen failures.
 *
 * <p>Default radius = 4 chunks (9×9 grid). Max = 16 (33×33 grid, ≈10s scan).
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class WorldgenScanCommand {

    private WorldgenScanCommand() {}

    /** Per-chunk expected ore-block count = veinCount × veinSize, from placed_feature JSONs.
     *  Only listed for ores whose primary spawn matches the dimension being scanned. */
    private static final Map<String, ExpectedSpawn> EXPECTED = new HashMap<>();
    private record ExpectedSpawn(double perChunk, String dim) {}
    static {
        // soa_additions overworld
        EXPECTED.put("soa_additions:abyssal_ore_block",   new ExpectedSpawn(12*3, "overworld"));
        EXPECTED.put("soa_additions:aeroite_ore",         new ExpectedSpawn(20*8 / 2.0, "overworld")); // rarity 1/2
        EXPECTED.put("soa_additions:aqualite_ore",        new ExpectedSpawn(3*8,  "overworld"));
        EXPECTED.put("soa_additions:chromium_ore",        new ExpectedSpawn(2*4 / 3.0, "overworld")); // rarity 1/3
        EXPECTED.put("soa_additions:cryonium_ore",        new ExpectedSpawn(6*4,  "overworld"));
        EXPECTED.put("soa_additions:cytosinite_ore",      new ExpectedSpawn(2*4,  "overworld"));
        EXPECTED.put("soa_additions:experience_ore",      new ExpectedSpawn(1*4,  "overworld"));
        EXPECTED.put("soa_additions:manganese_ore",       new ExpectedSpawn(8*4,  "overworld"));
        EXPECTED.put("soa_additions:shadowium_ore",       new ExpectedSpawn(1*4,  "overworld"));
        EXPECTED.put("soa_additions:void_ore_block",      new ExpectedSpawn(4*3,  "overworld"));
        // soa_additions nether
        EXPECTED.put("soa_additions:infernium_ore_block", new ExpectedSpawn(8*4,  "nether"));
        // soa_additions end
        EXPECTED.put("soa_additions:ether_ore_block",     new ExpectedSpawn(16*3, "end"));
        EXPECTED.put("soa_additions:titanium_ore",        new ExpectedSpawn(1*4,  "end"));
        EXPECTED.put("soa_additions:uru_ore",             new ExpectedSpawn(0.10, "end")); // 10% per chunk (custom feat)

        // TAIGA overworld
        EXPECTED.put("taiga:karmesine_ore",  new ExpectedSpawn(8*1, "overworld"));
        EXPECTED.put("taiga:ovium_ore",      new ExpectedSpawn(8*1, "overworld"));
        EXPECTED.put("taiga:jauxum_ore",     new ExpectedSpawn(8*1, "overworld"));
        EXPECTED.put("taiga:vibranium_ore",  new ExpectedSpawn(8*4, "overworld"));   // primary; secondary +1*3/7
        EXPECTED.put("taiga:dilithium_ore",  new ExpectedSpawn(12*5, "overworld"));
        EXPECTED.put("taiga:basalt_block",   new ExpectedSpawn(12,   "overworld"));  // single-block lava replace
        EXPECTED.put("taiga:eezo_ore",       new ExpectedSpawn(3,    "overworld"));
        EXPECTED.put("taiga:duranite_ore",   new ExpectedSpawn(0.06, "overworld"));  // 6% per chunk meteor
        EXPECTED.put("taiga:meteorite_block", new ExpectedSpawn(0.06 * 30, "overworld")); // ~30 hull blocks per meteor
        // TAIGA nether
        EXPECTED.put("taiga:tiberium_ore",   new ExpectedSpawn(15*22, "nether"));
        EXPECTED.put("taiga:prometheum_ore", new ExpectedSpawn(18*3,  "nether"));
        EXPECTED.put("taiga:valyrium_ore",   new ExpectedSpawn(10*3,  "nether"));
        EXPECTED.put("taiga:osram_ore",      new ExpectedSpawn(1.0/7, "nether"));
        // TAIGA end
        EXPECTED.put("taiga:aurorium_ore",   new ExpectedSpawn(10*3, "end"));
        EXPECTED.put("taiga:palladium_ore",  new ExpectedSpawn(10*3, "end"));
        EXPECTED.put("taiga:abyssum_ore",    new ExpectedSpawn(4,    "end"));
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa").then(
                        Commands.literal("worldgen").then(
                                Commands.literal("scan")
                                        .executes(ctx -> scan(ctx.getSource(), 4))
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 16))
                                                .executes(ctx -> scan(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "radius"))))
                        )
                )
        );
    }

    private static int scan(CommandSourceStack src, int radius) {
        ServerLevel level = src.getLevel();
        BlockPos origin = BlockPos.containing(src.getPosition());
        ChunkPos centerChunk = new ChunkPos(origin);
        String dimName = dimName(level);

        long start = System.nanoTime();
        Map<String, Long> counts = new HashMap<>();
        int chunksScanned = 0;
        int chunksSkipped = 0;
        long blocksRead = 0;

        int minSection = level.getMinSection();
        int maxSection = level.getMaxSection();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos cPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                ChunkAccess chunk = level.getChunk(cPos.x, cPos.z, ChunkStatus.FULL, false);
                if (!(chunk instanceof LevelChunk lc) || !lc.getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                    chunksSkipped++;
                    continue;
                }
                chunksScanned++;
                BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
                int baseX = cPos.getMinBlockX();
                int baseZ = cPos.getMinBlockZ();
                for (int sectionIdx = minSection; sectionIdx < maxSection; sectionIdx++) {
                    int yBase = sectionIdx << 4;
                    for (int ly = 0; ly < 16; ly++) {
                        int y = yBase + ly;
                        for (int lx = 0; lx < 16; lx++) {
                            for (int lz = 0; lz < 16; lz++) {
                                cur.set(baseX + lx, y, baseZ + lz);
                                BlockState state = chunk.getBlockState(cur);
                                if (state.isAir()) continue;
                                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                                if (id == null) continue;
                                String idStr = id.toString();
                                if (!isOurBlock(idStr)) continue;
                                counts.merge(idStr, 1L, Long::sum);
                            }
                        }
                    }
                    blocksRead += 16 * 16 * 16;
                }
            }
        }

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        final int finalChunksScanned = chunksScanned;
        final int finalChunksSkipped = chunksSkipped;
        final long finalBlocksRead = blocksRead;

        src.sendSuccess(() -> Component.literal(
                "── /soa worldgen scan ──").withStyle(ChatFormatting.GOLD), false);
        src.sendSuccess(() -> Component.literal(
                String.format("center=(%d,%d) dim=%s radius=%d chunks=%d (skipped %d)",
                        centerChunk.x, centerChunk.z, dimName, radius,
                        finalChunksScanned, finalChunksSkipped))
                .withStyle(ChatFormatting.GRAY), false);

        if (counts.isEmpty()) {
            src.sendSuccess(() -> Component.literal(
                    "  No SoA / TAIGA ore blocks found in scanned area.")
                    .withStyle(ChatFormatting.YELLOW), false);
        } else {
            counts.entrySet().stream()
                    .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed())
                    .forEach(e -> {
                        String id = e.getKey();
                        long n = e.getValue();
                        double perChunk = (double) n / Math.max(1, finalChunksScanned);
                        ExpectedSpawn exp = EXPECTED.get(id);
                        String tail;
                        ChatFormatting color;
                        if (exp == null) {
                            tail = "";
                            color = ChatFormatting.WHITE;
                        } else if (!exp.dim.equals(dimName)) {
                            tail = String.format("  (expected in %s, not %s)", exp.dim, dimName);
                            color = ChatFormatting.GRAY;
                        } else {
                            double ratio = exp.perChunk == 0 ? Double.POSITIVE_INFINITY : perChunk / exp.perChunk;
                            tail = String.format("  expected ≈%.2f/chunk (ratio %.2fx)", exp.perChunk, ratio);
                            if (ratio < 0.25)      color = ChatFormatting.RED;
                            else if (ratio < 0.5)  color = ChatFormatting.YELLOW;
                            else if (ratio > 2.0)  color = ChatFormatting.AQUA;
                            else                   color = ChatFormatting.GREEN;
                        }
                        Component line = Component.literal(
                                String.format("  %-40s %6d (%.2f/chunk)%s", id, n, perChunk, tail))
                                .withStyle(color);
                        src.sendSuccess(() -> line, false);
                    });
        }

        src.sendSuccess(() -> Component.literal(
                String.format("scanned %d blocks in %dms", finalBlocksRead, elapsedMs))
                .withStyle(ChatFormatting.DARK_GRAY), false);
        return chunksScanned;
    }

    private static boolean isOurBlock(String id) {
        if (id.startsWith("soa_additions:")) {
            // any block whose id contains "ore" — covers *_ore, *_ore_block, etc.
            return id.contains("ore") || id.equals("soa_additions:obsidiorite");
        }
        if (id.startsWith("taiga:")) {
            // any *_ore plus the special blocks (meteor hull / basalt replacer / blocks)
            return id.contains("ore") || id.contains("meteorite") || id.contains("basalt");
        }
        return false;
    }

    private static String dimName(ServerLevel level) {
        ResourceLocation rl = level.dimension().location();
        if (rl.equals(new ResourceLocation("minecraft", "overworld"))) return "overworld";
        if (rl.equals(new ResourceLocation("minecraft", "the_nether"))) return "nether";
        if (rl.equals(new ResourceLocation("minecraft", "the_end")))    return "end";
        return rl.toString();
    }
}
