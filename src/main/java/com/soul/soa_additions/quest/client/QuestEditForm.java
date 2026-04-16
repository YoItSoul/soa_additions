package com.soul.soa_additions.quest.client;

import com.soul.soa_additions.quest.model.NodeShape;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.RewardScope;
import com.soul.soa_additions.quest.net.RewardDraft;
import com.soul.soa_additions.quest.net.TaskDraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Form state for the quest editor popup. Holds the {@link EditBox} widgets
 * for the metadata fields, the per-task row widgets (each task is its own
 * value/count pair plus a type cycle button), the non-text toggles, and the
 * draggable popup offset.
 *
 * <p>The form is split into tabs to keep the popup compact:
 * <ul>
 *   <li><b>General</b> — title, icon, description, position, size, shape</li>
 *   <li><b>Deps</b> — dependencies, exclusions, dep mode, show lines</li>
 *   <li><b>Flags</b> — visibility, optional, auto-claim, repeatable, repeat scope</li>
 *   <li><b>Tasks</b> — task list</li>
 *   <li><b>Rewards</b> — reward list</li>
 * </ul>
 *
 * <p>The form mostly stores state — layout coordinates and click routing
 * still live on {@link QuestBookScreen}, which calls into the per-row helpers
 * defined here so it doesn't have to know the row geometry directly.</p>
 */
public final class QuestEditForm {

    public enum Tab {
        GENERAL("General"),
        DEPS("Deps"),
        FLAGS("Flags"),
        TASKS("Tasks"),
        REWARDS("Rewards");

        public final String label;
        Tab(String label) { this.label = label; }

        private static final Tab[] VALUES = values();
        public static int count() { return VALUES.length; }
        public static Tab byIndex(int i) { return VALUES[i]; }
    }

    public Tab activeTab = Tab.GENERAL;

    public final String chapterId;
    public final String questId;     // immutable for existing quests
    public final boolean isNew;

    public final EditBox titleField;
    public final EditBox iconField;
    /** Multi-line description editor. Newlines split lines; the literal token
     *  {@code ||} is also expanded to a blank line for users who prefer the
     *  legacy single-line shorthand. */
    public final MultiLineEditBox descField;
    public final EditBox depsField;  // comma-separated ids
    public final EditBox exclField;  // comma-separated mutually-exclusive ids
    public final EditBox xField;
    public final EditBox yField;
    public final EditBox sizeField;

    public NodeShape shape;
    public com.soul.soa_additions.quest.model.Visibility visibility;
    public boolean optional;
    public boolean autoClaim;
    public boolean repeatable;
    public RewardScope repeatScope = RewardScope.TEAM;
    public boolean depsAll;
    public int minDeps;
    public boolean showDeps;

    /** Per-task row state. Mutable: add/remove tracked by the screen. */
    public final List<TaskRow> taskRows = new ArrayList<>();

    /** Per-reward row state. Mirrors {@link #taskRows}. */
    public final List<RewardRow> rewardRows = new ArrayList<>();

    /** Drag offset applied to the popup's base centered position. Mutated by
     *  the screen when the user drags the title bar. */
    public int dragOffsetX;
    public int dragOffsetY;

