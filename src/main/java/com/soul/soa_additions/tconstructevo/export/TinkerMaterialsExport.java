package com.soul.soa_additions.tconstructevo.export;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Recipe;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Exports every registered Tinkers' Construct material (visible + hidden) into
 * a reviewable JSON blob. Used by {@code /soa export tinker_materials} so the
 * user can audit which materials are missing stats, traits, harvest tier, or
 * binding stats — the export surfaces gaps that the runtime tooltip doesn't.
 *
 * <p>Split into its own class so {@link com.soul.soa_additions.export.RegistryExportCommand}
 * stays loadable without TConstruct present; this class is only touched after
 * a runtime {@code ModList.isLoaded("tconstruct")} gate.
 */
public final class TinkerMaterialsExport {

    private TinkerMaterialsExport() {}

    /**
     * Scans the loaded tconstruct:material recipe registry for each material's
     * canonical obtainable item. Prefers the recipe whose needed/value pair is
     * closest to 1:1 (the natural single-unit form like an ingot or gemstone).
     * Compressed forms (block: needed=1 value=9) and sub-unit forms (nugget:
     * needed=9 value=1) score worse because they're not the natural quest
     * target. After picking the best recipe, item form within that recipe is
     * further ranked via {@link #scoreItemForm} so that mods listing both an
     * ingot and a block in the same display set still prefer the ingot.
     *
     * <p>Returns empty map if server or recipe manager is unavailable.
     */
    private static Map<MaterialId, ItemStack> collectPrimaryItems(MinecraftServer server) {
        Map<MaterialId, ItemStack> out = new HashMap<>();
        if (server == null) return out;
        // Track each material's best score so far. Lower score = preferred recipe.
        Map<MaterialId, Integer> bestScore = new HashMap<>();
        try {
            var recipes = server.getRecipeManager().getRecipes();
            for (Recipe<?> r : recipes) {
                if (!(r instanceof MaterialRecipe mr)) continue;
                MaterialId mid = mr.getMaterial().getId();
                int needed = mr.getNeeded();
                int value = mr.getValue();
                if (needed <= 0 || value <= 0) continue;
                // Distance from 1:1 — ingot (1,1)=0, block (1,9)=8, nugget (9,1)=8.
                int recipeScore = Math.abs(value - 1) + Math.abs(needed - 1);
                List<ItemStack> display = mr.getDisplayItems();
                if (display == null || display.isEmpty()) continue;
                // Pick the best item form within this recipe's display set.
                ItemStack best = null;
                int bestItemScore = Integer.MAX_VALUE;
                for (ItemStack candidate : display) {
                    if (candidate == null || candidate.isEmpty()) continue;
                    ResourceLocation rl = BuiltInRegistries.ITEM.getKey(candidate.getItem());
                    int s = rl == null ? 50 : scoreItemForm(rl.getPath());
                    if (s < bestItemScore) {
                        bestItemScore = s;
                        best = candidate;
                    }
                }
                if (best == null) continue;
                int totalScore = recipeScore * 100 + bestItemScore;
                Integer prev = bestScore.get(mid);
                if (prev != null && prev <= totalScore) continue;
                bestScore.put(mid, totalScore);
                out.put(mid, best.copy());
            }
        } catch (Throwable t) {
            TConstructEvoPlugin.LOG.warn("Failed to collect material primary items: {}", t.toString());
        }
        return out;
    }

