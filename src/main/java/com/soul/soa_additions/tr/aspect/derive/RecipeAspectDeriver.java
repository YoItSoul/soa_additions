package com.soul.soa_additions.tr.aspect.derive;

import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.AspectMap;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.core.Aspects;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recipe-walking aspect deriver. For an item the player encounters that has
 * no explicit JSON entry and no tag-based bulk aspect, walks the recipe
 * registry to find recipes that produce the item, sums the input
 * ingredients' aspects (recursively), applies a per-derivation loss factor,
 * and picks the cheapest result.
 *
 * <p>This is the Thaumcraft 4 "auto-aspect" mechanism — what made TC4 feel
 * like every item in the game had thoughtful aspects assigned even though
 * Azanor only hand-curated a few hundred. Tinker's hammer recipe? Sum of
 * its ingredients (sticks → arbor + perditio loss; iron ingots → metallum;
 * etc.) → felt right.
 *
 * <p>Cycle protection: a thread-local visiting set tracks items currently
 * being derived; a recursive call into the same item returns empty,
 * preventing infinite recursion when recipe A → B and B → A both exist
 * (uncommon but possible with conversion recipes).
 *
 * <p>Cache: results are stored in a static map cleared on datapack reload.
 * The first request for an item walks the recipe tree once; all subsequent
 * requests are O(1).
 */
public final class RecipeAspectDeriver {

    /** Per-derivation aspect loss — 25% reduction per crafting step. */
    private static final float LOSS_FACTOR = 0.75f;

    /** Recursion depth cap. Tight (5) because the modpack's recipe graph is
     *  deep — every step multiplies work, and aspects past depth 5 are
     *  whittled to ~24% of source magnitude anyway by LOSS_FACTOR. */
    private static final int MAX_DEPTH = 5;

    /** Thread-local cycle-detection set. */
    private static final ThreadLocal<Set<ResourceLocation>> VISITING =
            ThreadLocal.withInitial(HashSet::new);

    /** Per-item derived cache. */
    private static final Map<ResourceLocation, List<AspectStack>> CACHE = new HashMap<>();

    private static final List<AspectStack> IN_PROGRESS = Collections.emptyList();

    /** Recipe-by-result-item reverse index. Built lazily on first derivation
     *  request after a reload; without it, every derive() walks every recipe
     *  in the entire modpack (~10k in SoA), which is a 2-second freeze on
     *  the render thread. With it, lookup is O(matches) per item. */
    private static final Map<ResourceLocation, List<Recipe<?>>> RECIPES_BY_RESULT = new HashMap<>();
    private static volatile boolean indexBuilt = false;
    private static final Object INDEX_LOCK = new Object();

    private RecipeAspectDeriver() {}

    public static void clearCache() {
        synchronized (CACHE) { CACHE.clear(); }
        synchronized (INDEX_LOCK) {
            RECIPES_BY_RESULT.clear();
            indexBuilt = false;
        }
    }

    public static int cacheSize() {
        synchronized (CACHE) { return CACHE.size(); }
    }

    /** Pre-warm the reverse index on the server thread — called from the
     *  datapack-sync hook so the first monocle hover doesn't pay the cost
     *  on the render thread. Safe to call repeatedly; idempotent. */
    public static void preWarmIndex(Level level) {
        if (indexBuilt) return;
        buildIndex(level.getRecipeManager(), level.registryAccess());
    }

    /** Eagerly derive aspects for every registered item on a low-priority
     *  background thread. After this completes, every subsequent lookup
     *  hits the cache — no first-time-hover stutter ever, even on obscure
     *  modded items the player encounters mid-game. The thread is daemon +
     *  MIN_PRIORITY so it can't starve the main game. */
    public static void preWarmAllItems(Level level) {
        if (!indexBuilt) buildIndex(level.getRecipeManager(), level.registryAccess());
        Thread worker = new Thread(() -> {
            long start = System.nanoTime();
            int n = 0;
            for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys()) {
                try {
                    derive(level, id);
                    n++;
                } catch (Throwable t) {
                    // One bad modded item shouldn't halt warm-up.
                }
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            ThaumicRemnants.LOG.info("[deriver] eager-warmed {} items in {} ms (cache size {})",
                    n, elapsedMs, cacheSize());
        }, "TR-AspectDeriver-Warmup");
        worker.setDaemon(true);
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.start();
    }