    public QuestEditForm(String chapterId, Quest existing, int defaultX, int defaultY) {
        this.chapterId = chapterId;
        this.isNew = existing == null;
        this.questId = isNew ? freshId() : existing.id();

        var font = Minecraft.getInstance().font;
        int w = 240;
        titleField = new EditBox(font, 0, 0, w, 16, Component.literal("Title"));
        iconField  = new EditBox(font, 0, 0, w, 16, Component.literal("Icon"));
        descField  = new MultiLineEditBox(font, 0, 0, w, DESC_H,
                Component.literal("Description (use Enter for new lines, || for blank line)"),
                Component.literal("Description"));
        descField.setCharacterLimit(2048);
        depsField  = new EditBox(font, 0, 0, w, 16, Component.literal("Dependencies"));
        exclField  = new EditBox(font, 0, 0, w, 16, Component.literal("Exclusions"));
        xField     = new EditBox(font, 0, 0, 60, 16, Component.literal("X"));
        yField     = new EditBox(font, 0, 0, 60, 16, Component.literal("Y"));
        sizeField  = new EditBox(font, 0, 0, 60, 16, Component.literal("Size"));

        titleField.setMaxLength(128);
        iconField.setMaxLength(128);
        depsField.setMaxLength(512);
        exclField.setMaxLength(512);
        xField.setMaxLength(6);
        yField.setMaxLength(6);
        sizeField.setMaxLength(4);

        if (existing == null) {
            titleField.setValue("New Quest");
            iconField.setValue("minecraft:paper");
            descField.setValue("");
            depsField.setValue("");
            exclField.setValue("");
            xField.setValue(String.valueOf(defaultX));
            yField.setValue(String.valueOf(defaultY));
            sizeField.setValue(String.valueOf(Quest.DEFAULT_SIZE));
            shape = NodeShape.ICON;
            visibility = com.soul.soa_additions.quest.model.Visibility.NORMAL;
            optional = false;
            autoClaim = false;
            depsAll = true;
            minDeps = -1;
            showDeps = true;
            repeatable = false;
            repeatScope = RewardScope.TEAM;
        } else {
            titleField.setValue(existing.title());
            iconField.setValue(existing.icon());
            descField.setValue(String.join("\n", existing.description()));
            depsField.setValue(String.join(",", existing.dependencies()));
            exclField.setValue(existing.exclusions() == null ? "" : String.join(",", existing.exclusions()));
            xField.setValue(String.valueOf(existing.posX() >= 0 ? existing.posX() : defaultX));
            yField.setValue(String.valueOf(existing.posY() >= 0 ? existing.posY() : defaultY));
            sizeField.setValue(String.valueOf(existing.sizeOrDefault()));
            shape = existing.shape();
            visibility = existing.visibility();
            optional = existing.optional();
            autoClaim = existing.autoClaim();
            depsAll = existing.depsAll();
            minDeps = existing.minDeps();
            showDeps = existing.showDeps();
            repeatable = existing.repeatable();
            repeatScope = existing.repeatScope() == null ? RewardScope.TEAM : existing.repeatScope();
            for (var t : existing.tasks()) addTaskRow(TaskDraft.fromTask(t));
            for (var r : existing.rewards()) addRewardRow(RewardDraft.fromReward(r));
        }
    }

    /** All EditBoxes visible on the current tab. Only includes the aux box
     *  for types that actually use it, so click focus tracking doesn't steal
     *  keystrokes for an invisible widget. */
    public List<EditBox> boxes() {
        List<EditBox> all = new ArrayList<>();
        switch (activeTab) {
            case GENERAL -> {
                all.add(titleField);
                all.add(iconField);
                all.add(xField);
                all.add(yField);
                all.add(sizeField);
            }
            case DEPS -> {
                all.add(depsField);
                all.add(exclField);
            }
            case FLAGS -> { /* no text boxes on this tab */ }
            case TASKS -> {
                for (TaskRow r : taskRows) {
                    // STAT uses a clickable button for the stat type instead
                    // of a text field, so skip its value EditBox.
                    if (!r.type.usesStatTypeButton()) all.add(r.value);
                    if (r.type.usesCount()) all.add(r.count);
                    if (r.type.usesAux()) all.add(r.aux);
                }
            }
            case REWARDS -> {
                for (RewardRow rr : rewardRows) {
                    if (rr.type.editable()) {
                        all.add(rr.value);
                        if (rr.type.usesCount()) all.add(rr.count);
                    }
                }
            }
        }
        return all;
    }

    /** Slugs a new quest id from timestamp so the form has something unique. */
    private static String freshId() {
        return "quest_" + Long.toString(System.currentTimeMillis(), 36);
    }

    public List<String> descriptionLines() {
        String raw = descField.getValue();
        if (raw.isEmpty()) return List.of();
        String normalized = raw.replace("||", "\n\n");
        List<String> out = new ArrayList<>();
        for (String part : normalized.split("\n", -1)) out.add(part);
        return out;
    }

    public List<String> dependencyIds() {
        return splitCsv(depsField.getValue());
    }

    public List<String> exclusionIds() {
        return splitCsv(exclField.getValue());
    }

