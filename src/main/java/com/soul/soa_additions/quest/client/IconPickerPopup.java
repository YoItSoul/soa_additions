package com.soul.soa_additions.quest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Searchable item picker. Shows a grid of every registered item; the search
 * box filters by registry id and localized display name. Click an item to
 * commit it via the supplied callback (typically setting an icon field on the
 * edit form). Drawn as a self-contained overlay — input routing is handled by
 * its host screen.
 */
@OnlyIn(Dist.CLIENT)
public final class IconPickerPopup {

    public final EditBox searchBox;
    private final Consumer<String> onPick;
    private final List<Entry> all;
    private List<Entry> filtered;
    private String lastQuery = "";
    private int scroll;

    /** Persisted across opens so the user doesn't lose their place when they
     *  close and reopen mid-edit. */
    private static int rememberedScroll;
    private static String rememberedQuery = "";

    // Layout: a fixed grid of icons, scrollable. Sized to fit comfortably
    // alongside the edit popup without dominating the screen.
    public static final int W = 220;
    public static final int H = 220;
    public static final int CELL = 18;
    public static final int COLS = 11;
    public static final int GRID_TOP = 32;
    public static final int GRID_LEFT = 6;

    private static List<Entry> CACHED_ALL;

    public static void invalidateCache() {
        CACHED_ALL = null;
    }

    public IconPickerPopup(Consumer<String> onPick) {
        this.onPick = onPick;
        this.all = loadAll();
        var font = Minecraft.getInstance().font;
        this.searchBox = new EditBox(font, 0, 0, W - 12, 14, Component.literal("Search items…"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setValue(rememberedQuery);
        this.scroll = rememberedScroll;
        applyFilter(rememberedQuery);
    }

    private static List<Entry> loadAll() {
        if (CACHED_ALL != null) return CACHED_ALL;
        List<Entry> out = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null) continue;
            ItemStack stack = new ItemStack(item);
            String name;
            try { name = stack.getHoverName().getString(); }
            catch (Exception e) { name = id.getPath(); }
            out.add(new Entry(id, name, stack));
        }
        out.sort((a, b) -> a.id.toString().compareTo(b.id.toString()));
        CACHED_ALL = out;
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
        g.drawString(Minecraft.getInstance().font, "Pick an icon", x + 6, y + 7, 0xFFE6E9F5, true);

        // Re-filter when the search changes.
        String q = searchBox.getValue();
        if (!q.equals(lastQuery)) applyFilter(q);

        searchBox.setPosition(x + 6, y + 24);
        searchBox.setWidth(W - 12);
        searchBox.render(g, mouseX, mouseY, pt);

        // Grid
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

        // Hover tooltip — id + display name
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
        // Search box
        if (mouseY >= y + 24 && mouseY < y + 38) {
            searchBox.setFocused(true);
            searchBox.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        searchBox.setFocused(false);
        // Grid
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
            rememberedQuery = lastQuery;
            rememberedScroll = scroll;
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
