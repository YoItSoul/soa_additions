package com.soul.soa_additions.quest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Searchable registry picker for task value fields. Shows a grid of every
 * entry in the chosen registry; the search box filters by registry id and
 * localized display name. Click an entry to commit its id via the supplied
 * callback. Modeled on {@link IconPickerPopup}.
 */
@OnlyIn(Dist.CLIENT)
public final class RegistryPickerPopup {

    public enum Mode {
        ITEM("Pick an item", true),
        BLOCK("Pick a block", true),
        ENTITY("Pick an entity", true),
        STAT_TYPE("Pick a stat type", true),
        CUSTOM_STAT("Pick a custom stat", true),
        DIMENSION("Pick a dimension", false),
        ADVANCEMENT("Pick an advancement", false);

        public final String title;
        /** Whether the entry list can be cached across opens (built-in registries
         *  are static; dimensions and advancements depend on server state). */
        public final boolean cacheable;
        Mode(String title, boolean cacheable) { this.title = title; this.cacheable = cacheable; }
    }

    public final EditBox searchBox;
    private final Consumer<String> onPick;
    private final Mode mode;
    private final List<Entry> all;
    private List<Entry> filtered;
    private String lastQuery = "";
    private int scroll;

    private static final Map<Mode, List<Entry>> CACHE = new EnumMap<>(Mode.class);
    private static final Map<Mode, Integer> rememberedScrolls = new EnumMap<>(Mode.class);
    private static final Map<Mode, String> rememberedQueries = new EnumMap<>(Mode.class);

    public static final int W = 220;
    public static final int H = 220;
    public static final int CELL = 18;
    public static final int COLS = 11;
    public static final int GRID_TOP = 32;
    public static final int GRID_LEFT = 6;

    public RegistryPickerPopup(Mode mode, Consumer<String> onPick) {
        this.mode = mode;
        this.onPick = onPick;
        this.all = loadAll(mode);
        var font = Minecraft.getInstance().font;
        this.searchBox = new EditBox(font, 0, 0, W - 12, 14, Component.literal("Search\u2026"));
        this.searchBox.setMaxLength(64);
        String rq = rememberedQueries.getOrDefault(mode, "");
        this.searchBox.setValue(rq);
        this.scroll = rememberedScrolls.getOrDefault(mode, 0);
        applyFilter(rq);
    }

