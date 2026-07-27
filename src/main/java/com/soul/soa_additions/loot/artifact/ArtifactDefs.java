package com.soul.soa_additions.loot.artifact;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Datapack loader for TConEvo artifact definitions ({@code data/<ns>/artifacts/*.json}).
 * Format is 1:1 with GC's {@code config/tconevo/artifacts/custom_*.json}: weight, name,
 * lore, tool OR armour, materials, free_mods, mods (string or {id, level}).
 */
public final class ArtifactDefs extends SimpleJsonResourceReloadListener {

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static volatile Map<ResourceLocation, Def> DEFS = Map.of();
    private static volatile int TOTAL_WEIGHT = 0;

    public record Mod(String id, int level) {}

    public record Def(ResourceLocation defId, int weight, String name, String lore,
                      String tool, String armour, List<String> materials,
                      int freeMods, List<Mod> mods) {}

    public ArtifactDefs() {
        super(GSON, "artifacts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager rm, ProfilerFiller profiler) {
        Map<ResourceLocation, Def> out = new LinkedHashMap<>();
        int total = 0;
        for (Map.Entry<ResourceLocation, JsonElement> e : files.entrySet()) {
            try {
                JsonObject o = e.getValue().getAsJsonObject();
                List<String> mats = new ArrayList<>();
                if (o.has("materials")) {
                    o.getAsJsonArray("materials").forEach(m -> mats.add(m.getAsString()));
                }
                List<Mod> mods = new ArrayList<>();
                if (o.has("mods")) {
                    o.getAsJsonArray("mods").forEach(m -> {
                        if (m.isJsonObject()) {
                            JsonObject mo = m.getAsJsonObject();
                            mods.add(new Mod(mo.get("id").getAsString(),
                                    mo.has("level") ? mo.get("level").getAsInt() : 1));
                        } else {
                            mods.add(new Mod(m.getAsString(), 1));
                        }
                    });
                }
                Def def = new Def(e.getKey(),
                        o.has("weight") ? o.get("weight").getAsInt() : 1000,
                        o.has("name") ? o.get("name").getAsString() : e.getKey().getPath(),
                        o.has("lore") ? readLore(o.get("lore")) : "",
                        o.has("tool") ? o.get("tool").getAsString() : null,
                        o.has("armour") ? o.get("armour").getAsString() : null,
                        mats,
                        o.has("free_mods") ? o.get("free_mods").getAsInt() : 0,
                        mods);
                if (def.tool() == null && def.armour() == null) {
                    LOGGER.warn("Artifact {} has neither tool nor armour — skipped", e.getKey());
                    continue;
                }
                out.put(e.getKey(), def);
                total += Math.max(1, def.weight());
            } catch (Exception ex) {
                LOGGER.error("Bad artifact definition {}: {}", e.getKey(), ex.toString());
            }
        }
        DEFS = Map.copyOf(out);
        TOTAL_WEIGHT = total;
        LOGGER.info("Loaded {} tconevo artifact definitions", out.size());
    }

    /** Lore may be a single string or an array of lines (1:1 with GC's multi-line lore). */
    private static String readLore(JsonElement el) {
        if (!el.isJsonArray()) return el.getAsString();
        StringBuilder sb = new StringBuilder();
        el.getAsJsonArray().forEach(line -> {
            if (sb.length() > 0) sb.append('\n');
            sb.append(line.getAsString());
        });
        return sb.toString();
    }

    /** Weighted random pick across all loaded definitions; null when none are loaded. */
    public static Def pick(RandomSource random) {
        Map<ResourceLocation, Def> defs = DEFS;
        int total = TOTAL_WEIGHT;
        if (defs.isEmpty() || total <= 0) return null;
        int roll = random.nextInt(total);
        int cum = 0;
        for (Def d : defs.values()) {
            cum += Math.max(1, d.weight());
            if (roll < cum) return d;
        }
        return null;
    }

    public static Map<ResourceLocation, Def> all() {
        return DEFS;
    }
}
