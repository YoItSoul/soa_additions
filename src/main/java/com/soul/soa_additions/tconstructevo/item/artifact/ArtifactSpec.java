package com.soul.soa_additions.tconstructevo.item.artifact;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parsed-from-JSON form of a tconevo artifact. Mirrors the spec used by
 * {@code xyz.phanta.tconevo.artifact.type.ArtifactTypeTool$Spec} from the
 * 1.12.2 jar so the original 14 shipped artifact JSONs (and any user-authored
 * ones) parse without modification.
 *
 * <p>JSON shape:
 * <pre>{@code
 * {
 *   "type":      "tconevo:tool" | "tconevo:armour",
 *   "weight":    400,                 // loot weight (optional, default 1)
 *   "name":      "Agneyastra",
 *   "lore":      "single line"        // or ["multi", "line"]
 *   "tool":      "scythe",            // tool definition id (without namespace = tconstruct)
 *   "materials": ["kyronite", ...],
 *   "free_mods": 3,                   // optional; default 0
 *   "mods": [                         // optional; default empty
 *     "fiery",                        // shorthand: level 1
 *     {"id": "fiery", "level": 75}
 *   ]
 * }
 * }</pre>
 *
 * <p>The {@code data_tag} arbitrary-NBT block from 1.12.2 is intentionally
 * dropped — none of the shipped artifacts use it and no public users have
 * been observed in the wild.</p>
 */
public final class ArtifactSpec {

    public final String type;
    public final int weight;
    public final String name;
    public final List<String> lore;
    public final String tool;
    public final List<String> materials;
    public final int freeMods;
    public final List<ModEntry> modifiers;

    public ArtifactSpec(String type, int weight, String name, List<String> lore,
                        String tool, List<String> materials, int freeMods,
                        List<ModEntry> modifiers) {
        this.type = type;
        this.weight = weight;
        this.name = name;
        this.lore = Collections.unmodifiableList(lore);
        this.tool = tool;
        this.materials = Collections.unmodifiableList(materials);
        this.freeMods = freeMods;
        this.modifiers = Collections.unmodifiableList(modifiers);
    }

    public static ArtifactSpec parse(JsonObject dto) {
        String type = GsonHelper.getAsString(dto, "type", "tconevo:tool");
        int weight = GsonHelper.getAsInt(dto, "weight", 1);
        String name = GsonHelper.getAsString(dto, "name");
        List<String> lore = parseLore(dto);
        String tool = parseToolField(dto, type);
        List<String> materials = parseMaterials(dto);
        int freeMods = GsonHelper.getAsInt(dto, "free_mods", 0);
        List<ModEntry> modifiers = parseModifiers(dto);
        return new ArtifactSpec(type, weight, name, lore, tool, materials, freeMods, modifiers);
    }

    /** Tool-type artifacts use a "tool" field naming a TC3 tool definition.
     *  Armour-type artifacts use an "armour" field naming a slot
     *  ({@code boots}/{@code helmet}/{@code chestplate}/{@code leggings}); we
     *  map that to TC3's plate armor item id. The dual schema mirrors the
     *  original 1.12.2 tconevo's split between ArtifactTypeTool and
     *  ArtifactTypeArmour, so existing JSON parses unchanged. */
    private static String parseToolField(JsonObject dto, String type) {
        if ("tconevo:armour".equals(type) || "tconevo:armor".equals(type)) {
            String slot = GsonHelper.getAsString(dto, "armour", null);
            if (slot == null) slot = GsonHelper.getAsString(dto, "armor", null);
            if (slot == null) {
                throw new JsonSyntaxException("armour-type artifact missing \"armour\" field");
            }
            return switch (slot) {
                case "boots"      -> "tconstruct:plate_boots";
                case "helmet"     -> "tconstruct:plate_helmet";
                case "chestplate" -> "tconstruct:plate_chestplate";
                case "leggings"   -> "tconstruct:plate_leggings";
                default -> throw new JsonSyntaxException(
                        "unknown armour slot \"" + slot + "\" (expected boots/helmet/chestplate/leggings)");
            };
        }
        return GsonHelper.getAsString(dto, "tool");
    }

    private static List<String> parseLore(JsonObject dto) {
        if (!dto.has("lore")) return Collections.emptyList();
        JsonElement el = dto.get("lore");
        if (el.isJsonArray()) {
            List<String> out = new ArrayList<>();
            for (JsonElement line : el.getAsJsonArray()) {
                if (!line.isJsonPrimitive() || !line.getAsJsonPrimitive().isString()) {
                    throw new JsonSyntaxException("lore entries must be strings");
                }
                out.add(line.getAsString());
            }
            return out;
        }
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            return Collections.singletonList(el.getAsString());
        }
        throw new JsonSyntaxException("\"lore\" must be a string or string array");
    }

    private static List<String> parseMaterials(JsonObject dto) {
        JsonArray arr = GsonHelper.getAsJsonArray(dto, "materials");
        List<String> out = new ArrayList<>();
        for (JsonElement el : arr) {
            if (!el.isJsonPrimitive() || !el.getAsJsonPrimitive().isString()) {
                throw new JsonSyntaxException("\"materials\" entries must be strings");
            }
            out.add(el.getAsString());
        }
        return out;
    }

    private static List<ModEntry> parseModifiers(JsonObject dto) {
        if (!dto.has("mods")) return Collections.emptyList();
        JsonArray arr = GsonHelper.getAsJsonArray(dto, "mods");
        List<ModEntry> out = new ArrayList<>();
        for (JsonElement el : arr) {
            if (el.isJsonObject()) {
                JsonObject obj = el.getAsJsonObject();
                out.add(new ModEntry(GsonHelper.getAsString(obj, "id"),
                        GsonHelper.getAsInt(obj, "level", 1)));
            } else if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                out.add(new ModEntry(el.getAsString(), 1));
            } else {
                throw new JsonSyntaxException("\"mods\" entries must be strings or {id, level} objects");
            }
        }
        return out;
    }

    public record ModEntry(String id, int level) {}
}
