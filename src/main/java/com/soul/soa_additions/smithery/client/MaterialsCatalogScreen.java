package com.soul.soa_additions.smithery.client;

import com.soul.soa_additions.mining.MiningLevels;
import com.soul.soa_additions.quest.client.jei.JeiCompat;
import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.alloy.AlloyComponent;
import com.soul.smithery.api.alloy.AlloyDefinition;
import com.soul.smithery.api.cast.CastResults;
import com.soul.smithery.api.material.Material;
import com.soul.smithery.api.material.MaterialStats;
import com.soul.smithery.api.melting.MeltingRecipe;
import com.soul.smithery.api.modifier.ModifierEffect;
import com.soul.smithery.api.part.PartEligibility;
import com.soul.smithery.api.part.PartType;
import com.soul.smithery.api.stage.SmitheryStages;
import com.soul.smithery.api.synergy.SynergyDefinition;
import com.soul.smithery.api.tool.ToolType;
import com.soul.smithery.item.PartItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Browsable catalog of every registered Smithery material.
 *
 * <p>Successor to the TConstruct-era catalog deleted with Tinkers. The data model changed — Tinkers
 * split stats across per-part stat types, Smithery gives each material one block plus optional
 * armour and ranged sub-blocks — and so did the layout, because the old one hid everything behind
 * hover tooltips and a cycle button labelled "◀ right-click | left-click ▶". Here the stat groups
 * are plain tabs, sorting is a click on a column header, and selecting a material fills a detail
 * pane with the entire stat block. Nothing is reachable only by hovering the right pixel.</p>
 *
 * <p>The detail pane answers the whole question, not just "what are its numbers": which parts the
 * material actually casts into, what melts or presses into it, the alloy that produces it, which
 * tools it can go in, its stage gate, and every trait scope — universal, head-only, per-part and
 * per-tool-type. Trait scope in particular was invisible before: a material whose only traits are
 * keyed to a bow limb or a chestplate looked traitless.</p>
 *
 * <p>Every string a row draws — name, level label, formatted numbers, trait labels — is resolved
 * once when the row list is rebuilt, not per frame. Translation lookups and registry probes in a
 * render loop were the old screen's other problem: it re-resolved every visible trait name sixty
 * times a second. The detail block is heavier still, so it is built on first selection and cached
 * per row rather than for all 300-odd materials up front.</p>
 */
public final class MaterialsCatalogScreen extends Screen {

    // ── palette ──────────────────────────────────────────────────────────────────
    private static final int COL_BACKDROP    = 0xE8101014;
    private static final int COL_PANEL       = 0xFF1B1B22;
    private static final int COL_PANEL_ALT   = 0xFF212129;
    private static final int COL_HEADER      = 0xFF2A2A34;
    private static final int COL_BORDER      = 0xFF3C3C4A;
    private static final int COL_ACCENT      = 0xFF5AA9E6;
    private static final int COL_ROW_HOVER   = 0x30FFFFFF;
    private static final int COL_ROW_SEL     = 0x605AA9E6;
    private static final int COL_TEXT        = 0xFFE8E8F0;
    private static final int COL_TEXT_DIM    = 0xFF9A9AAC;
    private static final int COL_TEXT_FAINT  = 0xFF6A6A7A;
    private static final int COL_STAT_A      = 0xFFFF9E80;
    private static final int COL_STAT_B      = 0xFF9EE493;

    // ── metrics ──────────────────────────────────────────────────────────────────
    private static final int MARGIN = 10;
    private static final int TITLE_H = 22;
    private static final int TOOLBAR_H = 24;
    private static final int TABS_H = 22;
    private static final int COLHEAD_H = 16;
    private static final int ROW_H = 20;
    private static final int FOOTER_H = 14;
    private static final int DETAIL_MIN_W = 200;
    private static final int DETAIL_MAX_W = 300;
    private static final int SCROLLBAR_W = 6;
    private static final int ICON_PITCH = 18;
    private static final int MIN_NAME_W = 110;

    private static final ResourceLocation INGOT_PART =
            ResourceLocation.fromNamespaceAndPath("smithery", "ingot");
    private static final ResourceLocation NUGGET_PART =
            ResourceLocation.fromNamespaceAndPath("smithery", "nugget");

    /** Probing every part type per material is expensive; the answer never changes at runtime. */
    private static final Map<ResourceLocation, List<ItemStack>> PARTS_CACHE = new HashMap<>();

    private final List<Row> allRows = new ArrayList<>();
    private final List<Row> visible = new ArrayList<>();
    /** Icon hitboxes from the last detail-pane frame, for hover tooltips and click-through to JEI. */
    private final List<IconHit> iconHits = new ArrayList<>();

    private Index index;
    private EditBox search;
    private SortKey sortKey = SortKey.NAME;
    private boolean ascending = true;
    private int viewIndex = 0;
    private Row selected;

    private int scroll;
    private int listX, listY, listW, listH;
    /** Measured from the strings the columns actually hold, not guessed - see measureColumns(). */
    private int levelColW = 70, statAColW = 70, statBColW = 90;
    private int detailX, detailY, detailW, detailH;
    private int detailScroll;
    private int lastDetailW = -1;

    public MaterialsCatalogScreen() {
        super(Component.literal("Smithery Materials"));
    }

    // ────────────────────────────────────────────────────────────────────────────
    // layout
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        if (allRows.isEmpty()) loadMaterials();

        // init() runs again on resize and on every tab switch, so carry the typed filter across
        // rather than silently clearing what the player searched for.
        String previous = search == null ? "" : search.getValue();

        int contentTop = MARGIN + TITLE_H;
        int searchW = Math.min(200, this.width / 3);
        search = new EditBox(this.font, MARGIN + 4, contentTop + 3, searchW, 16, Component.literal("Filter"));
        search.setHint(Component.literal("Search name, id, trait or part").withStyle(ChatFormatting.DARK_GRAY));
        search.setResponder(s -> { refilter(); scroll = 0; });
        this.addRenderableWidget(search);
        if (!previous.isEmpty()) search.setValue(previous);

        // Stat groups as labelled tabs. A cycle button hides its own options; tabs show all of
        // them at once and take one click to reach any of them.
        int tabY = contentTop + TOOLBAR_H;
        int tabX = MARGIN + 4;
        for (int i = 0; i < StatView.ALL.size(); i++) {
            StatView v = StatView.ALL.get(i);
            int w = this.font.width(v.label) + 14;
            final int index = i;
            this.addRenderableWidget(Button.builder(
                            Component.literal(v.label).withStyle(i == viewIndex ? ChatFormatting.WHITE : ChatFormatting.GRAY),
                            b -> selectView(index))
                    .bounds(tabX, tabY, w, 18).build());
            tabX += w + 3;
        }

