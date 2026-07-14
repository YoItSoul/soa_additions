package com.soul.soa_additions.oresight.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Client-side ore-sight tracker + renderer.
 *
 * <p>State (single Minecraft client = one player):</p>
 * <ul>
 *   <li>{@link #tracked} — Set of blocks the player currently has ore-sight
 *       for. Updated via {@link #applySync} from the server packet.</li>
 *   <li>{@link #cache} — Per-block pulse-scan results: a list of BlockPos
 *       within the scan radius that are still that block. Refreshed every
 *       100 ticks (~5 s) per the user's "pulse" semantics.</li>
 * </ul>
 *
 * <p>Renderer hook: {@link RenderLevelStageEvent.Stage#AFTER_TRANSLUCENT_BLOCKS}
 * draws an axis-aligned wireframe around each cached BlockPos using vanilla's
 * {@code lines} render type (one buffer call per render frame, ~12 line
 * segments per cube). Cubes whose stored block no longer matches what's in
 * the world are dropped immediately so freshly-mined ore stops glowing.</p>
 *
 * <p>Render scope is bounded by the 10-block scan radius and pulse cadence,
 * so the cost is negligible even with multiple ore-sights stacked.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT)
public final class OreSightClient {

    public static final int SCAN_RADIUS = 10;
    public static final int PULSE_INTERVAL_TICKS = 100;   // 5 seconds at 20 TPS

    /** Custom RenderType for the ore-sight outline: depth-test disabled so
     *  the wireframe shows through walls (xray-style). Depth writes are also
     *  disabled so it doesn't pollute the depth buffer for subsequent passes.
     *  Built on the same shader and vertex format as {@link RenderType#lines()}
     *  so {@link LevelRenderer#renderLineBox} writes are compatible. */
    private static final RenderType THROUGH_WALLS_LINES = ThroughWallsLines.LINES;

    /** Holder class — RenderType.create() needs access to RenderStateShard's
     *  protected static fields, which we get via subclassing. */
    private static final class ThroughWallsLines extends RenderType {
        private ThroughWallsLines(String n, com.mojang.blaze3d.vertex.VertexFormat f,
                                  VertexFormat.Mode m, int b, boolean c, boolean s,
                                  Runnable on, Runnable off) {
            super(n, f, m, b, c, s, on, off);
        }
        static final RenderType LINES = create(
                "soa_additions:ore_sight_through_walls_lines",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES,
                256,
                false, false,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new LineStateShard(OptionalDouble.of(2.5)))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .createCompositeState(false));
    }

    /** Blocks the player has ore-sight for, with absolute game-time expiry.
     *  Mirrors the server-side {@link com.soul.soa_additions.oresight.OreSightTracker}. */
    private static final Map<Block, Long> trackedExpiries = new HashMap<>();
    private static final Set<Block> tracked = new HashSet<>();

    /** Master ore-sight expiry tick. 0 = inactive. When non-zero and current
     *  game-time is below it, the scan accepts every {@link com.soul.soa_additions.oresight.OreSight#isOreBlock}
     *  block in range instead of only those in {@link #tracked}. */
    private static long masterExpiry = 0L;

    /** Cached per-block scan results. Cleared on tracker change or world unload. */
    private static final Map<Block, List<BlockPos>> cache = new HashMap<>();

    /** Last game-tick the cache was refreshed (for 5-second pulse). */
    private static long lastPulseTick = -1L;

    /** Per-block highlight color memo — avoids a registry-key lookup + string
     *  hash per block per render frame. Pure function of the block, so it
     *  never needs invalidation; bounded by distinct blocks ever highlighted. */
    private static final Map<Block, Integer> colorCache = new HashMap<>();

    /** Constant unit wireframe box — hoisted out of the per-pos render loop. */
    private static final AABB UNIT_BOX = new AABB(0, 0, 0, 1, 1, 1).inflate(0.002);

    /** How often (client ticks) stale cached positions are re-validated
     *  between pulses. 5t = 0.25 s — a mined ore's highlight lingers at most
     *  a quarter-second instead of costing a full getBlockState sweep per tick. */
    private static final int VALIDATE_INTERVAL_TICKS = 5;

    private OreSightClient() {}

    /** Called from the network packet handler. */
    public static void applySync(Map<Block, Long> entries, long master) {
        trackedExpiries.clear();
        trackedExpiries.putAll(entries);
        tracked.clear();
        tracked.addAll(entries.keySet());
        masterExpiry = master;
        // Master mode rescans every block each pulse, so don't bother retaining
        // anything; non-master mode keeps only currently-tracked entries.
        if (masterExpiry == 0L) {
            cache.keySet().retainAll(tracked);
        } else {
            cache.clear();
        }
        // Force a fresh scan on next render frame.
        lastPulseTick = -1L;
    }

    /** True iff master ore-sight is currently active client-side. */
    public static boolean isMasterActive() {
        if (masterExpiry == 0L) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        return mc.level.getGameTime() < masterExpiry;
    }

    /** All ores currently being tracked, sorted by expiry (latest first). */
    public static List<Block> trackedSortedByExpiry() {
        if (trackedExpiries.isEmpty()) return java.util.Collections.emptyList();
        return trackedExpiries.entrySet().stream()
                .sorted(Map.Entry.<Block, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Snapshot of the cached scan results' block keys. Used by the icon
     *  renderer when master ore-sight is active and the per-block tracker
     *  set is empty — the cache has whatever ores happen to be in scan range
     *  this pulse, which is a fine choice for the HUD icon. */
    public static List<Block> cachedScanBlocks() {
        if (cache.isEmpty()) return java.util.Collections.emptyList();
        return new ArrayList<>(cache.keySet());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        boolean master = isMasterActive();
        if (!master && tracked.isEmpty()) return;

        long now = mc.level.getGameTime();
        if (lastPulseTick < 0 || now - lastPulseTick >= PULSE_INTERVAL_TICKS) {
            pulseScan(mc.level, mc.player, master);
            lastPulseTick = now;
        } else if (now % VALIDATE_INTERVAL_TICKS == 0) {
            // Drop positions where the block has changed since last scan
            // (mining a tracked ore removes the highlight within 0.25 s).
            for (Map.Entry<Block, List<BlockPos>> e : cache.entrySet()) {
                Block expected = e.getKey();
                List<BlockPos> list = e.getValue();
                list.removeIf(pos -> mc.level.getBlockState(pos).getBlock() != expected);
            }
        }
    }

    private static void pulseScan(ClientLevel level, Player player, boolean master) {
        BlockPos center = player.blockPosition();
        int r = SCAN_RADIUS;
        int yMin = Math.max(level.getMinBuildHeight(), center.getY() - r);
        int yMax = Math.min(level.getMaxBuildHeight() - 1, center.getY() + r);

        Map<Block, List<BlockPos>> fresh = new HashMap<>();
        if (!master) {
            for (Block b : tracked) fresh.put(b, new ArrayList<>(8));
        }

        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int y = yMin; y <= yMax; y++) {
                    cur.set(center.getX() + dx, y, center.getZ() + dz);
                    Block here = level.getBlockState(cur).getBlock();
                    if (master) {
                        if (!com.soul.soa_additions.oresight.OreSight.isOreBlock(here)) continue;
                        fresh.computeIfAbsent(here, k -> new ArrayList<>(8)).add(cur.immutable());
                    } else {
                        if (!tracked.contains(here)) continue;
                        fresh.get(here).add(cur.immutable());
                    }
                }
            }
        }
        cache.clear();
        cache.putAll(fresh);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // AFTER_PARTICLES runs after solid/translucent/entity/BE/particles, so
        // the depth buffer is fully populated and no other batch is in flight
        // when we grab our buffer. Earlier stages (AFTER_TRANSLUCENT_BLOCKS)
        // can leave shared-buffer state from concurrent batches that subtly
        // overrides our NO_DEPTH_TEST setup, leaving the wireframe occluded.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (cache.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack pose = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(THROUGH_WALLS_LINES);

        for (Map.Entry<Block, List<BlockPos>> entry : cache.entrySet()) {
            int color = blockHighlightColor(entry.getKey());
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8)  & 0xFF) / 255f;
            float b = ( color        & 0xFF) / 255f;

            for (BlockPos pos : entry.getValue()) {
                pose.pushPose();
                pose.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
                LevelRenderer.renderLineBox(pose, lines, UNIT_BOX, r, g, b, 0.85f);
                pose.popPose();
            }
        }
        buffers.endBatch(THROUGH_WALLS_LINES);
    }

    /** Stable per-block tint derived from the registry id hash, falling back
     *  to a soft cyan if the registry name hashes degenerate. Memoized —
     *  called per block per render frame. */
    private static int blockHighlightColor(Block block) {
        return colorCache.computeIfAbsent(block, blk -> {
            net.minecraft.resources.ResourceLocation id =
                    net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(blk);
            if (id == null) return 0x4FBFD7;
            int h = id.toString().hashCode();
            // Map hash → bright HSV-ish color
            int r = 100 + ((h >>> 0)  & 0x7F);
            int g = 100 + ((h >>> 8)  & 0x7F);
            int b = 100 + ((h >>> 16) & 0x7F);
            return (r << 16) | (g << 8) | b;
        });
    }
}