    /** Build the result→recipes index. Called lazily; one-time per reload. */
    private static void buildIndex(RecipeManager rm, RegistryAccess access) {
        synchronized (INDEX_LOCK) {
            if (indexBuilt) return;
            int n = 0;
            for (Recipe<?> recipe : rm.getRecipes()) {
                ItemStack result;
                try { result = recipe.getResultItem(access); }
                catch (Throwable t) { continue; }
                if (result == null || result.isEmpty()) continue;
                ResourceLocation rid = ForgeRegistries.ITEMS.getKey(result.getItem());
                if (rid == null) continue;
                RECIPES_BY_RESULT.computeIfAbsent(rid, k -> new ArrayList<>()).add(recipe);
                n++;
            }
            indexBuilt = true;
            ThaumicRemnants.LOG.info("[deriver] indexed {} recipes by result, {} unique items",
                    n, RECIPES_BY_RESULT.size());
        }
    }

    /**
     * Resolve the derived aspect composition for the given item id, using
     * recipes in the supplied level's RecipeManager. Combines the
     * tag-registry contribution (if any) with the recipe-derived contribution.
     *
     * <p>{@code level} can be null — in that case only the tag layer
     * contributes (because we have no RecipeManager to walk). Cached in
     * either case; a future call with a non-null level will overwrite the
     * tag-only result with a recipe-aware one.
     */
    public static List<AspectStack> derive(@Nullable Level level, ResourceLocation itemId) {
        synchronized (CACHE) {
            List<AspectStack> hit = CACHE.get(itemId);
            if (hit != null && hit != IN_PROGRESS) return hit;
        }

        Set<ResourceLocation> visiting = VISITING.get();
        boolean topLevel = visiting.isEmpty();
        try {
            List<AspectStack> result = deriveInternal(level, itemId, visiting, 0);
            synchronized (CACHE) { CACHE.put(itemId, result); }
            return result;
        } finally {
            if (topLevel) visiting.clear();
        }
    }