    private static List<String> splitCsv(String raw) {
        if (raw.isEmpty()) return List.of();
        List<String> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public int posX() { return safeInt(xField.getValue(), -1); }
    public int posY() { return safeInt(yField.getValue(), -1); }
    public int size() {
        int s = safeInt(sizeField.getValue(), Quest.DEFAULT_SIZE);
        if (s < 8) s = 8;
        if (s > 128) s = 128;
        return s;
    }

    private static int safeInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return fallback; }
    }

    /** Cycle shape to the next enum value. */
    public void cycleShape() {
        NodeShape[] all = NodeShape.values();
        shape = all[(shape.ordinal() + 1) % all.length];
    }

    /** Append a quest id to the dependencies field, comma-separated. No-op
     *  if the id is already in the list. */
    public void appendDependency(String questId) {
        if (questId == null || questId.isEmpty()) return;
        List<String> existing = dependencyIds();
        if (existing.contains(questId)) return;
        String cur = depsField.getValue().trim();
        if (cur.isEmpty()) depsField.setValue(questId);
        else if (cur.endsWith(",")) depsField.setValue(cur + questId);
        else depsField.setValue(cur + "," + questId);
    }

    public void addTaskRow(TaskDraft initial) {
        var font = Minecraft.getInstance().font;
        EditBox value = new EditBox(font, 0, 0, 168, 14, Component.literal("Task value"));
        value.setMaxLength(160);
        value.setValue(initial.value());
        EditBox count = new EditBox(font, 0, 0, 32, 14, Component.literal("Count"));
        count.setMaxLength(5);
        count.setValue(String.valueOf(initial.count()));
        EditBox aux = new EditBox(font, 0, 0, 200, 14, Component.literal("Extra"));
        aux.setMaxLength(160);
        aux.setValue(initial.aux() == null ? "" : initial.aux());
        TaskRow row = new TaskRow(initial.type(), value, count, aux);
        row.tagMode = initial.tagMode();
        row.consume = initial.consume();
        taskRows.add(row);
    }

    public void removeTaskRow(int index) {
        if (index >= 0 && index < taskRows.size()) taskRows.remove(index);
    }

    // ---------- reward rows ----------

    public void addRewardRow(RewardDraft initial) {
        var font = Minecraft.getInstance().font;
        EditBox value = new EditBox(font, 0, 0, 200, 14, Component.literal("Reward value"));
        value.setMaxLength(256);
        value.setValue(initial.value() == null ? "" : initial.value());
        EditBox count = new EditBox(font, 0, 0, 48, 14, Component.literal("Count"));
        count.setMaxLength(9);
        count.setValue(String.valueOf(initial.count()));
        RewardRow row = new RewardRow(initial.type(), value, count);
        row.levels = initial.levels();
        row.scope = initial.scope() == null ? RewardScope.PLAYER : initial.scope();
        row.otherJson = initial.type() == RewardDraft.Type.OTHER ? initial.aux() : "";
        rewardRows.add(row);
    }

    public void removeRewardRow(int index) {
        if (index >= 0 && index < rewardRows.size()) rewardRows.remove(index);
    }

    public List<RewardDraft> rewardDrafts() {
        List<RewardDraft> out = new ArrayList<>(rewardRows.size());
        for (RewardRow r : rewardRows) {
            int c = safeInt(r.count.getValue(), 0);
            out.add(new RewardDraft(
                    r.type,
                    r.value.getValue(),
                    c,
                    r.levels,
                    r.scope,
                    r.type == RewardDraft.Type.OTHER ? r.otherJson : ""));
        }
        return out;
    }

    /** Single reward row state. */
    public static final class RewardRow {
        public RewardDraft.Type type;
        public final EditBox value;
        public final EditBox count;
        public boolean levels;
        public RewardScope scope = RewardScope.PLAYER;
        /** Preserved raw JSON body for OTHER rewards we can't express. */
        public String otherJson = "";
        RewardRow(RewardDraft.Type type, EditBox value, EditBox count) {
            this.type = type; this.value = value; this.count = count;
        }
    }

    public List<TaskDraft> drafts() {
        List<TaskDraft> out = new ArrayList<>(taskRows.size());
        for (TaskRow r : taskRows) {
            int c = safeInt(r.count.getValue(), 1);
            out.add(new TaskDraft(r.type, r.value.getValue(), c, r.aux.getValue(), r.tagMode, r.consume));
        }
        return out;
    }

    /** Single task row state. Mutable type field (cycled by button click). */
    public static final class TaskRow {
        public TaskDraft.Type type;
        public final EditBox value;
        public final EditBox count;
        public final EditBox aux;
        public boolean tagMode;
        public boolean consume;
        TaskRow(TaskDraft.Type type, EditBox value, EditBox count, EditBox aux) {
            this.type = type; this.value = value; this.count = count; this.aux = aux;
        }

        /** True if this type needs a second sub-row for extras (tag/consume
         *  toggles for item/craft, stat_value edit box for stat). */
        public boolean hasSubRow() {
            return type.supportsTag() || type.usesAux();
        }
    }

    // ---------- layout ----------

    public static final int PAD = 12;
    /** Vertical pitch for a single-line field row: label (10) + gap (2) +
     *  field (16) + gap (8) = 36. Extra breathing room prevents labels from
     *  overlapping the field above them. */
    public static final int ROW_H = 36;
    /** Y offset below the popup top where tab content starts (title bar 24 +
     *  tab strip 18 + gap 4 = 46). */
    public static final int CONTENT_Y_OFFSET = 46;
    public static final int TAB_STRIP_H = 18;
    public static final int TASK_ROW_H = 18;
    public static final int TASK_SUB_ROW_H = 18;

    /** Pixel height reserved for the multi-line description widget. */
    public static final int DESC_H = 88;

    /** Height of a single row including its optional sub-rows.
     *  ITEM/CRAFT get two sub-rows: one for tag/consume toggles, one for NBT. */
    public int rowHeight(TaskRow r) {
        int h = TASK_ROW_H;
        if (r.hasSubRow()) h += TASK_SUB_ROW_H;
        // Second sub-row for aux (NBT) when toggles already occupy the first.
        if (r.type.supportsTag() && r.type.usesAux()) h += TASK_SUB_ROW_H;
        return h;
    }

    /** Height of the task list section in pixels, including the header + add button. */
    public int taskSectionHeight() {
        int rows = 0;
        for (TaskRow r : taskRows) rows += rowHeight(r);
        return 14 + Math.max(TASK_ROW_H, rows) + 18;
    }

    public static final int REWARD_ROW_H = 18;

    /** Pixel height of the reward section including header + add button. */
    public int rewardSectionHeight() {
        int rows = rewardRows.size() * REWARD_ROW_H;
        return 14 + Math.max(REWARD_ROW_H, rows) + 18;
    }

    /** The Y where tab content begins, given the popup top. */
    public int contentTop(int popupY) {
        return popupY + CONTENT_Y_OFFSET;
    }

    /** Height of the content area for the current tab. */
    public int tabContentHeight() {
        return switch (activeTab) {
            // title + icon + desc + x/y/size + shape button row
            case GENERAL -> ROW_H * 3 + DESC_H + 20 + 28;
            // deps + excl + dep mode/show lines + min_deps button rows
            case DEPS -> ROW_H * 2 + 28 + 22;
            // three rows of toggle buttons
            case FLAGS -> 24 * 3 + 12;
            case TASKS -> taskSectionHeight();
            case REWARDS -> rewardSectionHeight();
        };
    }

    /** Total popup height including title bar, tab strip, content, and bottom bar. */
    public int totalHeight() {
        // title bar (24) + tab strip (18) + gap (4) + content + gap (8) + bottom bar (26)
        return 24 + TAB_STRIP_H + 4 + tabContentHeight() + 8 + 26;
    }

    /** Y of the task/reward section header, relative to the popup top. Used
     *  when the Tasks or Rewards tab is active. */
    public int listSectionTop(int popupY) {
        return contentTop(popupY);
    }

    public void layout(int x, int y, int w) {
        int lx = x + PAD;
        int top = contentTop(y);

        switch (activeTab) {
            case GENERAL -> {
                int row = top;
                titleField.setPosition(lx, row);           titleField.setWidth(w - PAD * 2);
                row += ROW_H;
                iconField.setPosition(lx, row);            iconField.setWidth(w - PAD * 2 - 64);
                row += ROW_H;
                descField.setX(lx); descField.setY(row);
                descField.setWidth(w - PAD * 2);
                descField.setHeight(DESC_H);
                row += DESC_H + 20;
                xField.setPosition(lx, row);
                yField.setPosition(lx + 72, row);
                sizeField.setPosition(lx + 144, row);
            }
            case DEPS -> {
                int row = top;
                depsField.setPosition(lx, row);            depsField.setWidth(w - PAD * 2);
                row += ROW_H;
                exclField.setPosition(lx, row);            exclField.setWidth(w - PAD * 2);
            }
            case FLAGS -> { /* no text fields to position */ }
            case TASKS -> {
                int rowY = listSectionTop(y) + 14;
                for (TaskRow r : taskRows) {
                    int valueX = lx + 92;
                    int countReserve = r.type.usesCount() ? 36 : 0;
                    int browseReserve = r.type.hasPicker() ? 18 : 0;
                    int valueW = w - PAD * 2 - 92 - countReserve - browseReserve - 18;
                    r.value.setPosition(valueX, rowY);
                    r.value.setWidth(valueW);
                    if (r.type.usesCount()) {
                        r.count.setPosition(valueX + valueW + 4, rowY);
                        r.count.setWidth(32);
                    }
                    if (r.type.usesAux()) {
                        int auxBrowseReserve = r.type.hasAuxPicker() ? 18 : 0;
                        // When tag toggles occupy the first sub-row, place aux
                        // on a second sub-row below them.
                        int auxSubRowOffset = r.type.supportsTag() ? TASK_SUB_ROW_H * 2 : TASK_SUB_ROW_H;
                        r.aux.setPosition(valueX, rowY + auxSubRowOffset);
                        r.aux.setWidth(valueW + countReserve - auxBrowseReserve);
                    }
                    rowY += rowHeight(r);
                }
            }
            case REWARDS -> {
                int rwY = listSectionTop(y) + 14;
                for (RewardRow rr : rewardRows) {
                    int typeBtnW = 88;
                    int valueX = lx + typeBtnW + 4;
                    int countReserve = rr.type.usesCount() ? 52 : 0;
                    int valueW = w - PAD * 2 - (typeBtnW + 4) - countReserve - 18;
                    rr.value.setPosition(valueX, rwY);
                    rr.value.setWidth(Math.max(20, valueW));
                    if (rr.type.usesCount()) {
                        rr.count.setPosition(valueX + valueW + 4, rwY);
                        rr.count.setWidth(48);
                    }
                    rwY += REWARD_ROW_H;
                }
            }
        }
    }

    public void renderFields(GuiGraphics g, int mouseX, int mouseY, float pt) {
        if (activeTab == Tab.GENERAL) {
            descField.render(g, mouseX, mouseY, pt);
        }
        for (EditBox eb : boxes()) eb.render(g, mouseX, mouseY, pt);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public boolean click(double mouseX, double mouseY, int button) {
        if (activeTab == Tab.GENERAL) {
            boolean descInside = inside(mouseX, mouseY,
                    descField.getX(), descField.getY(), descField.getWidth(), descField.getHeight());
            descField.setFocused(descInside);
            if (descInside) {
                descField.mouseClicked(mouseX, mouseY, button);
            }
        } else {
            descField.setFocused(false);
        }
        boolean handled = false;
        for (EditBox eb : boxes()) {
            boolean in = inside(mouseX, mouseY, eb.getX(), eb.getY(), eb.getWidth(), eb.getHeight());
            eb.setFocused(in);
            if (in) {
                eb.mouseClicked(mouseX, mouseY, button);
                handled = true;
            }
        }
        return handled;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (descField.isFocused() && descField.keyPressed(keyCode, scanCode, modifiers)) return true;
        for (EditBox eb : boxes()) {
            if (eb.isFocused() && eb.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    public boolean charTyped(char c, int modifiers) {
        if (descField.isFocused() && descField.charTyped(c, modifiers)) return true;
        for (EditBox eb : boxes()) {
            if (eb.isFocused() && eb.charTyped(c, modifiers)) return true;
        }
        return false;
    }

    /** True if any text input in the form currently holds keyboard focus. */
    public boolean anyFieldFocused() {
        if (descField.isFocused()) return true;
        for (EditBox eb : boxes()) if (eb.isFocused()) return true;
        return false;
    }

    /** True if the dependencies field currently has keyboard focus — used by
     *  the screen to enable click-to-pick from background quest nodes. */
    public boolean depsFieldFocused() { return activeTab == Tab.DEPS && depsField.isFocused(); }
}
