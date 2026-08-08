package com.soul.soa_additions.smithery.client;

import com.soul.soa_additions.mining.MiningLevels;
import com.soul.soa_additions.quest.client.jei.JeiCompat;
import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.material.Material;
import com.soul.smithery.api.material.MaterialStats;
import com.soul.smithery.api.modifier.ModifierEffect;
import com.soul.smithery.api.part.PartType;
import com.soul.smithery.item.PartItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * <p>Every string a row draws — name, level label, formatted numbers, trait labels — is resolved
 * once when the row list is rebuilt, not per frame. Translation lookups and registry probes in a
 * render loop were the old screen's other problem: it re-resolved every visible trait name sixty
 * times a second.</p>
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
    private static final int DETAIL_W = 200;
    private static final int SCROLLBAR_W = 6;

    /** Probing every part type per material is expensive; the answer never changes at runtime. */
    private static final Map<ResourceLocation, ItemStack> ICON_CACHE = new HashMap<>();

    private final List<Row> allRows = new ArrayList<>();
    private final List<Row> visible = new ArrayList<>();

    private EditBox search;
    private SortKey sortKey = SortKey.NAME;
    private boolean ascending = true;
    private int viewIndex = 0;
    private Row selected;

    private int scroll;
    private int listX, listY, listW, listH;
    private int detailX, detailY, detailW, detailH;
    private int detailScroll;

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
        search.setHint(Component.literal("Search name, id or trait").withStyle(ChatFormatting.DARK_GRAY));
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
        detailW = this.width > 560 ? DETAIL_W : 0;   // narrow screens drop the pane rather than crush it
        listW = this.width - MARGIN * 2 - (detailW > 0 ? detailW + 6 : 0);
        listH = this.height - listY - MARGIN - FOOTER_H;

        detailX = listX + listW + 6;
        detailY = contentTop + TOOLBAR_H + TABS_H;
        detailH = this.height - detailY - MARGIN - FOOTER_H;

        refilter();
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
        for (Material material : SmitheryAPI.MATERIALS.all()) {
            try {
                allRows.add(new Row(material, this.font));
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

    // ────────────────────────────────────────────────────────────────────────────
    // input
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
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
        String hint = "Click a material for full stats  •  click again for recipes  •  Esc to close";
        g.drawString(this.font, hint, this.width - MARGIN - this.font.width(hint), MARGIN + 6, COL_TEXT_FAINT, false);

        super.render(g, mouseX, mouseY, partial);   // search box + tab buttons

        renderColumnHeader(g, mouseX, mouseY);
        renderList(g, mouseX, mouseY);
        if (detailW > 0) renderDetail(g);

        String count = visible.size() + " of " + allRows.size() + " materials";
        g.drawString(this.font, count, MARGIN, this.height - FOOTER_H + 2, COL_TEXT_DIM, false);
    }

    private int[] columnBounds() {
        // name | level | stat A | stat B  — traits share the name column's row, drawn to the right.
        int nameW = Math.max(120, listW - 70 - 70 - 90);
        int x0 = listX;
        int x1 = x0 + nameW;
        int x2 = x1 + 70;
        int x3 = x2 + 70;
        return new int[]{x0, x1, x2, x3, listX + listW};
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
            g.drawString(this.font, label, b[i] + 4, y + 4, active ? COL_ACCENT : hovered ? COL_TEXT : COL_TEXT_DIM, false);
        }
    }

    private void renderList(GuiGraphics g, int mouseX, int mouseY) {
        g.fill(listX, listY, listX + listW, listY + listH, COL_PANEL);
        g.enableScissor(listX, listY, listX + listW, listY + listH);

        int[] b = columnBounds();
        int first = Math.max(0, scroll / ROW_H);
        int last = Math.min(visible.size(), (scroll + listH) / ROW_H + 1);

        for (int i = first; i < last; i++) {
            Row row = visible.get(i);
            int top = listY + i * ROW_H - scroll;
            boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= top && mouseY < top + ROW_H;

            if (i % 2 == 1) g.fill(listX, top, listX + listW, top + ROW_H, COL_PANEL_ALT);
            if (row == selected) g.fill(listX, top, listX + listW, top + ROW_H, COL_ROW_SEL);
            else if (hovered) g.fill(listX, top, listX + listW, top + ROW_H, COL_ROW_HOVER);

            int textY = top + (ROW_H - 8) / 2;
            if (!row.icon.isEmpty()) g.renderItem(row.icon, listX + 3, top + (ROW_H - 16) / 2);
            g.drawString(this.font, row.shortName, listX + 23, textY, row.hasStats ? COL_TEXT : COL_TEXT_FAINT, false);

            g.drawString(this.font, row.levelLabel, b[1] + 4, textY, row.levelColor, false);
            g.drawString(this.font, row.statAText, b[2] + 4, textY, row.hasStats ? COL_STAT_A : COL_TEXT_FAINT, false);
            g.drawString(this.font, row.statBText, b[3] + 4, textY, row.hasStats ? COL_STAT_B : COL_TEXT_FAINT, false);
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
    }

    private List<Component> detailLines() {
        return selected == null ? List.of() : selected.detail;
    }

    private int detailContentHeight() {
        return detailLines().size() * 10 + 26;
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

        g.enableScissor(detailX, detailY, detailX + detailW, detailY + detailH);
        int y = detailY + 6 - detailScroll;
        if (!selected.icon.isEmpty()) g.renderItem(selected.icon, detailX + 6, y);
        g.drawString(this.font, selected.shortName, detailX + 26, y + 4, COL_TEXT, false);
        y += 22;
        for (Component line : selected.detail) {
            g.drawString(this.font, line, detailX + 6, y, COL_TEXT, false);
            y += 10;
        }
        g.disableScissor();
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

    /** One material, with every string it needs already resolved. */
    static final class Row {
        final ResourceLocation id;
        final MaterialStats stats;
        final String name;
        final String shortName;
        final String sortName;
        final String searchBlob;
        final int level;
        final String levelLabel;
        final int levelColor;
        final ItemStack icon;
        final List<Component> detail;

        float statA = Float.NaN;
        float statB = Float.NaN;
        String statAText = "—";
        String statBText = "—";
        boolean hasStats;

        Row(Material material, net.minecraft.client.gui.Font font) {
            this.id = material.id();
            this.stats = material.stats();
            this.name = Component.translatable(PartItem.materialTranslationKey(id)).getString();
            this.sortName = name.toLowerCase(Locale.ROOT);
            this.level = stats.harvestLevel();
            this.levelLabel = MiningLevels.levelName(level).getString() + " (" + level + ")";
            this.levelColor = 0xFF000000 | levelTint(level);
            this.icon = icon(id);

            List<ModifierEffect> traits = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (ModifierEffect e : stats.universalModifiers()) if (seen.add(key(e))) traits.add(e);
            for (ModifierEffect e : stats.headModifiers()) if (seen.add(key(e))) traits.add(e);

            StringBuilder blob = new StringBuilder(sortName).append(' ').append(id).append(' ');
            for (ModifierEffect e : traits) {
                blob.append(Component.translatable(PartItem.modifierTranslationKey(e.modifierId())).getString()
                        .toLowerCase(Locale.ROOT)).append(' ').append(e.modifierId()).append(' ');
            }
            this.searchBlob = blob.toString().toLowerCase(Locale.ROOT);

            int maxName = 150;
            String n = name;
            if (font.width(n) > maxName) {
                while (n.length() > 1 && font.width(n + "...") > maxName) n = n.substring(0, n.length() - 1);
                n = n + "...";
            }
            this.shortName = n;
            this.detail = buildDetail(traits);
        }

        private static String key(ModifierEffect e) { return e.modifierId().toString(); }

        boolean matches(String needle) { return searchBlob.contains(needle); }

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

        /** The whole stat block, so the detail pane never sends anyone hunting elsewhere. */
        private List<Component> buildDetail(List<ModifierEffect> traits) {
            List<Component> t = new ArrayList<>();
            t.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
            t.add(Component.empty());
            t.add(header("Tool"));
            t.add(stat("Mining Level", levelLabel));
            t.add(stat("Mining Speed", fmt(stats.miningSpeed())));
            t.add(stat("Attack Damage", fmt(stats.attackDamage())));
            t.add(stat("Durability/Ingot", String.valueOf(stats.durabilityPerIngot())));
            t.add(stat("Binder Multiplier", fmt(stats.binderMultiplier())));
            t.add(stat("Melting Temp", fmt(stats.meltingTemp()) + " C"));
            t.add(stat("Fluid Base", String.valueOf(stats.fluidBase())));
            if (stats.castOnly()) t.add(stat("Cast Only", "yes"));
            if (stats.foil()) t.add(stat("Foil", "yes"));
            if (stats.emissive()) t.add(stat("Emissive", "yes"));

            if (stats.supportsArmor()) {
                MaterialStats.ArmorStats a = stats.armorStats();
                t.add(Component.empty());
                t.add(header("Armour"));
                t.add(stat("Core Durability", fmt(a.coreDurability())));
                t.add(stat("Core Defense", fmt(a.coreDefense())));
                t.add(stat("Plates Durability", fmt(a.platesDurability())));
                t.add(stat("Plates Modifier", fmt(a.platesModifier())));
                t.add(stat("Plates Toughness", fmt(a.platesToughness())));
                t.add(stat("Trim Durability", fmt(a.trimDurability())));
            }

            MaterialStats.RangedStats r = stats.rangedStats();
            if (r != null) {
                t.add(Component.empty());
                t.add(header("Ranged"));
                t.add(stat("Draw Speed", fmt(r.drawSpeed())));
                t.add(stat("Range", fmt(r.range())));
                t.add(stat("Bonus Damage", fmt(r.bonusDamage())));
                t.add(stat("Bowstring", fmt(r.bowstring())));
                t.add(stat("Shaft Modifier", fmt(r.shaftModifier())));
                t.add(stat("Bonus Ammo", String.valueOf(r.bonusAmmo())));
                t.add(stat("Accuracy", fmt(r.accuracy())));
                t.add(stat("Fletching Mod", fmt(r.fletchingModifier())));
            }

            if (!traits.isEmpty()) {
                t.add(Component.empty());
                t.add(header("Traits"));
                for (ModifierEffect e : traits) {
                    t.add(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.translatable(PartItem.modifierTranslationKey(e.modifierId()))
                                    .withStyle(ChatFormatting.AQUA)));
                    String desc = PartItem.modifierDescription(e.modifierId()).getString();
                    for (String line : wrap(desc, 30)) {
                        t.add(Component.literal("   " + line).withStyle(ChatFormatting.GRAY));
                    }
                }
            }

            List<Component> slots = new ArrayList<>();
            for (PartType pt : SmitheryAPI.PART_TYPES.all()) {
                int n = stats.modifierSlotsFor(pt);
                if (n > 0) slots.add(stat(pt.id().getPath(), String.valueOf(n)));
            }
            if (!slots.isEmpty()) {
                t.add(Component.empty());
                t.add(header("Modifier Slots"));
                t.addAll(slots);
            }
            return t;
        }

        private static List<String> wrap(String s, int width) {
            List<String> out = new ArrayList<>();
            if (s == null || s.isEmpty()) return out;
            StringBuilder line = new StringBuilder();
            for (String word : s.split("\\s+")) {
                if (line.length() + word.length() + 1 > width && line.length() > 0) {
                    out.add(line.toString());
                    line.setLength(0);
                }
                if (line.length() > 0) line.append(' ');
                line.append(word);
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

        /** GC's ladder colour for the level, so the column reads at a glance. */
        private static int levelTint(int level) {
            Integer rgb = MiningLevels.levelColour(level).getColor();
            return rgb != null ? rgb : 0xFFFFFF;
        }

        /**
         * First registered part item for the material. Smithery names them
         * {@code smithery:<material path>_<part path>} and skips ineligible part types, so this
         * probes — once per material, cached, because it walks every part type.
         */
        private static ItemStack icon(ResourceLocation materialId) {
            return ICON_CACHE.computeIfAbsent(materialId, mid -> {
                for (PartType pt : SmitheryAPI.PART_TYPES.all()) {
                    Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(
                            "smithery", mid.getPath() + "_" + pt.id().getPath()));
                    if (item != null && item != Items.AIR) return new ItemStack(item);
                }
                return ItemStack.EMPTY;
            });
        }
    }
}
