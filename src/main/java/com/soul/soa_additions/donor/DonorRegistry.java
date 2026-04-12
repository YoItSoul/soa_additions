package com.soul.soa_additions.donor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side registry of all donors. Persisted as {@code donors.json} in the
 * world save directory. Thread-safe — the map is a ConcurrentHashMap and the
 * list snapshot is rebuilt atomically on mutation.
 */
public final class DonorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/donors");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<UUID, DonorData> byUuid = new ConcurrentHashMap<>();
    /** Sorted snapshot for the GUI wall. Rebuilt on mutation. */
    private static volatile List<DonorData> sortedList = List.of();
    private static Path savePath;

    private DonorRegistry() {}

    // ---------- lifecycle ----------

    public static void init(MinecraftServer server) {
        savePath = server.getWorldPath(LevelResource.ROOT).resolve("donors.json");
        load();
    }

    public static void shutdown() {
        save();
        byUuid.clear();
        sortedList = List.of();
        savePath = null;
    }

    // ---------- queries ----------

    public static boolean isDonor(UUID uuid) { return byUuid.containsKey(uuid); }

    public static Optional<DonorData> get(UUID uuid) {
        return Optional.ofNullable(byUuid.get(uuid));
    }

    /** Sorted list: legends first, then champions, then supporters. Within
     *  each tier, sorted by donation date (oldest first). */
    public static List<DonorData> all() { return sortedList; }

    public static int count() { return byUuid.size(); }

    // ---------- mutations ----------

    public static void add(DonorData donor) {
        byUuid.put(donor.uuid(), donor);
        rebuildSorted();
        save();
    }

    public static boolean remove(UUID uuid) {
        DonorData removed = byUuid.remove(uuid);
        if (removed != null) {
            rebuildSorted();
            save();
            return true;
        }
        return false;
    }

    // ---------- persistence ----------

    private static void rebuildSorted() {
        List<DonorData> list = new ArrayList<>(byUuid.values());
        list.sort(Comparator
                .comparing((DonorData d) -> d.tier().ordinal()).reversed()
                .thenComparing(DonorData::donatedAt));
        sortedList = Collections.unmodifiableList(list);
    }

    private static void load() {
        byUuid.clear();
        if (savePath == null || !Files.exists(savePath)) {
            sortedList = List.of();
            return;
        }
        try {
            String json = Files.readString(savePath);
            JsonArray arr = GSON.fromJson(json, JsonArray.class);
            if (arr == null) { sortedList = List.of(); return; }
            for (var elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                String name = obj.get("name").getAsString();
                DonorData.Tier tier = DonorData.Tier.fromName(obj.get("tier").getAsString());
                Instant donated = Instant.parse(obj.get("donated_at").getAsString());
                String message = obj.has("message") ? obj.get("message").getAsString() : "";
                byUuid.put(uuid, new DonorData(uuid, name, tier, donated, message));
            }
            rebuildSorted();
            LOG.info("Loaded {} donor(s) from {}", byUuid.size(), savePath);
        } catch (Exception e) {
            LOG.error("Failed to load donors.json", e);
        }
    }

    private static void save() {
        if (savePath == null) return;
        try {
            JsonArray arr = new JsonArray();
            for (DonorData d : byUuid.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("uuid", d.uuid().toString());
                obj.addProperty("name", d.name());
                obj.addProperty("tier", d.tier().name());
                obj.addProperty("donated_at", d.donatedAt().toString());
                obj.addProperty("message", d.message());
                arr.add(obj);
            }
            Files.createDirectories(savePath.getParent());
            Files.writeString(savePath, GSON.toJson(arr));
        } catch (IOException e) {
            LOG.error("Failed to save donors.json", e);
        }
    }
}