    /** Returns the appropriate aux-field picker mode for a STAT task given
     *  the stat type id currently in the primary value field. Returns null
     *  if the stat type is unrecognised (user can still type manually). */
    public static Mode auxModeForStatType(String statTypeId) {
        if (statTypeId == null) return null;
        return switch (statTypeId) {
            case "minecraft:custom" -> Mode.CUSTOM_STAT;
            case "minecraft:mined" -> Mode.BLOCK;
            case "minecraft:crafted", "minecraft:used", "minecraft:broken",
                 "minecraft:picked_up", "minecraft:dropped" -> Mode.ITEM;
            case "minecraft:killed", "minecraft:killed_by" -> Mode.ENTITY;
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private static List<Entry> loadAll(Mode mode) {
        if (mode.cacheable) {
            List<Entry> cached = CACHE.get(mode);
            if (cached != null) return cached;
        }

        List<Entry> out = new ArrayList<>();
        switch (mode) {
            case ITEM -> {
                for (var item : BuiltInRegistries.ITEM) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                    if (id == null) continue;
                    ItemStack stack = new ItemStack(item);
                    String name;
                    try { name = stack.getHoverName().getString(); }
                    catch (Exception e) { name = id.getPath(); }
                    out.add(new Entry(id, name, stack));
                }
            }
            case BLOCK -> {
                for (Block block : BuiltInRegistries.BLOCK) {
                    ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                    if (id == null) continue;
                    var asItem = block.asItem();
                    ItemStack stack = (asItem != Items.AIR) ? new ItemStack(asItem) : new ItemStack(Items.BARRIER);
                    String name;
                    try { name = block.getName().getString(); }
                    catch (Exception e) { name = id.getPath(); }
                    out.add(new Entry(id, name, stack));
                }
            }
            case ENTITY -> {
                for (EntityType<?> et : BuiltInRegistries.ENTITY_TYPE) {
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(et);
                    if (id == null) continue;
                    SpawnEggItem egg = SpawnEggItem.byId(et);
                    ItemStack stack = (egg != null) ? new ItemStack(egg) : new ItemStack(Items.BARRIER);
                    String name;
                    try { name = et.getDescription().getString(); }
                    catch (Exception e) { name = id.getPath(); }
                    out.add(new Entry(id, name, stack));
                }
            }
            case STAT_TYPE -> {
                for (StatType<?> st : BuiltInRegistries.STAT_TYPE) {
                    ResourceLocation id = BuiltInRegistries.STAT_TYPE.getKey(st);
                    if (id == null) continue;
                    // Use a representative icon per stat category.
                    ItemStack stack = switch (id.toString()) {
                        case "minecraft:mined" -> new ItemStack(Items.IRON_PICKAXE);
                        case "minecraft:crafted" -> new ItemStack(Items.CRAFTING_TABLE);
                        case "minecraft:used" -> new ItemStack(Items.BONE_MEAL);
                        case "minecraft:broken" -> new ItemStack(Items.WOODEN_PICKAXE);
                        case "minecraft:picked_up" -> new ItemStack(Items.HOPPER);
                        case "minecraft:dropped" -> new ItemStack(Items.DROPPER);
                        case "minecraft:killed" -> new ItemStack(Items.DIAMOND_SWORD);
                        case "minecraft:killed_by" -> new ItemStack(Items.SKELETON_SKULL);
                        case "minecraft:custom" -> new ItemStack(Items.BOOK);
                        default -> new ItemStack(Items.PAPER);
                    };
                    out.add(new Entry(id, id.getPath(), stack));
                }
            }
            case CUSTOM_STAT -> {
                for (ResourceLocation stat : BuiltInRegistries.CUSTOM_STAT) {
                    ResourceLocation id = BuiltInRegistries.CUSTOM_STAT.getKey(stat);
                    if (id == null) continue;
                    out.add(new Entry(id, id.getPath(), new ItemStack(Items.PAPER)));
                }
            }
            case DIMENSION -> {
                var conn = Minecraft.getInstance().getConnection();
                if (conn != null) {
                    for (var key : conn.levels()) {
                        ResourceLocation id = key.location();
                        ItemStack stack = switch (id.toString()) {
                            case "minecraft:overworld" -> new ItemStack(Items.GRASS_BLOCK);
                            case "minecraft:the_nether" -> new ItemStack(Items.NETHERRACK);
                            case "minecraft:the_end" -> new ItemStack(Items.END_STONE);
                            default -> new ItemStack(Items.ENDER_PEARL);
                        };
                        out.add(new Entry(id, id.toString(), stack));
                    }
                }
            }
            case ADVANCEMENT -> {
                var conn = Minecraft.getInstance().getConnection();
                if (conn != null) {
                    try {
                        // In 1.20.1, ClientAdvancements stores the tree internally.
                        // Access it via reflection since the field is private.
                        var clientAdv = conn.getAdvancements();
                        java.lang.reflect.Field listField = null;
                        for (var f : clientAdv.getClass().getDeclaredFields()) {
                            if (net.minecraft.advancements.AdvancementList.class.isAssignableFrom(f.getType())) {
                                listField = f;
                                break;
                            }
                        }
                        if (listField != null) {
                            listField.setAccessible(true);
                            var list = (net.minecraft.advancements.AdvancementList) listField.get(clientAdv);
                            for (var adv : list.getAllAdvancements()) {
                                ResourceLocation id = adv.getId();
                                ItemStack icon = new ItemStack(Items.KNOWLEDGE_BOOK);
                                String name = id.toString();
                                var display = adv.getDisplay();
                                if (display != null) {
                                    icon = display.getIcon();
                                    try { name = display.getTitle().getString(); }
                                    catch (Exception ignored) {}
                                }
                                out.add(new Entry(id, name, icon));
                            }
                        }
                    } catch (Exception e) {
                        // Reflection failed — user can still type advancement IDs manually.
                    }
                }
            }
        }
        out.sort((a, b) -> a.id.toString().compareTo(b.id.toString()));
        if (mode.cacheable) CACHE.put(mode, out);
        return out;
    }

    private void applyFilter(String q) {
        lastQuery = q;
        if (q == null || q.isEmpty()) {
            filtered = all;
            return;
        }
        String needle = q.toLowerCase(Locale.ROOT);
        List<Entry> out = new ArrayList<>();
        for (Entry e : all) {
            if (e.id.toString().contains(needle) || e.displayName.toLowerCase(Locale.ROOT).contains(needle)) {
                out.add(e);
            }
        }
        filtered = out;
        scroll = 0;
    }

    public void render(GuiGraphics g, int x, int y, int mouseX, int mouseY, float pt) {
        g.fill(x + 4, y + 4, x + W + 4, y + H + 4, 0x80000000);
        g.fill(x, y, x + W, y + H, 0xF00E1220);
        g.fill(x, y, x + W, y + 22, 0xFF222842);
        outline(g, x, y, W, H, 0xFF3A4264);
        g.drawString(Minecraft.getInstance().font, mode.title, x + 6, y + 7, 0xFFE6E9F5, true);

        String q = searchBox.getValue();
        if (!q.equals(lastQuery)) applyFilter(q);

        searchBox.setPosition(x + 6, y + 24);
        searchBox.setWidth(W - 12);
        searchBox.render(g, mouseX, mouseY, pt);

        int gridX = x + GRID_LEFT;
        int gridY = y + GRID_TOP + 12;
        int rows = (H - GRID_TOP - 14) / CELL;
        int start = scroll * COLS;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                int idx = start + r * COLS + c;
                if (idx >= filtered.size()) break;
                Entry e = filtered.get(idx);
                int cx = gridX + c * CELL;
                int cy = gridY + r * CELL;
                boolean hover = mouseX >= cx && mouseX < cx + CELL && mouseY >= cy && mouseY < cy + CELL;
                if (hover) g.fill(cx, cy, cx + CELL, cy + CELL, 0xFF1F253E);
                g.renderFakeItem(e.stack, cx + 1, cy + 1);
            }
        }

