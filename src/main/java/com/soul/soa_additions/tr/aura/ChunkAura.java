package com.soul.soa_additions.tr.aura;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.Aspects;
import com.soul.soa_additions.tr.core.AspectStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Per-chunk aspect aura store. Backs the world-driven "aspects in the air"
 * mechanic the design calls Weave: each chunk has a pool of aspect points
 * seeded from its biome, accumulated by block breaks and mob kills, and
 * eventually drawn from by auramancy.
 *
 * <p>Storage is a fastutil {@code Object2IntOpenHashMap<Aspect>} — small,
 * cache-friendly, and only 6 primal slots get touched in the common case
 * since most chunks will only ever accumulate primal-heavy aspects from
 * their biome. Compounds are added when blocks/mobs with compound aspects
 * are destroyed in the chunk.
 *
 * <p>Caps: per-aspect 1000, total 5000. Past those, increments no-op (no
 * overflow, no error). This matches the design's "prevent unbounded farming"
 * note — a chunk can accumulate 5 fully-saturated aspects but no more.
 */
public final class ChunkAura {

    public static final Capability<ChunkAura.Data> CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation CAP_ID =
            new ResourceLocation(ThaumicRemnants.MODID, "chunk_aura");

    /** Hard ceiling on a single aspect's stored value in one chunk. */
    public static final int PER_ASPECT_CAP = 500;
    /** Hard ceiling on the sum of all aspect values in one chunk. */
    public static final int TOTAL_CAP = 1000;

    private ChunkAura() {}

    // ---------------- Data ----------------

    public static final class Data {
        private final Object2IntOpenHashMap<Aspect> pool = new Object2IntOpenHashMap<>(6);
        private boolean seeded = false;

        public int get(Aspect aspect) { return pool.getInt(aspect); }
        public int total() {
            int t = 0;
            for (int v : pool.values()) t += v;
            return t;
        }
        public Map<Aspect, Integer> snapshot() {
            Map<Aspect, Integer> out = new java.util.LinkedHashMap<>(pool.size());
            for (var e : pool.object2IntEntrySet()) out.put(e.getKey(), e.getIntValue());
            return Collections.unmodifiableMap(out);
        }
        public boolean isSeeded() { return seeded; }

        /** Add {@code delta} (clamped to per-aspect and total caps). Returns
         *  the actual amount added (may be less than requested if a cap was hit). */
        public int add(Aspect aspect, int delta) {
            if (delta <= 0) return 0;
            int cur = pool.getInt(aspect);
            int afterPer = Math.min(PER_ASPECT_CAP, cur + delta);
            int allowedByPer = afterPer - cur;
            int currentTotal = total();
            int allowedByTotal = Math.max(0, TOTAL_CAP - currentTotal);
            int actual = Math.min(allowedByPer, allowedByTotal);
            if (actual <= 0) return 0;
            pool.put(aspect, cur + actual);
            return actual;
        }

        /** Drain {@code delta} (clamped to current value). Returns actual drained. */
        public int drain(Aspect aspect, int delta) {
            if (delta <= 0) return 0;
            int cur = pool.getInt(aspect);
            int actual = Math.min(cur, delta);
            if (actual > 0) {
                int next = cur - actual;
                if (next > 0) pool.put(aspect, next);
                else pool.removeInt(aspect);
            }
            return actual;
        }

        public void clear() { pool.clear(); }

        /** Mark seeded — prevents re-seeding on subsequent reads. Called by
         *  the seeder once it's filled the initial biome composition. */
        void markSeeded() { this.seeded = true; }

        /** Internal write used by the biome seeder; bypasses caps so initial
         *  values from a generous biome JSON don't get silently truncated. */
        void seedAdd(Aspect aspect, int amount) {
            if (amount <= 0) return;
            pool.addTo(aspect, amount);
        }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            CompoundTag aspects = new CompoundTag();
            for (var e : pool.object2IntEntrySet()) {
                aspects.putInt(e.getKey().id().toString(), e.getIntValue());
            }
            tag.put("aspects", aspects);
            tag.putBoolean("seeded", seeded);
            return tag;
        }

