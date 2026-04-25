package com.soul.soa_additions.tr.aura;

import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.core.Aspects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Biome → initial aspect composition lookup. Used by {@link ChunkAura} to
 * seed a chunk's aura on first load.
 *
 * <p>Three tiers of resolution, in priority order:
 * <ol>
 *   <li><b>Datapack JSON.</b> If {@code data/<ns>/tr_aspects/biome/<path>.json}
 *       exists, that's the authoritative composition. Loaded by
 *       {@link com.soul.soa_additions.tr.data.AspectMapLoader}.</li>
 *   <li><b>Tag-derived default.</b> If no JSON exists, infer from the
 *       biome's Forge / vanilla tag set: {@code is_hot} adds Ignis,
 *       {@code is_forest} adds Herba+Arbor, {@code is_nether} stacks
 *       Ignis+Perditio+Mortuus, etc. This is what makes the system
 *       auto-handle modded biomes — drop in BetterEnd, Biomes O' Plenty,
 *       Terralith, whatever, and their biomes get reasonable aspects
 *       without a hand-written JSON for each.</li>
 *   <li><b>Hard fallback.</b> If even tag derivation produces nothing
 *       (a biome with literally no tags), emit a small Aer+Terra baseline
 *       so chunks have *some* aura instead of zero.</li>
 * </ol>
 *
 * <p>Datapack overrides win because pack makers explicitly chose those
 * values; we don't second-guess them with tag inference.
 */
public final class BiomeAspectMap {

    private static final Map<ResourceLocation, List<AspectStack>> ENTRIES = new HashMap<>();
    private static final List<AspectStack> EMPTY = Collections.emptyList();

    /** Last-ditch baseline for biomes that have no JSON AND no tags we
     *  recognise. Ten each of the two safest primals — enough to be
     *  observable, low enough not to compete with proper compositions. */
    private static final List<AspectStack> HARD_FALLBACK = List.of(
            new AspectStack(Aspects.AER, 10),
            new AspectStack(Aspects.TERRA, 10)
    );

    private BiomeAspectMap() {}

    public static void put(ResourceLocation biomeId, List<AspectStack> stacks) {
        ENTRIES.put(biomeId, List.copyOf(stacks));
    }

    public static void clear() { ENTRIES.clear(); }
    public static int size() { return ENTRIES.size(); }
    public static boolean hasEntry(ResourceLocation biomeId) { return ENTRIES.containsKey(biomeId); }

    /** Resolve a composition for the given biome. Use this overload when
     *  you have the biome Holder available — it enables tag-derived
     *  defaults for biomes without an explicit JSON entry. */
    public static List<AspectStack> resolve(@Nullable ResourceLocation biomeId, @Nullable Holder<Biome> holder) {
        if (biomeId != null) {
            List<AspectStack> entry = ENTRIES.get(biomeId);
            if (entry != null) return entry;
        }
        if (holder != null) {
            List<AspectStack> derived = deriveFromTags(holder);
            if (!derived.isEmpty()) return derived;
        }
        return HARD_FALLBACK;
    }

    /** ID-only resolution (no Holder) — falls straight to the hard
     *  fallback if no JSON entry exists. Kept for testing and the rare
     *  caller that doesn't have a biome Holder; production code should
     *  use {@link #resolve(ResourceLocation, Holder)}. */
    public static List<AspectStack> forBiome(@Nullable ResourceLocation biomeId) {
        if (biomeId == null) return HARD_FALLBACK;
        return ENTRIES.getOrDefault(biomeId, HARD_FALLBACK);
    }

