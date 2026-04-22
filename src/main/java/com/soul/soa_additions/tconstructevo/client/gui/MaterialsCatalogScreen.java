package com.soul.soa_additions.tconstructevo.client.gui;

import com.soul.soa_additions.quest.client.jei.JeiCompat;
import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tools.stats.GripMaterialStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.LimbMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Sortable, scrollable catalog of every visible Tinkers' Construct material
 * (including every TC3 addon's materials, since they all register through
 * {@link MaterialRegistry}). A "Part" cycle button at the top right switches
 * which stat type drives the two numeric columns and the per-stat trait list,
 * so users can compare e.g. plating armor across materials, not just head
 * damage. Hovering a trait name pops its localised description tooltip.
 */
public final class MaterialsCatalogScreen extends Screen {

    private static final int PAD = 6;
    private static final int PART_ROW_HEIGHT = 22;
    private static final int SORT_ROW_HEIGHT = 22;
    private static final int HEADER_HEIGHT = PART_ROW_HEIGHT + SORT_ROW_HEIGHT;
    /** Height of a row that displays a single trait. */
    private static final int ROW_BASE_HEIGHT = 14;
    /** Vertical pixels added per extra trait beyond the first. */
    private static final int TRAIT_LINE_HEIGHT = 10;

    private SortKey sortKey = SortKey.NAME;
    private boolean ascending = true;
    private int partIndex = 0;

    private MaterialList list;
    private final List<MaterialRow> rows = new ArrayList<>();

    // Tooltip to render after all children so it doesn't get overdrawn by the
    // list. Row.render sets these; Screen.render consumes them.
    private List<Component> pendingTooltip;
    private int pendingTooltipX;
    private int pendingTooltipY;
    private ItemStack pendingItemTooltip = ItemStack.EMPTY;

    public MaterialsCatalogScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        rebuildRows();
        // Row 0: Part cycle button (right-aligned), title "Part" indicator
        int partBtnW = 220;
        Button partBtn = Button.builder(partButtonLabel(), btn -> cyclePart(1))
                .bounds(this.width - PAD - partBtnW, PAD, partBtnW, 18)
                .build();
        // Right-click cycles backward; vanilla Button doesn't expose
        // right-click, so emulate with a hidden second button is overkill —
        // instead support shift-click to reverse via mouseClicked override.
        this.addRenderableWidget(partBtn);

        // Row 1: Sort buttons
        int sortRowY = PAD + PART_ROW_HEIGHT;
        int btnW = Math.max(60, (this.width - PAD * 6) / 5);
        int x = PAD;
        for (SortKey k : SortKey.values()) {
            final SortKey key = k;
            Button.Builder b = Button.builder(
                    headerLabel(key),
                    btn -> toggleSort(key)
            ).bounds(x, sortRowY, btnW, 18);
            this.addRenderableWidget(b.build());
            x += btnW + PAD;
        }