        listX = MARGIN;
        listY = contentTop + TOOLBAR_H + TABS_H + COLHEAD_H;
        // Narrow screens drop the pane rather than crush it; wide ones give it room, because the
        // detail block is now long enough that a 200px column wraps almost every line.
        detailW = this.width > 560 ? clamp(this.width / 3, DETAIL_MIN_W, DETAIL_MAX_W) : 0;
        listW = this.width - MARGIN * 2 - (detailW > 0 ? detailW + 6 : 0);
        listH = this.height - listY - MARGIN - FOOTER_H;

        detailX = listX + listW + 6;
        detailY = contentTop + TOOLBAR_H + TABS_H;
        detailH = this.height - detailY - MARGIN - FOOTER_H;

        // Detail text is wrapped to the pane at build time, so a resize has to throw it away.
        if (detailW != lastDetailW) {
            for (Row r : allRows) r.invalidateDetail();
            lastDetailW = detailW;
            detailScroll = 0;
        }

        measureColumns();
        refilter();
        scroll = clamp(scroll, 0, Math.max(0, visible.size() * ROW_H - listH));
    }

    private void selectView(int index) {
        viewIndex = index;
        recomputeViewStats();
        applySort();
        scroll = 0;
        this.rebuildWidgets();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // data
    // ────────────────────────────────────────────────────────────────────────────

    private void loadMaterials() {
        index = Index.build();
        for (Material material : SmitheryAPI.MATERIALS.all()) {
            try {
                allRows.add(new Row(material, this.font, index));
            } catch (Throwable ignored) {
                // A material with a malformed stat block shouldn't cost the whole catalog.
            }
        }
        recomputeViewStats();
    }

    private void recomputeViewStats() {
        StatView view = StatView.ALL.get(viewIndex);
        for (Row r : allRows) r.applyView(view);
    }

    private void refilter() {
        String needle = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        visible.clear();
        for (Row r : allRows) {
            if (needle.isEmpty() || r.matches(needle)) visible.add(r);
        }
        applySort();
    }

    private void applySort() {
        Comparator<Row> presence = Comparator.comparing((Row r) -> !r.hasStats);
        Comparator<Row> cmp = switch (sortKey) {
            case NAME   -> Comparator.comparing(r -> r.sortName);
            case LEVEL  -> Comparator.comparingInt(r -> r.level);
            case STAT_A -> Comparator.comparingDouble(r -> Float.isNaN(r.statA) ? Double.NEGATIVE_INFINITY : r.statA);
            case STAT_B -> Comparator.comparingDouble(r -> Float.isNaN(r.statB) ? Double.NEGATIVE_INFINITY : r.statB);
        };
        if (!ascending) cmp = cmp.reversed();
        visible.sort(presence.thenComparing(cmp).thenComparing(r -> r.sortName));
    }

    private void toggleSort(SortKey k) {
        if (sortKey == k) ascending = !ascending;
        else { sortKey = k; ascending = k != SortKey.STAT_A && k != SortKey.STAT_B; } // numbers: best first
        applySort();
        scroll = 0;
    }

    private List<DLine> detailLines() {
        if (selected == null) return List.of();
        return selected.detail(index, iconsPerRow(), wrapPixels());
    }

    private int iconsPerRow() { return Math.max(1, (detailW - 14) / ICON_PITCH); }

    /** Rough character budget for one wrapped detail line — the font averages ~5px per glyph. */
    /** Wrap width in pixels, the same budget {@link #drawCell} enforces when the line is drawn. */
    private int wrapPixels() { return Math.max(60, detailW - 12 - SCROLLBAR_W); }

    // ────────────────────────────────────────────────────────────────────────────
    // input
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Part icons in the detail pane are the fastest route from "what is this material" to
        // "how do I make one", so they take the click before anything else.
        if (detailW > 0 && mx >= detailX && mx < detailX + detailW && my >= detailY && my < detailY + detailH) {
            for (IconHit hit : iconHits) {
                if (hit.contains((int) mx, (int) my)) {
                    if (JeiCompat.available()) JeiCompat.showItem(hit.stack);
                    return true;
                }
            }
        }

        int headerY = listY - COLHEAD_H;
        if (my >= headerY && my < listY && mx >= listX && mx < listX + listW) {
            int[] bounds = columnBounds();
            for (SortKey k : SortKey.values()) {
                if (mx >= bounds[k.ordinal()] && mx < bounds[k.ordinal() + 1]) { toggleSort(k); return true; }
            }
            return true;
        }
        if (mx >= listX && mx < listX + listW && my >= listY && my < listY + listH) {
            int index = (int) ((my - listY) + scroll) / ROW_H;
            if (index >= 0 && index < visible.size()) {
                Row row = visible.get(index);
                // Second click on an already-selected row asks JEI what makes it — a plain click
                // never leaves the screen unexpectedly.
                if (selected == row && button == 0 && !row.icon.isEmpty() && JeiCompat.available()) {
                    JeiCompat.showItemUses(row.icon);
                } else {
                    selected = row;
                    detailScroll = 0;
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (detailW > 0 && mx >= detailX && mx < detailX + detailW) {
            int max = Math.max(0, detailContentHeight() - (detailH - 8));
            detailScroll = clamp(detailScroll - (int) (delta * 12), 0, max);
            return true;
        }
        int max = Math.max(0, visible.size() * ROW_H - listH);
        scroll = clamp(scroll - (int) (delta * ROW_H * 2), 0, max);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_UP) {
            if (visible.isEmpty()) return true;
            int i = selected == null ? -1 : visible.indexOf(selected);
            i = clamp(i + (key == GLFW.GLFW_KEY_DOWN ? 1 : -1), 0, visible.size() - 1);
            selected = visible.get(i);
            detailScroll = 0;
            ensureVisible(i);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER && selected != null && !selected.icon.isEmpty() && JeiCompat.available()) {
            JeiCompat.showItemUses(selected.icon);
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        // Typing anywhere goes to the search box, so nobody has to find and click it first.
        if (!search.isFocused() && !Character.isISOControl(c)) {
            search.setFocused(true);
            return search.charTyped(c, mods);
        }
        return super.charTyped(c, mods);
    }

    private void ensureVisible(int index) {
        int top = index * ROW_H;
        if (top < scroll) scroll = top;
        else if (top + ROW_H > scroll + listH) scroll = top + ROW_H - listH;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // render
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.fill(0, 0, this.width, this.height, COL_BACKDROP);

        g.drawString(this.font, "Smithery Materials", MARGIN + 2, MARGIN + 6, COL_TEXT, false);
        String hint = "Click a material for full stats  •  click a part icon for its recipe  •  Esc to close";
        g.drawString(this.font, hint, this.width - MARGIN - this.font.width(hint), MARGIN + 6, COL_TEXT_FAINT, false);

        super.render(g, mouseX, mouseY, partial);   // search box + tab buttons

        renderColumnHeader(g, mouseX, mouseY);
        Row hoveredRow = renderList(g, mouseX, mouseY);
        iconHits.clear();
        if (detailW > 0) renderDetail(g);

        String count = visible.size() + " of " + allRows.size() + " materials";
        g.drawString(this.font, count, MARGIN, this.height - FOOTER_H + 2, COL_TEXT_DIM, false);

        // Tooltips last, so they sit over both panes.
        ItemStack hoveredIcon = hoveredIcon(mouseX, mouseY);
        if (!hoveredIcon.isEmpty()) {
            g.renderTooltip(this.font, hoveredIcon, mouseX, mouseY);
        } else if (hoveredRow != null) {
            g.renderComponentTooltip(this.font, hoveredRow.summary(), mouseX, mouseY);
        }
    }

    private ItemStack hoveredIcon(int mouseX, int mouseY) {
        if (detailW <= 0 || mouseX < detailX || mouseX >= detailX + detailW
                || mouseY < detailY || mouseY >= detailY + detailH) {
            return ItemStack.EMPTY;
        }
        for (IconHit hit : iconHits) {
            if (hit.contains(mouseX, mouseY)) return hit.stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Sizes the three right-hand columns to the widest string they will ever hold, once per layout.
     *
     * <p>They used to be a flat 70px each, which is narrower than what goes in them: a level label
     * reads "Extraordinary (7)" at ~78px, so every row at that tier painted over the stat column
     * beside it. Measuring costs one pass over the roster on resize or tab switch and nothing per
     * frame; {@link #clip} then holds the invariant even for a label nothing measured.</p>
     */
    private void measureColumns() {
        StatView view = StatView.ALL.get(viewIndex);
        int level = this.font.width("Level ▲");
        int a = this.font.width(view.statALabel + " ▲");
        int b = this.font.width(view.statBLabel + " ▲");
        for (Row r : allRows) {
            level = Math.max(level, this.font.width(r.levelLabel));
            a = Math.max(a, this.font.width(r.statAText));
            b = Math.max(b, this.font.width(r.statBText));
        }
        levelColW = clamp(level + 10, 60, 130);
        statAColW = clamp(a + 10, 50, 110);
        // The last column runs under the scrollbar, so it has to give that width back.
        statBColW = clamp(b + 10 + SCROLLBAR_W, 56, 116);
    }

    private int[] columnBounds() {
        // name | level | stat A | stat B  — traits share the name column's row, drawn to the right.
        int lvl = levelColW, a = statAColW, b = statBColW;
        int room = Math.max(0, listW - MIN_NAME_W);
        int sum = lvl + a + b;
        if (sum > room) {   // a window too narrow for the measured widths shrinks them, not the name
            lvl = Math.max(24, lvl * room / sum);
            a = Math.max(20, a * room / sum);
            b = Math.max(20, b * room / sum);
        }
        int x0 = listX;
        int x1 = x0 + Math.max(MIN_NAME_W, listW - lvl - a - b);
        int x2 = x1 + lvl;
        int x3 = x2 + a;
        return new int[]{x0, x1, x2, x3, Math.max(x3, listX + listW)};
    }

    /** Truncates to fit, with an ellipsis, so no cell can paint over the column next to it. */
    private String clip(String text, int maxW) {
        if (maxW <= 0 || text.isEmpty()) return "";
        if (this.font.width(text) <= maxW) return text;
        int ellipsis = this.font.width("…");
        if (maxW <= ellipsis) return "";
        return this.font.plainSubstrByWidth(text, maxW - ellipsis) + "…";
    }

    private void drawCell(GuiGraphics g, String text, int x, int y, int maxW, int colour) {
        String fit = clip(text, maxW);
        if (!fit.isEmpty()) g.drawString(this.font, fit, x, y, colour, false);
    }

    /** Same guarantee for a styled line: split at the pane width and keep only what fits on one. */
    private void drawCell(GuiGraphics g, Component text, int x, int y, int maxW, int colour) {
        if (maxW <= 0) return;
        if (this.font.width(text) <= maxW) {
            g.drawString(this.font, text, x, y, colour, false);
            return;
        }
        List<FormattedCharSequence> parts = this.font.split(text, maxW);
        if (!parts.isEmpty()) g.drawString(this.font, parts.get(0), x, y, colour, false);
    }

    private void renderColumnHeader(GuiGraphics g, int mouseX, int mouseY) {
        int y = listY - COLHEAD_H;
        g.fill(listX, y, listX + listW, listY, COL_HEADER);
        g.fill(listX, listY - 1, listX + listW, listY, COL_BORDER);
        int[] b = columnBounds();
        StatView view = StatView.ALL.get(viewIndex);
        String[] labels = {"Material", "Level", view.statALabel, view.statBLabel};
        for (SortKey k : SortKey.values()) {
            int i = k.ordinal();
            boolean hovered = mouseX >= b[i] && mouseX < b[i + 1] && mouseY >= y && mouseY < listY;
            boolean active = sortKey == k;
            String label = labels[i] + (active ? (ascending ? " ▲" : " ▼") : "");
            drawCell(g, label, b[i] + 4, y + 4, b[i + 1] - b[i] - 8,
                    active ? COL_ACCENT : hovered ? COL_TEXT : COL_TEXT_DIM);
        }
    }

    /** Draws the list and returns the row under the cursor, if any. */
    private Row renderList(GuiGraphics g, int mouseX, int mouseY) {
        g.fill(listX, listY, listX + listW, listY + listH, COL_PANEL);
        g.enableScissor(listX, listY, listX + listW, listY + listH);

        int[] b = columnBounds();
        int first = Math.max(0, scroll / ROW_H);
        int last = Math.min(visible.size(), (scroll + listH) / ROW_H + 1);
        Row hoveredRow = null;
        // The row bands extend past the panel once scrolled, so the header sits over one of them.
        boolean inList = mouseX >= listX && mouseX < listX + listW
                && mouseY >= listY && mouseY < listY + listH;

        for (int i = first; i < last; i++) {
            Row row = visible.get(i);
            int top = listY + i * ROW_H - scroll;
            boolean hovered = inList && mouseY >= top && mouseY < top + ROW_H;
            if (hovered) hoveredRow = row;

            if (i % 2 == 1) g.fill(listX, top, listX + listW, top + ROW_H, COL_PANEL_ALT);
            if (row == selected) g.fill(listX, top, listX + listW, top + ROW_H, COL_ROW_SEL);
            else if (hovered) g.fill(listX, top, listX + listW, top + ROW_H, COL_ROW_HOVER);

            int textY = top + (ROW_H - 8) / 2;
            if (!row.icon.isEmpty()) g.renderItem(row.icon, listX + 3, top + (ROW_H - 16) / 2);
            int nameX = listX + 23;
            String name = clip(row.shortName, b[1] - 4 - nameX);
            g.drawString(this.font, name, nameX, textY, row.hasStats ? COL_TEXT : COL_TEXT_FAINT, false);

            // Traits ride in the leftover space of the name column: the single most-asked question
            // about a material, and it used to take a click to answer. Offset from the name as it
            // was actually drawn, not from the unclipped one.
            int traitX = nameX + this.font.width(name) + 8;
            int traitRoom = b[1] - 4 - traitX;
            if (traitRoom > 24) {
                String traits = row.traitsFor(this.font, traitRoom);
                if (!traits.isEmpty()) g.drawString(this.font, traits, traitX, textY, COL_TEXT_FAINT, false);
            }

            drawCell(g, row.levelLabel, b[1] + 4, textY, b[2] - b[1] - 8, row.levelColor);
            drawCell(g, row.statAText, b[2] + 4, textY, b[3] - b[2] - 8,
                    row.hasStats ? COL_STAT_A : COL_TEXT_FAINT);
            // The last column ends under the scrollbar track, so it stops short of it.
            drawCell(g, row.statBText, b[3] + 4, textY, b[4] - b[3] - 8 - SCROLLBAR_W,
                    row.hasStats ? COL_STAT_B : COL_TEXT_FAINT);
        }
        g.disableScissor();

        // Scrollbar only when there's something to scroll.
        int content = visible.size() * ROW_H;
        if (content > listH) {
            int trackX = listX + listW - SCROLLBAR_W;
            g.fill(trackX, listY, trackX + SCROLLBAR_W, listY + listH, 0x40000000);
            int thumbH = Math.max(20, listH * listH / content);
            int thumbY = listY + (listH - thumbH) * scroll / Math.max(1, content - listH);
            g.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH, COL_BORDER);
        }
        border(g, listX, listY, listW, listH);

        if (visible.isEmpty()) {
            String empty = "No materials match that search.";
            g.drawString(this.font, empty, listX + (listW - this.font.width(empty)) / 2, listY + listH / 2 - 4,
                    COL_TEXT_DIM, false);
        }
        return hoveredRow;
    }

    private int detailContentHeight() {
        int h = 26;
        for (DLine line : detailLines()) h += line.height();
        return h;
    }

    private void renderDetail(GuiGraphics g) {
        g.fill(detailX, detailY, detailX + detailW, detailY + detailH, COL_PANEL);
        border(g, detailX, detailY, detailW, detailH);

        if (selected == null) {
            String msg = "Select a material";
            g.drawString(this.font, msg, detailX + (detailW - this.font.width(msg)) / 2, detailY + detailH / 2 - 4,
                    COL_TEXT_FAINT, false);
            return;
        }

        List<DLine> lines = detailLines();
        g.enableScissor(detailX, detailY, detailX + detailW, detailY + detailH);
        int y = detailY + 6 - detailScroll;
        if (!selected.icon.isEmpty()) g.renderItem(selected.icon, detailX + 6, y);
        int textRight = detailW - 12 - (detailContentHeight() > detailH ? SCROLLBAR_W : 0);
        drawCell(g, selected.shortName, detailX + 26, y + 4, textRight - 20, COL_TEXT);
        y += 22;
        for (DLine line : lines) {
            if (line.icons != null) {
                int x = detailX + 6;
                for (ItemStack stack : line.icons) {
                    g.renderItem(stack, x, y);
                    // Hitboxes are recorded even when scrolled out of view is impossible — the
                    // scissor clips drawing, so only on-screen rows reach here.
                    if (y + 16 > detailY && y < detailY + detailH) iconHits.add(new IconHit(x, y, stack));
                    x += ICON_PITCH;
                }
            } else {
                drawCell(g, line.text, detailX + 6 + line.indent, y, textRight - line.indent, COL_TEXT);
            }
            y += line.height();
        }
        g.disableScissor();

        int content = detailContentHeight();
        if (content > detailH) {
            int trackX = detailX + detailW - SCROLLBAR_W;
            g.fill(trackX, detailY, trackX + SCROLLBAR_W, detailY + detailH, 0x40000000);
            int thumbH = Math.max(20, detailH * detailH / content);
            int thumbY = detailY + (detailH - thumbH) * detailScroll / Math.max(1, content - detailH);
            g.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH, COL_BORDER);
        }
    }

    private void border(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + 1, COL_BORDER);
        g.fill(x, y + h - 1, x + w, y + h, COL_BORDER);
        g.fill(x, y, x + 1, y + h, COL_BORDER);
        g.fill(x + w - 1, y, x + w, y + h, COL_BORDER);
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    @Override
    public boolean isPauseScreen() { return false; }

    // ────────────────────────────────────────────────────────────────────────────
    // model
    // ────────────────────────────────────────────────────────────────────────────

    public enum SortKey { NAME, LEVEL, STAT_A, STAT_B }

    /** One line of the detail pane: either a text run at an indent, or a row of item icons. */
    private static final class DLine {
        final Component text;
        final List<ItemStack> icons;
        final int indent;

        private DLine(Component text, List<ItemStack> icons, int indent) {
            this.text = text; this.icons = icons; this.indent = indent;
        }

        static DLine of(Component text) { return new DLine(text, null, 0); }
        static DLine of(Component text, int indent) { return new DLine(text, null, indent); }
        static DLine icons(List<ItemStack> icons) { return new DLine(null, icons, 0); }

        int height() { return icons != null ? ICON_PITCH : 10; }
    }

    /** A drawn item icon and where it landed, so a click or hover can find it again. */
    private record IconHit(int x, int y, ItemStack stack) {
        boolean contains(int mx, int my) { return mx >= x && mx < x + 16 && my >= y && my < y + 16; }
    }

    /**
     * Reverse lookups the per-material detail needs, built once when the screen opens.
     *
     * <p>Smithery indexes melting and press recipes by <em>input item</em> because that is what
     * the machines look up; a catalog wants the opposite direction, and walking several thousand
     * recipe entries per selected material would be felt.</p>
     */
    private static final class Index {
        final Map<ResourceLocation, List<ItemStack>> meltInputs = new HashMap<>();
        final Map<ResourceLocation, List<ItemStack>> pressInputs = new HashMap<>();
        final Map<ResourceLocation, List<AlloyDefinition>> alloysByResult = new HashMap<>();
        final Map<ResourceLocation, List<SynergyDefinition>> synergies = new HashMap<>();
        final Map<ResourceLocation, String> stageById;
        final Map<String, String> stageByPath;

        private Index(Map<ResourceLocation, String> stageById, Map<String, String> stageByPath) {
            this.stageById = stageById;
            this.stageByPath = stageByPath;
        }

        static Index build() {
            SmitheryStages.Snapshot gates = SmitheryStages.snapshot();
            Index idx = new Index(gates.materials(), gates.materialPaths());

            for (MeltingRecipe recipe : SmitheryAPI.MELTING_RECIPES.values()) {
                ItemStack stack = stackOf(recipe.inputItemId());
                // Part items remelt into their own material; listing all 25 of them as "sources"
                // buries the two or three items a player actually smelts.
                if (!stack.isEmpty() && !(stack.getItem() instanceof PartItem)) {
                    idx.meltInputs.computeIfAbsent(recipe.outputMaterialId(), k -> new ArrayList<>()).add(stack);
                }
            }
            for (Map.Entry<ResourceLocation, ResourceLocation> e : SmitheryAPI.PRESS_INPUTS.entrySet()) {
                ItemStack stack = stackOf(e.getKey());
                if (!stack.isEmpty()) {
                    idx.pressInputs.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(stack);
                }
            }
            for (AlloyDefinition alloy : SmitheryAPI.ALLOYS.all()) {
                idx.alloysByResult.computeIfAbsent(alloy.resultMaterialId(), k -> new ArrayList<>()).add(alloy);
            }
            for (SynergyDefinition syn : SmitheryAPI.SYNERGIES.all()) {
                idx.synergies.computeIfAbsent(syn.materialA(), k -> new ArrayList<>()).add(syn);
                idx.synergies.computeIfAbsent(syn.materialB(), k -> new ArrayList<>()).add(syn);
            }
            return idx;
        }

        String stageFor(ResourceLocation materialId) {
            String stage = stageById.get(materialId);
            return stage != null ? stage : stageByPath.get(materialId.getPath());
        }

        static ItemStack stackOf(ResourceLocation itemId) {
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }
    }

    /**
     * A pair of comparable numbers pulled from a material's stat block, plus the test for whether
     * the material has that block at all — a material with no armour shows "—" rather than a
     * misleading zero.
     */
    public static final class StatView {
        final String label;
        final String statALabel;
        final String statBLabel;
        final Predicate<MaterialStats> applies;
        final Function<MaterialStats, Float> a;
        final Function<MaterialStats, Float> b;

        StatView(String label, String aLabel, String bLabel, Predicate<MaterialStats> applies,
                 Function<MaterialStats, Float> a, Function<MaterialStats, Float> b) {
            this.label = label; this.statALabel = aLabel; this.statBLabel = bLabel;
            this.applies = applies; this.a = a; this.b = b;
        }

        public static final List<StatView> ALL = List.of(
                new StatView("Tool", "Speed", "Attack", s -> true,
                        MaterialStats::miningSpeed, MaterialStats::attackDamage),
                new StatView("Durability", "Per Ingot", "Binder x", s -> true,
                        s -> (float) s.durabilityPerIngot(), MaterialStats::binderMultiplier),
                new StatView("Smelting", "Melt C", "Per Ingot", s -> true,
                        MaterialStats::meltingTemp, s -> (float) s.durabilityPerIngot()),
                new StatView("Armour", "Defense", "Tough", MaterialStats::supportsArmor,
                        s -> s.armorStats().coreDefense(), s -> s.armorStats().platesToughness()),
                new StatView("Bow", "Draw", "Range", MaterialStats::supportsBow,
                        s -> s.rangedStats().drawSpeed(), s -> s.rangedStats().range()),
                new StatView("Arrow", "Accuracy", "Ammo", s -> s.rangedStats() != null,
                        s -> s.rangedStats().accuracy(), s -> (float) s.rangedStats().bonusAmmo())
        );
    }

    /** One material, with every string the list needs already resolved. */
    static final class Row {
        /** Kept so the detail builder can wrap to measured widths rather than a glyph estimate. */
        private final Font font;
        final ResourceLocation id;
        final MaterialStats stats;
        final String name;
        final String shortName;
        final String sortName;
        final String searchBlob;
        final int level;
        final String levelLabel;
        final int levelColor;
        final List<ItemStack> parts;
        final ItemStack icon;
        final String stage;
        final String traitLine;
        final List<ModifierEffect> headline;

        float statA = Float.NaN;
        float statB = Float.NaN;
        String statAText = "—";
        String statBText = "—";
        boolean hasStats;

        private List<DLine> detail;
        private List<Component> summary;
        private String traitFit = "";
        private int traitFitRoom = -1;

        Row(Material material, Font font, Index index) {
            this.font = font;
            this.id = material.id();
            this.stats = material.stats();
            this.name = Component.translatable(PartItem.materialTranslationKey(id)).getString();
            this.sortName = name.toLowerCase(Locale.ROOT);
            this.level = stats.harvestLevel();
            this.levelLabel = MiningLevels.levelName(level).getString() + " (" + level + ")";
            this.levelColor = 0xFF000000 | levelTint(level);
            this.parts = partsFor(id);
            this.stage = index.stageFor(id);

            // The row's icon is a part when the material has one; a cast-only or press-only
            // material falls back to whatever item it comes from, so no row is left blank.
            ItemStack chosen = parts.isEmpty() ? ItemStack.EMPTY : parts.get(0);
            if (chosen.isEmpty()) chosen = first(index.meltInputs.get(id));
            if (chosen.isEmpty()) chosen = first(index.pressInputs.get(id));
            if (chosen.isEmpty()) chosen = castResult(id, INGOT_PART);
            this.icon = chosen;

            // Universal and head traits are the ones every tool built from this material gets;
            // the scoped ones are listed by scope in the detail pane instead of muddling this.
            List<ModifierEffect> traits = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (ModifierEffect e : stats.universalModifiers()) if (seen.add(key(e))) traits.add(e);
            for (ModifierEffect e : stats.headModifiers()) if (seen.add(key(e))) traits.add(e);
            this.headline = List.copyOf(traits);

            StringBuilder line = new StringBuilder();
            StringBuilder blob = new StringBuilder(sortName).append(' ').append(id).append(' ');
            for (ModifierEffect e : traits) {
                String label = modifierName(e.modifierId());
                if (line.length() > 0) line.append(", ");
                line.append(label);
                blob.append(label.toLowerCase(Locale.ROOT)).append(' ').append(e.modifierId()).append(' ');
            }
            this.traitLine = line.toString();
            for (ItemStack part : parts) blob.append(part.getHoverName().getString().toLowerCase(Locale.ROOT)).append(' ');
            if (stage != null) blob.append(stage.toLowerCase(Locale.ROOT)).append(' ');
            this.searchBlob = blob.toString().toLowerCase(Locale.ROOT);

            int maxName = 200;
            String n = name;
            if (font.width(n) > maxName) {
                while (n.length() > 1 && font.width(n + "...") > maxName) n = n.substring(0, n.length() - 1);
                n = n + "...";
            }
            this.shortName = n;
        }

        private static String key(ModifierEffect e) { return e.modifierId().toString(); }

        boolean matches(String needle) { return searchBlob.contains(needle); }

        void invalidateDetail() { detail = null; }

        /** Trait text clipped to the space left in the name column, recomputed only on resize. */
        String traitsFor(Font font, int room) {
            if (traitLine.isEmpty()) return "";
            if (room == traitFitRoom) return traitFit;
            traitFitRoom = room;
            traitFit = font.width(traitLine) <= room
                    ? traitLine
                    : font.plainSubstrByWidth(traitLine, Math.max(4, room - 6)) + "…";
            return traitFit;
        }

        void applyView(StatView view) {
            hasStats = safeApplies(view);
            statA = hasStats ? safe(view.a) : Float.NaN;
            statB = hasStats ? safe(view.b) : Float.NaN;
            statAText = hasStats ? fmt(statA) : "—";
            statBText = hasStats ? fmt(statB) : "—";
        }

        private boolean safeApplies(StatView v) {
            try { return v.applies.test(stats); } catch (Throwable t) { return false; }
        }

        private float safe(Function<MaterialStats, Float> f) {
            try { return f.apply(stats); } catch (Throwable t) { return Float.NaN; }
        }

        /** Compact hover card for the list row — the numbers the current tab is hiding. */
        List<Component> summary() {
            if (summary != null) return summary;
            List<Component> t = new ArrayList<>();
            t.add(Component.literal(name).withStyle(ChatFormatting.WHITE));
            t.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (stage != null) {
                t.add(Component.literal("Stage: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(stage).withStyle(ChatFormatting.GOLD)));
            }
            t.add(stat("Mining Level", levelLabel));
            t.add(stat("Speed / Attack", fmt(stats.miningSpeed()) + " / " + fmt(stats.attackDamage())));
            t.add(stat("Durability/Ingot", String.valueOf(stats.durabilityPerIngot())));
            if (stats.meltingTemp() > 0f) t.add(stat("Melts at", fmt(stats.meltingTemp()) + " C"));
            t.add(stat("Parts", parts.isEmpty() ? "none" : String.valueOf(parts.size())));
            List<String> forms = new ArrayList<>();
            if (stats.supportsArmor()) forms.add("armour");
            if (stats.supportsBow()) forms.add("bow");
            if (stats.castOnly()) forms.add("cast-only");
            if (!forms.isEmpty()) t.add(stat("Supports", String.join(", ", forms)));
            if (!traitLine.isEmpty()) {
                t.add(Component.literal("Traits: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(traitLine).withStyle(ChatFormatting.AQUA)));
            }
            summary = t;
            return t;
        }

        /** The whole stat block, so the detail pane never sends anyone hunting elsewhere. */
        List<DLine> detail(Index index, int iconsPerRow, int wrapPx) {
            if (detail == null) detail = buildDetail(index, iconsPerRow, wrapPx);
            return detail;
        }

        private List<DLine> buildDetail(Index index, int iconsPerRow, int wrapPx) {
            List<DLine> t = new ArrayList<>();
            t.add(DLine.of(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY)));
            if (stage != null) {
                t.add(DLine.of(Component.literal("Locked until stage: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(stage).withStyle(ChatFormatting.GOLD))));
            }

            addParts(t, iconsPerRow, wrapPx);

            t.add(DLine.of(Component.empty()));
            t.add(DLine.of(header("Tool")));
            t.add(DLine.of(stat("Mining Level", levelLabel)));
            t.add(DLine.of(stat("Mining Speed", fmt(stats.miningSpeed()))));
            t.add(DLine.of(stat("Attack Damage", fmt(stats.attackDamage()))));
            t.add(DLine.of(stat("Durability/Ingot", String.valueOf(stats.durabilityPerIngot()))));
            t.add(DLine.of(stat("Binder Multiplier", fmt(stats.binderMultiplier()))));
            t.add(DLine.of(stat("Melting Temp", stats.meltingTemp() > 0f ? fmt(stats.meltingTemp()) + " C" : "does not melt")));
            t.add(DLine.of(stat("Fluid Base", String.valueOf(stats.fluidBase()))));
            String fluid = fluidName();
            if (fluid != null) t.add(DLine.of(stat("Fluid", fluid)));
            if (stats.storageForms()) t.add(DLine.of(stat("Storage Forms", "block, ingot, nugget")));
            if (stats.castOnly()) t.add(DLine.of(stat("Cast Only", "yes — no parts, casting only")));
            if (stats.foil()) t.add(DLine.of(stat("Foil", "yes")));
            if (stats.emissive()) t.add(DLine.of(stat("Emissive", "yes")));

            if (stats.supportsArmor()) {
                MaterialStats.ArmorStats a = stats.armorStats();
                t.add(DLine.of(Component.empty()));
                t.add(DLine.of(header("Armour")));
                t.add(DLine.of(stat("Core Durability", fmt(a.coreDurability()))));
                t.add(DLine.of(stat("Core Defense", fmt(a.coreDefense()))));
                t.add(DLine.of(stat("Plates Durability", fmt(a.platesDurability()))));
                t.add(DLine.of(stat("Plates Modifier", fmt(a.platesModifier()))));
                t.add(DLine.of(stat("Plates Toughness", fmt(a.platesToughness()))));
                t.add(DLine.of(stat("Trim Durability", fmt(a.trimDurability()))));
            }

            MaterialStats.RangedStats r = stats.rangedStats();
            // rangedStats() is never null — materials without bow stats get an all-default block,
            // and printing eight zeroes for every metal in the pack is noise, not information.
            if (r != null && hasRangedStats(r)) {
                t.add(DLine.of(Component.empty()));
                t.add(DLine.of(header(stats.supportsBow() ? "Ranged" : "Ranged (arrow parts only)")));
                t.add(DLine.of(stat("Draw Speed", fmt(r.drawSpeed()))));
                t.add(DLine.of(stat("Range", fmt(r.range()))));
                t.add(DLine.of(stat("Bonus Damage", fmt(r.bonusDamage()))));
                t.add(DLine.of(stat("Bowstring", fmt(r.bowstring()))));
                t.add(DLine.of(stat("Shaft Modifier", fmt(r.shaftModifier()))));
                t.add(DLine.of(stat("Bonus Ammo", String.valueOf(r.bonusAmmo()))));
                t.add(DLine.of(stat("Accuracy", fmt(r.accuracy()))));
                t.add(DLine.of(stat("Fletching Mod", fmt(r.fletchingModifier()))));
            }

            addTraits(t, wrapPx);
            addToolTypes(t, wrapPx);

            List<Component> slots = new ArrayList<>();
            for (PartType pt : SmitheryAPI.PART_TYPES.all()) {
                int n = stats.modifierSlotsFor(pt);
                if (n > 0) slots.add(stat(partName(pt.id()), String.valueOf(n)));
            }
            if (!slots.isEmpty()) {
                t.add(DLine.of(Component.empty()));
                t.add(DLine.of(header("Modifier Slots")));
                for (Component c : slots) t.add(DLine.of(c));
            }

            addSources(t, index, iconsPerRow, wrapPx);
            addSynergies(t, index, wrapPx);
            return t;
        }

        /** Every part item this material casts or presses into — the answer to "what can I make". */
        private void addParts(List<DLine> t, int iconsPerRow, int wrapPx) {
            t.add(DLine.of(Component.empty()));
            if (parts.isEmpty()) {
                t.add(DLine.of(header("Parts")));
                String why = stats.castOnly()
                        ? "Cast-only material: it fills casts and casting basins but has no tool parts."
                        : "No part items are registered for this material.";
                for (String s : wrap(why, wrapPx)) {
                    t.add(DLine.of(Component.literal(s).withStyle(ChatFormatting.GRAY)));
                }
                return;
            }
            t.add(DLine.of(header("Parts (" + parts.size() + ")")));
            for (int i = 0; i < parts.size(); i += iconsPerRow) {
                t.add(DLine.icons(List.copyOf(parts.subList(i, Math.min(parts.size(), i + iconsPerRow)))));
            }
            for (String s : wrap("Hover for a name, click for its recipe.", wrapPx)) {
                t.add(DLine.of(Component.literal(s).withStyle(ChatFormatting.DARK_GRAY)));
            }
        }

        /**
         * Traits by scope. A trait keyed to a bow limb or a chestplate is not a trait every tool
         * gets, and lumping the four scopes together made materials look like they granted far
         * more than they do — or, when a material had only scoped traits, nothing at all.
         */
        private void addTraits(List<DLine> t, int wrapPx) {
            List<DLine> body = new ArrayList<>();
            addTraitGroup(body, "From any part", stats.universalModifiers(), wrapPx);
            addTraitGroup(body, "As the head", stats.headModifiers(), wrapPx);
            for (PartType pt : SmitheryAPI.PART_TYPES.all()) {
                addTraitGroup(body, "As " + partName(pt.id()).toLowerCase(Locale.ROOT),
                        stats.partModifiers(pt), wrapPx);
            }
            for (ToolType tt : SmitheryAPI.TOOL_TYPES.all()) {
                addTraitGroup(body, "On " + toolName(tt.id()).toLowerCase(Locale.ROOT),
                        stats.toolTypeModifiers(tt), wrapPx);
            }
            if (body.isEmpty()) return;
            t.add(DLine.of(Component.empty()));
            t.add(DLine.of(header("Traits")));
            t.addAll(body);
        }

        private void addTraitGroup(List<DLine> t, String scope, List<ModifierEffect> effects, int wrapPx) {
            if (effects == null || effects.isEmpty()) return;
            t.add(DLine.of(Component.literal(scope).withStyle(ChatFormatting.DARK_AQUA)));
            for (ModifierEffect e : effects) {
                Component label = Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(modifierName(e.modifierId())).withStyle(ChatFormatting.AQUA));
                String params = params(e);
                if (!params.isEmpty()) {
                    label = label.copy().append(Component.literal(" " + params).withStyle(ChatFormatting.DARK_GRAY));
                }
                t.add(DLine.of(label, 4));
                String desc = PartItem.modifierDescription(e.modifierId()).getString();
                for (String line : wrap(desc, wrapPx - 10)) {
                    t.add(DLine.of(Component.literal(line).withStyle(ChatFormatting.GRAY), 10));
                }
            }
        }

        /** Which tool templates this material can occupy at all, and which it can fill entirely. */
        private void addToolTypes(List<DLine> t, int wrapPx) {
            // Eligibility is declared per part type even for materials that never mint a part item,
            // so a cast-only material would otherwise claim a slot in every tool in the game.
            if (parts.isEmpty()) return;
            List<String> whole = new ArrayList<>();
            List<String> partial = new ArrayList<>();
            for (ToolType tt : SmitheryAPI.TOOL_TYPES.all()) {
                int allowed = 0;
                for (ToolType.Slot slot : tt.slots()) {
                    if (PartEligibility.isAllowed(slot.partType().id(), id)) allowed++;
                }
                if (allowed == 0) continue;
                (allowed == tt.slots().size() ? whole : partial).add(toolName(tt.id()));
            }
            if (whole.isEmpty() && partial.isEmpty()) return;
            t.add(DLine.of(Component.empty()));
            t.add(DLine.of(header("Goes Into")));
            if (!whole.isEmpty()) {
                t.add(DLine.of(Component.literal("Every slot of").withStyle(ChatFormatting.DARK_AQUA)));
                for (String s : wrap(String.join(", ", whole), wrapPx - 4)) {
                    t.add(DLine.of(Component.literal(s).withStyle(ChatFormatting.GRAY), 4));
                }
            }
            if (!partial.isEmpty()) {
                t.add(DLine.of(Component.literal("Some slots of").withStyle(ChatFormatting.DARK_AQUA)));
                for (String s : wrap(String.join(", ", partial), wrapPx - 4)) {
                    t.add(DLine.of(Component.literal(s).withStyle(ChatFormatting.GRAY), 4));
                }
            }
        }

        /** Where the material comes from: alloy recipe, meltable items, press inputs, cast results. */
        private void addSources(List<DLine> t, Index index, int iconsPerRow, int wrapPx) {
            List<AlloyDefinition> alloys = index.alloysByResult.get(id);
            List<ItemStack> melts = index.meltInputs.get(id);
            List<ItemStack> presses = index.pressInputs.get(id);
            ItemStack ingot = castResult(id, INGOT_PART);
            ItemStack nugget = castResult(id, NUGGET_PART);
            if (alloys == null && melts == null && presses == null && ingot.isEmpty() && nugget.isEmpty()) return;

            t.add(DLine.of(Component.empty()));
            t.add(DLine.of(header("Obtaining")));

            if (alloys != null) {
                for (AlloyDefinition alloy : alloys) {
                    StringBuilder sb = new StringBuilder();
                    for (AlloyComponent c : alloy.components()) {
                        if (sb.length() > 0) sb.append(" + ");
                        sb.append(c.ratio()).append(" ").append(materialName(c.materialId()));
                    }
                    sb.append("  →  ").append(alloy.batchOutputMb()).append(" mB");
                    t.add(DLine.of(Component.literal("Alloy").withStyle(ChatFormatting.DARK_AQUA)));
                    for (String s : wrap(sb.toString(), wrapPx - 4)) {
                        t.add(DLine.of(Component.literal(s).withStyle(ChatFormatting.GRAY), 4));
                    }
                    t.add(DLine.of(stat("  Min temp", fmt(alloy.minTemp()) + " C"), 4));
                }
            }
            if (melts != null) {
                t.add(DLine.of(Component.literal("Melts from").withStyle(ChatFormatting.DARK_AQUA)));
                addIconRows(t, melts, iconsPerRow, 12);
            }
            if (presses != null) {
                t.add(DLine.of(Component.literal("Part press").withStyle(ChatFormatting.DARK_AQUA)));
                addIconRows(t, presses, iconsPerRow, 12);
            }
            List<ItemStack> casts = new ArrayList<>();
            if (!ingot.isEmpty()) casts.add(ingot);
            if (!nugget.isEmpty()) casts.add(nugget);
            if (!casts.isEmpty()) {
                t.add(DLine.of(Component.literal("Casts into").withStyle(ChatFormatting.DARK_AQUA)));
                addIconRows(t, casts, iconsPerRow, 12);
            }
        }

        /** Two-material bonuses — invisible everywhere else until a tool is already built. */
        private void addSynergies(List<DLine> t, Index index, int wrapPx) {
            List<SynergyDefinition> synergies = index.synergies.get(id);
            if (synergies == null || synergies.isEmpty()) return;
            t.add(DLine.of(Component.empty()));
            t.add(DLine.of(header("Synergies")));
            for (SynergyDefinition syn : synergies) {
                ResourceLocation other = syn.materialA().equals(id) ? syn.materialB() : syn.materialA();
                t.add(DLine.of(Component.literal("With " + materialName(other)).withStyle(ChatFormatting.DARK_AQUA)));
                for (Map.Entry<ResourceLocation, ModifierEffect> e : syn.effectsPerToolType().entrySet()) {
                    String line = modifierName(e.getValue().modifierId()) + " on " + toolName(e.getKey());
                    for (String s : wrap(line, wrapPx - 10)) {
                        t.add(DLine.of(Component.literal(s).withStyle(ChatFormatting.GRAY), 4));
                    }
                }
            }
        }

        private static void addIconRows(List<DLine> t, List<ItemStack> stacks, int iconsPerRow, int cap) {
            List<ItemStack> shown = stacks.size() > cap ? stacks.subList(0, cap) : stacks;
            for (int i = 0; i < shown.size(); i += iconsPerRow) {
                t.add(DLine.icons(List.copyOf(shown.subList(i, Math.min(shown.size(), i + iconsPerRow)))));
            }
            if (stacks.size() > cap) {
                t.add(DLine.of(Component.literal("+" + (stacks.size() - cap) + " more")
                        .withStyle(ChatFormatting.DARK_GRAY), 4));
            }
        }

        private static boolean hasRangedStats(MaterialStats.RangedStats r) {
            return r.drawSpeed() > 0f || r.range() > 0f || r.bonusDamage() > 0f || r.bonusAmmo() != 0
                    || r.bowstring() != 1f || r.shaftModifier() != 1f
                    || r.accuracy() != 1f || r.fletchingModifier() != 1f;
        }

        private String fluidName() {
            ResourceLocation bound = stats.boundFluid();
            ResourceLocation fluidId = bound != null
                    ? bound
                    : ResourceLocation.fromNamespaceAndPath("smithery", "molten_" + id.getPath());
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) return null;
            try {
                return fluid.getFluidType().getDescription().getString();
            } catch (Throwable t) {
                return fluidId.toString();
            }
        }

        private static String params(ModifierEffect e) {
            Map<String, Object> params = e.params();
            if (params == null || params.isEmpty()) return "";
            StringBuilder sb = new StringBuilder("(");
            for (Map.Entry<String, Object> p : params.entrySet()) {
                if (sb.length() > 1) sb.append(", ");
                sb.append(p.getKey()).append('=').append(p.getValue());
            }
            return sb.append(')').toString();
        }

        /**
         * Greedy word wrap to a pixel width.
         *
         * <p>This used to count characters against a "~5px per glyph" estimate, which ran about a
         * tenth over on ordinary text: the builder emitted lines wider than the pane, and the draw
         * side then quietly dropped the last word or two of each. Measuring with the font that will
         * draw the line removes the guess.</p>
         */
        private List<String> wrap(String s, int width) {
            List<String> out = new ArrayList<>();
            if (s == null || s.isEmpty()) return out;
            int space = font.width(" ");
            StringBuilder line = new StringBuilder();
            int lineW = 0;
            for (String word : s.split("\\s+")) {
                int wordW = font.width(word);
                if (lineW > 0 && lineW + space + wordW > width) {
                    out.add(line.toString());
                    line.setLength(0);
                    lineW = 0;
                }
                if (lineW > 0) { line.append(' '); lineW += space; }
                line.append(word);
                lineW += wordW;
            }
            if (line.length() > 0) out.add(line.toString());
            return out;
        }

        private static Component header(String s) {
            return Component.literal(s).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }

        private static Component stat(String label, String value) {
            return Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
        }

        private static String fmt(float v) {
            if (Float.isNaN(v)) return "—";
            if (v == Math.floor(v) && Math.abs(v) < 1_000_000f) return String.valueOf((int) v);
            return String.format(Locale.ROOT, "%.2f", v);
        }

        private static String materialName(ResourceLocation materialId) {
            return Component.translatable(PartItem.materialTranslationKey(materialId)).getString();
        }

        private static String partName(ResourceLocation partTypeId) {
            return Component.translatable(PartItem.partTranslationKey(partTypeId)).getString();
        }

        private static String toolName(ResourceLocation toolTypeId) {
            return Component.translatable(PartItem.toolTypeTranslationKey(toolTypeId)).getString();
        }

        private static String modifierName(ResourceLocation modifierId) {
            return Component.translatable(PartItem.modifierTranslationKey(modifierId)).getString();
        }

        private static ItemStack first(List<ItemStack> stacks) {
            return stacks == null || stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
        }

        private static ItemStack castResult(ResourceLocation materialId, ResourceLocation partTypeId) {
            try {
                Item item = CastResults.resolve(materialId, partTypeId);
                return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
            } catch (Throwable t) {
                return ItemStack.EMPTY;
            }
        }

        /** GC's ladder colour for the level, so the column reads at a glance. */
        private static int levelTint(int level) {
            Integer rgb = MiningLevels.levelColour(level).getColor();
            return rgb != null ? rgb : 0xFFFFFF;
        }

        /**
         * Every registered part item for the material, in part-type order.
         *
         * <p>Smithery names them {@code <material path>_<part path>} but registers them in
         * <em>whichever mod's</em> item register asked for them, not in {@code smithery:} — SOA's
         * 300-odd materials land in {@code soa_additions:}. Probing only the Smithery namespace was
         * why almost every row in this screen used to show no parts at all. Cached, because this
         * walks every part type in three namespaces.</p>
         */
        private static List<ItemStack> partsFor(ResourceLocation materialId) {
            return PARTS_CACHE.computeIfAbsent(materialId, mid -> {
                List<ItemStack> found = new ArrayList<>();
                for (PartType pt : SmitheryAPI.PART_TYPES.all()) {
                    if (pt.syntheticCast()) continue;
                    Item item = findPart(mid, pt.id());
                    if (item != null) found.add(new ItemStack(item));
                }
                return List.copyOf(found);
            });
        }

        private static Item findPart(ResourceLocation materialId, ResourceLocation partTypeId) {
            String path = materialId.getPath() + "_" + partTypeId.getPath();
            for (String namespace : new String[]{materialId.getNamespace(), "soa_additions", "smithery"}) {
                Item item = ForgeRegistries.ITEMS.getValue(
                        ResourceLocation.fromNamespaceAndPath(namespace, path));
                // The instanceof check keeps an unrelated item that happens to share the composed
                // name out of the part list.
                if (item instanceof PartItem) return item;
            }
            return null;
        }
    }
}
