package com.soul.soa_additions.tconstructevo.export;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraft.world.item.Tier;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

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

    public static JsonArray dump() {
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

        for (IMaterial m : mats) {
            MaterialId id = m.getIdentifier();
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            o.addProperty("tier", m.getTier());
            o.addProperty("hidden", m.isHidden());
            o.addProperty("craftable", m.isCraftable());
            o.addProperty("sort_order", m.getSortOrder());

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