    /**
     * Resolve a material's canonical item via the soa_additions convention:
     * an item tag at {@code <modid>:tinker_materials/<material_path>}. The tag
     * commonly lists multiple forms of the material (ingot, block, nugget,
     * gemstone, etc.); registry iteration doesn't guarantee file-order, so we
     * RANK the candidates and return the most-quest-friendly form: the
     * "single unit" representation (ingot / gem / planks / stick / base item)
     * is preferred over compressed (block) or sub-unit (nugget) forms.
     *
     * <p>Returns {@code ItemStack.EMPTY} if no such tag exists or it's empty.
     */
    private static ItemStack resolveTagFallback(MaterialId materialId) {
        String namespace = materialId.getNamespace();
        String path = materialId.getPath();
        ResourceLocation tagId = new ResourceLocation(namespace, "tinker_materials/" + path);
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        Optional<HolderSet.Named<Item>> set = BuiltInRegistries.ITEM.getTag(tagKey);
        if (set.isEmpty()) return ItemStack.EMPTY;

        Item best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Holder<Item> holder : set.get()) {
            Item item;
            try { item = holder.value(); } catch (Throwable t) { continue; }
            if (item == null) continue;
            ResourceLocation rl = BuiltInRegistries.ITEM.getKey(item);
            if (rl == null) continue;
            int score = scoreItemForm(rl.getPath());
            if (score < bestScore) {
                bestScore = score;
                best = item;
            }
        }
        return best == null ? ItemStack.EMPTY : new ItemStack(best);
    }

    /** Lower score = preferred form for use as a quest task input. */
    private static int scoreItemForm(String path) {
        if (path.endsWith("_ingot")) return 0;
        if (path.endsWith("_gemstone")) return 1;
        if (path.endsWith("_gem")) return 2;
        if (path.endsWith("_shard")) return 3;
        if (path.endsWith("_stick")) return 4;
        if (path.endsWith("_petal")) return 5;
        if (path.endsWith("_planks")) return 6;
        if (path.endsWith("_log")) return 8;
        if (path.endsWith("_nugget")) return 90;
        if (path.endsWith("_block")) return 99;
        return 50; // base name / unknown form
    }

    public static JsonArray dump(MinecraftServer server) {
        JsonArray arr = new JsonArray();
        IMaterialRegistry reg;
        try {
            reg = MaterialRegistry.getInstance();
        } catch (Throwable t) {
            TConstructEvoPlugin.LOG.warn("MaterialRegistry not available for export: {}", t.toString());
            return arr;
        }
        List<IMaterial> mats = new ArrayList<>(reg.getAllMaterials());
        mats.sort(Comparator.comparing(m -> m.getIdentifier().toString()));

        // Build material → primary canonical item map by scanning loaded
        // tconstruct:material recipes. The "primary" item is the highest-value
        // recipe form (typically the ingot at value=1, needed=1) — usually the
        // first display item of that recipe.
        Map<MaterialId, ItemStack> primaryItems = collectPrimaryItems(server);

        for (IMaterial m : mats) {
            MaterialId id = m.getIdentifier();
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            o.addProperty("tier", m.getTier());
            o.addProperty("hidden", m.isHidden());
            o.addProperty("craftable", m.isCraftable());
            o.addProperty("sort_order", m.getSortOrder());

            // Primary obtainable item: prefer the MaterialRecipe scan, fall
            // back to the convention `<modid>:tinker_materials/<material>` item
            // tag (used by soa_additions for materials that don't ship a
            // dedicated MaterialRecipe — only a tag-based item mapping).
            ItemStack primary = primaryItems.get(id);
            String primarySource = primary != null && !primary.isEmpty() ? "material_recipe" : null;
            if (primary == null || primary.isEmpty()) {
                primary = resolveTagFallback(id);
                if (primary != null && !primary.isEmpty()) {
                    primarySource = "tinker_materials_tag";
                }
            }
            if (primary != null && !primary.isEmpty()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(primary.getItem());
                if (itemId != null) {
                    JsonObject pj = new JsonObject();
                    pj.addProperty("id", itemId.toString());
                    pj.addProperty("count", primary.getCount());
                    if (primarySource != null) pj.addProperty("source", primarySource);
                    try { pj.addProperty("name", primary.getHoverName().getString()); } catch (Throwable ignored) {}
                    o.add("primary_item", pj);
                }
            }

            // Stats per stat type
            JsonObject statsJson = new JsonObject();
            Collection<IMaterialStats> stats;
            try {
                stats = reg.getAllStats(id);
            } catch (Throwable t) {
                stats = List.of();
            }
            List<IMaterialStats> sorted = new ArrayList<>(stats);
            sorted.sort(Comparator.comparing(s -> s.getIdentifier().toString()));
            for (IMaterialStats s : sorted) {
                MaterialStatsId statId = s.getIdentifier();
                JsonObject statJson = dumpStat(s);
                // Traits specific to this stat type
                List<ModifierEntry> traits;
                try {
                    traits = reg.getTraits(id, statId);
                } catch (Throwable t) {
                    traits = List.of();
                }
                statJson.add("traits", dumpTraits(traits));
                statJson.addProperty("has_unique_traits", reg.hasUniqueTraits(id, statId));
                statsJson.add(statId.toString(), statJson);
            }
            o.add("stats", statsJson);

            // Default traits (fallback used when a stat type has no traits of its own).
            List<ModifierEntry> defaults;
            try {
                defaults = reg.getDefaultTraits(id);
            } catch (Throwable t) {
                defaults = List.of();
            }
            o.add("default_traits", dumpTraits(defaults));

            // Flag gaps for quick scanning.
            JsonArray gaps = new JsonArray();
            if (stats.isEmpty()) gaps.add("no_stats");
            if (defaults.isEmpty() && stats.stream().allMatch(s -> {
                try { return reg.getTraits(id, s.getIdentifier()).isEmpty(); }
                catch (Throwable t) { return true; }
            })) gaps.add("no_traits_anywhere");
            boolean hasHead = stats.stream().anyMatch(s -> s.getIdentifier().toString().equals("tconstruct:head"));
            boolean hasHandle = stats.stream().anyMatch(s -> s.getIdentifier().toString().equals("tconstruct:handle"));
            boolean hasBinding = stats.stream().anyMatch(s -> s.getIdentifier().toString().equals("tconstruct:extra"));
            if (!hasHead) gaps.add("missing_head");
            if (!hasHandle) gaps.add("missing_handle");
            if (!hasBinding) gaps.add("missing_extra_binding");
            if (gaps.size() > 0) o.add("gaps", gaps);

            arr.add(o);
        }
        return arr;
    }

    /** Pull every record component on a stat via reflection so we don't have to
     *  hardcode HeadMaterialStats / HandleMaterialStats / Grip / Limb / Plating
     *  / StatlessMaterialStats / third-party stat types individually. */
    private static JsonObject dumpStat(IMaterialStats s) {
        JsonObject o = new JsonObject();
        o.addProperty("class", s.getClass().getName());
        o.addProperty("stat_type", s.getType() != null ? s.getType().getId().toString() : "?");
        try {
            o.addProperty("localized_name", s.getLocalizedName().getString());
        } catch (Throwable ignored) {}

        Class<?> c = s.getClass();
        if (c.isRecord()) {
            for (var comp : c.getRecordComponents()) {
                try {
                    Object v = comp.getAccessor().invoke(s);
                    putValue(o, comp.getName(), v);
                } catch (Throwable ignored) {}
            }
        } else {
            // Enum (StatlessMaterialStats) or plain class — walk declared fields.
            if (c.isEnum()) {
                try { o.addProperty("enum", ((Enum<?>) s).name()); } catch (Throwable ignored) {}
            }
            for (Field f : c.getDeclaredFields()) {
                int mods = f.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(mods)) continue;
                f.setAccessible(true);
                try {
                    putValue(o, f.getName(), f.get(s));
                } catch (Throwable ignored) {}
            }
        }

        // Localized info bullets (human-readable lines the tooltip would show).
        try {
            JsonArray info = new JsonArray();
            for (var line : s.getLocalizedInfo()) info.add(line.getString());
            if (info.size() > 0) o.add("localized_info", info);
        } catch (Throwable ignored) {}
        return o;
    }

    private static void putValue(JsonObject o, String key, Object v) {
        if (v == null) { o.add(key, null); return; }
        if (v instanceof Number n) { o.addProperty(key, n); return; }
        if (v instanceof Boolean b) { o.addProperty(key, b); return; }
        if (v instanceof Character ch) { o.addProperty(key, ch); return; }
        if (v instanceof Tier t) {
            JsonObject tj = new JsonObject();
            tj.addProperty("level", t.getLevel());
            tj.addProperty("uses", t.getUses());
            tj.addProperty("speed", t.getSpeed());
            tj.addProperty("attack_bonus", t.getAttackDamageBonus());
            tj.addProperty("enchantment_value", t.getEnchantmentValue());
            try {
                var tag = t.getTag();
                if (tag != null) tj.addProperty("tool_level_tag", tag.location().toString());
            } catch (Throwable ignored) {}
            o.add(key, tj);
            return;
        }
        o.addProperty(key, v.toString());
    }

    private static JsonArray dumpTraits(List<ModifierEntry> traits) {
        JsonArray arr = new JsonArray();
        for (ModifierEntry entry : traits) {
            JsonObject t = new JsonObject();
            try {
                Modifier mod = entry.getModifier();
                t.addProperty("id", entry.getId().toString());
                t.addProperty("level", entry.getLevel());
                try { t.addProperty("name", mod.getDisplayName().getString()); } catch (Throwable ignored) {}
                try { t.addProperty("description", mod.getDescription().getString()); } catch (Throwable ignored) {}
            } catch (Throwable ignored) {}
            arr.add(t);
        }
        return arr;
    }
}