        public void read(CompoundTag tag) {
            pool.clear();
            CompoundTag aspects = tag.getCompound("aspects");
            for (String key : aspects.getAllKeys()) {
                ResourceLocation rl = ResourceLocation.tryParse(key);
                if (rl == null) continue;
                try {
                    Aspect a = Aspects.byId(rl);
                    int v = aspects.getInt(key);
                    if (v > 0) pool.put(a, v);
                } catch (Exception ignored) {
                    // Unknown aspect (datapack changed since save) — skip silently.
                }
            }
            seeded = tag.getBoolean("seeded");
        }
    }

    // ---------------- Provider ----------------

    static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final Data data = new Data();
        private final LazyOptional<Data> opt = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
            return cap == CAP ? opt.cast() : LazyOptional.empty();
        }

        @Override public CompoundTag serializeNBT() { return data.write(); }
        @Override public void deserializeNBT(CompoundTag tag) { data.read(tag); }
    }

    // ---------------- API ----------------

    private static final Data EMPTY = new Data();

    public static Data of(@Nullable LevelChunk chunk) {
        if (chunk == null) return EMPTY;
        Data d = chunk.getCapability(CAP).orElse(EMPTY);
        if (d != EMPTY && !d.isSeeded()) {
            seedFromBiome(chunk, d);
        }
        return d;
    }

    public static Data empty() { return EMPTY; }

    /** Y-levels we sample for biome composition when seeding a chunk.
     *  Distributed across the full 1.20.1 build range (-64..320) plus a
     *  high-air sample so floating-island / End chunks still register.
     *  Chosen to hit the bands where distinct biomes most commonly live:
     *  high air, mountain peaks, surface, mid-cave, deepslate, and the
     *  -50 deep-dark sweet spot. */
    private static final int[] BIOME_SAMPLE_Y = { 280, 128, 64, 16, -16, -50, -64 };

    /** Lazy biome-driven initial composition. 1.18+ biomes are 3D — a
     *  single chunk's vertical column can hold a surface biome (plains),
     *  a cave biome (lush_caves), and a deep biome (deep_dark) all at
     *  once. We sample {@link #BIOME_SAMPLE_Y} altitudes, dedupe to
     *  unique biomes, and sum each one's composition so any biome
     *  present anywhere in the chunk contributes. Same biome appearing
     *  at multiple sample points is counted once.
     *
     *  <p>Per-chunk ±25% jitter is applied on top of the merged total so
     *  adjacent chunks with the same biome stack still read differently.
     *  Deterministic per (chunk position, world seed) so reseeds
     *  reproduce exactly.
     *
     *  <p>First read on a fresh chunk; persisted thereafter via the
     *  {@code seeded} flag in NBT. */
    private static void seedFromBiome(LevelChunk chunk, Data d) {
        try {
            int cx = chunk.getPos().getMiddleBlockX();
            int cz = chunk.getPos().getMiddleBlockZ();
            var registryAccess = chunk.getLevel().registryAccess();
            var biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

            // Collect unique biomes present anywhere in the column. Use
            // LinkedHashMap so seed-resolution order is deterministic
            // (sample-Y order) — only matters for log readability since
            // sums are commutative.
            java.util.LinkedHashMap<ResourceLocation, net.minecraft.core.Holder<Biome>> uniqueBiomes =
                    new java.util.LinkedHashMap<>();
            for (int y : BIOME_SAMPLE_Y) {
                net.minecraft.core.Holder<Biome> holder =
                        chunk.getLevel().getBiome(new net.minecraft.core.BlockPos(cx, y, cz));
                Biome biome = holder.value();
                ResourceKey<Biome> key = biomeRegistry.getResourceKey(biome).orElse(null);
                if (key == null) continue;
                uniqueBiomes.putIfAbsent(key.location(), holder);
            }
            if (uniqueBiomes.isEmpty()) return;

            // Deterministic RNG per (chunk, world seed) — same chunk
            // re-seeded after a /reload always produces the same numbers.
            long worldSeed = chunk.getLevel() instanceof net.minecraft.server.level.ServerLevel sl
                    ? sl.getSeed() : 0L;
            java.util.Random rng = new java.util.Random(
                    worldSeed
                            ^ ((long) chunk.getPos().x * 0x9E3779B97F4A7C15L)
                            ^ ((long) chunk.getPos().z * 0xBF58476D1CE4E5B9L));

            // Sum each unique biome's composition. resolve() walks
            // datapack JSON → tag-derived default → hard fallback, so
            // any modded biome still contributes via its forge:is_* tags.
            java.util.LinkedHashMap<com.soul.soa_additions.tr.core.Aspect, Integer> merged =
                    new java.util.LinkedHashMap<>();
            for (var entry : uniqueBiomes.entrySet()) {
                for (AspectStack as : BiomeAspectMap.resolve(entry.getKey(), entry.getValue())) {
                    merged.merge(as.aspect(), as.amount(), Integer::sum);
                }
            }

            // Apply ±25% jitter on the merged totals. Floor + max(1) so
            // small inputs don't round to zero. seedAdd bypasses the
            // PER_ASPECT_CAP / TOTAL_CAP clamps deliberately — those are
            // for impart accumulation, not initial worldgen seed.
            for (var entry : merged.entrySet()) {
                double jitter = 0.75 + rng.nextDouble() * 0.5;
                int amount = Math.max(1, (int) Math.floor(entry.getValue() * jitter));
                d.seedAdd(entry.getKey(), amount);
            }
        } catch (Throwable t) {
            ThaumicRemnants.LOG.warn("Failed to seed chunk aura from biome at {}: {}",
                    chunk.getPos(), t.toString());
        } finally {
            d.markSeeded();
        }
    }

    // ---------------- Registration ----------------

    public static void register(IEventBus modBus) {
        modBus.addListener(ChunkAura::onRegisterCaps);
    }

    private static void onRegisterCaps(RegisterCapabilitiesEvent event) {
        event.register(Data.class);
    }

    @Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {
        private Events() {}

        @SubscribeEvent
        public static void onAttach(AttachCapabilitiesEvent<LevelChunk> event) {
            // Idempotent — same FakePlayer/cap-rebuild gotcha as KnownAspects.
            if (event.getCapabilities().containsKey(CAP_ID)) return;
            event.addCapability(CAP_ID, new Provider());
        }
    }
}
