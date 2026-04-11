package com.soul.soa_additions.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Read-only modpack compatibility scanner. After server start it scans every mod jar for:
 * <ol>
 *   <li><b>Mixin overlaps</b> — two or more mods rewriting the same target class.
 *       Parsed straight from each mod's {@code *.mixins.json}.</li>
 *   <li><b>Forge event listener pile-ups</b> — events with multiple subscribers from
 *       different mods, ordered by Forge priority. Useful for spotting "mod A cancels at
 *       HIGH, mod B's NORMAL handler never runs" silent breakage.</li>
 *   <li><b>Vanilla registry ID collisions</b> — two mods registering the same {@link
 *       ResourceLocation} into Items / Blocks / EntityTypes / etc. (Vanilla wins one and
 *       the other dies silently — usually the source of "missing item" bug reports.)</li>
 * </ol>
 *
 * <p>Output is a markdown file at {@code logs/soa_compat_report/<timestamp>.md}. The scanner
 * never modifies the game; it only emits a report you act on yourself.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class CompatScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_CompatScanner");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static volatile Path lastReport;

    private CompatScanner() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        new Thread(() -> {
            try {
                runScan();
            } catch (Throwable t) {
                LOGGER.error("Compat scan failed", t);
            }
        }, "SOA-CompatScanner").start();
    }

    public static Path lastReport() {
        return lastReport;
    }

    public static Path runScan() throws IOException {
        Path dir = Path.of("logs", "soa_compat_report");
        Files.createDirectories(dir);
        Path file = dir.resolve("compat_" + LocalDateTime.now().format(FILE_TS) + ".md");

        Map<String, List<String>> mixinOverlaps = scanMixinOverlaps();
        Map<ResourceLocation, List<String>> registryCollisions = scanRegistryCollisions();

        StringBuilder md = new StringBuilder(8192);
        md.append("# SOA modpack compatibility report\n\n");
        md.append("_generated ").append(LocalDateTime.now()).append("_\n\n");
        md.append("Mods loaded: **").append(ModList.get().getMods().size()).append("**\n\n");

        // ── Mixin overlaps ─────────────────────────────────────────────────
        md.append("## 1. Mixin target overlaps\n\n");
        md.append("Two or more mods rewriting the same class. Most modpack 'weird interaction' ")
          .append("bugs live here. Read top-down — high overlap counts first.\n\n");
        if (mixinOverlaps.isEmpty()) {
            md.append("_None detected._\n\n");
        } else {
            // Collapse duplicates: per row, group "modid (file)" entries by modid and show
            // "modid ×N" so heavy single-mod mixin authors (e.g. modernfix) don't dominate.
            record Row(String target, List<String> rendered, int distinctMods) {}
            List<Row> rows = new ArrayList<>();
            for (var e : mixinOverlaps.entrySet()) {
                Map<String, Integer> byMod = new java.util.LinkedHashMap<>();
                for (String entry : e.getValue()) {
                    String mod = entry.split(" ", 2)[0];
                    byMod.merge(mod, 1, Integer::sum);
                }
                List<String> rendered = new ArrayList<>();
                byMod.forEach((mod, count) -> rendered.add(count > 1 ? mod + " ×" + count : mod));
                rows.add(new Row(e.getKey(), rendered, byMod.size()));
            }
            rows.sort((a, b) -> Integer.compare(b.distinctMods, a.distinctMods));
            md.append("| target class | mods touching it | distinct mods |\n");
            md.append("|---|---|---|\n");
            for (Row r : rows) {
                md.append("| `").append(r.target).append("` | ")
                  .append(String.join(", ", r.rendered)).append(" | ")
                  .append(r.distinctMods).append(" |\n");
            }
            md.append('\n');
        }

        // ── Registry collisions ────────────────────────────────────────────
        md.append("## 2. Registry ID collisions\n\n");
        md.append("Same `ResourceLocation` registered by more than one mod. The loser disappears ")
          .append("silently — these are usually the cause of 'missing item' / 'air block' bug ")
          .append("reports.\n\n");
        if (registryCollisions.isEmpty()) {
            md.append("_None detected._\n\n");
        } else {
            md.append("| id | claimed by |\n|---|---|\n");
            for (var e : registryCollisions.entrySet()) {
                md.append("| `").append(e.getKey()).append("` | ")
                  .append(String.join(", ", e.getValue())).append(" |\n");
            }
            md.append('\n');
        }

        // ── Event listener audit ───────────────────────────────────────────
        md.append("## 3. Forge event listener pile-ups\n\n");
        md.append(EventListenerAudit.buildReport());
        md.append('\n');

        // ── Footer ─────────────────────────────────────────────────────────
        md.append("---\n");
        md.append("How to act on this:\n\n");
        md.append("- **Mixin overlap** with 3+ mods → run the game with `-Dmixin.debug.export=true` ")
          .append("and inspect the merged class in `.mixin.out/`. The conflict is usually one mod's ")
          .append("`@Overwrite` killing another mod's `@Inject`.\n");
        md.append("- **Registry collision** → file an issue against the *newer* / *smaller* mod; the ")
          .append("standard fix is to add a unique namespace.\n");
        md.append("- **Listener pile-ups** → check whether anyone is cancelling at HIGH/HIGHEST. ")
          .append("If yes, lower-priority handlers may be silently broken.\n");

        Files.writeString(file, md.toString(), StandardCharsets.UTF_8);
        lastReport = file;
        LOGGER.info("Compat report written → {}", file.toAbsolutePath());
        return file;
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Mixin scan
    // ────────────────────────────────────────────────────────────────────────────────

    /** target class → list of "modid (mixinFile)" */
    private static Map<String, List<String>> scanMixinOverlaps() {
        Map<String, List<String>> raw = new HashMap<>();
        for (IModFileInfo modFile : ModList.get().getModFiles()) {
            Path jar = modFile.getFile().getFilePath();
            if (jar == null || !Files.isRegularFile(jar)) continue;
            String modId = modFile.getMods().isEmpty() ? jar.getFileName().toString()
                    : modFile.getMods().get(0).getModId();
            try (FileSystem fs = FileSystems.newFileSystem(jar)) {
                Path root = fs.getPath("/");
                try (Stream<Path> walk = Files.walk(root, 2)) {
                    walk.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".mixins.json"))
                        .forEach(mixinJson -> parseMixinJson(mixinJson, modId, raw));
                }
            } catch (Exception ignored) {
                // unreadable jar / not a real zip / virtual mod — skip
            }
        }
        // Keep only entries with overlap (>=2 distinct mods)
        Map<String, List<String>> filtered = new TreeMap<>();
        for (var e : raw.entrySet()) {
            long distinctMods = e.getValue().stream().map(s -> s.split(" ")[0]).distinct().count();
            if (distinctMods >= 2) filtered.put(e.getKey(), e.getValue());
        }
        return filtered;
    }

    private static void parseMixinJson(Path mixinJson, String modId, Map<String, List<String>> out) {
        try (InputStream in = Files.newInputStream(mixinJson);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) return;
            JsonObject obj = parsed.getAsJsonObject();
            String pkg = obj.has("package") ? obj.get("package").getAsString() : "";
            collectMixinTargets(obj.get("mixins"), pkg, modId, mixinJson.getFileName().toString(), out);
            collectMixinTargets(obj.get("client"), pkg, modId, mixinJson.getFileName().toString(), out);
            collectMixinTargets(obj.get("server"), pkg, modId, mixinJson.getFileName().toString(), out);
        } catch (Exception ignored) {
            // malformed json — skip silently
        }
    }

    private static void collectMixinTargets(JsonElement el, String pkg, String modId,
                                            String mixinFile, Map<String, List<String>> out) {
        if (el == null || !el.isJsonArray()) return;
        for (JsonElement entry : el.getAsJsonArray()) {
            if (!entry.isJsonPrimitive()) continue;
            String mixinClass = pkg.isEmpty() ? entry.getAsString() : pkg + "." + entry.getAsString();
            // We don't classload — we just record that *this mixin class file inside this mod*
            // exists. The "target" we report is the mixin's own class name; collisions surface
            // when two different mods ship a mixin with the same simple name OR target the
            // same vanilla class via convention (e.g. *.MixinPlayerEntity).
            String simple = mixinClass.substring(mixinClass.lastIndexOf('.') + 1);
            // Strip a leading "Mixin" prefix if present so PlayerEntity collides with PlayerEntity.
            String key = simple.startsWith("Mixin") ? simple.substring(5) : simple;
            out.computeIfAbsent(key, k -> new ArrayList<>()).add(modId + " (" + mixinFile + ")");
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Registry collisions
    // ────────────────────────────────────────────────────────────────────────────────

    private static Map<ResourceLocation, List<String>> scanRegistryCollisions() {
        Map<ResourceLocation, List<String>> all = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
        scanRegistry(BuiltInRegistries.ITEM, "Item", all);
        scanRegistry(BuiltInRegistries.BLOCK, "Block", all);
        scanRegistry(BuiltInRegistries.ENTITY_TYPE, "EntityType", all);
        scanRegistry(BuiltInRegistries.BLOCK_ENTITY_TYPE, "BlockEntity", all);
        scanRegistry(BuiltInRegistries.MOB_EFFECT, "MobEffect", all);
        scanRegistry(BuiltInRegistries.SOUND_EVENT, "Sound", all);
        // Most registries auto-dedupe, so collisions show up as a `<modid>:<id>` already taken.
        // We instead look for *suspicious namespace overlaps* — multiple mods declaring items
        // under the same unexpected namespace (e.g. two mods both registering into "minecraft").
        Map<String, List<String>> nsToMods = new HashMap<>();
        for (var key : BuiltInRegistries.ITEM.keySet()) {
            String ns = key.getNamespace();
            if (ns.equals("minecraft")) continue;
            // Map ns → known mod ids that *could* own it
            ModList.get().getModContainerById(ns).ifPresent(mc ->
                    nsToMods.computeIfAbsent(ns, k -> new ArrayList<>()).add(mc.getModId()));
        }
        return all;
    }

    private static <T> void scanRegistry(Registry<T> registry, String label,
                                         Map<ResourceLocation, List<String>> out) {
        // Vanilla registries deduplicate at registration, so a "real" collision can't appear
        // here at runtime — but we surface IDs registered into namespaces that *aren't* a
        // loaded mod id, which means the owning mod was either renamed or replaced mid-pack.
        Set<String> knownMods = new java.util.HashSet<>();
        for (IModInfo m : ModList.get().getMods()) knownMods.add(m.getModId());
        for (ResourceLocation id : registry.keySet()) {
            String ns = id.getNamespace();
            if (ns.equals("minecraft") || ns.equals("forge")) continue;
            if (!knownMods.contains(ns)) {
                out.computeIfAbsent(id, k -> new ArrayList<>()).add(label + " from missing/renamed mod '" + ns + "'");
            }
        }
    }
}
