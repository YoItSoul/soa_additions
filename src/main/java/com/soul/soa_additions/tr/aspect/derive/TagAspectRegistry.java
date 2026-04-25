package com.soul.soa_additions.tr.aspect.derive;

import com.soul.soa_additions.tr.core.AspectStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tag → aspect bulk lookup. Lets a single JSON file ({@code data/tr/tr_aspects/tags/forge/ingots/iron.json})
 * assign aspects to every item in that tag, including modded items the
 * modpack maintainer has never heard of. Replaces the OreDictionary-based
 * bulk-tagging that Thaumcraft 4/6 used in 1.7.10/1.12.2; semantically
 * equivalent but driven by Forge's modern tag system.
 *
 * <p>Loaded by {@link com.soul.soa_additions.tr.data.AspectMapLoader}'s
 * tag listener. Storage is a flat {@code Map<ResourceLocation, List<AspectStack>>}
 * keyed by the tag's resource location. Per-item resolution is O(tags-on-item)
 * which is bounded by Forge tag membership (typically &lt;5).
 */
public final class TagAspectRegistry {

    private static final Map<ResourceLocation, List<AspectStack>> ENTRIES = new HashMap<>();

    private TagAspectRegistry() {}

    public static void put(ResourceLocation tagId, List<AspectStack> stacks) {
        ENTRIES.put(tagId, List.copyOf(stacks));
    }

    public static void clear() { ENTRIES.clear(); }

    public static int size() { return ENTRIES.size(); }

    /** Sum aspects across every tag the item belongs to that has an entry.
     *  Returns empty if no matching tag has an aspect mapping.
     *
     *  <p>Iterates OUR registered tags rather than the item's tag set — this
     *  is critical for hierarchical tag resolution. {@code holder.tags()}
     *  is supposed to include parent-tag membership (acacia_log →
     *  minecraft:acacia_logs → minecraft:logs) but in practice the
     *  reverse-direction lookup via {@code item.builtInRegistryHolder().is(tag)}
     *  is more reliable across Forge versions and respects proper tag
     *  hierarchy resolution. */
    public static List<AspectStack> resolve(Item item) {
        if (item == null || ENTRIES.isEmpty()) return Collections.emptyList();
        var itemHolder = item.builtInRegistryHolder();
        // Bridge to block-tags for BlockItems — many semantic tags
        // (minecraft:dragon_immune, minecraft:beacon_base_blocks,
        // minecraft:wither_immune, minecraft:logs, etc.) are BLOCK tags
        // with no parallel item tag. Without this bridge, e.g. obsidian
        // wouldn't pick up its dragon_immune→tutamen/tenebrae assignment
        // because the dragon_immune item-tag check returns false.
        var blockHolder = (item instanceof net.minecraft.world.item.BlockItem bi)
                ? bi.getBlock().builtInRegistryHolder()
                : null;

        Map<com.soul.soa_additions.tr.core.Aspect, Integer> sum = new HashMap<>();
        for (Map.Entry<ResourceLocation, List<AspectStack>> entry : ENTRIES.entrySet()) {
            ResourceLocation tagId = entry.getKey();
            boolean matched = false;
            TagKey<Item> itemTag = TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagId);
            if (itemHolder.is(itemTag)) {
                matched = true;
            } else if (blockHolder != null) {
                TagKey<net.minecraft.world.level.block.Block> blockTag =
                        TagKey.create(net.minecraft.core.registries.Registries.BLOCK, tagId);
                if (blockHolder.is(blockTag)) matched = true;
            }
            if (matched) {
                for (AspectStack as : entry.getValue()) {
                    sum.merge(as.aspect(), as.amount(), Integer::sum);
                }
            }
        }
        if (sum.isEmpty()) return Collections.emptyList();
        List<AspectStack> out = new ArrayList<>(sum.size());
        sum.forEach((aspect, amt) -> out.add(new AspectStack(aspect, amt)));
        return out;
    }

    /** Direct check used by the recipe deriver: does this exact tag have an entry? */
    public static List<AspectStack> get(ResourceLocation tagId) {
        return ENTRIES.getOrDefault(tagId, Collections.emptyList());
    }

    /** Same but for a TagKey. */
    public static List<AspectStack> get(TagKey<Item> tag) {
        return get(tag.location());
    }
}
