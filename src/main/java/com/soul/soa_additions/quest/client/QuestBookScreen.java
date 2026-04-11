package com.soul.soa_additions.quest.client;

import com.soul.soa_additions.network.ModNetworking;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.layout.LayoutResult;
import com.soul.soa_additions.quest.layout.QuestLayout;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.NodeShape;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.i18n.QuestText;
import com.soul.soa_additions.quest.net.ClientQuestState;
import com.soul.soa_additions.quest.net.QuestCheckmarkPacket;
import com.soul.soa_additions.quest.net.QuestClaimPacket;
import com.soul.soa_additions.quest.progress.QuestStatus;
import com.soul.soa_additions.quest.task.CheckmarkTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimum-viable quest book. Deliberately unstyled — this is bones and
 * muscle only, per the explicit "bones, muscle, nerves before a pretty
 * face" directive. What this proves:
 *
 * <ul>
 *   <li>The client progress cache populates from {@link ClientQuestState}
 *       on login and stays in sync after claims.</li>
 *   <li>The {@link QuestLayout} Sugiyama algorithm produces sensible
 *       positions without any manual X/Y input.</li>
 *   <li>Dependency lines render from the computed positions.</li>
 *   <li>Claim packets round-trip and the display refreshes.</li>
 * </ul>
 *
 * <p>Left pane is a chapter list. Clicking a chapter runs the layout and
 * renders nodes as colored boxes. Click a node → detail popup with tasks,
 * rewards, and a claim button when READY. No pan/zoom yet — viewport
 * controls land with the aesthetic pass.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class QuestBookScreen extends Screen {

    private static final int LEFT_PANE_WIDTH = 140;
    private static final int HEADER_H = 44;
    // The background shape is drawn at 2× the icon; icon is 16×16, so node = 32×32.
    private static final int NODE_W = 32;
    private static final int NODE_H = 32;
    private static final int ICON_SIZE = 16;
    private static final int COL_SPACING = 72;
    private static final int ROW_SPACING = 48;

    // Color palette — backed by QuestBookConfig so every value is a config
    // knob. Each getter parses the config string with a hard-coded fallback
    // so a typo in the TOML can't brick the screen.
    private static int COL_BG()         { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.BACKGROUND,         0x80000000); }
    private static int COL_BG_GRAD()    { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.BACKGROUND_GRADIENT, 0x80000000); }
    private static int COL_PANE()       { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.LEFT_PANE,          0xC01A1F33); }
    private static int COL_PANE_ALT()   { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.LEFT_PANE_SELECTED, 0xFF222842); }
    private static int COL_PANE_HOVER() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.LEFT_PANE_HOVER,    0xFF1F253E); }
    private static int COL_BORDER()     { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.BORDER,             0xFF3A4264); }
    private static int COL_SEP()        { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.SEPARATOR,          0xFF2A3050); }
    private static int COL_TEXT()       { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.TEXT,               0xFFE6E9F5); }
    private static int COL_TEXT_DIM()   { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.TEXT_DIM,           0xFF8A91AE); }
    private static int COL_TEXT_MUTED() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.TEXT_MUTED,         0xFFBFC5DC); }
    private static int COL_ACCENT()     { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.ACCENT,             0xFF6B8CFF); }
    private static int COL_HEADING()    { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.HEADING,            0xFF6B8CFF); }
    private static int COL_OUTLINE_IDLE()  { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.NODE_OUTLINE_IDLE,  0xFF0A0D18); }
    private static int COL_OUTLINE_HOVER() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.NODE_OUTLINE_HOVER, 0xFFFFFFFF); }
    private static int COL_EDGE()       { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.EDGE_NORMAL,        0xFF5A6391); }
    private static int COL_EDGE_OR()    { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.EDGE_OR_GROUP,      0xFFB59A3A); }
    private static int COL_DETAIL_BG()  { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.DETAIL_BACKGROUND,  0xF00E1220); }
    private static int COL_DETAIL_HEADER() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.DETAIL_HEADER,   0xFF222842); }
    private static int COL_DETAIL_BORDER() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.DETAIL_BORDER,   0xFF3A4264); }
    private static int COL_DETAIL_SHADOW() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.DETAIL_SHADOW,   0x80000000); }
    private static int COL_TOOLTIP_BG() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.TOOLTIP_BACKGROUND, 0xF00A0D18); }
    private static int COL_CLAIMED_TICK_BG() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.CLAIMED_TICK_BG, 0xFF1A1F33); }
    private static int COL_CLAIMED_TICK_FG() { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.CLAIMED_TICK_FG, 0xFF66FF66); }
    private static int COL_CLAIM_BUTTON()   { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.CLAIM_BUTTON,   0xFF4EB02E); }
    private static int COL_SUBMIT_BUTTON()  { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.SUBMIT_BUTTON,  0xFF3E6FB5); }
    private static int COL_CHECKMARK_BOX()  { return com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.CHECKMARK_BOX,  0xFF3A4264); }

    // Status → background shape color.
    private static int statusColor(QuestStatus s) {
        return switch (s) {
            case LOCKED  -> com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.STATUS_LOCKED,  0xFF3A3F55);
            case VISIBLE -> com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.STATUS_VISIBLE, 0xFF3E6FB5);
            case READY   -> com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.STATUS_READY,   0xFF4EB02E);
            case CLAIMED -> com.soul.soa_additions.config.QuestBookConfig.argb(com.soul.soa_additions.config.QuestBookConfig.STATUS_CLAIMED, 0xFFC9A227);
        };
    }

    private List<Chapter> chapters = List.of();
    private Chapter selected;
    private LayoutResult layout;
    private Quest hoveredQuest;
    private Quest openDetailQuest;
    private final Map<String, int[]> nodeBounds = new HashMap<>(); // fullId → [x,y,w,h]
    // Drag state: armed on mouse-down over a node in edit mode, promoted to
    // "active" once the cursor moves past DRAG_THRESHOLD. A release before
    // that threshold is treated as a plain click (opens the detail popup)
    // so edit mode doesn't prevent inspecting quests.
    private String draggingQuestId;
    private boolean dragActive;
    private int dragGrabOffsetX;
    private int dragGrabOffsetY;
    private double dragStartX;
    private double dragStartY;
    private static final double DRAG_THRESHOLD = 4.0;
    // Snap step for quest positions in edit mode. 16 keeps things on a tidy
    // half-icon grid without feeling sluggish.
    private static final int SNAP = 16;
    private int graphOriginX;
    private int graphOriginY;
    private final Map<Integer, int[]> checkmarkBounds = new HashMap<>(); // task index → [x,y,w,h]
    private final Map<Integer, int[]> submitBounds = new HashMap<>(); // task index → [x,y,w,h]
    /** Task index → [x,y,w,h] rect covering the entire rendered task row in
     *  the detail popup. Right-clicking one opens the task context menu. */
    private final Map<Integer, int[]> detailTaskBounds = new HashMap<>();
    /** When the task context menu is open, the quest and task index it's for. */
    private String contextTaskFullId;
    private int contextTaskIndex = -1;
    /** Per-frame click rects for item-name spans inside task descriptions.
     *  Each entry is {x,y,w,h, itemId-encoded-as-list-index}. The matching
     *  ItemStack is stored in {@link #itemLinkStacks} at the same index. */
    private final List<int[]> itemLinkBounds = new ArrayList<>();
    private final List<net.minecraft.world.item.ItemStack> itemLinkStacks = new ArrayList<>();

    // Editor overlay state.
    private static QuestBookScreen activeInstance;
    private boolean contextMenuOpen;
    private Quest contextQuest;      // right-click target (null = empty-space menu)
    private int contextMenuX, contextMenuY;
    private final List<int[]> contextMenuBounds = new ArrayList<>(); // [x,y,w,h] per row
    private final List<String> contextMenuLabels = new ArrayList<>();
    private boolean placementMode;   // next click in graph area creates a new quest
    private QuestEditForm editForm;  // non-null while an edit/create form is open
    private IconPickerPopup iconPicker; // non-null while the icon picker is showing
    // Title-bar drag state for the edit popup. Armed on press, promoted on motion.
    private boolean popupDragging;
    private double popupDragStartMx, popupDragStartMy;
    private int popupDragStartOffX, popupDragStartOffY;
    // Per-row hit rectangles for the task list, rebuilt every frame so click
    // routing in handleFormClick can match without re-deriving the geometry.
    private final List<int[]> taskRowRemoveBounds = new ArrayList<>();
    private final List<int[]> taskRowTypeBounds = new ArrayList<>();
    private final List<int[]> taskRowTagBounds = new ArrayList<>();     // may hold null for rows without a tag toggle
    private final List<int[]> taskRowConsumeBounds = new ArrayList<>(); // ditto for consume toggle
    private int[] taskAddBounds;     // [x,y,w,h]
    private final List<int[]> rewardRowTypeBounds = new ArrayList<>();
    private final List<int[]> rewardRowRemoveBounds = new ArrayList<>();
    private final List<int[]> rewardRowLevelsBounds = new ArrayList<>();
    private final List<int[]> rewardRowScopeBounds = new ArrayList<>();
    private int[] rewardAddBounds;
    private int rewardTypeDropdownRow = -1;
    private int rewardDropdownX, rewardDropdownY;
    private int[] iconPickButtonBounds; // [x,y,w,h]
    private final List<int[]> tabBounds = new ArrayList<>(); // per-tab [x,y,w,h]
    // Task-type dropdown overlay state. typeDropdownRow >= 0 means open, and
    // identifies which task row's type is being chosen. dropdownX/Y are the
    // top-left of the dropdown list (rendered above the form on z 520).
    private int typeDropdownRow = -1;
    private int dropdownX, dropdownY;

    // Chapter list editor state.
    /** Index of the chapter row currently being dragged in the side list, or
     *  -1 if no drag is active. Armed on left-press in edit mode and promoted
     *  to a real reorder once the cursor moves DRAG_THRESHOLD pixels. */
    private int chapterDragIndex = -1;
    private int chapterDragInsertIndex = -1;
    private double chapterDragStartY;
    private boolean chapterDragActive;
    /** Right-click context menu state for the chapter list. {@code -1} target
     *  index means the click landed on empty space (Add Chapter only). */
    private boolean chapterContextOpen;
    private int chapterContextTargetIndex = -1;
    private int chapterContextX, chapterContextY;
    private final List<int[]> chapterContextBounds = new ArrayList<>();
    private final List<String> chapterContextLabels = new ArrayList<>();
    /** Pending delete-confirm for a chapter, by id. Renders an overlay with
     *  Confirm/Cancel buttons until resolved. */
    private String chapterDeletePendingId;
    private String chapterDeletePendingTitle;
    private int[] chapterDeleteConfirmBounds;
    private int[] chapterDeleteCancelBounds;
    /** Rename-chapter modal state. While non-null, a small popup with a text
     *  box is up and consumes input. */
    private String chapterRenamePendingId;
    private EditBox chapterRenameBox;
    private int[] chapterRenameOkBounds;
    private int[] chapterRenameCancelBounds;
    // Chapter edit modal state.
    private String chapterEditPendingId;
    private EditBox chapterEditTitleBox;
    private EditBox chapterEditIconBox;
    private EditBox chapterEditDescBox;
    private EditBox chapterEditReqChaptersBox;  // comma-separated chapter ids
    private EditBox chapterEditReqQuestsBox;    // comma-separated quest ids
    private EditBox chapterEditParentBox;       // parent chapter id
    private com.soul.soa_additions.quest.model.Visibility chapterEditVisibility;
    private int[] chapterEditSaveBounds;
    private int[] chapterEditCancelBounds;
    private int[] chapterEditVisBounds;
    private int chapterEditScrollOffset;        // scroll for tall modal

    public QuestBookScreen() {
        super(Component.literal("Quest Book"));
    }

    @Override
    protected void init() {
        activeInstance = this;
        chapters = QuestRegistry.chaptersFor(ClientQuestState.packMode());
        if (!chapters.isEmpty() && selected == null) {
            selectChapter(chapters.get(0));
        }
    }

    @Override
    public void removed() {
        if (activeInstance == this) activeInstance = null;
        super.removed();
    }

    /** Called from {@code QuestEditPacket} after a chapter mutation lands on
     * the client, so the live screen (if any) rebuilds its layout against the
     * new chapter record instead of holding the stale one. */
    public static void onChapterMutated(String chapterId) {
        QuestBookScreen s = activeInstance;
        if (s == null) return;
        // Always refresh the chapter list so adds/deletes/renames/reorders
        // show up immediately in the side pane — previously this early-returned
        // unless the mutation targeted the currently-selected chapter, which
        // meant newly-added chapters never appeared until the book reopened.
        s.chapters = QuestRegistry.chaptersFor(ClientQuestState.packMode());
        // If nothing is selected yet and we now have chapters, pick the first.
        if (s.selected == null) {
            if (!s.chapters.isEmpty()) s.selectChapter(s.chapters.get(0));
            return;
        }
        // If the selected chapter was deleted, fall back to the first available.
        if (QuestRegistry.chapter(s.selected.id()).isEmpty()) {
            s.selected = null;
            s.openDetailQuest = null;
            s.hoveredQuest = null;
            s.contextQuest = null;
            s.contextMenuOpen = false;
            if (!s.chapters.isEmpty()) s.selectChapter(s.chapters.get(0));
            return;
        }
        // Re-run layout for the selected chapter. Even if the mutation targeted
        // a different chapter, the selected chapter might reference it (e.g. via
        // cross-chapter dependencies), so always refresh the layout to stay current.
        QuestRegistry.chapter(s.selected.id()).ifPresent(c -> {
            s.selected = c;
            s.layout = QuestLayout.compute(c);
            // Drop stale references to quests that may have been deleted by
            // the mutation, so we don't keep rendering a popup or hover state
            // for a quest that no longer exists.
            if (s.openDetailQuest != null && !containsQuest(c, s.openDetailQuest.id())) {
                s.openDetailQuest = null;
            }
            if (s.contextQuest != null && !containsQuest(c, s.contextQuest.id())) {
                s.contextQuest = null;
                s.contextMenuOpen = false;
            }
            s.hoveredQuest = null;
        });
    }

    private void selectChapter(Chapter c) {
        this.selected = c;
        this.layout = QuestLayout.compute(c);
        this.openDetailQuest = null;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // No renderBackground() — the quest book is 50% translucent black by
        // default so the world stays visible behind it. Gradient endpoints
        // are driven by config so a solid/translucent/gradient look are all
        // just TOML tweaks.
        g.fillGradient(0, 0, this.width, this.height, COL_BG(), COL_BG_GRAD());

        renderChapterList(g, mouseX, mouseY);
        renderGraph(g, mouseX, mouseY);
        renderHeader(g);
        super.render(g, mouseX, mouseY, partialTick);

        // Detail popup is drawn after super.render with a large Z translation
        // so it paints above the item-icon layer — renderFakeItem pushes
        // depth forward, and without this translate the popup's fill() calls
        // end up visually behind the icons on some GUI scales.
        if (openDetailQuest != null) {
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 400f);
            renderDetail(g, mouseX, mouseY);
            g.pose().popPose();
        }

        // Editor overlay: edit form and/or right-click context menu, drawn
        // above everything else so they're always interactable.
        if (editForm != null) {
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 450f);
            renderEditForm(g, mouseX, mouseY, partialTick);
            g.pose().popPose();
        }
        if (iconPicker != null) {
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 480f);
            int[] r = formRect();
            int px = r[0] + r[2] + 8;
            // Keep the picker on-screen if the popup got dragged near the right edge.
            if (px + IconPickerPopup.W > this.width - 4) px = r[0] - IconPickerPopup.W - 8;
            int py = r[1];
            iconPicker.render(g, px, py, mouseX, mouseY, partialTick);
            g.pose().popPose();
        }
        if (typeDropdownRow >= 0 && editForm != null) {
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 520f);
            renderTypeDropdown(g, mouseX, mouseY);
            g.pose().popPose();
        }
        if (rewardTypeDropdownRow >= 0 && editForm != null) {
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 520f);
            renderRewardTypeDropdown(g, mouseX, mouseY);
            g.pose().popPose();
        }
        if (contextMenuOpen) {
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 540f);
            renderContextMenu(g, mouseX, mouseY);
            g.pose().popPose();
        }
        // Chapter list overlays (right-click menu and delete confirm) sit on
        // top of everything so the modal can absorb every input.
        if (chapterContextOpen || chapterDeletePendingId != null || chapterRenamePendingId != null) {
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 560f);
            renderChapterOverlays(g, mouseX, mouseY);
            g.pose().popPose();
        }
        if (placementMode && editForm == null) {
            g.drawString(this.font, "Click in the graph to place a new quest (Esc to cancel)",
                    LEFT_PANE_WIDTH + 16, HEADER_H + 2, COL_ACCENT(), false);
        }

        // Hover label last, drawn as a plain filled box anchored above the
        // hovered node. Minecraft's built-in renderTooltip defers through the
        // pose stack in a way that doesn't reliably sit above item icons at
        // very small GUI scales, so we render our own simple box here.
        if (hoveredQuest != null && openDetailQuest == null) {
            int[] b = nodeBounds.get(hoveredQuest.fullId());
            if (b != null) {
                Component title = QuestText.questTitle(hoveredQuest);
                // Wrap hover label at 140 px so long titles don't leave the screen.
                List<FormattedCharSequence> wrapped = this.font.split(title, 140);
                int maxW = 0;
                for (FormattedCharSequence l : wrapped) maxW = Math.max(maxW, this.font.width(l));
                int tw = maxW + 8;
                int th = 4 + wrapped.size() * 10;
                int tx = b[0] + b[2] / 2 - tw / 2;
                int ty = b[1] - th - 3;
                if (tx < 2) tx = 2;
                if (tx + tw > this.width - 2) tx = this.width - 2 - tw;
                if (ty < 2) ty = b[1] + b[3] + 3;
                g.pose().pushPose();
                g.pose().translate(0f, 0f, 500f);
                g.fill(tx, ty, tx + tw, ty + th, COL_TOOLTIP_BG());
                drawBox(g, tx, ty, tw, th, COL_BORDER());
                for (int i = 0; i < wrapped.size(); i++) {
                    g.drawString(this.font, wrapped.get(i), tx + 4, ty + 3 + i * 10, COL_TEXT(), false);
                }
                g.pose().popPose();
            }
        }
    }

    // ---------- chapter list ----------

    /**
     * Compute the nesting depth of a chapter in the parent hierarchy.
     * Top-level chapters (no parent) return 0, their children return 1, etc.
     * Guards against cycles by capping at 5.
     */
    private int chapterDepth(Chapter chapter) {
        int depth = 0;
        String parentId = chapter.parentChapter();
        java.util.Set<String> seen = new java.util.HashSet<>();
        seen.add(chapter.id());
        while (parentId != null && !parentId.isEmpty() && depth < 5) {
            if (!seen.add(parentId)) break; // cycle guard
            depth++;
            Chapter parent = null;
            for (Chapter c : chapters) {
                if (c.id().equals(parentId)) { parent = c; break; }
            }
            if (parent == null) break;
            parentId = parent.parentChapter();
        }
        return depth;
    }

    /** Font scale factor for a chapter at the given nesting depth.
     *  Depth 0 (h1) = 1.3, depth 1 (h2) = 1.0, depth 2+ (h3+) = 0.85. */
    private static float chapterFontScale(int depth) {
        return switch (depth) {
            case 0 -> 1.3f;
            case 1 -> 1.0f;
            default -> 0.85f;
        };
    }

    /** Row height in pixels for a chapter at the given nesting depth. */
    private static int chapterRowHeight(int depth) {
        return switch (depth) {
            case 0 -> 28;
            case 1 -> 24;
            default -> 20;
        };
    }

    private void renderChapterList(GuiGraphics g, int mouseX, int mouseY) {
        // Side pane with subtle right-edge separator
        g.fill(0, 0, LEFT_PANE_WIDTH, this.height, COL_PANE());
        g.fill(LEFT_PANE_WIDTH - 1, 0, LEFT_PANE_WIDTH, this.height, COL_BORDER());

        // Heading
        g.drawString(this.font, Component.literal("CHAPTERS").withStyle(s -> s),
                10, 10, COL_HEADING(), false);
        g.fill(10, 22, LEFT_PANE_WIDTH - 10, 23, COL_SEP());

        int y = 28;
        // Compute the drop-target index when a drag is active so we can paint
        // the insert line at the right gap.
        if (chapterDragActive && chapterDragIndex >= 0) {
            int hoverIdx = chapterIndexAt((int) mouseY);
            if (hoverIdx < 0) hoverIdx = chapters.size();
            chapterDragInsertIndex = hoverIdx;
        }
        for (int idx = 0; idx < chapters.size(); idx++) {
            Chapter c = chapters.get(idx);
            if (!isChapterVisible(c)) continue;
            int depth = chapterDepth(c);
            int rowH = chapterRowHeight(depth);
            float fontScale = chapterFontScale(depth);
            int indent = 12 + depth * 10; // indent increases with depth

            boolean hover = mouseX >= 4 && mouseX < LEFT_PANE_WIDTH - 4 && mouseY >= y && mouseY < y + rowH;
            boolean sel = c == selected;
            boolean dragGhost = chapterDragActive && idx == chapterDragIndex;
            int bg = sel ? COL_PANE_ALT() : (hover ? COL_PANE_HOVER() : 0);
            if (bg != 0) g.fill(4, y, LEFT_PANE_WIDTH - 4, y + rowH, bg);
            if (sel) g.fill(4, y, 6, y + rowH, COL_ACCENT());
            // Drop-indicator line above this row when it's the insert target.
            if (chapterDragActive && idx == chapterDragInsertIndex && idx != chapterDragIndex) {
                g.fill(4, y - 1, LEFT_PANE_WIDTH - 4, y + 1, COL_ACCENT());
            }
            boolean badRef = inEditMode() && chapterHasSelfRef(c);
            int textColor = badRef ? 0xFFFF4444 : (sel ? COL_TEXT() : COL_TEXT_MUTED());
            if (dragGhost) textColor = (textColor & 0x00FFFFFF) | 0x70000000;

            // Render text with scale based on depth
            Component title = QuestText.chapterTitle(c);
            int textY = y + (rowH - (int)(this.font.lineHeight * fontScale)) / 2;
            if (fontScale != 1.0f) {
                var pose = g.pose();
                pose.pushPose();
                pose.scale(fontScale, fontScale, 1.0f);
                g.drawString(this.font, title,
                        (int)(indent / fontScale), (int)(textY / fontScale),
                        textColor, false);
                pose.popPose();
            } else {
                g.drawString(this.font, title, indent, textY, textColor, false);
            }
            y += rowH;
        }
        // Trailing drop indicator (insert at the very end).
        if (chapterDragActive && chapterDragInsertIndex == chapters.size()) {
            g.fill(4, y - 1, LEFT_PANE_WIDTH - 4, y + 1, COL_ACCENT());
        }
    }

    /** Map a Y pixel in the side pane to a chapter row index, or -1 if the
     *  pixel is outside the list area. Accounts for variable row heights
     *  based on chapter nesting depth. */
    private int chapterIndexAt(int yPixel) {
        if (yPixel < 28) return -1;
        int y = 28;
        for (int idx = 0; idx < chapters.size(); idx++) {
            Chapter c = chapters.get(idx);
            if (!isChapterVisible(c)) continue;
            int rowH = chapterRowHeight(chapterDepth(c));
            if (yPixel >= y && yPixel < y + rowH) return idx;
            y += rowH;
        }
        return -1;
    }

    /** True if the active client is currently in editor mode. Editors see
     *  hidden and invisible content so they can manage it. */
    private static boolean inEditMode() {
        return com.soul.soa_additions.quest.net.ClientQuestState.editMode();
    }

    /** Whether a quest should be rendered in the graph for this client. */
    private boolean isQuestVisible(Quest q) {
        if (inEditMode()) return true;
        // INVISIBLE on the parent chapter cascades onto every quest inside,
        // overriding the per-quest visibility. HIDDEN_UNTIL_DEPS on the
        // chapter does NOT cascade — that case only filters the chapter
        // entry in the side list, individual quests inside still follow
        // their own visibility rules.
        if (selected != null
                && selected.visibility() == com.soul.soa_additions.quest.model.Visibility.INVISIBLE) {
            return false;
        }
        return switch (q.visibility()) {
            case NORMAL -> true;
            case INVISIBLE -> false;
            case HIDDEN_UNTIL_DEPS -> {
                for (String dep : q.dependencies()) {
                    if (!depSatisfied(dep)) yield false;
                }
                yield true;
            }
        };
    }

    // ---------- editor validation: cycles & self-refs ----------

    /** Cached set of cyclic edge keys ({@code "from→to"}), recomputed each
     *  frame from the selected chapter. Only consulted in edit mode. */
    private final java.util.Set<String> cyclicEdgeKeys = new java.util.HashSet<>();
    /** Whether the selected chapter has a self-referential
     *  {@code requires_quests} entry (a required quest lives in this chapter). */
    private boolean selectedChapterSelfRefs;

    private static String edgeKey(String from, String to) { return from + "→" + to; }

    /** True if any entry of the chapter's {@code requires_quests} list resolves
     *  to a quest inside the same chapter — that's a guaranteed unreachable
     *  state because the chapter would gate itself. */
    private static boolean chapterHasSelfRef(Chapter c) {
        for (String dq : c.requiresQuests()) {
            String full = dq.contains("/") ? dq : c.id() + "/" + dq;
            if (full.startsWith(c.id() + "/")) return true;
        }
        return false;
    }

    /** Recompute cycle/self-ref state for the current selection. Cheap for the
     *  small graphs the quest book deals with — O(N²) over chapter quests. */
    private void recomputeEditorValidation() {
        cyclicEdgeKeys.clear();
        selectedChapterSelfRefs = false;
        if (selected == null) return;

        // Build dependency adjacency: dependent → list of dependency-fullIds.
        java.util.Map<String, java.util.List<String>> deps = new java.util.HashMap<>();
        for (Quest q : selected.quests()) {
            java.util.List<String> resolved = new java.util.ArrayList<>();
            for (String d : q.dependencies()) {
                resolved.add(d.contains("/") ? d : selected.id() + "/" + d);
            }
            deps.put(q.fullId(), resolved);
        }
        // For each edge from→to (from depends on to via the dependent's
        // declaration: dependent.id → dep.fullId), we want to mark the edge
        // cyclic iff `from` is reachable from `to` through the deps map. That
        // means following deps starting at `to`, we eventually hit `from`.
        for (Quest dependent : selected.quests()) {
            String to = dependent.fullId();
            for (String from : deps.getOrDefault(to, java.util.List.of())) {
                if (reachableViaDeps(deps, from, to)) {
                    cyclicEdgeKeys.add(edgeKey(from, to));
                }
            }
        }

        // Chapter self-ref: a requires_quests entry that lives in this chapter.
        for (String dq : selected.requiresQuests()) {
            String full = dq.contains("/") ? dq : selected.id() + "/" + dq;
            if (full.startsWith(selected.id() + "/")) {
                selectedChapterSelfRefs = true;
                break;
            }
        }
    }

    /** True if {@code target} is reachable from {@code start} by walking the
     *  dependency adjacency map. Iterative DFS with a visited set so it
     *  terminates even when the graph itself has cycles. */
    private static boolean reachableViaDeps(java.util.Map<String, java.util.List<String>> deps,
                                            String start, String target) {
        java.util.Deque<String> stack = new java.util.ArrayDeque<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            String cur = stack.pop();
            if (!seen.add(cur)) continue;
            if (cur.equals(target)) return true;
            for (String next : deps.getOrDefault(cur, java.util.List.of())) {
                if (!seen.contains(next)) stack.push(next);
            }
        }
        return false;
    }

    /** Whether a chapter should appear in the chapter list for this client. */
    private boolean isChapterVisible(Chapter c) {
        if (inEditMode()) return true;
        if (c.visibility() == com.soul.soa_additions.quest.model.Visibility.INVISIBLE) return false;
        if (c.visibility() == com.soul.soa_additions.quest.model.Visibility.HIDDEN_UNTIL_DEPS) {
            for (String depQ : c.requiresQuests()) {
                if (!depSatisfiedFullId(depQ)) return false;
            }
            for (String depCh : c.requiresChapters()) {
                if (!chapterFullyClaimed(depCh)) return false;
            }
        }
        return true;
    }

    /** A quest dependency string is normally a bare quest id within the same
     *  chapter; treat it as fully-qualified if it already contains a slash. */
    private boolean depClaimed(String depId) {
        String full = depId.contains("/") ? depId : (selected.id() + "/" + depId);
        return depClaimedFullId(full);
    }

    private boolean depClaimedFullId(String fullId) {
        return com.soul.soa_additions.quest.net.ClientQuestState.statusOf(fullId)
                == com.soul.soa_additions.quest.progress.QuestStatus.CLAIMED;
    }

    /** Same as {@link #depClaimed} but treats a quest as satisfied as soon as
     *  its tasks are complete (READY) — the player doesn't have to claim the
     *  reward before dependent quests are revealed. Used by HIDDEN_UNTIL_DEPS
     *  gating so prerequisite chains flow forward on completion, not on claim. */
    private boolean depSatisfied(String depId) {
        String full = depId.contains("/") ? depId : (selected.id() + "/" + depId);
        return depSatisfiedFullId(full);
    }

    private boolean depSatisfiedFullId(String fullId) {
        var s = com.soul.soa_additions.quest.net.ClientQuestState.statusOf(fullId);
        return s == com.soul.soa_additions.quest.progress.QuestStatus.READY
            || s == com.soul.soa_additions.quest.progress.QuestStatus.CLAIMED;
    }

    private boolean chapterFullyClaimed(String chapterId) {
        var c = QuestRegistry.chapter(chapterId).orElse(null);
        if (c == null) return false;
        for (Quest q : c.quests()) {
            if (q.optional()) continue;
            if (!depClaimedFullId(q.fullId())) return false;
        }
        return true;
    }

    /**
     * Resolve the content-relative pixel position for a quest. Checks (in
     * order): live drag override, server-broadcast override map, then the
     * layout's computed pixel coords. Returns null when the quest isn't in
     * the current layout (filtered out by pack mode, etc.).
     */
    private int[] nodePixel(String fullId) {
        int[] override = com.soul.soa_additions.quest.net.ClientQuestEditState.get(fullId);
        if (override != null) return override;
        LayoutResult.GridPosition p = layout.positions().get(fullId);
        if (p == null) return null;
        return new int[]{p.pixelX(), p.pixelY()};
    }

    // ---------- graph view ----------

    private void renderGraph(GuiGraphics g, int mouseX, int mouseY) {
        nodeBounds.clear();
        hoveredQuest = null;
        if (selected == null || layout == null) return;
        if (inEditMode()) recomputeEditorValidation();
        else { cyclicEdgeKeys.clear(); selectedChapterSelfRefs = false; }

        int originX = LEFT_PANE_WIDTH + 28;
        int originY = HEADER_H + 16;
        this.graphOriginX = originX;
        this.graphOriginY = originY;

        // First pass: determine the hovered quest so the edge pass can know
        // which lines to highlight. Edges need to be drawn before nodes (so
        // nodes paint on top), but the highlight logic needs hover info, so
        // we hit-test nodes here ahead of any drawing.
        for (Quest q : selected.quests()) {
            if (!isQuestVisible(q)) continue;
            int[] px = nodePixel(q.fullId());
            if (px == null) continue;
            int x = originX + px[0];
            int y = originY + px[1];
            int ns = q.sizeOrDefault();
            if (mouseX >= x && mouseX < x + ns && mouseY >= y && mouseY < y + ns) {
                hoveredQuest = q;
                break;
            }
        }
        String hoveredFullId = hoveredQuest == null ? null : hoveredQuest.fullId();

        // Draw edges from node-center to node-center. Edges connected to the
        // hovered quest are drawn last so they paint over their neighbors and
        // are clearly visible.
        // Pre-index quests by fullId for O(1) lookups during edge rendering.
        // Avoids stream().filter().findFirst() per edge per frame.
        java.util.Map<String, Quest> questByFullId = new java.util.HashMap<>(selected.quests().size());
        for (Quest qi : selected.quests()) questByFullId.put(qi.fullId(), qi);

        java.util.List<int[]> highlightedEdges = new java.util.ArrayList<>();
        for (LayoutResult.Edge edge : layout.edges()) {
            int[] from = nodePixel(edge.from());
            int[] to = nodePixel(edge.to());
            if (from == null || to == null) continue;
            // Honor the per-quest "show dependency lines" toggle on the
            // dependent (downstream) node — that's the quest that owns the
            // dependency, so its toggle controls whether the line is drawn.
            Quest dest = questByFullId.get(edge.to());
            Quest src = questByFullId.get(edge.from());
            if (dest != null && !dest.showDeps()) continue;
            // Skip edges into/out of nodes that are themselves filtered out.
            if (dest != null && !isQuestVisible(dest)) continue;
            if (src != null && !isQuestVisible(src)) continue;
            int srcSize = src != null ? src.sizeOrDefault() : NODE_W;
            int dstSize = dest != null ? dest.sizeOrDefault() : NODE_W;
            int x1 = originX + from[0] + srcSize / 2;
            int y1 = originY + from[1] + srcSize / 2;
            int x2 = originX + to[0]   + dstSize / 2;
            int y2 = originY + to[1]   + dstSize / 2;
            boolean highlight = hoveredFullId != null
                    && (edge.from().equals(hoveredFullId) || edge.to().equals(hoveredFullId));
            boolean cyclic = inEditMode() && cyclicEdgeKeys.contains(edgeKey(edge.from(), edge.to()));
            int edgeColor = cyclic ? 0xFFFF4444 : (edge.orGroup() ? COL_EDGE_OR() : COL_EDGE());
            if (highlight) {
                highlightedEdges.add(new int[]{x1, y1, x2, y2,
                        cyclic ? 2 : (edge.orGroup() ? 1 : 0)});
            } else {
                drawThickLine(g, x1, y1, x2, y2, edgeColor, cyclic ? 3 : 2);
            }
        }
        for (int[] e : highlightedEdges) {
            int base = switch (e[4]) {
                case 2 -> 0xFFFF4444;
                case 1 -> COL_EDGE_OR();
                default -> COL_EDGE();
            };
            int hl = e[4] == 2 ? base : brighten(brighten(base));
            drawThickLine(g, e[0], e[1], e[2], e[3], hl, 4);
        }

        // Draw nodes.
        for (Quest q : selected.quests()) {
            if (!isQuestVisible(q)) continue;
            int[] px = nodePixel(q.fullId());
            if (px == null) continue;
            int x = originX + px[0];
            int y = originY + px[1];
            int ns = q.sizeOrDefault();
            int iconS = Math.max(8, ns / 2);
            nodeBounds.put(q.fullId(), new int[]{x, y, ns, ns});

            QuestStatus status = ClientQuestState.statusOf(q.fullId());
            boolean hovered = q == hoveredQuest;

            ItemStack iconStack = resolveIcon(q.icon());
            drawShapeNode(g, x, y, ns, q.shape(), statusColor(status), iconStack, hovered);

            // Icon centered in the node; item rendering is always 16×16 but
            // we push a matrix scale so it tracks the per-quest size.
            float iconScale = iconS / 16f;
            g.pose().pushPose();
            g.pose().translate(x + (ns - iconS) / 2f, y + (ns - iconS) / 2f, 0f);
            g.pose().scale(iconScale, iconScale, 1f);
            g.renderFakeItem(iconStack, 0, 0);
            g.pose().popPose();

            // CLAIMED tick overlay in top-right corner
            if (status == QuestStatus.CLAIMED) {
                g.pose().pushPose();
                g.pose().translate(0f, 0f, 200f);
                g.fill(x + ns - 9, y - 1, x + ns + 1, y + 9, COL_CLAIMED_TICK_BG());
                g.drawString(this.font, "✔", x + ns - 7, y, COL_CLAIMED_TICK_FG(), true);
                g.pose().popPose();
            }
        }
    }

    /**
     * Draw the background shape behind a quest icon. For {@link NodeShape#ICON}
     * the icon itself is rendered at 2× scale with a color tint applied via
     * the shader color, producing a silhouetted background made of the icon.
     * For the geometric shapes we fill pixels of the appropriate silhouette
     * plus a subtle hover outline.
     */
    private void drawShapeNode(GuiGraphics g, int x, int y, int size, NodeShape shape, int color, ItemStack iconStack, boolean hovered) {
        int outline = hovered ? COL_OUTLINE_HOVER() : COL_OUTLINE_IDLE();
        switch (shape) {
            case SQUARE -> {
                g.fill(x, y, x + size, y + size, color);
                drawBox(g, x, y, size, size, outline);
            }
            case CIRCLE -> {
                fillCircle(g, x, y, size, color);
                if (hovered) strokeCircle(g, x, y, size, outline);
            }
            case RHOMBUS -> {
                fillRhombus(g, x, y, size, color);
                if (hovered) strokeRhombus(g, x, y, size, outline);
            }
            case ICON -> {
                // Flat-color silhouette of the icon at 2× size. We cache a
                // boolean alpha mask sampled from the item's texture atlas
                // sprite on first use; subsequent renders are a cheap pixel
                // loop filling each mask cell with the status color.
                boolean[][] mask = getSilhouetteMask(iconStack);
                if (mask == null) {
                    // Couldn't sample the texture for some reason — fall
                    // back to a plain square so the node still reads.
                    g.fill(x, y, x + size, y + size, color);
                    drawBox(g, x, y, size, size, outline);
                } else {
                    int mh = mask.length, mw = mask[0].length;
                    // Scale the mask to fit the node's footprint.
                    float sx = (float) size / mw;
                    float sy = (float) size / mh;
                    for (int py = 0; py < mh; py++) {
                        for (int px = 0; px < mw; px++) {
                            if (!mask[py][px]) continue;
                            int dx0 = x + (int) (px * sx);
                            int dy0 = y + (int) (py * sy);
                            int dx1 = x + (int) ((px + 1) * sx);
                            int dy1 = y + (int) ((py + 1) * sy);
                            if (dx1 <= dx0) dx1 = dx0 + 1;
                            if (dy1 <= dy0) dy1 = dy0 + 1;
                            g.fill(dx0, dy0, dx1, dy1, color);
                        }
                    }
                    if (hovered) strokeSilhouette(g, x, y, size, mask, outline);
                }
            }
        }
    }

    private static void drawBox(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void fillCircle(GuiGraphics g, int x, int y, int size, int color) {
        float r = size / 2f;
        float cx = x + r, cy = y + r;
        float r2 = r * r;
        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                float dx = (x + px + 0.5f) - cx;
                float dy = (y + py + 0.5f) - cy;
                if (dx * dx + dy * dy <= r2) {
                    g.fill(x + px, y + py, x + px + 1, y + py + 1, color);
                }
            }
        }
    }

    private static void strokeCircle(GuiGraphics g, int x, int y, int size, int color) {
        float r = size / 2f;
        float cx = x + r, cy = y + r;
        float outer = r * r;
        float inner = (r - 1.2f) * (r - 1.2f);
        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                float dx = (x + px + 0.5f) - cx;
                float dy = (y + py + 0.5f) - cy;
                float d2 = dx * dx + dy * dy;
                if (d2 <= outer && d2 >= inner) {
                    g.fill(x + px, y + py, x + px + 1, y + py + 1, color);
                }
            }
        }
    }

    private static void fillRhombus(GuiGraphics g, int x, int y, int size, int color) {
        int half = size / 2;
        for (int py = 0; py < size; py++) {
            int d = Math.abs(py - half);
            int w = size - 2 * d;
            int startX = x + d;
            g.fill(startX, y + py, startX + w, y + py + 1, color);
        }
    }

    // Per-item silhouette cache. Keyed by item registry id — different item
    // stacks of the same item share a mask, so this grows at most O(items).
    private static final Map<ResourceLocation, boolean[][]> SILHOUETTE_CACHE = new HashMap<>();
    // Sentinel used when mask sampling fails, so we don't retry every frame.
    private static final boolean[][] SILHOUETTE_MISS = new boolean[0][];

    private static boolean[][] getSilhouetteMask(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        boolean[][] cached = SILHOUETTE_CACHE.get(key);
        if (cached != null) return cached == SILHOUETTE_MISS ? null : cached;
        boolean[][] mask = buildMask(stack);
        SILHOUETTE_CACHE.put(key, mask == null ? SILHOUETTE_MISS : mask);
        return mask;
    }

    /**
     * Sample the alpha channel of the item's baked model's particle icon.
     * For flat items (inventory sprites) the particle icon IS the inventory
     * texture. For 3D items (tools, blocks) it's a representative face — good
     * enough to give a recognizable silhouette. If anything goes wrong we
     * return null and the caller falls back to a square.
     */
    private static final int MASK_SIZE = 32;

    /**
     * Render the item to an offscreen framebuffer (the same way it appears in
     * the inventory — baked model, GUI lighting, 3D block faces and all) and
     * sample the alpha channel to get a silhouette mask. Done once per item
     * and cached, so the framebuffer dance only runs on first sight.
     */
    private static boolean[][] buildMask(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        int S = MASK_SIZE;
        com.mojang.blaze3d.pipeline.TextureTarget target = null;
        NativeImage img = null;
        try {
            target = new com.mojang.blaze3d.pipeline.TextureTarget(S, S, true, Minecraft.ON_OSX);
            target.setClearColor(0f, 0f, 0f, 0f);
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);

            // Set up an ortho projection matching a S×S GUI.
            org.joml.Matrix4f prevProj = RenderSystem.getProjectionMatrix();
            com.mojang.blaze3d.vertex.VertexSorting prevSort = RenderSystem.getVertexSorting();
            org.joml.Matrix4f ortho = new org.joml.Matrix4f().setOrtho(0.0f, S, S, 0.0f, 1000.0f, 21000.0f);
            RenderSystem.setProjectionMatrix(ortho, com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);

            com.mojang.blaze3d.vertex.PoseStack mv = RenderSystem.getModelViewStack();
            mv.pushPose();
            mv.setIdentity();
            mv.translate(0.0, 0.0, -11000.0);
            RenderSystem.applyModelViewMatrix();

            GuiGraphics g = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
            g.pose().pushPose();
            g.pose().scale(2.0f, 2.0f, 1.0f); // 16×16 item → 32×32 footprint
            g.renderFakeItem(stack, 0, 0);
            g.pose().popPose();
            g.flush();

            mv.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(prevProj, prevSort);

            // Download the color attachment.
            img = new NativeImage(S, S, false);
            com.mojang.blaze3d.platform.GlStateManager._bindTexture(target.getColorTextureId());
            img.downloadTexture(0, false);

            mc.getMainRenderTarget().bindWrite(true);

            // Framebuffer pixels are bottom-up; flip on read.
            boolean[][] mask = new boolean[S][S];
            for (int py = 0; py < S; py++) {
                for (int px = 0; px < S; px++) {
                    int argb = img.getPixelRGBA(px, S - 1 - py);
                    int alpha = (argb >> 24) & 0xFF;
                    mask[py][px] = alpha > 32;
                }
            }
            return mask;
        } catch (Throwable t) {
            return null;
        } finally {
            if (img != null) img.close();
            if (target != null) {
                try { target.destroyBuffers(); } catch (Throwable ignored) {}
            }
            try { mc.getMainRenderTarget().bindWrite(true); } catch (Throwable ignored) {}
        }
    }

    /** Draw a 1-pixel outline around the silhouette's edge cells for hover. */
    private static void strokeSilhouette(GuiGraphics g, int x, int y, int size, boolean[][] mask, int color) {
        int mh = mask.length, mw = mask[0].length;
        float sx = (float) size / mw;
        float sy = (float) size / mh;
        for (int py = 0; py < mh; py++) {
            for (int px = 0; px < mw; px++) {
                if (!mask[py][px]) continue;
                boolean edge =
                        px == 0 || px == mw - 1 || py == 0 || py == mh - 1
                                || !mask[py][px - 1] || !mask[py][px + 1]
                                || !mask[py - 1][px] || !mask[py + 1][px];
                if (!edge) continue;
                int dx0 = x + (int) (px * sx);
                int dy0 = y + (int) (py * sy);
                int dx1 = x + (int) ((px + 1) * sx);
                int dy1 = y + (int) ((py + 1) * sy);
                if (dx1 <= dx0) dx1 = dx0 + 1;
                if (dy1 <= dy0) dy1 = dy0 + 1;
                g.fill(dx0, dy0, dx1, dy1, color);
            }
        }
    }

    private static void strokeRhombus(GuiGraphics g, int x, int y, int size, int color) {
        int half = size / 2;
        for (int py = 0; py < size; py++) {
            int d = Math.abs(py - half);
            int startX = x + d;
            int endX = x + size - d - 1;
            g.fill(startX, y + py, startX + 1, y + py + 1, color);
            g.fill(endX, y + py, endX + 1, y + py + 1, color);
        }
    }

    private static ItemStack resolveIcon(String id) {
        if (id == null || id.isEmpty()) return new ItemStack(Items.PAPER);
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
            return item == Items.AIR ? new ItemStack(Items.PAPER) : new ItemStack(item);
        } catch (Exception e) {
            return new ItemStack(Items.PAPER);
        }
    }

    /** Bresenham line stamping a {@code thickness}×{@code thickness} square at
     *  each step. Cheap enough for the ~50 edges a chapter has, and yields a
     *  visually solid line at thickness 2-4 without needing a real rasterizer. */
    private void drawThickLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color, int thickness) {
        int half = thickness / 2;
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1, y = y1;
        while (true) {
            g.fill(x - half, y - half, x - half + thickness, y - half + thickness, color);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
    }

    private void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        // Cheap Bresenham. Two matrices of fill() calls would be pricier
        // than doing it in one pass; this is plenty for ~50 edges.
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1, y = y1;
        while (true) {
            g.fill(x, y, x + 1, y + 1, color);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
    }

    // ---------- header ----------

    private void renderHeader(GuiGraphics g) {
        // Header bar across the graph pane
        g.fill(LEFT_PANE_WIDTH, 0, this.width, HEADER_H, COL_PANE());
        g.fill(LEFT_PANE_WIDTH, HEADER_H - 1, this.width, HEADER_H, COL_BORDER());

        Component header = selected != null ? QuestText.chapterTitle(selected) : Component.literal("No Chapter");
        g.drawString(this.font, header, LEFT_PANE_WIDTH + 16, 12, COL_TEXT(), true);
        g.drawString(this.font,
                "Mode: " + ClientQuestState.packMode().lower() + "   •   " + ClientQuestState.size() + " quests tracked",
                LEFT_PANE_WIDTH + 16, 26, COL_TEXT_DIM(), false);
    }

    // ---------- detail popup ----------

    private void renderDetail(GuiGraphics g, int mouseX, int mouseY) {
        int w = 280, h = 200;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        // Drop-shadow behind the popup
        g.fill(x + 4, y + 4, x + w + 4, y + h + 4, COL_DETAIL_SHADOW());
        g.fill(x, y, x + w, y + h, COL_DETAIL_BG());
        // Header band
        g.fill(x, y, x + w, y + 24, COL_DETAIL_HEADER());
        g.fill(x, y + 23, x + w, y + 24, COL_DETAIL_BORDER());
        drawBox(g, x, y, w, h, COL_DETAIL_BORDER());

        int innerPad = 10;
        int contentX = x + innerPad;
        int contentRight = x + w - innerPad;
        int wrapW = contentRight - contentX;

        Quest q = openDetailQuest;

        // Title (wrapped if absurdly long)
        List<FormattedCharSequence> titleLines = this.font.split(QuestText.questTitle(q), wrapW);
        int lineY = y + 7;
        for (int i = 0; i < titleLines.size() && i < 1; i++) {
            g.drawString(this.font, titleLines.get(i), contentX, lineY, COL_TEXT(), true);
        }

        int line = y + 32;
        // Description — wrap each source line to the popup width
        for (int i = 0; i < q.description().size(); i++) {
            if (line > y + h - 56) break;
            String raw = q.description().get(i);
            // Empty entries (from `||` shorthand or a literal blank line in
            // the multi-line editor) render as a 10-px vertical gap.
            if (raw == null || raw.isEmpty()) {
                line += 10;
                continue;
            }
            List<FormattedCharSequence> wrapped = this.font.split(QuestText.questDescLine(q, i), wrapW);
            for (FormattedCharSequence l : wrapped) {
                if (line > y + h - 56) break;
                g.drawString(this.font, l, contentX, line, COL_TEXT_DIM(), false);
                line += 10;
            }
        }

        line += 4;
        g.fill(contentX, line, contentRight, line + 1, COL_SEP());
        line += 4;
        g.drawString(this.font, "TASKS", contentX, line, COL_ACCENT(), false);
        line += 11;

        checkmarkBounds.clear();
        submitBounds.clear();
        itemLinkBounds.clear();
        itemLinkStacks.clear();
        detailTaskBounds.clear();
        for (int i = 0; i < q.tasks().size() && line < y + h - 30; i++) {
            int taskRowStart = line;
            var task = q.tasks().get(i);
            int count = ClientQuestState.taskCount(q.fullId(), i);
            int tgt = task.target();
            boolean done = count >= tgt;
            boolean isConsume = task instanceof com.soul.soa_additions.quest.task.ItemTask it && it.consume();

            int textX = contentX;
            int rightReserve = 0; // room to keep for a submit button
            if (task instanceof CheckmarkTask) {
                int boxX = contentX, boxY = line - 1;
                int border = done ? 0xFF55CC55 : 0xFFCCCCCC;
                g.fill(boxX, boxY, boxX + 9, boxY + 9, COL_CHECKMARK_BOX());
                drawBox(g, boxX, boxY, 9, 9, border);
                if (done) g.drawString(this.font, "✔", boxX + 1, boxY, 0xFF55CC55, false);
                checkmarkBounds.put(i, new int[]{boxX, boxY, 9, 9});
                textX = boxX + 12;
                // Wrap task description in the remaining width
                int availW = contentRight - textX;
                List<FormattedCharSequence> lines = this.font.split(Component.literal(task.describe()), availW);
                for (int k = 0; k < lines.size(); k++) {
                    g.drawString(this.font, lines.get(k), textX, line + k * 10, done ? 0xFF9BDF9B : 0xFFCCCCCC, false);
                }
                line += Math.max(10, lines.size() * 10);
                detailTaskBounds.put(i, new int[]{contentX, taskRowStart - 1, wrapW, line - taskRowStart + 1});
                continue;
            } else if (task instanceof com.soul.soa_additions.quest.task.ItemTask itemTask && itemTask.item() != null) {
                // Special-case: render the item name as a clickable span that
                // opens JEI for that item. Layout: "marker Obtain Nx <NAME> (c/t)"
                if (isConsume && !done) rightReserve = this.font.width("Submit") + 12;
                int color = done ? 0xFF9BDF9B : 0xFFCCCCCC;
                int linkColor = done ? 0xFF7FD0FF : 0xFF9CC9FF;
                String marker = done ? "✔ " : "• ";
                String prefix = marker + "Obtain " + count + "x ";
                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemTask.item());
                net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
                String name = stack.getHoverName().getString();
                String suffix = " (" + count + "/" + tgt + ")";
                int prefixW = this.font.width(prefix);
                int nameW = this.font.width(name);
                int suffixW = this.font.width(suffix);
                // If the whole thing fits on one line, draw inline; otherwise
                // fall back to wrapping the prefix and putting the name on its
                // own line so the click rect stays a tight rectangle.
                int availW = contentRight - textX - rightReserve;
                if (prefixW + nameW + suffixW <= availW) {
                    g.drawString(this.font, prefix, textX, line, color, false);
                    int nameX = textX + prefixW;
                    int[] rect = new int[]{nameX, line - 1, nameW, 10};
                    boolean linkHover = mouseX >= rect[0] && mouseX < rect[0] + rect[2]
                            && mouseY >= rect[1] && mouseY < rect[1] + rect[3];
                    int drawColor = linkHover ? 0xFFFFFFFF : linkColor;
                    g.drawString(this.font, name, nameX, line, drawColor, false);
                    // Underline to signal clickability.
                    g.fill(nameX, line + 9, nameX + nameW, line + 10, drawColor);
                    g.drawString(this.font, suffix, nameX + nameW, line, color, false);
                    itemLinkBounds.add(rect);
                    itemLinkStacks.add(stack);
                    int consumed = 11;
                    if (isConsume && !done) {
                        String label = "Submit";
                        int labelW = this.font.width(label);
                        int btnW = labelW + 8;
                        int sbX = contentRight - btnW;
                        int sbY = line - 1;
                        boolean hover = mouseX >= sbX && mouseX < sbX + btnW && mouseY >= sbY && mouseY < sbY + 12;
                        int bg = hover ? brighten(COL_SUBMIT_BUTTON()) : COL_SUBMIT_BUTTON();
                        g.fill(sbX, sbY, sbX + btnW, sbY + 12, bg);
                        drawBox(g, sbX, sbY, btnW, 12, COL_OUTLINE_IDLE());
                        g.drawString(this.font, label, sbX + 4, sbY + 2, COL_TEXT(), false);
                        submitBounds.put(i, new int[]{sbX, sbY, btnW, 12});
                    }
                    line += consumed;
                    detailTaskBounds.put(i, new int[]{contentX, taskRowStart - 1, wrapW, line - taskRowStart + 1});
                    continue;
                }
                // Wrapped fallback: draw "marker Obtain Nx" on line 1, then
                // "<NAME> (c/t)" on line 2 with the link rect on the name.
                g.drawString(this.font, prefix.stripTrailing(), textX, line, color, false);
                int line2Y = line + 10;
                int[] rect = new int[]{textX, line2Y - 1, nameW, 10};
                boolean linkHover = mouseX >= rect[0] && mouseX < rect[0] + rect[2]
                        && mouseY >= rect[1] && mouseY < rect[1] + rect[3];
                int drawColor = linkHover ? 0xFFFFFFFF : linkColor;
                g.drawString(this.font, name, textX, line2Y, drawColor, false);
                g.fill(textX, line2Y + 9, textX + nameW, line2Y + 10, drawColor);
                g.drawString(this.font, suffix, textX + nameW, line2Y, color, false);
                itemLinkBounds.add(rect);
                itemLinkStacks.add(stack);
                if (isConsume && !done) {
                    String label = "Submit";
                    int labelW = this.font.width(label);
                    int btnW = labelW + 8;
                    int sbX = contentRight - btnW;
                    int sbY = line - 1;
                    boolean hover = mouseX >= sbX && mouseX < sbX + btnW && mouseY >= sbY && mouseY < sbY + 12;
                    int bg = hover ? brighten(COL_SUBMIT_BUTTON()) : COL_SUBMIT_BUTTON();
                    g.fill(sbX, sbY, sbX + btnW, sbY + 12, bg);
                    drawBox(g, sbX, sbY, btnW, 12, COL_OUTLINE_IDLE());
                    g.drawString(this.font, label, sbX + 4, sbY + 2, COL_TEXT(), false);
                    submitBounds.put(i, new int[]{sbX, sbY, btnW, 12});
                }
                line += 21;
                detailTaskBounds.put(i, new int[]{contentX, taskRowStart - 1, wrapW, line - taskRowStart + 1});
                continue;
            } else {
                if (isConsume && !done) rightReserve = this.font.width("Submit") + 12;
                String marker = done ? "✔ " : "• ";
                int color = done ? 0xFF9BDF9B : 0xFFCCCCCC;
                String text = marker + task.describe() + " (" + count + "/" + tgt + ")";
                // Wrap to the remaining width minus the submit-button reserve
                int availW = contentRight - textX - rightReserve;
                List<FormattedCharSequence> lines = this.font.split(Component.literal(text), availW);
                for (int k = 0; k < lines.size(); k++) {
                    g.drawString(this.font, lines.get(k), textX, line + k * 10, color, false);
                }
                int consumed = Math.max(11, lines.size() * 10 + 1);
                if (isConsume && !done) {
                    String label = "Submit";
                    int labelW = this.font.width(label);
                    int btnW = labelW + 8;
                    int sbX = contentRight - btnW;
                    int sbY = line - 1;
                    boolean hover = mouseX >= sbX && mouseX < sbX + btnW && mouseY >= sbY && mouseY < sbY + 12;
                    int bg = hover ? brighten(COL_SUBMIT_BUTTON()) : COL_SUBMIT_BUTTON();
                    g.fill(sbX, sbY, sbX + btnW, sbY + 12, bg);
                    drawBox(g, sbX, sbY, btnW, 12, COL_OUTLINE_IDLE());
                    g.drawString(this.font, label, sbX + 4, sbY + 2, COL_TEXT(), false);
                    submitBounds.put(i, new int[]{sbX, sbY, btnW, 12});
                }
                line += consumed;
                detailTaskBounds.put(i, new int[]{contentX, taskRowStart - 1, wrapW, line - taskRowStart + 1});
                continue;
            }
        }

        QuestStatus status = ClientQuestState.statusOf(q.fullId());
        String btn = switch (status) {
            case LOCKED -> "Locked";
            case VISIBLE -> "In Progress";
            case READY -> "CLAIM";
            case CLAIMED -> "Claimed";
        };
        int btnW = 72, btnH = 18;
        int btnX = x + w - btnW - innerPad, btnY = y + h - btnH - innerPad;
        boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        int btnBg = status == QuestStatus.READY
                ? (btnHover ? brighten(COL_CLAIM_BUTTON()) : COL_CLAIM_BUTTON())
                : COL_SEP();
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        drawBox(g, btnX, btnY, btnW, btnH, COL_BORDER());
        int textW = this.font.width(btn);
        g.drawString(this.font, btn, btnX + (btnW - textW) / 2, btnY + 5, COL_TEXT(), status == QuestStatus.READY);
    }

    private static boolean containsQuest(Chapter c, String questId) {
        for (Quest q : c.quests()) if (q.id().equals(questId)) return true;
        return false;
    }

    private static boolean hit(double mx, double my, int[] r) {
        return mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
    }

    /** Apply a chapter-list context menu pick. */
    private void handleChapterContextChoice(String label) {
        if ("Add Chapter".equals(label)) {
            String id = "chapter_" + Long.toString(System.currentTimeMillis(), 36);
            ModNetworking.CHANNEL.sendToServer(
                    com.soul.soa_additions.quest.net.ChapterEditPacket.add(id, "New Chapter"));
        } else if ("Delete Chapter".equals(label)) {
            if (chapterContextTargetIndex < 0 || chapterContextTargetIndex >= chapters.size()) return;
            Chapter c = chapters.get(chapterContextTargetIndex);
            chapterDeletePendingId = c.id();
            chapterDeletePendingTitle = c.title();
        } else if ("Edit Chapter".equals(label)) {
            if (chapterContextTargetIndex < 0 || chapterContextTargetIndex >= chapters.size()) return;
            Chapter c = chapters.get(chapterContextTargetIndex);
            openChapterEdit(c);
        } else if ("Rename Chapter".equals(label)) {
            if (chapterContextTargetIndex < 0 || chapterContextTargetIndex >= chapters.size()) return;
            Chapter c = chapters.get(chapterContextTargetIndex);
            openChapterRename(c);
        }
    }

    /** Opens the rename modal for the given chapter with its current title
     *  prefilled. The EditBox is constructed lazily — the screen has already
     *  init'd by the time the context menu can possibly be opened. */
    private void openChapterRename(Chapter c) {
        chapterRenamePendingId = c.id();
        int w = 240;
        chapterRenameBox = new EditBox(this.font, 0, 0, w - 24, 18, Component.literal("Chapter title"));
        chapterRenameBox.setMaxLength(128);
        chapterRenameBox.setValue(c.title() == null ? "" : c.title());
        chapterRenameBox.setFocused(true);
        chapterRenameBox.moveCursorToEnd();
    }

    private void submitChapterRename() {
        if (chapterRenamePendingId == null || chapterRenameBox == null) return;
        String newTitle = chapterRenameBox.getValue();
        if (newTitle != null) newTitle = newTitle.trim();
        if (newTitle == null || newTitle.isEmpty()) { cancelChapterRename(); return; }
        ModNetworking.CHANNEL.sendToServer(
                com.soul.soa_additions.quest.net.ChapterEditPacket.rename(chapterRenamePendingId, newTitle));
        cancelChapterRename();
    }

    private void cancelChapterRename() {
        chapterRenamePendingId = null;
        chapterRenameBox = null;
        chapterRenameOkBounds = null;
        chapterRenameCancelBounds = null;
    }

    // ---------- chapter edit modal ----------

    private void openChapterEdit(Chapter c) {
        chapterEditPendingId = c.id();
        chapterEditScrollOffset = 0;
        int fw = 280;
        chapterEditTitleBox = new EditBox(this.font, 0, 0, fw - 24, 16, Component.literal("Title"));
        chapterEditTitleBox.setMaxLength(128);
        chapterEditTitleBox.setValue(c.title() == null ? "" : c.title());

        chapterEditIconBox = new EditBox(this.font, 0, 0, fw - 24, 16, Component.literal("Icon"));
        chapterEditIconBox.setMaxLength(128);
        chapterEditIconBox.setValue(c.icon() == null ? "" : c.icon());

        chapterEditDescBox = new EditBox(this.font, 0, 0, fw - 24, 16, Component.literal("Description"));
        chapterEditDescBox.setMaxLength(512);
        chapterEditDescBox.setValue(c.description() == null ? "" : String.join("||", c.description()));

        chapterEditReqChaptersBox = new EditBox(this.font, 0, 0, fw - 24, 16, Component.literal("Req chapters"));
        chapterEditReqChaptersBox.setMaxLength(512);
        chapterEditReqChaptersBox.setValue(c.requiresChapters() == null ? "" : String.join(",", c.requiresChapters()));

        chapterEditReqQuestsBox = new EditBox(this.font, 0, 0, fw - 24, 16, Component.literal("Req quests"));
        chapterEditReqQuestsBox.setMaxLength(512);
        chapterEditReqQuestsBox.setValue(c.requiresQuests() == null ? "" : String.join(",", c.requiresQuests()));

        chapterEditParentBox = new EditBox(this.font, 0, 0, fw - 24, 16, Component.literal("Parent chapter"));
        chapterEditParentBox.setMaxLength(128);
        chapterEditParentBox.setValue(c.parentChapter() == null ? "" : c.parentChapter());

        chapterEditVisibility = c.visibility() == null ? com.soul.soa_additions.quest.model.Visibility.NORMAL : c.visibility();
    }

    private void submitChapterEdit() {
        if (chapterEditPendingId == null) return;
        String title = chapterEditTitleBox.getValue().trim();
        if (title.isEmpty()) { cancelChapterEdit(); return; }
        List<String> desc = new ArrayList<>();
        String rawDesc = chapterEditDescBox.getValue();
        if (!rawDesc.isEmpty()) {
            for (String part : rawDesc.split("\\|\\|", -1)) desc.add(part);
        }
        List<String> reqCh = splitCsv(chapterEditReqChaptersBox.getValue());
        List<String> reqQ = splitCsv(chapterEditReqQuestsBox.getValue());
        String parent = chapterEditParentBox.getValue().trim();
        ModNetworking.CHANNEL.sendToServer(
                com.soul.soa_additions.quest.net.ChapterEditPacket.edit(
                        chapterEditPendingId, title, desc,
                        chapterEditIconBox.getValue().trim(),
                        chapterEditVisibility, reqCh, reqQ, parent));
        cancelChapterEdit();
    }

    private void cancelChapterEdit() {
        chapterEditPendingId = null;
        chapterEditTitleBox = null;
        chapterEditIconBox = null;
        chapterEditDescBox = null;
        chapterEditReqChaptersBox = null;
        chapterEditReqQuestsBox = null;
        chapterEditParentBox = null;
        chapterEditSaveBounds = null;
        chapterEditCancelBounds = null;
        chapterEditVisBounds = null;
    }

    private List<EditBox> chapterEditBoxes() {
        if (chapterEditPendingId == null) return List.of();
        return List.of(chapterEditTitleBox, chapterEditIconBox, chapterEditDescBox,
                chapterEditReqChaptersBox, chapterEditReqQuestsBox, chapterEditParentBox);
    }

    private static List<String> splitCsv(String raw) {
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    /** Rendered after the rest of the screen so it sits on top. Called from
     *  the existing render() flow alongside the other overlays. */
    private void renderChapterOverlays(GuiGraphics g, int mouseX, int mouseY) {
        if (chapterContextOpen) {
            chapterContextBounds.clear();
            chapterContextLabels.clear();
            List<String> labels = new ArrayList<>();
            labels.add("Add Chapter");
            if (chapterContextTargetIndex >= 0 && chapterContextTargetIndex < chapters.size()) {
                labels.add("Edit Chapter");
                labels.add("Rename Chapter");
                labels.add("Delete Chapter");
            }
            int w = 110, rowH = 14;
            int h = labels.size() * rowH + 4;
            int x = chapterContextX;
            int y = chapterContextY;
            if (x + w > this.width) x = this.width - w;
            if (y + h > this.height) y = this.height - h;
            g.fill(x, y, x + w, y + h, 0xF0202020);
            drawBox(g, x, y, w, h, COL_BORDER());
            for (int i = 0; i < labels.size(); i++) {
                int ry = y + 2 + i * rowH;
                int[] rect = new int[]{x + 2, ry, w - 4, rowH};
                boolean hover = hit(mouseX, mouseY, rect);
                if (hover) g.fill(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3], COL_PANE_HOVER());
                int color = labels.get(i).equals("Delete Chapter") ? 0xFFFF6666 : COL_TEXT();
                g.drawString(this.font, labels.get(i), x + 6, ry + 3, color, false);
                chapterContextBounds.add(rect);
                chapterContextLabels.add(labels.get(i));
            }
        }
        if (chapterDeletePendingId != null) {
            int w = 280, h = 110;
            int x = (this.width - w) / 2;
            int y = (this.height - h) / 2;
            g.fill(0, 0, this.width, this.height, 0xA0000000);
            g.fill(x, y, x + w, y + h, 0xFF1A1A1A);
            drawBox(g, x, y, w, h, COL_BORDER());
            g.drawString(this.font, "Delete \"" + chapterDeletePendingTitle + "\"?", x + 12, y + 12, 0xFFFF6666, false);
            g.drawString(this.font, "This will permanently delete the chapter", x + 12, y + 30, COL_TEXT(), false);
            g.drawString(this.font, "and EVERY quest inside it.", x + 12, y + 42, COL_TEXT(), false);
            int btnW = 80, btnH = 18;
            int confirmX = x + 12, cancelX = x + w - btnW - 12;
            int btnY = y + h - btnH - 12;
            chapterDeleteConfirmBounds = new int[]{confirmX, btnY, btnW, btnH};
            chapterDeleteCancelBounds = new int[]{cancelX, btnY, btnW, btnH};
            boolean hovC = hit(mouseX, mouseY, chapterDeleteConfirmBounds);
            g.fill(confirmX, btnY, confirmX + btnW, btnY + btnH, hovC ? 0xFFAA3333 : 0xFF802020);
            drawBox(g, confirmX, btnY, btnW, btnH, COL_BORDER());
            int tw = this.font.width("Delete");
            g.drawString(this.font, "Delete", confirmX + (btnW - tw) / 2, btnY + 5, 0xFFFFFFFF, false);
            boolean hovX = hit(mouseX, mouseY, chapterDeleteCancelBounds);
            g.fill(cancelX, btnY, cancelX + btnW, btnY + btnH, hovX ? COL_PANE_HOVER() : COL_PANE_ALT());
            drawBox(g, cancelX, btnY, btnW, btnH, COL_BORDER());
            int tw2 = this.font.width("Cancel");
            g.drawString(this.font, "Cancel", cancelX + (btnW - tw2) / 2, btnY + 5, COL_TEXT(), false);
        }
        if (chapterRenamePendingId != null && chapterRenameBox != null) {
            int w = 280, h = 96;
            int x = (this.width - w) / 2;
            int y = (this.height - h) / 2;
            g.fill(0, 0, this.width, this.height, 0xA0000000);
            g.fill(x, y, x + w, y + h, 0xFF1A1A1A);
            drawBox(g, x, y, w, h, COL_BORDER());
            g.drawString(this.font, "Rename chapter", x + 12, y + 12, COL_TEXT(), false);

            // Position + render the EditBox inside the modal each frame (cheap).
            chapterRenameBox.setX(x + 12);
            chapterRenameBox.setY(y + 28);
            chapterRenameBox.setWidth(w - 24);
            chapterRenameBox.render(g, mouseX, mouseY, 0f);

            int btnW = 80, btnH = 18;
            int okX = x + 12, cancelX = x + w - btnW - 12;
            int btnY = y + h - btnH - 12;
            chapterRenameOkBounds = new int[]{okX, btnY, btnW, btnH};
            chapterRenameCancelBounds = new int[]{cancelX, btnY, btnW, btnH};
            boolean hovOk = hit(mouseX, mouseY, chapterRenameOkBounds);
            g.fill(okX, btnY, okX + btnW, btnY + btnH, hovOk ? COL_PANE_HOVER() : COL_PANE_ALT());
            drawBox(g, okX, btnY, btnW, btnH, COL_BORDER());
            int tw3 = this.font.width("Save");
            g.drawString(this.font, "Save", okX + (btnW - tw3) / 2, btnY + 5, COL_TEXT(), false);
            boolean hovCx = hit(mouseX, mouseY, chapterRenameCancelBounds);
            g.fill(cancelX, btnY, cancelX + btnW, btnY + btnH, hovCx ? COL_PANE_HOVER() : COL_PANE_ALT());
            drawBox(g, cancelX, btnY, btnW, btnH, COL_BORDER());
            int tw4 = this.font.width("Cancel");
            g.drawString(this.font, "Cancel", cancelX + (btnW - tw4) / 2, btnY + 5, COL_TEXT(), false);
        }
        if (chapterEditPendingId != null) {
            renderChapterEditModal(g, mouseX, mouseY);
        }
    }

    private void renderChapterEditModal(GuiGraphics g, int mouseX, int mouseY) {
        int w = 340;
        // 6 fields × 28 (label 10 + box 16 + gap 2) + visibility button (20) + header (24) + buttons (30) + padding (16)
        int h = 6 * 28 + 20 + 24 + 30 + 16;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        g.fill(0, 0, this.width, this.height, 0xA0000000);
        g.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        drawBox(g, x, y, w, h, COL_BORDER());
        g.fill(x, y, x + w, y + 20, COL_DETAIL_HEADER());
        g.drawString(this.font, "Edit Chapter: " + chapterEditPendingId, x + 8, y + 6, COL_TEXT(), true);

        int lx = x + 12;
        int fw = w - 24;
        int row = y + 24;
        int fieldH = 28;

        // Title
        g.drawString(this.font, "Title", lx, row, COL_TEXT_DIM(), false);
        row += 10;
        chapterEditTitleBox.setX(lx); chapterEditTitleBox.setY(row); chapterEditTitleBox.setWidth(fw);
        chapterEditTitleBox.render(g, mouseX, mouseY, 0f);
        row += fieldH - 10;

        // Icon
        g.drawString(this.font, "Icon", lx, row, COL_TEXT_DIM(), false);
        row += 10;
        chapterEditIconBox.setX(lx); chapterEditIconBox.setY(row); chapterEditIconBox.setWidth(fw);
        chapterEditIconBox.render(g, mouseX, mouseY, 0f);
        row += fieldH - 10;

        // Description
        g.drawString(this.font, "Description (use || for line breaks)", lx, row, COL_TEXT_DIM(), false);
        row += 10;
        chapterEditDescBox.setX(lx); chapterEditDescBox.setY(row); chapterEditDescBox.setWidth(fw);
        chapterEditDescBox.render(g, mouseX, mouseY, 0f);
        row += fieldH - 10;

        // Requires Chapters
        g.drawString(this.font, "Requires chapters (comma-separated ids)", lx, row, COL_TEXT_DIM(), false);
        row += 10;
        chapterEditReqChaptersBox.setX(lx); chapterEditReqChaptersBox.setY(row); chapterEditReqChaptersBox.setWidth(fw);
        chapterEditReqChaptersBox.render(g, mouseX, mouseY, 0f);
        row += fieldH - 10;

        // Requires Quests
        g.drawString(this.font, "Requires quests (comma-separated ids)", lx, row, COL_TEXT_DIM(), false);
        row += 10;
        chapterEditReqQuestsBox.setX(lx); chapterEditReqQuestsBox.setY(row); chapterEditReqQuestsBox.setWidth(fw);
        chapterEditReqQuestsBox.render(g, mouseX, mouseY, 0f);
        row += fieldH - 10;

        // Parent chapter
        g.drawString(this.font, "Parent chapter id (blank = top-level)", lx, row, COL_TEXT_DIM(), false);
        row += 10;
        chapterEditParentBox.setX(lx); chapterEditParentBox.setY(row); chapterEditParentBox.setWidth(fw);
        chapterEditParentBox.render(g, mouseX, mouseY, 0f);
        row += fieldH - 10;

        // Visibility toggle button
        row += 4;
        chapterEditVisBounds = new int[]{lx, row, 180, 16};
        drawFormButton(g, lx, row, 180, "Visibility: " + chapterEditVisibility.lower(), mouseX, mouseY);
        row += 24;

        // Save / Cancel buttons
        int btnW = 80, btnH = 18;
        int saveX = lx, cancelX = x + w - btnW - 12;
        chapterEditSaveBounds = new int[]{saveX, row, btnW, btnH};
        chapterEditCancelBounds = new int[]{cancelX, row, btnW, btnH};
        boolean hovSave = hit(mouseX, mouseY, chapterEditSaveBounds);
        g.fill(saveX, row, saveX + btnW, row + btnH, hovSave ? COL_PANE_HOVER() : COL_PANE_ALT());
        drawBox(g, saveX, row, btnW, btnH, COL_BORDER());
        int twS = this.font.width("Save");
        g.drawString(this.font, "Save", saveX + (btnW - twS) / 2, row + 5, COL_TEXT(), false);
        boolean hovCancel = hit(mouseX, mouseY, chapterEditCancelBounds);
        g.fill(cancelX, row, cancelX + btnW, row + btnH, hovCancel ? COL_PANE_HOVER() : COL_PANE_ALT());
        drawBox(g, cancelX, row, btnW, btnH, COL_BORDER());
        int twC = this.font.width("Cancel");
        g.drawString(this.font, "Cancel", cancelX + (btnW - twC) / 2, row + 5, COL_TEXT(), false);
    }

    /** Lightens an ARGB color by ~20% for hover states. Clamps per-channel. */
    private static int brighten(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 40);
        int gr = Math.min(255, ((argb >> 8) & 0xFF) + 40);
        int bl = Math.min(255, (argb & 0xFF) + 40);
        return (a << 24) | (r << 16) | (gr << 8) | bl;
    }

    // ---------- input ----------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Chapter edit modal — absorbs all input while open.
        if (chapterEditPendingId != null) {
            if (chapterEditSaveBounds != null && hit(mouseX, mouseY, chapterEditSaveBounds)) {
                submitChapterEdit();
                return true;
            }
            if (chapterEditCancelBounds != null && hit(mouseX, mouseY, chapterEditCancelBounds)) {
                cancelChapterEdit();
                return true;
            }
            if (chapterEditVisBounds != null && hit(mouseX, mouseY, chapterEditVisBounds)) {
                chapterEditVisibility = chapterEditVisibility.next();
                return true;
            }
            // Route clicks to EditBoxes for focus management.
            for (EditBox eb : chapterEditBoxes()) {
                boolean in = mouseX >= eb.getX() && mouseX < eb.getX() + eb.getWidth()
                        && mouseY >= eb.getY() && mouseY < eb.getY() + eb.getHeight();
                eb.setFocused(in);
                if (in) eb.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        // Rename-chapter modal is topmost — absorbs all input while it's up.
        if (chapterRenamePendingId != null && chapterRenameBox != null) {
            if (chapterRenameOkBounds != null && hit(mouseX, mouseY, chapterRenameOkBounds)) {
                submitChapterRename();
                return true;
            }
            if (chapterRenameCancelBounds != null && hit(mouseX, mouseY, chapterRenameCancelBounds)) {
                cancelChapterRename();
                return true;
            }
            // Let the EditBox handle the click (focus / caret placement).
            chapterRenameBox.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        // Chapter delete-confirmation overlay is topmost — it's a modal yes/no
        // and absorbs every click while it's up.
        if (chapterDeletePendingId != null) {
            if (chapterDeleteConfirmBounds != null
                    && hit(mouseX, mouseY, chapterDeleteConfirmBounds)) {
                ModNetworking.CHANNEL.sendToServer(
                        com.soul.soa_additions.quest.net.ChapterEditPacket.delete(chapterDeletePendingId));
                chapterDeletePendingId = null;
                return true;
            }
            chapterDeletePendingId = null;
            return true;
        }
        // Chapter context menu intercepts clicks while open.
        if (chapterContextOpen) {
            for (int i = 0; i < chapterContextBounds.size(); i++) {
                if (hit(mouseX, mouseY, chapterContextBounds.get(i))) {
                    handleChapterContextChoice(chapterContextLabels.get(i));
                    chapterContextOpen = false;
                    return true;
                }
            }
            chapterContextOpen = false;
            return true;
        }
        // Type dropdown is even higher than the icon picker — it's a transient
        // overlay opened from a task row's type button. Any click closes it,
        // and clicks inside it pick a type before closing.
        if (typeDropdownRow >= 0) {
            handleTypeDropdownClick(mouseX, mouseY);
            return true;
        }
        if (rewardTypeDropdownRow >= 0) {
            handleRewardTypeDropdownClick(mouseX, mouseY);
            return true;
        }
        // Right-click in the chapter list (edit mode only) opens its own menu.
        if (button == 1 && inEditMode() && mouseX < LEFT_PANE_WIDTH) {
            chapterContextTargetIndex = chapterIndexAt((int) mouseY);
            chapterContextOpen = true;
            chapterContextX = (int) mouseX;
            chapterContextY = (int) mouseY;
            return true;
        }
        // Icon picker is always topmost — it eats clicks inside its bounds and
        // dismisses on clicks outside. The picker only ever opens via the edit
        // form, so the form is guaranteed to still be present underneath.
        if (iconPicker != null) {
            int[] r = formRect();
            int px = r[0] + r[2] + 8;
            if (px + IconPickerPopup.W > this.width - 4) px = r[0] - IconPickerPopup.W - 8;
            int py = r[1];
            if (iconPicker.click(px, py, mouseX, mouseY, button)) return true;
            iconPicker = null;
            return true;
        }
        // Edit form eats most input while open. Two exceptions: title-bar
        // drag, and click-through to background quest nodes when the deps
        // field is focused (for picking dependencies visually).
        if (editForm != null) {
            int[] r = formRect();
            int x = r[0], y = r[1], w = r[2], h = r[3];
            boolean insidePopup = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
            // Title bar drag
            if (button == 0 && insidePopup && mouseY < y + 24) {
                popupDragging = true;
                popupDragStartMx = mouseX;
                popupDragStartMy = mouseY;
                popupDragStartOffX = editForm.dragOffsetX;
                popupDragStartOffY = editForm.dragOffsetY;
                return true;
            }
            if (insidePopup) {
                if (handleFormClick(mouseX, mouseY, button)) return true;
                return true;
            }
            // Click-through: if the deps field is focused and the click hits
            // a background quest node, append its id to the deps list.
            if (button == 0 && editForm.depsFieldFocused() && hoveredQuest != null
                    && !hoveredQuest.id().equals(editForm.questId)) {
                editForm.appendDependency(hoveredQuest.id());
                return true;
            }
            // Any other outside click is swallowed so we don't open the
            // detail popup or fire a placement-mode click while editing.
            return true;
        }
        // Context menu: a click anywhere while open either activates a row
        // or dismisses the menu.
        if (contextMenuOpen) {
            if (button == 0) {
                for (int i = 0; i < contextMenuBounds.size(); i++) {
                    int[] b = contextMenuBounds.get(i);
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        activateContextMenu(i);
                        return true;
                    }
                }
            }
            closeContextMenu();
            return true;
        }
        // Right-click in the graph opens a context menu (on a node or empty
        // space). Chapter list and detail popup ignore right-click.
        if (button == 1) {
            // Right-click on a task row inside the detail popup opens a small
            // menu with "Copy unlock command" — handy for wiring up custom
            // (CHECKMARK) tasks to datapacks / command rewards.
            if (openDetailQuest != null) {
                for (var entry : detailTaskBounds.entrySet()) {
                    int[] b = entry.getValue();
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        openTaskContextMenu(mouseX, mouseY, openDetailQuest.fullId(), entry.getKey());
                        return true;
                    }
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
            if (com.soul.soa_additions.quest.net.ClientQuestState.editMode()
                    && mouseX >= LEFT_PANE_WIDTH && mouseY >= HEADER_H) {
                openContextMenu(mouseX, mouseY, hoveredQuest);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        // Placement mode: first left-click in graph area spawns a new form.
        if (button == 0 && placementMode && openDetailQuest == null
                && mouseX >= LEFT_PANE_WIDTH && mouseY >= HEADER_H && selected != null) {
            int cx = (int) (mouseX - graphOriginX);
            int cy = (int) (mouseY - graphOriginY);
            cx = Math.round((float) cx / SNAP) * SNAP;
            cy = Math.round((float) cy / SNAP) * SNAP;
            placementMode = false;
            editForm = new QuestEditForm(selected.id(), null, cx, cy);
            return true;
        }
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        if (openDetailQuest != null) {
            // Checkmark clicks first — they sit inside the detail popup.
            for (var entry : checkmarkBounds.entrySet()) {
                int[] b = entry.getValue();
                if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                    ModNetworking.CHANNEL.sendToServer(
                            new QuestCheckmarkPacket(openDetailQuest.fullId(), entry.getKey()));
                    return true;
                }
            }
            // Item-name link → open JEI for that item
            for (int i = 0; i < itemLinkBounds.size(); i++) {
                int[] b = itemLinkBounds.get(i);
                if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                    com.soul.soa_additions.quest.client.jei.JeiCompat.showItem(itemLinkStacks.get(i));
                    return true;
                }
            }
            // Submit buttons for consume ItemTasks
            for (var entry : submitBounds.entrySet()) {
                int[] b = entry.getValue();
                if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                    ModNetworking.CHANNEL.sendToServer(
                            new com.soul.soa_additions.quest.net.QuestSubmitPacket(
                                    openDetailQuest.fullId(), entry.getKey()));
                    return true;
                }
            }
            // Detail claim button
            int w = 280, h = 200;
            int x = (this.width - w) / 2;
            int y = (this.height - h) / 2;
            int innerPad = 10, btnW = 72, btnH = 18;
            int btnX = x + w - btnW - innerPad, btnY = y + h - btnH - innerPad;
            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                if (ClientQuestState.statusOf(openDetailQuest.fullId()) == QuestStatus.READY) {
                    ModNetworking.CHANNEL.sendToServer(new QuestClaimPacket(openDetailQuest.fullId()));
                    // Close the detail popup on claim so the player sees
                    // the chat reward summary + the node state flip back in
                    // the graph instead of staring at a now-stale popup.
                    openDetailQuest = null;
                }
                return true;
            }
            // Click outside closes detail
            if (mouseX < x || mouseX >= x + w || mouseY < y || mouseY >= y + h) {
                openDetailQuest = null;
                return true;
            }
            return true;
        }

        // Chapter list click
        if (mouseX < LEFT_PANE_WIDTH) {
            int idx = chapterIndexAt((int) mouseY);
            // In edit mode, arm a drag for reorder. The actual reorder is
            // committed in mouseReleased once movement clears DRAG_THRESHOLD;
            // a click without drag still selects the chapter.
            if (inEditMode() && idx >= 0) {
                chapterDragIndex = idx;
                chapterDragActive = false;
                chapterDragStartY = mouseY;
            }
            if (idx >= 0) selectChapter(chapters.get(idx));
            return true;
        }

        // Node click — in edit mode, arm a drag but don't commit to it
        // until the cursor actually moves DRAG_THRESHOLD pixels. A straight
        // press-and-release still opens the detail popup.
        if (hoveredQuest != null) {
            if (com.soul.soa_additions.quest.net.ClientQuestState.editMode()) {
                int[] px = nodePixel(hoveredQuest.fullId());
                if (px != null) {
                    draggingQuestId = hoveredQuest.fullId();
                    dragActive = false;
                    dragGrabOffsetX = (int) mouseX - (graphOriginX + px[0]);
                    dragGrabOffsetY = (int) mouseY - (graphOriginY + px[1]);
                    dragStartX = mouseX;
                    dragStartY = mouseY;
                    return true;
                }
            }
            openDetailQuest = hoveredQuest;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (chapterDragIndex >= 0 && button == 0) {
            if (!chapterDragActive && Math.abs(mouseY - chapterDragStartY) >= DRAG_THRESHOLD) {
                chapterDragActive = true;
            }
            return true;
        }
        if (popupDragging && editForm != null && button == 0) {
            editForm.dragOffsetX = popupDragStartOffX + (int) (mouseX - popupDragStartMx);
            editForm.dragOffsetY = popupDragStartOffY + (int) (mouseY - popupDragStartMy);
            return true;
        }
        if (draggingQuestId != null && button == 0) {
            if (!dragActive) {
                double moved = Math.hypot(mouseX - dragStartX, mouseY - dragStartY);
                if (moved < DRAG_THRESHOLD) return true;
                dragActive = true;
            }
            int nx = (int) mouseX - dragGrabOffsetX - graphOriginX;
            int ny = (int) mouseY - dragGrabOffsetY - graphOriginY;
            // Snap to the nearest grid cell.
            nx = Math.round((float) nx / SNAP) * SNAP;
            ny = Math.round((float) ny / SNAP) * SNAP;
            com.soul.soa_additions.quest.net.ClientQuestEditState.setLocal(draggingQuestId, nx, ny);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (chapterDragIndex >= 0 && button == 0) {
            int from = chapterDragIndex;
            int to = chapterDragInsertIndex;
            boolean wasDrag = chapterDragActive;
            chapterDragIndex = -1;
            chapterDragInsertIndex = -1;
            chapterDragActive = false;
            if (wasDrag && to >= 0 && to != from && to != from + 1) {
                List<String> ids = new ArrayList<>(chapters.size());
                for (Chapter c : chapters) ids.add(c.id());
                String moved = ids.remove(from);
                int adjusted = to > from ? to - 1 : to;
                if (adjusted > ids.size()) adjusted = ids.size();
                ids.add(adjusted, moved);
                ModNetworking.CHANNEL.sendToServer(
                        com.soul.soa_additions.quest.net.ChapterEditPacket.reorder(ids));
            }
            return true;
        }
        if (popupDragging && button == 0) {
            popupDragging = false;
            return true;
        }
        if (draggingQuestId != null && button == 0) {
            String id = draggingQuestId;
            boolean wasDrag = dragActive;
            draggingQuestId = null;
            dragActive = false;
            if (wasDrag) {
                int[] pos = com.soul.soa_additions.quest.net.ClientQuestEditState.get(id);
                if (pos != null) {
                    int slash = id.indexOf('/');
                    if (slash > 0) {
                        String chapterId = id.substring(0, slash);
                        String questId = id.substring(slash + 1);
                        ModNetworking.CHANNEL.sendToServer(
                                new com.soul.soa_additions.quest.net.QuestMovePacket(chapterId, questId, pos[0], pos[1]));
                    }
                }
            } else {
                // Treat as a plain click: open the detail popup for the
                // quest we armed the drag on.
                com.soul.soa_additions.quest.QuestRegistry.quest(id).ifPresent(q -> openDetailQuest = q);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ---------- editor overlay ----------

    /** Context menu rows for the right-click popup. Stored as the list of
     * form buttons drawn + click-targeted; bounds are computed in render. */
    private void openContextMenu(double mouseX, double mouseY, Quest target) {
        contextMenuOpen = true;
        contextQuest = target;
        contextMenuX = (int) mouseX;
        contextMenuY = (int) mouseY;
        contextMenuLabels.clear();
        contextMenuBounds.clear();
        if (target != null) {
            contextMenuLabels.add("Edit quest");
            contextMenuLabels.add("Copy ID");
            contextMenuLabels.add("Delete quest");
        }
        contextMenuLabels.add("Add quest here");
    }

    private void closeContextMenu() {
        contextMenuOpen = false;
        contextQuest = null;
        contextTaskFullId = null;
        contextTaskIndex = -1;
        contextMenuLabels.clear();
        contextMenuBounds.clear();
    }

    /** Task-row context menu (right-click inside the detail popup). Offers
     *  clipboard shortcuts for the {@code /soa quests task} subcommands so
     *  CHECKMARK / custom tasks can be wired into datapacks or command
     *  rewards without retyping ids. */
    private void openTaskContextMenu(double mouseX, double mouseY, String fullId, int taskIndex) {
        contextMenuOpen = true;
        contextQuest = null;
        contextTaskFullId = fullId;
        contextTaskIndex = taskIndex;
        contextMenuX = (int) mouseX;
        contextMenuY = (int) mouseY;
        contextMenuLabels.clear();
        contextMenuBounds.clear();
        contextMenuLabels.add("Copy unlock command");
        contextMenuLabels.add("Copy reset command");
    }

    private void activateContextMenu(int rowIndex) {
        String label = contextMenuLabels.get(rowIndex);
        Quest q = contextQuest;
        String taskFullId = contextTaskFullId;
        int taskIdx = contextTaskIndex;
        closeContextMenu();
        switch (label) {
            case "Copy unlock command" -> {
                if (taskFullId != null && taskIdx >= 0) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(
                            "/soa quests task complete @p " + taskFullId + " " + taskIdx);
                }
                return;
            }
            case "Copy reset command" -> {
                if (taskFullId != null && taskIdx >= 0) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(
                            "/soa quests task uncomplete @p " + taskFullId + " " + taskIdx);
                }
                return;
            }
        }
        switch (label) {
            case "Edit quest" -> {
                if (q != null && selected != null) {
                    int[] px = nodePixel(q.fullId());
                    int dx = px != null ? px[0] : 0;
                    int dy = px != null ? px[1] : 0;
                    editForm = new QuestEditForm(selected.id(), q, dx, dy);
                }
            }
            case "Copy ID" -> {
                if (q != null) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(q.id());
                }
            }
            case "Delete quest" -> {
                if (q != null && selected != null) {
                    ModNetworking.CHANNEL.sendToServer(
                            com.soul.soa_additions.quest.net.QuestEditPacket.delete(selected.id(), q.id()));
                }
            }
            case "Add quest here" -> {
                placementMode = true;
            }
        }
    }

    private void renderContextMenu(GuiGraphics g, int mouseX, int mouseY) {
        int rowH = 14;
        int pad = 4;
        int w = 0;
        for (String s : contextMenuLabels) w = Math.max(w, this.font.width(s));
        w += pad * 2 + 4;
        int h = contextMenuLabels.size() * rowH + pad * 2;
        int x = contextMenuX;
        int y = contextMenuY;
        if (x + w > this.width) x = this.width - w - 2;
        if (y + h > this.height) y = this.height - h - 2;
        g.fill(x, y, x + w, y + h, COL_DETAIL_BG());
        drawBox(g, x, y, w, h, COL_DETAIL_BORDER());
        contextMenuBounds.clear();
        for (int i = 0; i < contextMenuLabels.size(); i++) {
            int ry = y + pad + i * rowH;
            int[] b = new int[]{x + pad, ry, w - pad * 2, rowH};
            contextMenuBounds.add(b);
            boolean hover = mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3];
            if (hover) g.fill(b[0], b[1], b[0] + b[2], b[1] + b[3], COL_PANE_HOVER());
            g.drawString(this.font, contextMenuLabels.get(i), b[0] + 4, b[1] + 3, COL_TEXT(), false);
        }
    }

    // --- edit form ---

    private int[] formRect() {
        int w = 380;
        int h = editForm == null ? 200 : editForm.totalHeight();
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        if (editForm != null) {
            x += editForm.dragOffsetX;
            y += editForm.dragOffsetY;
        }
        return new int[]{x, y, w, h};
    }

    private void renderEditForm(GuiGraphics g, int mouseX, int mouseY, float pt) {
        int[] r = formRect();
        int x = r[0], y = r[1], w = r[2], h = r[3];
        g.fill(x + 4, y + 4, x + w + 4, y + h + 4, COL_DETAIL_SHADOW());
        g.fill(x, y, x + w, y + h, COL_DETAIL_BG());
        g.fill(x, y, x + w, y + 24, COL_DETAIL_HEADER());
        drawBox(g, x, y, w, h, COL_DETAIL_BORDER());
        String title = editForm.isNew ? "New Quest" : ("Edit: " + editForm.questId);
        g.drawString(this.font, title, x + 10, y + 8, COL_TEXT(), true);
        String hint = "(drag)";
        g.drawString(this.font, hint, x + w - this.font.width(hint) - 8, y + 8, COL_TEXT_DIM(), false);

        // Tab strip just below the title bar.
        renderTabStrip(g, x, y + 24, w, mouseX, mouseY);

        editForm.layout(x, y, w);

        int pad = QuestEditForm.PAD;
        int lx = x + pad;
        int labelOffset = 11;

        // Clear per-row bounds that only apply to specific tabs.
        taskRowRemoveBounds.clear();
        taskRowTypeBounds.clear();
        taskRowTagBounds.clear();
        taskRowConsumeBounds.clear();
        rewardRowTypeBounds.clear();
        rewardRowRemoveBounds.clear();
        rewardRowLevelsBounds.clear();
        rewardRowScopeBounds.clear();
        taskAddBounds = null;
        rewardAddBounds = null;
        iconPickButtonBounds = null;

        switch (editForm.activeTab) {
            case GENERAL -> {
                g.drawString(this.font, "Title",
                        lx, editForm.titleField.getY() - labelOffset, COL_TEXT_DIM(), false);
                g.drawString(this.font, "Icon",
                        lx, editForm.iconField.getY() - labelOffset, COL_TEXT_DIM(), false);
                g.drawString(this.font, "Description (Enter = newline, || = blank line)",
                        lx, editForm.descField.getY() - labelOffset, COL_TEXT_DIM(), false);
                g.drawString(this.font, "X", lx,        editForm.xField.getY() - labelOffset, COL_TEXT_DIM(), false);
                g.drawString(this.font, "Y", lx + 72,   editForm.yField.getY() - labelOffset, COL_TEXT_DIM(), false);
                g.drawString(this.font, "Size", lx + 144, editForm.sizeField.getY() - labelOffset, COL_TEXT_DIM(), false);

                editForm.renderFields(g, mouseX, mouseY, pt);

                // Icon picker button.
                int iconRowY = editForm.iconField.getY();
                int pickX = x + w - pad - 60;
                iconPickButtonBounds = new int[]{pickX, iconRowY, 60, 16};
                drawFormButton(g, pickX, iconRowY + 1, 60, "Pick\u2026", mouseX, mouseY);
                try {
                    ItemStack stack = resolveIcon(editForm.iconField.getValue());
                    g.renderFakeItem(stack, pickX - 20, iconRowY);
                } catch (Exception ignored) {}

                // Shape button below X/Y/Size.
                int shapeY = editForm.xField.getY() + QuestEditForm.ROW_H;
                drawFormButton(g, lx, shapeY, 120, "Shape: " + editForm.shape.name(), mouseX, mouseY);
            }
            case DEPS -> {
                g.drawString(this.font, "Dependencies (comma-separated ids, click nodes to add)",
                        lx, editForm.depsField.getY() - labelOffset, COL_TEXT_DIM(), false);
                g.drawString(this.font, "Exclusions (ids \u2014 once any completes, this locks)",
                        lx, editForm.exclField.getY() - labelOffset, COL_TEXT_DIM(), false);

                editForm.renderFields(g, mouseX, mouseY, pt);

                // Dep mode + show lines buttons below the fields.
                int btnY = editForm.exclField.getY() + QuestEditForm.ROW_H;
                drawFormButton(g, lx,       btnY, 100, "Deps: " + (editForm.depsAll ? "ALL" : "ANY"), mouseX, mouseY);
                drawFormButton(g, lx + 106, btnY, 130, "Show lines: " + onOff(editForm.showDeps), mouseX, mouseY);
            }
            case FLAGS -> {
                int top = editForm.contentTop(y);
                int flagRow = top;
                drawFormButton(g, lx,        flagRow, 130, "Vis: " + editForm.visibility.lower(), mouseX, mouseY);
                drawFormButton(g, lx + 136,  flagRow, 100, "Optional: " + onOff(editForm.optional), mouseX, mouseY);
                int flagRow2 = flagRow + 20;
                drawFormButton(g, lx,        flagRow2, 130, "Auto-claim: " + onOff(editForm.autoClaim), mouseX, mouseY);
                int flagRow3 = flagRow2 + 20;
                drawFormButton(g, lx,        flagRow3, 130, "Repeatable: " + onOff(editForm.repeatable), mouseX, mouseY);
                drawFormButton(g, lx + 136,  flagRow3, 130, "Repeat: " + editForm.repeatScope.lower(), mouseX, mouseY);
            }
            case TASKS -> {
                int sectionTop = editForm.listSectionTop(y);
                g.fill(x + pad, sectionTop, x + w - pad, sectionTop + 1, COL_SEP());
                g.drawString(this.font, "TASKS", x + pad, sectionTop + 3, COL_ACCENT(), false);

                int rowY = sectionTop + 14;
                for (int i = 0; i < editForm.taskRows.size(); i++) {
                    QuestEditForm.TaskRow tr = editForm.taskRows.get(i);
                    int typeBtnX = x + pad;
                    int typeBtnW = 88;
                    taskRowTypeBounds.add(new int[]{typeBtnX, rowY, typeBtnW, 14});
                    drawFormButton(g, typeBtnX, rowY, typeBtnW, tr.type.name(), mouseX, mouseY);
                    g.drawString(this.font, "\u25BC",
                            typeBtnX + typeBtnW - 9, rowY + 3, COL_TEXT_DIM(), false);
                    int removeX = x + w - pad - 14;
                    taskRowRemoveBounds.add(new int[]{removeX, rowY, 14, 14});
                    drawFormButton(g, removeX, rowY, 14, "x", mouseX, mouseY);

                    if (tr.hasSubRow()) {
                        int subY = rowY + QuestEditForm.TASK_ROW_H;
                        int[] tagB = null, conB = null;
                        if (tr.type.supportsTag()) {
                            tagB = new int[]{typeBtnX, subY, 72, 14};
                            drawFormButton(g, tagB[0], tagB[1], tagB[2], "Tag: " + onOff(tr.tagMode), mouseX, mouseY);
                        }
                        if (tr.type.supportsConsume()) {
                            int cx = typeBtnX + (tagB != null ? 78 : 0);
                            conB = new int[]{cx, subY, 92, 14};
                            drawFormButton(g, conB[0], conB[1], conB[2], "Consume: " + onOff(tr.consume), mouseX, mouseY);
                        }
                        if (tr.type.usesAux()) {
                            g.drawString(this.font, "stat value:", x + pad, subY + 3, COL_TEXT_DIM(), false);
                        }
                        taskRowTagBounds.add(tagB);
                        taskRowConsumeBounds.add(conB);
                    } else {
                        taskRowTagBounds.add(null);
                        taskRowConsumeBounds.add(null);
                    }

                    rowY += editForm.rowHeight(tr);
                }
                taskAddBounds = new int[]{x + pad, rowY + 2, 86, 14};
                drawFormButton(g, taskAddBounds[0], taskAddBounds[1], taskAddBounds[2], "+ Add task", mouseX, mouseY);

                editForm.renderFields(g, mouseX, mouseY, pt);
            }
            case REWARDS -> {
                int rSectionTop = editForm.listSectionTop(y);
                g.fill(x + pad, rSectionTop, x + w - pad, rSectionTop + 1, COL_SEP());
                g.drawString(this.font, "REWARDS", x + pad, rSectionTop + 3, COL_ACCENT(), false);

                int rRowY = rSectionTop + 14;
                for (int i = 0; i < editForm.rewardRows.size(); i++) {
                    QuestEditForm.RewardRow rr = editForm.rewardRows.get(i);
                    int typeBtnX = x + pad;
                    int typeBtnW = 88;
                    rewardRowTypeBounds.add(new int[]{typeBtnX, rRowY, typeBtnW, 14});
                    drawFormButton(g, typeBtnX, rRowY, typeBtnW, rr.type.name(), mouseX, mouseY);
                    g.drawString(this.font, "\u25BC",
                            typeBtnX + typeBtnW - 9, rRowY + 3, COL_TEXT_DIM(), false);

                    if (rr.type == com.soul.soa_additions.quest.net.RewardDraft.Type.OTHER) {
                        int lx2 = typeBtnX + typeBtnW + 4;
                        g.drawString(this.font, "(preserved) " + rr.value.getValue(),
                                lx2, rRowY + 3, COL_TEXT_DIM(), false);
                    }

                    if (rr.type == com.soul.soa_additions.quest.net.RewardDraft.Type.XP) {
                        int lbX = x + w - pad - 14 - 4 - 72;
                        int[] lb = new int[]{lbX, rRowY, 72, 14};
                        rewardRowLevelsBounds.add(lb);
                        drawFormButton(g, lb[0], lb[1], lb[2], "Levels: " + onOff(rr.levels), mouseX, mouseY);
                    } else {
                        rewardRowLevelsBounds.add(null);
                    }

                    int pTBtnX = x + w - pad - 14 - 4 - 20;
                    if (rr.type == com.soul.soa_additions.quest.net.RewardDraft.Type.XP) pTBtnX -= 76;
                    int[] scB = new int[]{pTBtnX, rRowY, 20, 14};
                    rewardRowScopeBounds.add(scB);
                    drawFormButton(g, scB[0], scB[1], scB[2],
                            rr.scope == com.soul.soa_additions.quest.model.RewardScope.TEAM ? "T" : "P",
                            mouseX, mouseY);

                    int removeX = x + w - pad - 14;
                    rewardRowRemoveBounds.add(new int[]{removeX, rRowY, 14, 14});
                    drawFormButton(g, removeX, rRowY, 14, "x", mouseX, mouseY);

                    rRowY += QuestEditForm.REWARD_ROW_H;
                }
                rewardAddBounds = new int[]{x + pad, rRowY + 2, 96, 14};
                drawFormButton(g, rewardAddBounds[0], rewardAddBounds[1], rewardAddBounds[2], "+ Add reward", mouseX, mouseY);

                editForm.renderFields(g, mouseX, mouseY, pt);
            }
        }

        // Save / Cancel pinned bottom-right.
        drawFormButton(g, x + w - 64 - pad, y + h - 22, 60, "Save",   mouseX, mouseY);
        drawFormButton(g, x + w - 130 - pad, y + h - 22, 60, "Cancel", mouseX, mouseY);
    }

    /** Renders the tab strip below the title bar. */
    private void renderTabStrip(GuiGraphics g, int x, int stripY, int w, int mouseX, int mouseY) {
        int tabCount = QuestEditForm.Tab.count();
        int tabW = (w - QuestEditForm.PAD * 2) / tabCount;
        int lx = x + QuestEditForm.PAD;
        // Background for the strip.
        g.fill(x, stripY, x + w, stripY + QuestEditForm.TAB_STRIP_H, COL_PANE());
        g.fill(x, stripY + QuestEditForm.TAB_STRIP_H - 1, x + w, stripY + QuestEditForm.TAB_STRIP_H, COL_SEP());
        tabBounds.clear();
        for (int i = 0; i < tabCount; i++) {
            QuestEditForm.Tab tab = QuestEditForm.Tab.byIndex(i);
            int tx = lx + i * tabW;
            int tw = (i == tabCount - 1) ? (x + w - QuestEditForm.PAD - tx) : tabW; // last tab absorbs remainder
            tabBounds.add(new int[]{tx, stripY, tw, QuestEditForm.TAB_STRIP_H});
            boolean active = tab == editForm.activeTab;
            boolean hover = mouseX >= tx && mouseX < tx + tw && mouseY >= stripY && mouseY < stripY + QuestEditForm.TAB_STRIP_H;
            int bg = active ? COL_DETAIL_BG() : (hover ? COL_PANE_HOVER() : COL_PANE());
            g.fill(tx, stripY, tx + tw, stripY + QuestEditForm.TAB_STRIP_H, bg);
            if (active) {
                // Active tab gets no bottom border so it blends into content.
                g.fill(tx, stripY + QuestEditForm.TAB_STRIP_H - 1, tx + tw, stripY + QuestEditForm.TAB_STRIP_H, COL_DETAIL_BG());
            }
            // Side borders between tabs.
            if (i > 0) g.fill(tx, stripY, tx + 1, stripY + QuestEditForm.TAB_STRIP_H, COL_SEP());
            int textW = this.font.width(tab.label);
            g.drawString(this.font, tab.label, tx + (tw - textW) / 2, stripY + 5, active ? COL_ACCENT() : COL_TEXT(), false);
        }
    }

    private static String onOff(boolean b) { return b ? "ON" : "OFF"; }

    /** Renders the task-type dropdown opened from a row's type button. */
    private void renderTypeDropdown(GuiGraphics g, int mouseX, int mouseY) {
        var types = com.soul.soa_additions.quest.net.TaskDraft.Type.values();
        int rowH = 14;
        int w = 96;
        int h = types.length * rowH + 2;
        int dx = dropdownX;
        int dy = dropdownY;
        // Keep the list on-screen if it would overflow the bottom edge.
        if (dy + h > this.height - 4) dy = Math.max(4, this.height - 4 - h);
        g.fill(dx, dy, dx + w, dy + h, COL_DETAIL_BG());
        drawBox(g, dx, dy, w, h, COL_BORDER());
        for (int i = 0; i < types.length; i++) {
            int ry = dy + 1 + i * rowH;
            boolean hover = mouseX >= dx && mouseX < dx + w && mouseY >= ry && mouseY < ry + rowH;
            if (hover) g.fill(dx + 1, ry, dx + w - 1, ry + rowH, COL_PANE_HOVER());
            g.drawString(this.font, types[i].name(), dx + 4, ry + 3, COL_TEXT(), false);
        }
    }

    /** Renders the reward-type dropdown opened from a reward row's type button. */
    private void renderRewardTypeDropdown(GuiGraphics g, int mouseX, int mouseY) {
        // Offer only the directly-editable types; OTHER can't be chosen from UI,
        // it only exists as a round-trip preservation slot.
        var types = new com.soul.soa_additions.quest.net.RewardDraft.Type[]{
                com.soul.soa_additions.quest.net.RewardDraft.Type.ITEM,
                com.soul.soa_additions.quest.net.RewardDraft.Type.XP,
                com.soul.soa_additions.quest.net.RewardDraft.Type.COMMAND,
        };
        int rowH = 14;
        int w = 96;
        int h = types.length * rowH + 2;
        int dx = rewardDropdownX;
        int dy = rewardDropdownY;
        if (dy + h > this.height - 4) dy = Math.max(4, this.height - 4 - h);
        g.fill(dx, dy, dx + w, dy + h, COL_DETAIL_BG());
        drawBox(g, dx, dy, w, h, COL_BORDER());
        for (int i = 0; i < types.length; i++) {
            int ry = dy + 1 + i * rowH;
            boolean hover = mouseX >= dx && mouseX < dx + w && mouseY >= ry && mouseY < ry + rowH;
            if (hover) g.fill(dx + 1, ry, dx + w - 1, ry + rowH, COL_PANE_HOVER());
            g.drawString(this.font, types[i].name(), dx + 4, ry + 3, COL_TEXT(), false);
        }
    }

    /** Click handler for the reward-type dropdown. */
    private boolean handleRewardTypeDropdownClick(double mouseX, double mouseY) {
        if (rewardTypeDropdownRow < 0 || editForm == null) return false;
        var types = new com.soul.soa_additions.quest.net.RewardDraft.Type[]{
                com.soul.soa_additions.quest.net.RewardDraft.Type.ITEM,
                com.soul.soa_additions.quest.net.RewardDraft.Type.XP,
                com.soul.soa_additions.quest.net.RewardDraft.Type.COMMAND,
        };
        int rowH = 14;
        int w = 96;
        int h = types.length * rowH + 2;
        int dx = rewardDropdownX;
        int dy = rewardDropdownY;
        if (dy + h > this.height - 4) dy = Math.max(4, this.height - 4 - h);
        boolean inside = mouseX >= dx && mouseX < dx + w && mouseY >= dy && mouseY < dy + h;
        if (!inside) { rewardTypeDropdownRow = -1; return false; }
        int idx = (int) ((mouseY - dy - 1) / rowH);
        if (idx < 0 || idx >= types.length) { rewardTypeDropdownRow = -1; return true; }
        if (rewardTypeDropdownRow >= editForm.rewardRows.size()) { rewardTypeDropdownRow = -1; return true; }
        var row = editForm.rewardRows.get(rewardTypeDropdownRow);
        var next = types[idx];
        if (row.type != next) {
            row.type = next;
            row.value.setValue(next.defaultValue());
            if (next != com.soul.soa_additions.quest.net.RewardDraft.Type.XP) row.levels = false;
            if (next == com.soul.soa_additions.quest.net.RewardDraft.Type.XP) row.count.setValue("50");
            else if (next == com.soul.soa_additions.quest.net.RewardDraft.Type.ITEM) row.count.setValue("1");
        }
        rewardTypeDropdownRow = -1;
        return true;
    }

    /** Click hit-test for the task-type dropdown. Returns true if the click
     *  was inside the dropdown (selecting an entry or dismissing it). */
    private boolean handleTypeDropdownClick(double mouseX, double mouseY) {
        if (typeDropdownRow < 0 || editForm == null) return false;
        var types = com.soul.soa_additions.quest.net.TaskDraft.Type.values();
        int rowH = 14;
        int w = 96;
        int h = types.length * rowH + 2;
        int dx = dropdownX;
        int dy = dropdownY;
        if (dy + h > this.height - 4) dy = Math.max(4, this.height - 4 - h);
        boolean inside = mouseX >= dx && mouseX < dx + w && mouseY >= dy && mouseY < dy + h;
        if (!inside) {
            typeDropdownRow = -1;
            return false;
        }
        int idx = (int) ((mouseY - dy - 1) / rowH);
        if (idx < 0 || idx >= types.length) {
            typeDropdownRow = -1;
            return true;
        }
        if (typeDropdownRow >= editForm.taskRows.size()) {
            typeDropdownRow = -1;
            return true;
        }
        var row = editForm.taskRows.get(typeDropdownRow);
        var prev = row.type;
        var next = types[idx];
        row.type = next;
        // Reset the value box only when toggling to/from checkmark — cycling
        // between id-shaped types preserves what the user already typed in.
        boolean prevText = prev == com.soul.soa_additions.quest.net.TaskDraft.Type.CHECKMARK;
        boolean nextText = next == com.soul.soa_additions.quest.net.TaskDraft.Type.CHECKMARK;
        if (prevText != nextText) row.value.setValue(next.defaultValue());
        if (next == com.soul.soa_additions.quest.net.TaskDraft.Type.STAT
                && (row.aux.getValue() == null || row.aux.getValue().isEmpty())) {
            row.aux.setValue(next.defaultAux());
        }
        if (!next.supportsTag()) row.tagMode = false;
        if (!next.supportsConsume()) row.consume = false;
        typeDropdownRow = -1;
        return true;
    }

    private void drawFormButton(GuiGraphics g, int x, int y, int w, String label, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 14;
        int bg = hover ? COL_PANE_HOVER() : COL_PANE_ALT();
        g.fill(x, y, x + w, y + 14, bg);
        drawBox(g, x, y, w, 14, COL_BORDER());
        g.drawString(this.font, label, x + 4, y + 3, COL_TEXT(), false);
    }

    private boolean hitFormButton(double mx, double my, int x, int y, int w) {
        return mx >= x && mx < x + w && my >= y && my < y + 14;
    }

    private boolean handleFormClick(double mouseX, double mouseY, int button) {
        if (button != 0) return true;

        // Tab clicks — always check first.
        for (int i = 0; i < tabBounds.size(); i++) {
            int[] b = tabBounds.get(i);
            if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                editForm.activeTab = QuestEditForm.Tab.byIndex(i);
                return true;
            }
        }

        // Route to EditBox fields first.
        editForm.click(mouseX, mouseY, button);

        int[] r = formRect();
        int x = r[0], y = r[1], w = r[2], h = r[3];
        int pad = QuestEditForm.PAD;
        int lx = x + pad;

        switch (editForm.activeTab) {
            case GENERAL -> {
                // Icon picker button
                if (iconPickButtonBounds != null
                        && mouseX >= iconPickButtonBounds[0] && mouseX < iconPickButtonBounds[0] + iconPickButtonBounds[2]
                        && mouseY >= iconPickButtonBounds[1] && mouseY < iconPickButtonBounds[1] + iconPickButtonBounds[3]) {
                    iconPicker = new IconPickerPopup(picked -> {
                        editForm.iconField.setValue(picked);
                        iconPicker = null;
                    });
                    return true;
                }
                // Shape button
                int shapeY = editForm.xField.getY() + QuestEditForm.ROW_H;
                if (hitFormButton(mouseX, mouseY, lx, shapeY, 120)) { editForm.cycleShape(); return true; }
            }
            case DEPS -> {
                int btnY = editForm.exclField.getY() + QuestEditForm.ROW_H;
                if (hitFormButton(mouseX, mouseY, lx, btnY, 100))       { editForm.depsAll = !editForm.depsAll; return true; }
                if (hitFormButton(mouseX, mouseY, lx + 106, btnY, 130)) { editForm.showDeps = !editForm.showDeps; return true; }
            }
            case FLAGS -> {
                int top = editForm.contentTop(y);
                int flagRow = top;
                if (hitFormButton(mouseX, mouseY, lx, flagRow, 130))       { editForm.visibility = editForm.visibility.next(); return true; }
                if (hitFormButton(mouseX, mouseY, lx + 136, flagRow, 100)) { editForm.optional = !editForm.optional; return true; }
                int flagRow2 = flagRow + 20;
                if (hitFormButton(mouseX, mouseY, lx, flagRow2, 130))      { editForm.autoClaim = !editForm.autoClaim; return true; }
                int flagRow3 = flagRow2 + 20;
                if (hitFormButton(mouseX, mouseY, lx, flagRow3, 130))      { editForm.repeatable = !editForm.repeatable; return true; }
                if (hitFormButton(mouseX, mouseY, lx + 136, flagRow3, 130)) {
                    editForm.repeatScope = editForm.repeatScope == com.soul.soa_additions.quest.model.RewardScope.TEAM
                            ? com.soul.soa_additions.quest.model.RewardScope.PLAYER
                            : com.soul.soa_additions.quest.model.RewardScope.TEAM;
                    return true;
                }
            }
            case TASKS -> {
                for (int i = 0; i < taskRowTagBounds.size(); i++) {
                    int[] b = taskRowTagBounds.get(i);
                    if (b == null) continue;
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        editForm.taskRows.get(i).tagMode = !editForm.taskRows.get(i).tagMode;
                        return true;
                    }
                }
                for (int i = 0; i < taskRowConsumeBounds.size(); i++) {
                    int[] b = taskRowConsumeBounds.get(i);
                    if (b == null) continue;
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        editForm.taskRows.get(i).consume = !editForm.taskRows.get(i).consume;
                        return true;
                    }
                }
                for (int i = 0; i < taskRowTypeBounds.size(); i++) {
                    int[] b = taskRowTypeBounds.get(i);
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        typeDropdownRow = i;
                        dropdownX = b[0];
                        dropdownY = b[1] + b[3];
                        return true;
                    }
                }
                for (int i = 0; i < taskRowRemoveBounds.size(); i++) {
                    int[] b = taskRowRemoveBounds.get(i);
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        editForm.removeTaskRow(i);
                        return true;
                    }
                }
                if (taskAddBounds != null
                        && mouseX >= taskAddBounds[0] && mouseX < taskAddBounds[0] + taskAddBounds[2]
                        && mouseY >= taskAddBounds[1] && mouseY < taskAddBounds[1] + taskAddBounds[3]) {
                    editForm.addTaskRow(com.soul.soa_additions.quest.net.TaskDraft.blank());
                    return true;
                }
            }
            case REWARDS -> {
                for (int i = 0; i < rewardRowTypeBounds.size(); i++) {
                    int[] b = rewardRowTypeBounds.get(i);
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        rewardTypeDropdownRow = i;
                        rewardDropdownX = b[0];
                        rewardDropdownY = b[1] + b[3];
                        return true;
                    }
                }
                for (int i = 0; i < rewardRowLevelsBounds.size(); i++) {
                    int[] b = rewardRowLevelsBounds.get(i);
                    if (b == null) continue;
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        editForm.rewardRows.get(i).levels = !editForm.rewardRows.get(i).levels;
                        return true;
                    }
                }
                for (int i = 0; i < rewardRowScopeBounds.size(); i++) {
                    int[] b = rewardRowScopeBounds.get(i);
                    if (b == null) continue;
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        var row = editForm.rewardRows.get(i);
                        row.scope = row.scope == com.soul.soa_additions.quest.model.RewardScope.PLAYER
                                ? com.soul.soa_additions.quest.model.RewardScope.TEAM
                                : com.soul.soa_additions.quest.model.RewardScope.PLAYER;
                        return true;
                    }
                }
                for (int i = 0; i < rewardRowRemoveBounds.size(); i++) {
                    int[] b = rewardRowRemoveBounds.get(i);
                    if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                        editForm.removeRewardRow(i);
                        return true;
                    }
                }
                if (rewardAddBounds != null
                        && mouseX >= rewardAddBounds[0] && mouseX < rewardAddBounds[0] + rewardAddBounds[2]
                        && mouseY >= rewardAddBounds[1] && mouseY < rewardAddBounds[1] + rewardAddBounds[3]) {
                    editForm.addRewardRow(com.soul.soa_additions.quest.net.RewardDraft.blank());
                    return true;
                }
            }
        }

        if (hitFormButton(mouseX, mouseY, x + w - 64 - pad, y + h - 22, 60)) { submitForm(); return true; }
        if (hitFormButton(mouseX, mouseY, x + w - 130 - pad, y + h - 22, 60)) { editForm = null; return true; }
        return true;
    }

    private void submitForm() {
        if (editForm == null) return;
        var pkt = new com.soul.soa_additions.quest.net.QuestEditPacket(
                com.soul.soa_additions.quest.net.QuestEditPacket.Op.UPSERT,
                editForm.chapterId,
                editForm.questId,
                editForm.titleField.getValue(),
                editForm.descriptionLines(),
                editForm.iconField.getValue(),
                editForm.shape,
                editForm.visibility,
                editForm.optional,
                editForm.autoClaim,
                editForm.depsAll,
                editForm.dependencyIds(),
                editForm.posX(),
                editForm.posY(),
                editForm.drafts(),
                editForm.showDeps,
                editForm.rewardDrafts(),
                editForm.size(),
                editForm.repeatable,
                editForm.repeatScope,
                editForm.exclusionIds()
        );
        ModNetworking.CHANNEL.sendToServer(pkt);
        editForm = null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (iconPicker != null) {
            iconPicker.scroll(delta);
            return true;
        }
        if (editForm != null && editForm.activeTab == QuestEditForm.Tab.GENERAL) {
            // Forward to the description widget so its internal scroll works.
            var d = editForm.descField;
            if (mouseX >= d.getX() && mouseX < d.getX() + d.getWidth()
                    && mouseY >= d.getY() && mouseY < d.getY() + d.getHeight()) {
                return d.mouseScrolled(mouseX, mouseY, delta);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (chapterEditPendingId != null) {
            if (keyCode == 256) { cancelChapterEdit(); return true; }  // ESC
            if (keyCode == 257 || keyCode == 335) {                    // ENTER
                // Only submit if no field has focus (so TAB navigation works).
                boolean anyFocused = false;
                for (EditBox eb : chapterEditBoxes()) if (eb.isFocused()) { anyFocused = true; break; }
                if (!anyFocused) { submitChapterEdit(); return true; }
            }
            for (EditBox eb : chapterEditBoxes()) {
                if (eb.isFocused() && eb.keyPressed(keyCode, scanCode, modifiers)) return true;
            }
            return true;
        }
        if (chapterRenamePendingId != null && chapterRenameBox != null) {
            if (keyCode == 256) { cancelChapterRename(); return true; }                 // ESC
            if (keyCode == 257 || keyCode == 335) { submitChapterRename(); return true; } // ENTER / numpad enter
            if (chapterRenameBox.keyPressed(keyCode, scanCode, modifiers)) return true;
            return true;
        }
        if (iconPicker != null) {
            if (keyCode == 256) { iconPicker = null; return true; }
            if (iconPicker.keyPressed(keyCode, scanCode, modifiers)) return true;
            return true;
        }
        if (editForm != null) {
            if (keyCode == 256) {
                if (typeDropdownRow >= 0) { typeDropdownRow = -1; return true; }
                if (rewardTypeDropdownRow >= 0) { rewardTypeDropdownRow = -1; return true; }
                editForm = null;
                return true;
            }
            // ENTER inside the multi-line description inserts a newline; only
            // submit when no text input has focus (so quick keyboard saves
            // still work after toggling buttons).
            if ((keyCode == 257 || keyCode == 335) && !editForm.anyFieldFocused()) {
                submitForm();
                return true;
            }
            if (editForm.keyPressed(keyCode, scanCode, modifiers)) return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (contextMenuOpen) {
            if (keyCode == 256) { closeContextMenu(); return true; }
        }
        if (placementMode && keyCode == 256) { placementMode = false; return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (chapterEditPendingId != null) {
            for (EditBox eb : chapterEditBoxes()) {
                if (eb.isFocused() && eb.charTyped(c, modifiers)) return true;
            }
            return true;
        }
        if (chapterRenamePendingId != null && chapterRenameBox != null) {
            if (chapterRenameBox.charTyped(c, modifiers)) return true;
            return true;
        }
        if (iconPicker != null) {
            if (iconPicker.charTyped(c, modifiers)) return true;
            return true;
        }
        if (editForm != null) {
            if (editForm.charTyped(c, modifiers)) return true;
        }
        return super.charTyped(c, modifiers);
    }
}
