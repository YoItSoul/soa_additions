package com.soul.soa_additions.tconstructevo.item.artifact;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data-pack-driven loader for tconevo artifact specs. JSON files at
 * {@code data/<namespace>/tconevo/artifacts/<id>.json} parse into
 * {@link ArtifactSpec} instances and are exposed via {@link #all()} for
 * downstream loot integration. Hot-reloads on {@code /reload}.
 *
 * <p>The 14 shipped tconevo artifact JSONs live in our resources at
 * {@code data/soa_additions/tconevo/artifacts/} so the system has content out
 * of the box. Modpack authors can override or add via their own data packs
 * since the loader iterates every namespace under that path.</p>
 *
 * <p>This loader handles parsing only. ItemStack construction (Tinkers tool
 * build + lore + free-mod slots + applied modifiers) and loot-table injection
 * land in follow-up patches once the build pipeline is wired.</p>
 */
public final class ArtifactManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static final String DIRECTORY = "tconevo/artifacts";

    public static final ArtifactManager INSTANCE = new ArtifactManager();

    private Map<ResourceLocation, ArtifactSpec> specs = Collections.emptyMap();
    private Map<ResourceLocation, ItemStack> stacks = Collections.emptyMap();

    private ArtifactManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries,
                         ResourceManager manager,
                         ProfilerFiller profiler) {
        Map<ResourceLocation, ArtifactSpec> parsed = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject obj = entry.getValue().getAsJsonObject();
                parsed.put(id, ArtifactSpec.parse(obj));
            } catch (Exception e) {
                TConstructEvoPlugin.LOG.warn("Failed to parse artifact {}: {}", id, e.getMessage());
            }
        }
        this.specs = Collections.unmodifiableMap(parsed);
        this.stacks = Collections.emptyMap(); // built lazily on first access
        TConstructEvoPlugin.LOG.info("Loaded {} tconevo artifact spec(s)", parsed.size());
    }

    public Collection<ArtifactSpec> all() {
        return specs.values();
    }

    public ArtifactSpec get(ResourceLocation id) {
        return specs.get(id);
    }

    /**
     * Lazily materialise every spec into an {@link ItemStack}. Build runs on
     * first call and is cached until the next data-pack reload. Materials and
     * modifiers come from {@link MaterialRegistry}/the modifier registry which
     * are themselves data-pack reload listeners — they're guaranteed to be
     * loaded before any loot table needs an artifact.
     */
    public synchronized Map<ResourceLocation, ItemStack> stacks() {
        if (stacks.isEmpty() && !specs.isEmpty()) {
            Map<ResourceLocation, ItemStack> built = new HashMap<>();
            for (Map.Entry<ResourceLocation, ArtifactSpec> entry : specs.entrySet()) {
                ItemStack stack = ArtifactBuilder.build(entry.getValue());
                if (stack != null && !stack.isEmpty()) built.put(entry.getKey(), stack);
            }
            this.stacks = Collections.unmodifiableMap(built);
            TConstructEvoPlugin.LOG.info("Built {}/{} tconevo artifact stack(s)", built.size(), specs.size());
        }
        return stacks;
    }
}
