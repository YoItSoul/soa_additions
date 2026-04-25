package com.soul.soa_additions.tr.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.aura.BiomeAspectMap;
import com.soul.soa_additions.tr.aura.EntityAspectMap;
import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.AspectMap;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.core.Aspects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Datapack-driven loader for item/block aspect compositions. Replaces all
 * Java-side defaults — every aspect assignment, including the ones for our
 * own runes, lives as a JSON file under {@code data/<namespace>/tr_aspects/item/}
 * or {@code .../block/}.
 *
 * <p>Two listeners are registered, one per folder (item / block). The folder
 * name doubles as the key prefix in {@link AspectMap}; the file's namespaced
 * path becomes the target item/block id. So
 * {@code data/minecraft/tr_aspects/item/dirt.json} maps to {@code minecraft:dirt}
 * and {@code data/tr/tr_aspects/item/aspect_rune_aer.json} maps to {@code tr:aspect_rune_aer}.
 *
 * <p>JSON format:
 * <pre>{@code
 * {
 *   "aspects": {
 *     "tr:terra": 1,
 *     "tr:perditio": 1
 *   }
 * }
 * }</pre>
 *
 * <p>Aspect ids without a namespace default to {@code tr:}. Unknown aspect
 * ids cause the entry to be skipped with a warning rather than failing the
 * whole reload.
 */
public final class AspectMapLoader {

