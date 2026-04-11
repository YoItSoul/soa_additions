package com.soul.soa_additions.quest.importer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * One-shot importer for FTB Quests chapter files. Reads
 * {@code <ftbdir>/quests/chapters/*.snbt}, translates each quest into our
 * in-house JSON chapter format, and writes one {@code <chapterId>.json} per
 * source file into the target directory.
 *
 * <p>This is intentionally lenient: unknown task/reward types are skipped with
 * a warning entry in the returned {@link Result} rather than failing the whole
 * import. It covers every task type seen in the Souls of Avarice pack at the
 * time of writing (dimension, item, advancement, questsadditions:break,
 * questsadditions:place, observation) and the common reward types (item, xp,
 * gamestage). Everything else becomes a checkmark task with the original type
 * name as the label, so quests still flow even if a few tasks must be finished
 * manually.</p>
 */
public final class FtbQuestsImporter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // Tile → pixel scale. FTB uses 1.5-tile grid steps; 48 px per tile gives
    // roughly the same visual density as our auto layout.
    private static final int PX_PER_TILE = 48;

    public static final class Result {
        public final List<String> writtenFiles = new ArrayList<>();
        public final List<String> warnings = new ArrayList<>();
        public int chapters;
        public int quests;
    }

    private FtbQuestsImporter() {}

    /**
     * Import every {@code .snbt} chapter file under {@code ftbQuestsRoot/chapters}
     * and write the resulting JSON chapters into {@code outDir}.
     *
     * @param ftbQuestsRoot the FTB Quests config directory (the one that
     *                      contains {@code chapters/}, {@code data.snbt}, etc.)
     * @param outDir        where to write the converted JSON files; created
     *                      if missing.
     */
    public static Result importFrom(Path ftbQuestsRoot, Path outDir) throws IOException {
        Result r = new Result();
        Path chaptersDir = ftbQuestsRoot.resolve("chapters");
        if (!Files.isDirectory(chaptersDir)) {
            r.warnings.add("No chapters dir at " + chaptersDir);
            return r;
        }
        Files.createDirectories(outDir);

        try (Stream<Path> files = Files.list(chaptersDir)) {
            for (Path p : files.sorted().toList()) {
                String name = p.getFileName().toString();
                if (!name.endsWith(".snbt")) continue;
                try {
                    String snbt = normalizeSnbt(Files.readString(p, StandardCharsets.UTF_8));
                    CompoundTag root = TagParser.parseTag(snbt);
                    JsonObject chapterJson = convertChapter(root, name.replace(".snbt", ""), r);
                    if (chapterJson == null) continue;
                    String chapterId = chapterJson.get("id").getAsString();
                    Path outFile = outDir.resolve(chapterId + ".json");
                    Files.writeString(outFile, GSON.toJson(chapterJson), StandardCharsets.UTF_8);
                    r.writtenFiles.add(outFile.toString());
                    r.chapters++;
                } catch (Exception ex) {
                    r.warnings.add("Failed to import " + name + ": " + ex.getMessage());
                }
            }
        }
        return r;
    }

    // ---------- chapter ----------

    private static JsonObject convertChapter(CompoundTag root, String fileName, Result r) {
        String filename = root.contains("filename") ? root.getString("filename") : fileName;
        String chapterId = slug(filename);
        String title = root.contains("title") ? root.getString("title") : chapterId;
        int order = root.contains("order_index") ? ((NumericTag) root.get("order_index")).getAsInt() : 0;

        JsonObject out = new JsonObject();
        out.addProperty("id", chapterId);
        out.addProperty("title", title);
        out.addProperty("icon", "minecraft:book");
        out.addProperty("sort_order", order);
        JsonArray modes = new JsonArray();
        modes.add("casual"); modes.add("adventure"); modes.add("expert");
        out.add("modes", modes);

        // Collect min x/y so we can offset all coords into non-negative pixel
        // space (our layout expects posX/posY >= 0 to count as "manual").
        ListTag quests = root.getList("quests", Tag.TAG_COMPOUND);
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        for (int i = 0; i < quests.size(); i++) {
            CompoundTag q = quests.getCompound(i);
            double x = q.contains("x") ? ((NumericTag) q.get("x")).getAsDouble() : 0;
            double y = q.contains("y") ? ((NumericTag) q.get("y")).getAsDouble() : 0;
            if (x < minX) minX = x;
            if (y < minY) minY = y;
        }
        if (minX == Double.POSITIVE_INFINITY) { minX = 0; minY = 0; }

        JsonArray questArr = new JsonArray();
        for (int i = 0; i < quests.size(); i++) {
            CompoundTag q = quests.getCompound(i);
            JsonObject qj = convertQuest(q, minX, minY, r);
            if (qj != null) { questArr.add(qj); r.quests++; }
        }
        out.add("quests", questArr);
        return out;
    }

    // ---------- quest ----------

    private static JsonObject convertQuest(CompoundTag q, double minX, double minY, Result r) {
        String id = q.getString("id").toLowerCase();
        JsonObject out = new JsonObject();
        out.addProperty("id", id);

        if (q.contains("title")) out.addProperty("title", q.getString("title"));
        if (q.contains("subtitle")) {
            JsonArray desc = new JsonArray();
            desc.add(q.getString("subtitle"));
            out.add("description", desc);
        } else if (q.contains("description")) {
            JsonArray desc = new JsonArray();
            ListTag lines = q.getList("description", Tag.TAG_STRING);
            for (int i = 0; i < lines.size(); i++) desc.add(lines.getString(i));
            out.add("description", desc);
        }

        if (q.contains("icon")) {
            String icon = readItemId(q.get("icon"));
            if (icon != null) out.addProperty("icon", icon);
        }

        // Dependencies — lowercase to match our id slugging.
        if (q.contains("dependencies", Tag.TAG_LIST)) {
            JsonArray deps = new JsonArray();
            ListTag d = q.getList("dependencies", Tag.TAG_STRING);
            for (int i = 0; i < d.size(); i++) deps.add(d.getString(i).toLowerCase());
            out.add("dependencies", deps);
        }

        // Position — offset + scale. FTB stores at tile resolution; we use
        // pixels. Clamp to >= 0 since -1 means "auto" in our model.
        double x = q.contains("x") ? ((NumericTag) q.get("x")).getAsDouble() : 0;
        double y = q.contains("y") ? ((NumericTag) q.get("y")).getAsDouble() : 0;
        int px = (int) Math.round((x - minX) * PX_PER_TILE);
        int py = (int) Math.round((y - minY) * PX_PER_TILE);
        out.addProperty("x", Math.max(0, px));
        out.addProperty("y", Math.max(0, py));
        out.addProperty("shape", "icon");

        // Tasks
        JsonArray taskArr = new JsonArray();
        if (q.contains("tasks", Tag.TAG_LIST)) {
            ListTag tasks = q.getList("tasks", Tag.TAG_COMPOUND);
            for (int i = 0; i < tasks.size(); i++) {
                JsonObject tj = convertTask(tasks.getCompound(i), r);
                if (tj != null) taskArr.add(tj);
            }
        }
        out.add("tasks", taskArr);

        // Rewards
        JsonArray rewardArr = new JsonArray();
        if (q.contains("rewards", Tag.TAG_LIST)) {
            ListTag rewards = q.getList("rewards", Tag.TAG_COMPOUND);
            for (int i = 0; i < rewards.size(); i++) {
                JsonObject rj = convertReward(rewards.getCompound(i), r);
                if (rj != null) rewardArr.add(rj);
            }
        }
        out.add("rewards", rewardArr);

        // Pick an icon from the first item task if none was set explicitly.
        if (!out.has("icon")) {
            for (int i = 0; i < taskArr.size(); i++) {
                JsonObject t = taskArr.get(i).getAsJsonObject();
                if (t.has("item")) { out.addProperty("icon", t.get("item").getAsString()); break; }
                if (t.has("block")) { out.addProperty("icon", t.get("block").getAsString()); break; }
            }
        }
        return out;
    }

    // ---------- task conversion ----------

    private static JsonObject convertTask(CompoundTag t, Result r) {
        String type = t.contains("type") ? t.getString("type") : "";
        JsonObject out = new JsonObject();
        switch (type) {
            case "dimension" -> {
                out.addProperty("type", "dimension");
                out.addProperty("dimension", t.getString("dimension"));
            }
            case "item" -> {
                Tag itemTag = t.get("item");
                long count = t.contains("count") ? ((NumericTag) t.get("count")).getAsLong() : 1;
                out.addProperty("type", "item");
                out.addProperty("count", (int) Math.max(1, count));
                if (t.contains("consume_items") && t.getBoolean("consume_items")) out.addProperty("consume", true);
                if (itemTag instanceof StringTag s) {
                    out.addProperty("item", s.getAsString());
                } else if (itemTag instanceof CompoundTag ic) {
                    String id = ic.contains("id") ? ic.getString("id") : "minecraft:air";
                    // FTB's "any of this tag" is encoded as itemfilters:tag
                    // with {tag:{value:"<tag_id>"}}. Convert that to our tag
                    // form directly — otherwise treat embedded tag NBT as an
                    // nbt filter.
                    if ("itemfilters:tag".equals(id) && ic.contains("tag", Tag.TAG_COMPOUND)) {
                        CompoundTag inner = ic.getCompound("tag");
                        if (inner.contains("value")) {
                            out.addProperty("tag", inner.getString("value"));
                            return out;
                        }
                    }
                    out.addProperty("item", id);
                    if (ic.contains("tag", Tag.TAG_COMPOUND)) {
                        CompoundTag nbt = ic.getCompound("tag");
                        if (!nbt.isEmpty()) out.addProperty("nbt", nbt.toString());
                    }
                } else {
                    r.warnings.add("item task with no item field: " + t);
                    return checkmark("Collect the required item");
                }
            }
            case "advancement" -> {
                out.addProperty("type", "advancement");
                out.addProperty("advancement", t.getString("advancement"));
            }
            case "questsadditions:break" -> {
                out.addProperty("type", "mine");
                out.addProperty("block", t.getString("block"));
                long count = t.contains("value") ? ((NumericTag) t.get("value")).getAsLong() : 1;
                out.addProperty("count", (int) Math.max(1, count));
            }
            case "questsadditions:place" -> {
                // No placement task yet — fall back to checkmark with a label.
                String block = t.contains("block") ? t.getString("block") : "block";
                return checkmark("Place " + block);
            }
            case "observation" -> {
                String tgt = t.contains("to_observe") ? t.getString("to_observe") : "something";
                return checkmark("Observe " + tgt);
            }
            case "checkmark" -> {
                return checkmark(t.contains("title") ? t.getString("title") : "Acknowledge");
            }
            case "xp" -> {
                long levels = t.contains("value") ? ((NumericTag) t.get("value")).getAsLong() : 1;
                return checkmark("Earn " + levels + " XP levels");
            }
            default -> {
                r.warnings.add("Unsupported FTB task type: " + type);
                return checkmark("Complete task: " + type);
            }
        }
        return out;
    }

    private static JsonObject checkmark(String text) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "checkmark");
        o.addProperty("text", text);
        return o;
    }

    // ---------- reward conversion ----------

    private static JsonObject convertReward(CompoundTag t, Result r) {
        String type = t.contains("type") ? t.getString("type") : "";
        JsonObject out = new JsonObject();
        switch (type) {
            case "item" -> {
                String id = readItemId(t.get("item"));
                if (id == null) return null;
                long count = t.contains("count") ? ((NumericTag) t.get("count")).getAsLong() : 1;
                out.addProperty("type", "item");
                out.addProperty("item", id);
                out.addProperty("count", (int) Math.max(1, count));
            }
            case "xp" -> {
                long amount = t.contains("xp") ? ((NumericTag) t.get("xp")).getAsLong() : 0;
                out.addProperty("type", "xp");
                out.addProperty("amount", (int) amount);
            }
            case "xp_levels" -> {
                long amount = t.contains("xp_levels") ? ((NumericTag) t.get("xp_levels")).getAsLong() : 0;
                out.addProperty("type", "xp");
                out.addProperty("amount", (int) amount);
                out.addProperty("levels", true);
            }
            case "gamestage" -> {
                if (!t.contains("stage")) return null;
                out.addProperty("type", "grant_stage");
                out.addProperty("stage", t.getString("stage"));
            }
            case "command" -> {
                out.addProperty("type", "command");
                out.addProperty("command", t.contains("command") ? t.getString("command") : "");
            }
            default -> {
                r.warnings.add("Unsupported FTB reward type: " + type);
                return null;
            }
        }
        return out;
    }

    // ---------- helpers ----------

    private static String readItemId(Tag tag) {
        if (tag instanceof StringTag s) return s.getAsString();
        if (tag instanceof CompoundTag c && c.contains("id")) return c.getString("id");
        return null;
    }

    /**
     * FTB writes SNBT with newlines as the field separator, which vanilla
     * {@link TagParser} doesn't accept — it wants commas. Convert every
     * unquoted line break to a comma, collapse runs, then strip commas that
     * end up adjacent to {@code &#123;}/{@code [}/{@code &#125;}/{@code ]}.
     */
    static String normalizeSnbt(String src) {
        StringBuilder out = new StringBuilder(src.length());
        boolean inStr = false;
        char quote = 0;
        boolean escape = false;
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (inStr) {
                out.append(c);
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (c == quote) inStr = false;
                continue;
            }
            if (c == '"' || c == '\'') { inStr = true; quote = c; out.append(c); continue; }
            if (c == '\r' || c == '\n') { out.append(','); continue; }
            out.append(c);
        }
        String s = out.toString();
        s = s.replaceAll("(,\\s*)+", ",");
        s = s.replaceAll("([\\{\\[])\\s*,", "$1");
        s = s.replaceAll(",\\s*([\\}\\]])", "$1");
        // Strip any stray leading/trailing commas so TagParser doesn't see
        // trailing data after the outermost closing brace.
        return s.replaceAll("^[,\\s]+", "").replaceAll("[,\\s]+$", "");
    }

    private static String slug(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (char c : s.toLowerCase().toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') b.append(c);
            else if (c == '-' || c == ' ') b.append('_');
        }
        String out = b.toString();
        return out.isEmpty() ? "chapter" : out;
    }
}
