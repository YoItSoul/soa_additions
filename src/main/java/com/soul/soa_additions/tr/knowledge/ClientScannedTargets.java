package com.soul.soa_additions.tr.knowledge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side mirror of the player's scanned targets. Read by the Monocle
 * HUD, item tooltip aspect lines, and any other UI that needs to gate
 * aspect display on prior scans.
 *
 * <p>Optimised for per-frame access — each {@code has(Block)} call is one
 * registry-key lookup (cached internally in BuiltInRegistries) plus one
 * HashSet contains-check, no allocations.
 */
public final class ClientScannedTargets {

    private static final Set<ResourceLocation> BLOCKS   = new HashSet<>();
    private static final Set<ResourceLocation> ITEMS    = new HashSet<>();
    private static final Set<ResourceLocation> ENTITIES = new HashSet<>();

    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    private ClientScannedTargets() {}

    /** Replace all three sets with the supplied snapshots. Called by the sync
     *  packet handler on the main thread. Notifies all listeners afterward. */
    public static void replace(Set<ResourceLocation> blocks,
                                Set<ResourceLocation> items,
                                Set<ResourceLocation> entities) {
        BLOCKS.clear();
        BLOCKS.addAll(blocks);
        ITEMS.clear();
        ITEMS.addAll(items);
        ENTITIES.clear();
        ENTITIES.addAll(entities);
        for (Runnable l : LISTENERS) {
            try { l.run(); } catch (Throwable ignored) {}
        }
    }

    // ---- Fast read path (per-frame safe) ----

    public static boolean has(Block block) {
        if (block == null) return false;
        return BLOCKS.contains(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static boolean has(Item item) {
        if (item == null) return false;
        return ITEMS.contains(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean has(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return ITEMS.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean hasBlock(ResourceLocation id)  { return BLOCKS.contains(id); }
    public static boolean hasItem(ResourceLocation id)   { return ITEMS.contains(id); }
    public static boolean hasEntity(ResourceLocation id) { return ENTITIES.contains(id); }

    public static boolean has(EntityType<?> type) {
        if (type == null) return false;
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return key != null && ENTITIES.contains(key);
    }

    public static int blockCount()  { return BLOCKS.size(); }
    public static int itemCount()   { return ITEMS.size(); }
    public static int entityCount() { return ENTITIES.size(); }

    public static Set<ResourceLocation> blockSnapshot()  { return Collections.unmodifiableSet(new HashSet<>(BLOCKS)); }
    public static Set<ResourceLocation> itemSnapshot()   { return Collections.unmodifiableSet(new HashSet<>(ITEMS)); }
    public static Set<ResourceLocation> entitySnapshot() { return Collections.unmodifiableSet(new HashSet<>(ENTITIES)); }

    public static void addListener(Runnable r) { LISTENERS.add(r); }
}