    public static final String ITEM_FOLDER   = "tr_aspects/item";
    public static final String BLOCK_FOLDER  = "tr_aspects/block";
    public static final String BIOME_FOLDER  = "tr_aspects/biome";
    public static final String ENTITY_FOLDER = "tr_aspects/entity";
    public static final String TAG_FOLDER    = "tr_aspects/tags";

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private AspectMapLoader() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        // First listener: clear the map before any per-folder listener fires,
        // so a removed JSON actually removes its entry. We do this by stuffing
        // the clear into a no-op listener that runs first via separate registration.
        // Each folder listener then fills only its own slice — items can't blow
        // away block entries and vice versa, since clearing happens once up-front.
        event.addListener(new ClearListener());
        event.addListener(new ItemListener());
        event.addListener(new BlockListener());
        event.addListener(new BiomeListener());
        event.addListener(new EntityListener());
        event.addListener(new TagListener());
    }

    /** Forge fires this when the server-authoritative datapack contents change.
     *  Re-syncing the AspectMap to clients will be added once we ship a
     *  server→client sync packet for the map itself; for now the client reloads
     *  in lockstep on its own resource reload, which is fine for single-player.
     *
     *  <p>Also: we use this hook to pre-warm the recipe-by-result index on
     *  the server thread BEFORE any player can hover something. Without this
     *  pre-warm, the first hover (typically client-side render thread) pays
     *  the ~50-100ms cost of indexing all ~10k recipes — a visible freeze.
     *  Server-thread pre-warm is invisible. */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        if (server != null) {
            server.execute(() -> {
                var ow = server.overworld();
                if (ow != null) {
                    com.soul.soa_additions.tr.aspect.derive.RecipeAspectDeriver
                            .preWarmIndex(ow);
                    // Then kick off eager full-derivation in a background
                    // worker thread. The index pre-warm above is the latency-
                    // critical part (without it, first hover walks all 10k
                    // recipes synchronously); full derivation just smooths
                    // out per-item cache misses that would otherwise trickle
                    // in during play. Daemon thread, MIN_PRIORITY — can't
                    // starve the main game.
                    com.soul.soa_additions.tr.aspect.derive.RecipeAspectDeriver
                            .preWarmAllItems(ow);
                }
            });
        }
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(AspectMapLoader.class);
    }

    // ------------- Internal listeners -------------

    private static final class ClearListener extends SimpleJsonResourceReloadListener {
        ClearListener() { super(GSON, "tr_aspects/__clear"); }
        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profiler) {
            // The folder doesn't exist; this listener has no JSON to consume,
            // its sole purpose is to run AspectMap.clear() before the data
            // listeners populate. Forge runs reload listeners in registration order.
            AspectMap.clear();
            BiomeAspectMap.clear();
            EntityAspectMap.clear();
            com.soul.soa_additions.tr.aspect.derive.TagAspectRegistry.clear();
            com.soul.soa_additions.tr.aspect.derive.RecipeAspectDeriver.clearCache();
        }
    }

    private static final class ItemListener extends SimpleJsonResourceReloadListener {
        ItemListener() { super(GSON, ITEM_FOLDER); }
        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profiler) {
            int loaded = 0;
            for (var entry : objects.entrySet()) {
                List<AspectStack> stacks = parse(entry.getKey(), entry.getValue());
                if (stacks != null && !stacks.isEmpty()) {
                    AspectMap.putItem(entry.getKey(), stacks);
                    loaded++;
                }
            }
            ThaumicRemnants.LOG.info("Loaded {} item aspect compositions", loaded);
        }
    }

    private static final class BlockListener extends SimpleJsonResourceReloadListener {
        BlockListener() { super(GSON, BLOCK_FOLDER); }
        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profiler) {
            int loaded = 0;
            for (var entry : objects.entrySet()) {
                List<AspectStack> stacks = parse(entry.getKey(), entry.getValue());
                if (stacks != null && !stacks.isEmpty()) {
                    AspectMap.putBlock(entry.getKey(), stacks);
                    loaded++;
                }
            }
            ThaumicRemnants.LOG.info("Loaded {} block aspect compositions", loaded);
        }
    }

    private static final class BiomeListener extends SimpleJsonResourceReloadListener {
        BiomeListener() { super(GSON, BIOME_FOLDER); }
        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profiler) {
            int loaded = 0;
            for (var entry : objects.entrySet()) {
                List<AspectStack> stacks = parse(entry.getKey(), entry.getValue());
                if (stacks != null && !stacks.isEmpty()) {
                    BiomeAspectMap.put(entry.getKey(), stacks);
                    loaded++;
                }
            }
            ThaumicRemnants.LOG.info("Loaded {} biome aspect compositions", loaded);
        }
    }

    private static final class EntityListener extends SimpleJsonResourceReloadListener {
        EntityListener() { super(GSON, ENTITY_FOLDER); }
        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profiler) {
            int loaded = 0;
            for (var entry : objects.entrySet()) {
                List<AspectStack> stacks = parse(entry.getKey(), entry.getValue());
                if (stacks != null && !stacks.isEmpty()) {
                    EntityAspectMap.put(entry.getKey(), stacks);
                    loaded++;
                }
            }
            ThaumicRemnants.LOG.info("Loaded {} entity aspect compositions", loaded);
        }
    }

    /** Tag-based bulk assignment loader. JSON files at
     *  {@code data/<ns>/tr_aspects/tags/<tag-namespace>/<tag-path>.json}
     *  map to the tag {@code <tag-namespace>:<tag-path>}, NOT to the file's
     *  own resource path. The SimpleJsonResourceReloadListener gives us
     *  resource paths like {@code tr_aspects/tags/forge/ingots/iron.json}
     *  → key {@code <ns>:forge/ingots/iron}; we re-derive the tag id by
     *  splitting on the first slash. */
    private static final class TagListener extends SimpleJsonResourceReloadListener {
        TagListener() { super(GSON, TAG_FOLDER); }
        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profiler) {
            int loaded = 0;
            for (var entry : objects.entrySet()) {
                ResourceLocation file = entry.getKey();
                // Path here is the post-prefix subpath, e.g. "forge/ingots/iron"
                int slash = file.getPath().indexOf('/');
                if (slash < 0) {
                    ThaumicRemnants.LOG.warn(
                            "Tag aspect file {} doesn't have <namespace>/<path> shape — skipping",
                            file);
                    continue;
                }
                String tagNs = file.getPath().substring(0, slash);
                String tagPath = file.getPath().substring(slash + 1);
                ResourceLocation tagId = ResourceLocation.tryBuild(tagNs, tagPath);
                if (tagId == null) {
                    ThaumicRemnants.LOG.warn("Bad tag id derived from {} → {}:{}",
                            file, tagNs, tagPath);
                    continue;
                }
                List<AspectStack> stacks = parse(file, entry.getValue());
                if (stacks != null && !stacks.isEmpty()) {
                    com.soul.soa_additions.tr.aspect.derive.TagAspectRegistry.put(tagId, stacks);
                    loaded++;
                }
            }
            ThaumicRemnants.LOG.info("Loaded {} tag aspect compositions", loaded);
        }
    }

    // ------------- Parsing -------------

    private static List<AspectStack> parse(ResourceLocation file, JsonElement element) {
        try {
            if (!element.isJsonObject()) throw new JsonSyntaxException("expected JSON object at root");
            JsonObject root = element.getAsJsonObject();
            JsonObject aspects = root.has("aspects") && root.get("aspects").isJsonObject()
                    ? root.getAsJsonObject("aspects")
                    : root; // bare-map shorthand: { "tr:terra": 1, ... }
            List<AspectStack> out = new ArrayList<>(aspects.size());
            for (var ent : aspects.entrySet()) {
                String idStr = ent.getKey();
                int amount;
                try { amount = ent.getValue().getAsInt(); }
                catch (Exception e) {
                    ThaumicRemnants.LOG.warn("Skipping non-integer amount for aspect {} in {}", idStr, file);
                    continue;
                }
                if (amount <= 0) continue;
                ResourceLocation aspectId = idStr.indexOf(':') >= 0
                        ? ResourceLocation.tryParse(idStr)
                        : new ResourceLocation(ThaumicRemnants.MODID, idStr);
                if (aspectId == null) {
                    ThaumicRemnants.LOG.warn("Bad aspect id '{}' in {}", idStr, file);
                    continue;
                }
                Aspect aspect;
                try { aspect = Aspects.byId(aspectId); }
                catch (Exception e) {
                    ThaumicRemnants.LOG.warn("Unknown aspect {} referenced in {}", aspectId, file);
                    continue;
                }
                out.add(new AspectStack(aspect, amount));
            }
            return out;
        } catch (Exception e) {
            ThaumicRemnants.LOG.error("Failed to parse aspect file {}: {}", file, e.getMessage());
            return null;
        }
    }
}