        int listTop = HEADER_HEIGHT + PAD;
        int listBottom = this.height - PAD;
        // Row height grows so every trait gets its own line — the list uses a
        // single uniform itemHeight, so we inflate every row to fit whichever
        // material has the most traits in the current part view. Single-trait
        // rows then have empty space, but trait readability beats density.
        int maxTraits = rows.stream().mapToInt(r -> r.traits.size()).max().orElse(1);
        int rowHeight = ROW_BASE_HEIGHT + Math.max(0, maxTraits - 1) * TRAIT_LINE_HEIGHT;
        this.list = new MaterialList(this, this.width, this.height, listTop, listBottom, rowHeight);
        this.addRenderableWidget(this.list);
        applySort();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Right-click on the part button cycles backward.
        if (button == 1) {
            int partBtnW = 220;
            int bx = this.width - PAD - partBtnW;
            int by = PAD;
            if (mouseX >= bx && mouseX < bx + partBtnW && mouseY >= by && mouseY < by + 18) {
                cyclePart(-1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void cyclePart(int direction) {
        int n = PartView.ALL.size();
        partIndex = ((partIndex + direction) % n + n) % n;
        // rebuildWidgets() calls init() which rebuilds rows + recomputes the
        // list's itemHeight to fit the new part's max trait count.
        this.rebuildWidgets();
    }

    private Component partButtonLabel() {
        PartView pv = PartView.ALL.get(partIndex);
        return Component.literal("Part: " + pv.shortLabel + "  (◀ right-click | left-click ▶)");
    }

    private Component headerLabel(SortKey k) {
        PartView pv = PartView.ALL.get(partIndex);
        String label = switch (k) {
            case NAME       -> "Name";
            case TIER       -> "Tier";
            case STAT_A     -> pv.statALabel;
            case STAT_B     -> pv.statBLabel;
            case TRAIT      -> "Trait";
        };
        String arrow = (sortKey == k) ? (ascending ? " ^" : " v") : "";
        return Component.literal(label + arrow);
    }

    private void toggleSort(SortKey k) {
        if (sortKey == k) {
            ascending = !ascending;
        } else {
            sortKey = k;
            ascending = true;
        }
        applySort();
        // Re-init to refresh button labels with the new arrow.
        this.rebuildWidgets();
    }

    private void rebuildRows() {
        rows.clear();
        IMaterialRegistry reg;
        try {
            reg = MaterialRegistry.getInstance();
        } catch (Throwable t) {
            TConstructEvoPlugin.LOG.warn("MaterialRegistry not available: {}", t.toString());
            return;
        }
        PartView pv = PartView.ALL.get(partIndex);
        MaterialStatsId statId = pv.statId;
        Map<MaterialId, ItemStack> repStacks = buildRepresentativeStacks(statId);
        Collection<IMaterial> mats = reg.getVisibleMaterials();
        for (IMaterial m : mats) {
            MaterialId id = m.getIdentifier();
            int tier = m.getTier();
            Optional<? extends IMaterialStats> statOpt = reg.getMaterialStats(id, statId);
            float statA = Float.NaN;
            float statB = Float.NaN;
            boolean hasStats = statOpt.isPresent();
            if (hasStats) {
                IMaterialStats stat = statOpt.get();
                statA = pv.extractA(stat);
                statB = pv.extractB(stat);
            }
            // Per-stat traits when the material actually has this part.
            // TC3's getTraits(id, statId) already falls back to the material's
            // default traits when no per-stat overrides exist, which is what
            // we want for parts that share the cross-material defaults.
            // When the material lacks this stat type entirely (e.g. wool
            // viewed as Plating) we deliberately show nothing — surfacing
            // default traits would mislead the user into thinking the
            // material has a part that doesn't actually exist.
            List<ModifierEntry> traits;
            if (hasStats) {
                try { traits = reg.getTraits(id, statId); }
                catch (Throwable t) { traits = Collections.emptyList(); }
            } else {
                traits = Collections.emptyList();
            }
            ItemStack rep = repStacks.getOrDefault(id, ItemStack.EMPTY);
            rows.add(new MaterialRow(id, displayName(id), tier, statA, statB, hasStats, traits, rep));
        }
    }

    /** Walk TC3's material-castable item registry and pick one representative
     *  item stack per material for the given stat type. Preference order:
     *  (1) an {@link IToolPart} whose {@code getStatType()} matches statId
     *      — this is the actual part item the user wants to see (e.g. when
     *      viewing "Plating: Helmet", we surface the Helmet Plate item);
     *  (2) any {@link IMaterialItem} that can use the material — fallback so
     *      stateless parts (binding, maille) and unmapped types still get an
     *      icon to click for JEI lookups. */
    private static Map<MaterialId, ItemStack> buildRepresentativeStacks(MaterialStatsId statId) {
        Map<MaterialId, ItemStack> out = new HashMap<>();
        try {
            Collection<Object2IntMap.Entry<IMaterialItem>> entries =
                    MaterialCastingLookup.getAllItemCosts();
            IMaterialRegistry reg = MaterialRegistry.getInstance();
            for (IMaterial m : reg.getVisibleMaterials()) {
                MaterialId id = m.getIdentifier();
                ItemStack matched = ItemStack.EMPTY;
                ItemStack fallback = ItemStack.EMPTY;
                for (Object2IntMap.Entry<IMaterialItem> e : entries) {
                    IMaterialItem item = e.getKey();
                    if (!item.canUseMaterial(id)) continue;
                    ItemStack s = item.withMaterialForDisplay(id);
                    if (s.isEmpty()) continue;
                    if (item instanceof IToolPart part && statId.equals(part.getStatType())) {
                        matched = s;
                        break;
                    }
                    if (fallback.isEmpty()) fallback = s;
                }
                ItemStack pick = !matched.isEmpty() ? matched : fallback;
                if (!pick.isEmpty()) out.put(id, pick);
            }
        } catch (Throwable t) {
            TConstructEvoPlugin.LOG.warn("Failed to build material representative stacks: {}", t.toString());
        }
        return out;
    }

    private static String displayName(MaterialId id) {
        String key = "material." + id.getNamespace() + "." + id.getPath();
        MutableComponent c = Component.translatable(key);
        // Component.translatable doesn't fall back nicely at render time
        // here — use the raw string and let the client-side localisation
        // substitute when rendered; for sorting we just want a stable label.
        String name = c.getString();
        if (name.equals(key)) {
            // translation missing — prettify the path
            String path = id.getPath().replace('_', ' ');
            name = Character.toUpperCase(path.charAt(0)) + path.substring(1);
        }
        return name;
    }

    private void applySort() {
        Comparator<MaterialRow> cmp;
        // Materials without the selected stat sort to the bottom regardless of
        // direction so the user isn't comparing populated rows against blanks.
        Comparator<MaterialRow> presence = Comparator.comparing((MaterialRow r) -> !r.hasStats);
        switch (sortKey) {
            case TIER       -> cmp = Comparator.comparingInt(r -> r.tier);
            case STAT_A     -> cmp = Comparator.comparingDouble(r -> Float.isNaN(r.statA) ? Double.NEGATIVE_INFINITY : r.statA);
            case STAT_B     -> cmp = Comparator.comparingDouble(r -> Float.isNaN(r.statB) ? Double.NEGATIVE_INFINITY : r.statB);
            case TRAIT      -> cmp = Comparator.comparing(r -> r.firstTraitName().toLowerCase(Locale.ROOT));
            case NAME       -> cmp = Comparator.comparing(r -> r.name.toLowerCase(Locale.ROOT));
            default         -> cmp = Comparator.comparing(r -> r.name.toLowerCase(Locale.ROOT));
        }
        if (!ascending) cmp = cmp.reversed();
        // Stable secondary key on name so equal primaries don't shuffle on re-sort.
        cmp = presence.thenComparing(cmp).thenComparing(r -> r.name.toLowerCase(Locale.ROOT));
        rows.sort(cmp);
        if (list != null) list.refill(rows);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        pendingTooltip = null;
        pendingItemTooltip = ItemStack.EMPTY;
        super.render(g, mouseX, mouseY, partial);
        if (pendingTooltip != null) {
            g.renderComponentTooltip(this.font, pendingTooltip, pendingTooltipX, pendingTooltipY);
        } else if (!pendingItemTooltip.isEmpty()) {
            g.renderTooltip(this.font, pendingItemTooltip, pendingTooltipX, pendingTooltipY);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public enum SortKey {
        NAME, TIER, STAT_A, STAT_B, TRAIT;
    }

    /** A "part" the catalog can show — a stat type plus how to pull two
     *  comparable numbers out of it. STAT_A is usually the headline value
     *  (durability for tools, armor for plating); STAT_B is the secondary
     *  (attack, mining speed, toughness, etc.). NaN means "not applicable
     *  to this stat type" — the row renderer prints "—". */
    public static final class PartView {
        final MaterialStatsId statId;
        final String shortLabel;
        final String statALabel;
        final String statBLabel;
        private final java.util.function.Function<IMaterialStats, Float> extractA;
        private final java.util.function.Function<IMaterialStats, Float> extractB;

        PartView(MaterialStatsId statId, String shortLabel, String aLabel, String bLabel,
                 java.util.function.Function<IMaterialStats, Float> a,
                 java.util.function.Function<IMaterialStats, Float> b) {
            this.statId = statId;
            this.shortLabel = shortLabel;
            this.statALabel = aLabel;
            this.statBLabel = bLabel;
            this.extractA = a;
            this.extractB = b;
        }

        float extractA(IMaterialStats s) { try { return extractA.apply(s); } catch (Throwable t) { return Float.NaN; } }
        float extractB(IMaterialStats s) { try { return extractB.apply(s); } catch (Throwable t) { return Float.NaN; } }

        /** Canonical part list. Order is also the cycle order. The four armor
         *  plating slots are split out so users can drill into helmet vs
         *  chestplate values, which can differ. */
        public static final List<PartView> ALL = List.of(
                new PartView(HeadMaterialStats.ID, "Head", "Damage", "Durability",
                        s -> ((HeadMaterialStats) s).attack(),
                        s -> (float) ((HeadMaterialStats) s).durability()),
                new PartView(HandleMaterialStats.ID, "Handle", "Damage Mult", "Durability Mult",
                        s -> ((HandleMaterialStats) s).attackDamage(),
                        s -> ((HandleMaterialStats) s).durability()),
                new PartView(StatlessMaterialStats.BINDING.getType().getId(), "Binding", "—", "—",
                        s -> Float.NaN, s -> Float.NaN),
                new PartView(GripMaterialStats.ID, "Grip", "Melee Damage", "Durability Mult",
                        s -> ((GripMaterialStats) s).meleeDamage(),
                        s -> ((GripMaterialStats) s).durability()),
                new PartView(LimbMaterialStats.ID, "Limb (Bow)", "Velocity", "Durability",
                        s -> ((LimbMaterialStats) s).velocity(),
                        s -> (float) ((LimbMaterialStats) s).durability()),
                new PartView(StatlessMaterialStats.BOWSTRING.getType().getId(), "Bowstring", "—", "—",
                        s -> Float.NaN, s -> Float.NaN),
                new PartView(StatlessMaterialStats.FLETCHING.getType().getId(), "Fletching", "—", "—",
                        s -> Float.NaN, s -> Float.NaN),
                new PartView(StatlessMaterialStats.MAILLE.getType().getId(), "Maille", "—", "—",
                        s -> Float.NaN, s -> Float.NaN),
                new PartView(new MaterialStatsId("tconstruct", "plating_helmet"),
                        "Plating: Helmet", "Armor", "Durability",
                        s -> ((PlatingMaterialStats) s).armor(),
                        s -> (float) ((PlatingMaterialStats) s).durability()),
                new PartView(new MaterialStatsId("tconstruct", "plating_chestplate"),
                        "Plating: Chestplate", "Armor", "Durability",
                        s -> ((PlatingMaterialStats) s).armor(),
                        s -> (float) ((PlatingMaterialStats) s).durability()),
                new PartView(new MaterialStatsId("tconstruct", "plating_leggings"),
                        "Plating: Leggings", "Armor", "Durability",
                        s -> ((PlatingMaterialStats) s).armor(),
                        s -> (float) ((PlatingMaterialStats) s).durability()),
                new PartView(new MaterialStatsId("tconstruct", "plating_boots"),
                        "Plating: Boots", "Armor", "Durability",
                        s -> ((PlatingMaterialStats) s).armor(),
                        s -> (float) ((PlatingMaterialStats) s).durability()),
                new PartView(new MaterialStatsId("tconstruct", "plating_shield"),
                        "Plating: Shield", "Toughness", "Durability",
                        s -> ((PlatingMaterialStats) s).toughness(),
                        s -> (float) ((PlatingMaterialStats) s).durability())
        );
    }

    static final class MaterialRow {
        final MaterialId id;
        final String name;
        final int tier;
        final float statA;
        final float statB;
        final boolean hasStats;
        final List<ModifierEntry> traits;
        final ItemStack representative;

        MaterialRow(MaterialId id, String name, int tier, float statA, float statB,
                    boolean hasStats, List<ModifierEntry> traits, ItemStack representative) {
            this.id = id;
            this.name = name;
            this.tier = tier;
            this.statA = statA;
            this.statB = statB;
            this.hasStats = hasStats;
            this.traits = traits;
            this.representative = representative;
        }

        String firstTraitName() {
            if (traits.isEmpty()) return "";
            Modifier m = traits.get(0).getModifier();
            return m.getDisplayName().getString();
        }
    }

    static final class MaterialList extends ObjectSelectionList<MaterialList.Row> {
        private final MaterialsCatalogScreen parent;

        MaterialList(MaterialsCatalogScreen parent, int width, int height, int top, int bottom, int itemHeight) {
            super(parent.minecraft, width, height, top, bottom, itemHeight);
            this.parent = parent;
        }

        void refill(List<MaterialRow> rows) {
            this.clearEntries();
            for (MaterialRow r : rows) this.addEntry(new Row(r));
        }

        @Override
        public int getRowWidth() { return Math.min(parent.width - 20, 680); }

        @Override
        protected int getScrollbarPosition() { return parent.width - 10; }

        final class Row extends ObjectSelectionList.Entry<Row> {
            final MaterialRow data;
            Row(MaterialRow data) { this.data = data; }

            @Override
            public Component getNarration() { return Component.literal(data.name); }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0 && !data.representative.isEmpty() && JeiCompat.available()) {
                    JeiCompat.showItemUses(data.representative);
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left, int width, int height,
                                int mouseX, int mouseY, boolean hovering, float partial) {
                int rowW = Math.min(width, 680);
                int colW = rowW / 5;
                int y = top + 2;
                int x = left + 4;

                // Item icon next to the material name so clickability is obvious.
                int iconSize = 16;
                int iconX = x;
                int iconY = top + (height - iconSize) / 2;
                if (!data.representative.isEmpty()) {
                    g.renderItem(data.representative, iconX, iconY);
                    if (mouseX >= iconX && mouseX < iconX + iconSize
                            && mouseY >= iconY && mouseY < iconY + iconSize) {
                        parent.pendingItemTooltip = data.representative;
                        parent.pendingTooltipX = mouseX;
                        parent.pendingTooltipY = mouseY;
                    }
                }
                int textX = x + iconSize + 4;
                int nameColor = (hovering && JeiCompat.available() && !data.representative.isEmpty())
                        ? 0xFFFF80 : 0xFFFFFF;
                // Materials lacking the selected stat type get a dimmer name so
                // the eye can ignore them while comparing.
                if (!data.hasStats) nameColor = 0x808080;
                String displayName = trunc(data.name, colW - iconSize - 8);
                g.drawString(parent.font, displayName, textX, y, nameColor);
                // Hovering the name pops the material's encyclopedia description
                // (added as material.<ns>.<path>.encyclopedia lang keys) so users
                // can see what the material does without leaving the catalog.
                int nameW = parent.font.width(displayName);
                if (mouseX >= textX && mouseX < textX + nameW
                        && mouseY >= y && mouseY < y + 9) {
                    String encKey = "material." + data.id.getNamespace() + "." + data.id.getPath() + ".encyclopedia";
                    Component enc = Component.translatable(encKey);
                    String text = enc.getString();
                    if (!text.equals(encKey) && !text.isEmpty()) {
                        List<Component> lines = new ArrayList<>();
                        lines.add(Component.literal(data.name).withStyle(ChatFormatting.YELLOW));
                        for (String s : text.split("\n")) {
                            lines.add(Component.literal(s).withStyle(ChatFormatting.GRAY));
                        }
                        parent.pendingTooltip = lines;
                        parent.pendingTooltipX = mouseX;
                        parent.pendingTooltipY = mouseY;
                    }
                }
                x += colW;
                g.drawString(parent.font, String.valueOf(data.tier), x, y, 0xFFE572);
                x += colW;
                g.drawString(parent.font, formatStat(data.statA, data.hasStats), x, y, 0xFF8888);
                x += colW;
                g.drawString(parent.font, formatStat(data.statB, data.hasStats), x, y, 0x88FF88);
                x += colW;

                // One trait per line so multi-trait materials are legible.
                // The list's itemHeight has already been inflated to the max
                // trait count across all rows, so we can stack freely.
                int traitY = y;
                for (int i = 0; i < data.traits.size(); i++) {
                    ModifierEntry entry = data.traits.get(i);
                    Modifier mod = entry.getModifier();
                    String label = mod.getDisplayName().getString();
                    int w = parent.font.width(label);
                    boolean hoverTrait = mouseX >= x && mouseX < x + w
                            && mouseY >= traitY && mouseY < traitY + 9;
                    int color = hoverTrait ? 0xFFFFFF : 0xAAAACC;
                    g.drawString(parent.font, label, x, traitY, color);
                    if (hoverTrait) {
                        List<Component> lines = new ArrayList<>();
                        lines.add(mod.getDisplayName().copy().withStyle(ChatFormatting.YELLOW));
                        Component desc = mod.getDescription();
                        if (desc != null && !desc.getString().isEmpty()) {
                            for (String s : desc.getString().split("\n")) {
                                lines.add(Component.literal(s).withStyle(ChatFormatting.GRAY));
                            }
                        }
                        parent.pendingTooltip = lines;
                        parent.pendingTooltipX = mouseX;
                        parent.pendingTooltipY = mouseY;
                    }
                    traitY += TRAIT_LINE_HEIGHT;
                }
            }

            private String formatStat(float v, boolean hasStats) {
                if (!hasStats) return "—";
                if (Float.isNaN(v)) return "—";
                if (v == Math.floor(v) && Math.abs(v) < 1_000_000f) {
                    return String.valueOf((int) v);
                }
                return String.format(Locale.ROOT, "%.2f", v);
            }

            private String trunc(String s, int maxW) {
                if (parent.font.width(s) <= maxW) return s;
                while (s.length() > 1 && parent.font.width(s + "...") > maxW) s = s.substring(0, s.length() - 1);
                return s + "...";
            }
        }
    }
}