        // Hover tooltip
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                int idx = start + r * COLS + c;
                if (idx >= filtered.size()) break;
                int cx = gridX + c * CELL;
                int cy = gridY + r * CELL;
                if (mouseX >= cx && mouseX < cx + CELL && mouseY >= cy && mouseY < cy + CELL) {
                    Entry e = filtered.get(idx);
                    String label = e.displayName + " (" + e.id + ")";
                    int tw = Minecraft.getInstance().font.width(label);
                    int tx = mouseX + 8;
                    int ty = mouseY + 8;
                    g.fill(tx - 2, ty - 2, tx + tw + 2, ty + 10, 0xF00A0D18);
                    outline(g, tx - 2, ty - 2, tw + 4, 12, 0xFF3A4264);
                    g.drawString(Minecraft.getInstance().font, label, tx, ty, 0xFFE6E9F5, false);
                    return;
                }
            }
        }
    }

    public boolean click(int x, int y, double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (mouseX < x || mouseX >= x + W || mouseY < y || mouseY >= y + H) return false;
        if (mouseY >= y + 24 && mouseY < y + 38) {
            searchBox.setFocused(true);
            searchBox.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        searchBox.setFocused(false);
        int gridX = x + GRID_LEFT;
        int gridY = y + GRID_TOP + 12;
        int rows = (H - GRID_TOP - 14) / CELL;
        int relX = (int) (mouseX - gridX);
        int relY = (int) (mouseY - gridY);
        if (relX < 0 || relY < 0) return true;
        int c = relX / CELL;
        int r = relY / CELL;
        if (c < 0 || c >= COLS || r < 0 || r >= rows) return true;
        int idx = scroll * COLS + r * COLS + c;
        if (idx < filtered.size()) {
            Entry e = filtered.get(idx);
            rememberedQueries.put(mode, lastQuery);
            rememberedScrolls.put(mode, scroll);
            onPick.accept(e.id.toString());
        }
        return true;
    }

    public boolean scroll(double delta) {
        int rows = (H - GRID_TOP - 14) / CELL;
        int totalRows = (filtered.size() + COLS - 1) / COLS;
        int maxScroll = Math.max(0, totalRows - rows);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) delta));
        return true;
    }

    public boolean charTyped(char c, int modifiers) {
        return searchBox.isFocused() && searchBox.charTyped(c, modifiers);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return searchBox.isFocused() && searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private record Entry(ResourceLocation id, String displayName, ItemStack stack) {}
}