    private static List<AspectStack> deriveInternal(@Nullable Level level,
                                                     ResourceLocation itemId,
                                                     Set<ResourceLocation> visiting,
                                                     int depth) {
        if (depth > MAX_DEPTH) return Collections.emptyList();
        if (!visiting.add(itemId)) return Collections.emptyList();
        try {
            var item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null) return Collections.emptyList();

            // Layer A: tag-based bulk lookup.
            Map<Aspect, Integer> sum = new HashMap<>();
            for (AspectStack as : TagAspectRegistry.resolve(item)) {
                sum.merge(as.aspect(), as.amount(), Integer::sum);
            }

            // Layer B: recipe walk (sums input aspects with loss factor).
            if (level != null) {
                List<AspectStack> bestRecipe = bestRecipeDerivation(level, item, visiting, depth);
                for (AspectStack as : bestRecipe) {
                    sum.merge(as.aspect(), as.amount(), Integer::sum);
                }
            }

            // Layer C: item-class heuristic. Catches "leaf" items with no
            // recipe and no tag membership — mob drops, world-gen items,
            // unknown modded tools/armor/foods. Always runs; combines with
            // tag + recipe rather than overriding them, so e.g. an iron
            // sword gets metallum (tag) + sticks→herba (recipe) + telum
            // (heuristic) — feels right.
            for (AspectStack as : ItemClassHeuristic.infer(item)) {
                sum.merge(as.aspect(), as.amount(), Integer::sum);
            }

            if (sum.isEmpty()) return Collections.emptyList();
            // Cap to top-N largest aspects with per-aspect amount limit. Without
            // this, a recipe with several ingredients × tag layer × heuristic
            // would dump 8+ aspects (e.g. stripped_jungle_wood pulling
            // herba/arbor from tag, plus modded sawmill recipe ingredients
            // adding terra/ordo/aer/ignis/perditio/victus). 5 kinds × 6 max
            // is the readable ceiling.
            return sum.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(5)
                    .map(e -> new AspectStack(e.getKey(), Math.min(6, e.getValue())))
                    .collect(java.util.stream.Collectors.toList());
        } finally {
            visiting.remove(itemId);
        }
    }

    /** Look up recipes producing this item via the reverse index. Walking
     *  the full recipe registry every call is the lockup we got bitten by —
     *  this version uses the index built lazily on first call after reload. */
    private static List<AspectStack> bestRecipeDerivation(Level level,
                                                           net.minecraft.world.item.Item item,
                                                           Set<ResourceLocation> visiting,
                                                           int depth) {
        RecipeManager rm = level.getRecipeManager();
        RegistryAccess access = level.registryAccess();
        if (!indexBuilt) buildIndex(rm, access);

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return Collections.emptyList();
        List<Recipe<?>> recipes = RECIPES_BY_RESULT.get(itemId);
        if (recipes == null || recipes.isEmpty()) return Collections.emptyList();

        Map<Aspect, Integer> bestSum = null;
        int bestCost = Integer.MAX_VALUE;

        for (Recipe<?> recipe : recipes) {
            ItemStack result;
            try { result = recipe.getResultItem(access); }
            catch (Throwable t) { continue; }
            if (result == null || result.isEmpty() || result.getItem() != item) continue;

            Map<Aspect, Integer> ingSum = new HashMap<>();
            boolean hadAnyIngredient = false;
            for (Ingredient ing : recipe.getIngredients()) {
                if (ing.isEmpty()) continue;
                ItemStack[] choices = ing.getItems();
                if (choices.length == 0) continue;
                // Pick the FIRST viable choice rather than exploring all.
                // For an Ingredient like "any wooden plank" with 7 candidates,
                // the previous "try every choice and pick cheapest" path
                // recursed 7× per slot; with deep recipe chains that
                // multiplied into a 10k-recipe lockup. First-choice is good
                // enough — players rarely care that planks-from-spruce-log
                // gives identical aspects to planks-from-oak-log.
                ItemStack pick = choices[0];
                if (pick.isEmpty()) continue;
                ResourceLocation pickId = ForgeRegistries.ITEMS.getKey(pick.getItem());
                if (pickId == null) continue;
                List<AspectStack> chosen = lookupOrDerive(level, pickId, visiting, depth + 1);
                hadAnyIngredient = true;
                for (AspectStack as : chosen) {
                    ingSum.merge(as.aspect(), as.amount(), Integer::sum);
                }
            }
            if (!hadAnyIngredient) continue;

            // Apply loss factor (round down, but minimum 1 if pre-loss > 0).
            Map<Aspect, Integer> lossed = new HashMap<>(ingSum.size());
            for (var entry : ingSum.entrySet()) {
                int reduced = (int) Math.floor(entry.getValue() * LOSS_FACTOR);
                if (reduced < 1 && entry.getValue() > 0) reduced = 1;
                if (reduced > 0) lossed.put(entry.getKey(), reduced);
            }

            // Cooking bias: smelting/blasting/smoking/campfire add ignis.
            if (recipe instanceof AbstractCookingRecipe) {
                lossed.merge(Aspects.IGNIS, 1, Integer::sum);
            }

            // Result quantity divides aspects proportionally — a recipe that
            // produces 4 planks from 1 log gives each plank 1/4 of the log's
            // aspects (post-loss). Critical for fairness on bulk recipes.
            int resultCount = Math.max(1, result.getCount());
            if (resultCount > 1) {
                Map<Aspect, Integer> divided = new HashMap<>(lossed.size());
                for (var entry : lossed.entrySet()) {
                    int per = entry.getValue() / resultCount;
                    if (per < 1 && entry.getValue() > 0) per = 1;
                    if (per > 0) divided.put(entry.getKey(), per);
                }
                lossed = divided;
            }

            int cost = lossed.values().stream().mapToInt(Integer::intValue).sum();
            if (cost > 0 && cost < bestCost) {
                bestCost = cost;
                bestSum = lossed;
            }
        }

        if (bestSum == null) return Collections.emptyList();
        List<AspectStack> out = new ArrayList<>(bestSum.size());
        bestSum.forEach((a, amt) -> out.add(new AspectStack(a, amt)));
        return out;
    }

    /** Look up an ingredient's aspects: prefer the static AspectMap (which
     *  itself routes JSON → cache → us), but call into ourselves for items
     *  not yet derived. The recursion goes through AspectMap so JSON
     *  overrides on intermediate items are honoured. */
    private static List<AspectStack> lookupOrDerive(Level level, ResourceLocation id,
                                                     Set<ResourceLocation> visiting, int depth) {
        // Honour explicit JSON overrides immediately.
        var item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) return Collections.emptyList();
        List<AspectStack> jsonHit = AspectMap.forItemJsonOnly(item);
        if (!jsonHit.isEmpty()) return jsonHit;

        // Then check our cache.
        synchronized (CACHE) {
            List<AspectStack> cached = CACHE.get(id);
            if (cached != null && cached != IN_PROGRESS) return cached;
        }

        // Recurse into derivation.
        return deriveInternal(level, id, visiting, depth);
    }

    private static int totalCost(List<AspectStack> stacks) {
        int sum = 0;
        for (AspectStack as : stacks) sum += as.amount();
        return sum;
    }

    /** Diagnostic: log a summary of what got derived. */
    public static void logCacheSummary() {
        synchronized (CACHE) {
            ThaumicRemnants.LOG.info("RecipeAspectDeriver cache: {} items derived", CACHE.size());
        }
    }
}
