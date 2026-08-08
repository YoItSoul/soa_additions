package com.soul.soa_additions.smithery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.material.Material;
import com.soul.smithery.api.material.MaterialStats;
import com.soul.smithery.api.modifier.ModifierEffect;
import com.soul.smithery.api.part.PartType;
import com.soul.smithery.api.tool.ToolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Dumps every registered Smithery material and the whole of its stat block to
 * {@code soa_exports/smithery_materials.json}.
 *
 * <p>Replaces the {@code tinker_materials} export target, which was a TConstruct-era leftover: it
 * was still offered as a tab-completion but had no implementation behind it, so it silently
 * exported nothing. Tinkers is gone and Smithery replaces it, so the material ladder now lives
 * here.</p>
 *
 * <p>Everything {@link MaterialStats} exposes is written out, including the parts that only make
 * sense per part type or per tool type — modifier slot counts, and the four separate modifier
 * scopes (universal / head / part-scoped / tool-type-scoped) that decide which traits a composed
 * tool actually ends up with. Reading traits off the universal list alone under-reports most
 * materials, which is the mistake this export exists to make impossible.</p>
 *
 * <p>Every class named here belongs to Smithery, an optional dependency, so this class is only
 * ever touched behind a {@code ModList.isLoaded("smithery")} check in the export command.</p>
 */
public final class SmitheryMaterialExport {

    private SmitheryMaterialExport() {}

    /** Builds the material array, sorted by id so diffs between runs stay readable. */
    public static JsonArray dump() {
        List<Material> materials = new ArrayList<>(SmitheryAPI.MATERIALS.all());
        materials.sort(Comparator.comparing(m -> m.id().toString()));

        List<PartType> partTypes = new ArrayList<>(SmitheryAPI.PART_TYPES.all());
        partTypes.sort(Comparator.comparing(p -> p.id().toString()));

        List<ToolType> toolTypes = new ArrayList<>(SmitheryAPI.TOOL_TYPES.all());
        toolTypes.sort(Comparator.comparing(t -> t.id().toString()));

        JsonArray arr = new JsonArray();
        for (Material material : materials) {
            try {
                arr.add(dumpMaterial(material, partTypes, toolTypes));
            } catch (Throwable t) {
                // One malformed material shouldn't cost the whole export.
                JsonObject broken = new JsonObject();
                broken.addProperty("id", String.valueOf(material.id()));
                broken.addProperty("mod", material.id().getNamespace());
                broken.addProperty("error", String.valueOf(t));
                arr.add(broken);
            }
        }
        return arr;
    }

