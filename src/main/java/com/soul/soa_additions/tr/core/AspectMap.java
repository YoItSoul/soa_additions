package com.soul.soa_additions.tr.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime registry of item/block aspect compositions. Read by the tooltip
 * renderer and the eventual Monocle HUD; written either by the bundled
 * {@link AspectMapDefaults} bootstrap or (later) by a datapack reload listener
 * for {@code data/tr/aspects/items/*.json} pack overrides.
 *
 * <p>Storage is keyed by registry id rather than by Item/Block instance so
 * that pack overrides can target unloaded items without holding hard
 * references. Lookup wraps the id resolution so callers don't have to.
 *
 * <p>Lookups are O(1). Per-frame safe.
 */
public final class AspectMap {

    private static final Map<ResourceLocation, List<AspectStack>> ITEM_ASPECTS = new HashMap<>();
    private static final Map<ResourceLocation, List<AspectStack>> BLOCK_ASPECTS = new HashMap<>();

    private static final List<AspectStack> EMPTY = Collections.emptyList();

    private AspectMap() {}

    // ---------------- Mutators ----------------

    /** Replace whatever composition is registered for this item id with the
     *  supplied list. {@code stacks} is defensively copied; pass an empty
     *  list to clear the entry (or use {@link #removeItem}). */
    public static void putItem(ResourceLocation itemId, List<AspectStack> stacks) {
        ITEM_ASPECTS.put(itemId, List.copyOf(stacks));
    }

    public static void putItem(Item item, AspectStack... stacks) {
        if (item == null) return;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        ITEM_ASPECTS.put(id, List.of(stacks));
    }

    public static void putItem(ResourceLocation itemId, AspectStack... stacks) {
        ITEM_ASPECTS.put(itemId, List.of(stacks));
    }

    public static void putBlock(ResourceLocation blockId, List<AspectStack> stacks) {
        BLOCK_ASPECTS.put(blockId, List.copyOf(stacks));
    }

    public static void putBlock(Block block, AspectStack... stacks) {
        if (block == null) return;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        BLOCK_ASPECTS.put(id, List.of(stacks));
    }

    public static void putBlock(ResourceLocation blockId, AspectStack... stacks) {
        BLOCK_ASPECTS.put(blockId, List.of(stacks));
    }

    public static void removeItem(ResourceLocation itemId) { ITEM_ASPECTS.remove(itemId); }
    public static void removeBlock(ResourceLocation blockId) { BLOCK_ASPECTS.remove(blockId); }

    public static void clear() {
        ITEM_ASPECTS.clear();
        BLOCK_ASPECTS.clear();
    }

    public static int itemCountDiag()  { return ITEM_ASPECTS.size(); }
    public static int blockCountDiag() { return BLOCK_ASPECTS.size(); }

    // ---------------- Read path ----------------
    //
    // Three-layer resolution chain (TC6-style + Forge tags):
    //   1. JSON override          (data/<ns>/tr_aspects/item/<path>.json)
    //   2. Tag-based bulk lookup  (TagAspectRegistry — forge:ingots/iron etc.)
    //   3. Recipe-walk derivation (RecipeAspectDeriver — sums input aspects
    //                              with loss factor; cooking adds ignis;
    //                              cached after first derivation)
    // forItem/forBlock walk all three; forItemJsonOnly skips 2 and 3
    // (used by the deriver to honour overrides without re-entering).

    /** Layer-1-only lookup: explicit JSON entry, no fallback. Used by the
     *  recipe deriver so an ingredient with a JSON override stops the
     *  derivation recursion at that override. */
    public static List<AspectStack> forItemJsonOnly(@Nullable Item item) {
        if (item == null) return EMPTY;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return ITEM_ASPECTS.getOrDefault(id, EMPTY);
    }

    /** Full chain. {@code level} provides RecipeManager access for derivation;
     *  pass null to skip the recipe layer (returns JSON or tag-only result). */
    public static List<AspectStack> forItem(@Nullable Item item, @Nullable net.minecraft.world.level.Level level) {
        if (item == null) return EMPTY;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        // Layer 1
        List<AspectStack> jsonHit = ITEM_ASPECTS.get(id);
        if (jsonHit != null && !jsonHit.isEmpty()) return jsonHit;
        // Layers 2 + 3 via the deriver (it combines tag + recipe)
        return com.soul.soa_additions.tr.aspect.derive.RecipeAspectDeriver.derive(level, id);
    }

    /** Convenience wrapper that pulls the level from the current minecraft
     *  context — works on either side. Use this from places that can't
     *  reasonably plumb a Level reference (tooltip handlers, JEI providers). */
    public static List<AspectStack> forItem(@Nullable Item item) {
        return forItem(item, currentLevel());
    }

    public static List<AspectStack> forItem(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return EMPTY;
        // NBT-blind for now — see prior comment on tools-with-modifiers.
        return forItem(stack.getItem());
    }

    public static List<AspectStack> forBlock(@Nullable Block block) {
        if (block == null) return EMPTY;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        // Block-specific JSON entry first.
        List<AspectStack> blk = BLOCK_ASPECTS.get(id);
        if (blk != null && !blk.isEmpty()) return blk;
        // Then the block's item form (most blocks share the id with their BlockItem).
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item != null && item != net.minecraft.world.item.Items.AIR) {
            return forItem(item);
        }
        return EMPTY;
    }

    /** Best-effort current Level lookup. Server thread → integrated/dedicated
     *  server overworld; client thread → ClientLevel. Returns null off-thread
     *  or before world load.
     *
     *  <p>The client branch is delegated to {@link ClientAccess} — a nested
     *  class loaded only on Dist.CLIENT. Any direct reference to
     *  {@code net.minecraft.client.Minecraft} (whose {@code level} field is
     *  typed {@code ClientLevel}) from this method's bytecode would put a
     *  client-only type in {@link AspectMap}'s constant pool, which fails to
     *  resolve on a dedicated server when this class is loaded — taking down
     *  every datapack reload listener that touches AspectMap with it. */
    @Nullable
    private static net.minecraft.world.level.Level currentLevel() {
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist
                == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            try {
                var lvl = ClientAccess.level();
                if (lvl != null) return lvl;
            } catch (Throwable ignored) {}
        }
        try {
            // Server-side: any loaded server level works — recipes are global.
            var srv = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (srv != null) {
                var ow = srv.overworld();
                if (ow != null) return ow;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /** Nested holder for client-only level access. Mirrors the pattern in
     *  {@code ClientIdentity.ClientAccess}. The JVM only loads this nested
     *  class when {@link #level()} is first invoked, which is gated by a
     *  {@code Dist.CLIENT} check in {@link #currentLevel()}, so the dedicated
     *  server never resolves {@code net.minecraft.client.Minecraft} or
     *  {@code ClientLevel}. */
    private static final class ClientAccess {
        @Nullable
        static net.minecraft.world.level.Level level() {
            var mc = net.minecraft.client.Minecraft.getInstance();
            return mc != null ? mc.level : null;
        }
    }

    /** Filter a composition down to the aspects the predicate accepts.
     *  Returns the original list if every stack passes (no allocation). */
    public static List<AspectStack> filter(List<AspectStack> in,
                                            java.util.function.Predicate<Aspect> include) {
        if (in.isEmpty()) return in;
        List<AspectStack> out = null;
        for (int i = 0; i < in.size(); i++) {
            AspectStack as = in.get(i);
            if (include.test(as.aspect())) {
                if (out != null) out.add(as);
            } else if (out == null) {
                // First rejection — copy what we accepted so far.
                out = new ArrayList<>(in.size());
                for (int j = 0; j < i; j++) out.add(in.get(j));
            }
        }
        return out == null ? in : out;
    }
}