    /** Inspect the biome's tag set + climate properties and build a
     *  composition from matching trait → aspect rules. Each rule adds to
     *  a running tally; the final list is the merged result.
     *
     *  <p>Rules are intentionally additive and overlapping — a swamp is
     *  both wet AND has the swamp tag, a Nether biome is both hot AND a
     *  nether biome, etc. That stacking gives modded biomes that combine
     *  traits (hot wet jungle, frozen mountain) reasonable mixed-aspect
     *  compositions without a special case for every combination. */
    public static List<AspectStack> deriveFromTags(Holder<Biome> holder) {
        Map<Aspect, Integer> out = new LinkedHashMap<>();
        Biome biome = holder.value();
        float temp = biome.getBaseTemperature();

        // ---- Climate (climate tags + raw temperature/downfall thresholds) ----
        boolean isHot  = holder.is(Tags.Biomes.IS_HOT) || temp > 1.0f;
        boolean isCold = holder.is(Tags.Biomes.IS_COLD) || holder.is(BiomeTags.IS_TAIGA) || temp < 0.3f;
        boolean isWet  = holder.is(Tags.Biomes.IS_WET) || biome.getModifiedClimateSettings().downfall() > 0.6f;
        boolean isDry  = holder.is(Tags.Biomes.IS_DRY) || biome.getModifiedClimateSettings().downfall() < 0.2f;
        boolean isSnowy = holder.is(Tags.Biomes.IS_SNOWY);

        // All values halved from initial design — TC4 baselines were
        // ~30-100 total per chunk, and we want 90%+ headroom under the
        // PER_ASPECT_CAP=500 / TOTAL_CAP=1000 ceilings so block-break /
        // mob-kill imparts have room to actually accumulate.

        if (isHot)  add(out, Aspects.IGNIS, 15);
        if (isCold) { add(out, Aspects.GELUM, 15); add(out, Aspects.AQUA, 5); }
        if (isWet)  add(out, Aspects.AQUA, 15);
        if (isDry)  { add(out, Aspects.PERDITIO, 8); add(out, Aspects.TERRA, 10); }
        if (isSnowy) { add(out, Aspects.GELUM, 12); add(out, Aspects.AER, 8); }

        // ---- Terrain shape ----
        if (holder.is(Tags.Biomes.IS_MOUNTAIN) || holder.is(Tags.Biomes.IS_PEAK)
                || holder.is(BiomeTags.IS_MOUNTAIN)) {
            add(out, Aspects.TERRA, 20);
            add(out, Aspects.AER, 12);
        }
        if (holder.is(Tags.Biomes.IS_SLOPE)) {
            add(out, Aspects.TERRA, 12);
            add(out, Aspects.MOTUS, 5);
        }
        if (holder.is(BiomeTags.IS_OCEAN) || holder.is(BiomeTags.IS_DEEP_OCEAN)) {
            add(out, Aspects.AQUA, 30);
            add(out, Aspects.LIMUS, 10);
        }
        if (holder.is(BiomeTags.IS_RIVER)) {
            add(out, Aspects.AQUA, 20);
        }
        if (holder.is(BiomeTags.IS_BEACH)) {
            add(out, Aspects.TERRA, 12);
            add(out, Aspects.AQUA, 10);
        }

        // ---- Vegetation ----
        if (holder.is(BiomeTags.IS_FOREST) || holder.is(Tags.Biomes.IS_DENSE)) {
            add(out, Aspects.HERBA, 20);
            add(out, Aspects.ARBOR, 18);
        }
        if (holder.is(BiomeTags.IS_JUNGLE)) {
            add(out, Aspects.HERBA, 30);
            add(out, Aspects.ARBOR, 20);
            add(out, Aspects.BESTIA, 10);
        }
        if (holder.is(Tags.Biomes.IS_PLAINS) || holder.is(Tags.Biomes.IS_SPARSE)) {
            add(out, Aspects.HERBA, 12);
            add(out, Aspects.AER, 12);
        }
        if (holder.is(BiomeTags.IS_SAVANNA)) {
            add(out, Aspects.HERBA, 12);
            add(out, Aspects.TERRA, 15);
            add(out, Aspects.IGNIS, 8);
        }
        if (holder.is(Tags.Biomes.IS_MAGICAL)) {
            add(out, Aspects.PRAECANTATIO, 15);
            add(out, Aspects.AURAM, 10);
        }
        if (holder.is(Tags.Biomes.IS_SWAMP)) {
            add(out, Aspects.AQUA, 18);
            add(out, Aspects.LIMUS, 18);
            add(out, Aspects.VENENUM, 10);
        }
        if (holder.is(Tags.Biomes.IS_MUSHROOM)) {
            add(out, Aspects.HERBA, 15);
            add(out, Aspects.LIMUS, 15);
            add(out, Aspects.SANO, 10);
            add(out, Aspects.ALIENIS, 8);
        }
        if (holder.is(Tags.Biomes.IS_SANDY) || holder.is(Tags.Biomes.IS_DESERT)) {
            add(out, Aspects.TERRA, 15);
            add(out, Aspects.PERDITIO, 12);
            add(out, Aspects.IGNIS, 8);
        }

        // ---- Underground / wasteland / void ----
        if (holder.is(Tags.Biomes.IS_UNDERGROUND) || holder.is(Tags.Biomes.IS_CAVE)) {
            add(out, Aspects.TERRA, 15);
            add(out, Aspects.TENEBRAE, 15);
            add(out, Aspects.PERDITIO, 5);
        }
        if (holder.is(Tags.Biomes.IS_VOID)) {
            add(out, Aspects.VACUOS, 40);
            add(out, Aspects.TENEBRAE, 20);
            add(out, Aspects.PERDITIO, 15);
        }
        if (holder.is(Tags.Biomes.IS_WASTELAND)) {
            add(out, Aspects.PERDITIO, 20);
            add(out, Aspects.MORTUUS, 10);
            add(out, Aspects.VITIUM, 5);
        }

        // ---- Dimension overlays ----
        // Stack on top of everything else so a hot Nether biome ends up
        // Ignis-dominant from the dimension rule + the climate rule.
        // (Forge's Tags.Biomes doesn't expose a top-level IS_NETHER/IS_END;
        // vanilla BiomeTags.IS_NETHER / IS_END covers both vanilla and
        // mod-added Nether/End biomes that opt in to those tags.)
        if (holder.is(BiomeTags.IS_NETHER)) {
            add(out, Aspects.IGNIS, 20);
            add(out, Aspects.PERDITIO, 15);
            add(out, Aspects.MORTUUS, 8);
        }
        if (holder.is(BiomeTags.IS_END)) {
            add(out, Aspects.ALIENIS, 25);
            add(out, Aspects.VACUOS, 15);
            add(out, Aspects.TENEBRAE, 10);
        }

        return out.entrySet().stream()
                .map(e -> new AspectStack(e.getKey(), e.getValue()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static void add(Map<Aspect, Integer> out, Aspect a, int amount) {
        out.merge(a, amount, Integer::sum);
    }
}