    private static JsonObject dumpMaterial(Material material, List<PartType> partTypes, List<ToolType> toolTypes) {
        ResourceLocation id = material.id();
        MaterialStats s = material.stats();

        JsonObject o = new JsonObject();
        o.addProperty("id", id.toString());
        // "mod" is what RegistryExportCommand#write counts by, so it has to be present.
        o.addProperty("mod", id.getNamespace());

        String nameKey = "smithery.material." + id.getNamespace() + "." + id.getPath();
        o.addProperty("name_key", nameKey);
        // Resolves on a client (or integrated server); on a dedicated server the key comes back
        // unchanged, which is why name_key is emitted alongside rather than instead.
        o.addProperty("name", Component.translatable(nameKey).getString());

        o.addProperty("harvest_level", s.harvestLevel());
        o.addProperty("mining_speed", s.miningSpeed());
        o.addProperty("attack_damage", s.attackDamage());
        o.addProperty("durability_per_ingot", s.durabilityPerIngot());
        o.addProperty("melting_temp", s.meltingTemp());
        o.addProperty("binder_multiplier", s.binderMultiplier());
        o.addProperty("cast_only", s.castOnly());
        o.addProperty("fluid_base", String.valueOf(s.fluidBase()));

        o.addProperty("molten_color", argb(s.moltenColor()));
        o.addProperty("part_color", argb(s.partColor()));
        o.addProperty("foil", s.foil());
        o.addProperty("emissive", s.emissive());
        if (s.hasColorCycle()) {
            JsonObject cycle = new JsonObject();
            cycle.addProperty("period_ticks", s.colorCyclePeriodTicks());
            JsonArray colors = new JsonArray();
            for (int c : s.colorCycle()) colors.add(argb(c));
            cycle.add("colors", colors);
            o.add("color_cycle", cycle);
        }

        // Modifier slots vary per part type; only non-zero entries are worth writing.
        JsonObject slots = new JsonObject();
        for (PartType pt : partTypes) {
            int n = s.modifierSlotsFor(pt);
            if (n > 0) slots.addProperty(pt.id().toString(), n);
        }
        o.add("modifier_slots", slots);

        o.addProperty("supports_armor", s.supportsArmor());
        MaterialStats.ArmorStats armor = s.armorStats();
        if (armor != null) {
            JsonObject a = new JsonObject();
            a.addProperty("core_durability", armor.coreDurability());
            a.addProperty("core_defense", armor.coreDefense());
            a.addProperty("plates_durability", armor.platesDurability());
            a.addProperty("plates_modifier", armor.platesModifier());
            a.addProperty("plates_toughness", armor.platesToughness());
            a.addProperty("trim_durability", armor.trimDurability());
            o.add("armor", a);
        }

        o.addProperty("supports_bow", s.supportsBow());
        MaterialStats.RangedStats ranged = s.rangedStats();
        if (ranged != null) {
            JsonObject r = new JsonObject();
            r.addProperty("draw_speed", ranged.drawSpeed());
            r.addProperty("range", ranged.range());
            r.addProperty("bonus_damage", ranged.bonusDamage());
            r.addProperty("bowstring", ranged.bowstring());
            r.addProperty("shaft_modifier", ranged.shaftModifier());
            r.addProperty("bonus_ammo", ranged.bonusAmmo());
            r.addProperty("accuracy", ranged.accuracy());
            r.addProperty("fletching_modifier", ranged.fletchingModifier());
            o.add("ranged", r);
        }

        // Four scopes, kept separate: which one a modifier sits in decides whether a composed tool
        // gets it from any part, only from the head, only from one part type, or only in one tool.
        JsonObject modifiers = new JsonObject();
        modifiers.add("universal", effects(s.universalModifiers()));
        modifiers.add("head", effects(s.headModifiers()));

        JsonObject byPart = new JsonObject();
        for (PartType pt : partTypes) {
            List<ModifierEffect> list = s.partModifiers(pt);
            if (list != null && !list.isEmpty()) byPart.add(pt.id().toString(), effects(list));
        }
        modifiers.add("by_part", byPart);

        JsonObject byTool = new JsonObject();
        for (ToolType tt : toolTypes) {
            List<ModifierEffect> list = s.toolTypeModifiers(tt);
            if (list != null && !list.isEmpty()) byTool.add(tt.id().toString(), effects(list));
        }
        modifiers.add("by_tool_type", byTool);
        o.add("modifiers", modifiers);

        return o;
    }

    private static JsonArray effects(List<ModifierEffect> list) {
        JsonArray arr = new JsonArray();
        if (list == null) return arr;
        for (ModifierEffect e : list) {
            JsonObject o = new JsonObject();
            o.addProperty("id", e.modifierId().toString());
            Map<String, Object> params = e.params();
            if (params != null && !params.isEmpty()) {
                JsonObject p = new JsonObject();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    Object v = entry.getValue();
                    if (v instanceof Number n) p.addProperty(entry.getKey(), n);
                    else if (v instanceof Boolean b) p.addProperty(entry.getKey(), b);
                    else p.addProperty(entry.getKey(), String.valueOf(v));
                }
                o.add("params", p);
            }
            arr.add(o);
        }
        return arr;
    }

    /** Colours as 8-digit ARGB hex — readable in a diff, unlike the signed ints they're stored as. */
    private static String argb(int color) {
        return String.format("%08X", color);
    }
}
