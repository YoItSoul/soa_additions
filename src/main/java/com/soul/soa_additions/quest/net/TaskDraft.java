package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.quest.task.AdvancementTask;
import com.soul.soa_additions.quest.task.BreedTask;
import com.soul.soa_additions.quest.task.CheckmarkTask;
import com.soul.soa_additions.quest.task.CraftTask;
import com.soul.soa_additions.quest.task.DimensionTask;
import com.soul.soa_additions.quest.task.ItemTask;
import com.soul.soa_additions.quest.task.KillTask;
import com.soul.soa_additions.quest.task.MineTask;
import com.soul.soa_additions.quest.task.ObserveTask;
import com.soul.soa_additions.quest.task.PlaceTask;
import com.soul.soa_additions.quest.task.StatTask;
import com.soul.soa_additions.quest.task.TameTask;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * GUI-side representation of a single task on a quest. Carries enough data
 * to round-trip every task type the editor knows how to express.
 *
 * <p>{@code value} carries the primary identifier — item/block/entity id,
 * advancement id, dimension id, stat type id, or checkmark text — depending
 * on {@link Type}. {@code aux} is used by STAT (the stat value id). {@code
 * tagMode} flips {@link ItemTask}/{@link CraftTask} to match an item tag
 * instead of a single item. {@code consume} enables the consume variant of
 * {@link ItemTask}.</p>
 *
 * <p>NBT filters remain JSON-only — SNBT doesn't fit a one-line edit box
 * cleanly. Custom trigger tasks are also JSON-only; in practice they're used
 * by mod integrations, not handwritten by a modpack author in-game.</p>
 */
public record TaskDraft(
        Type type,
        String value,
        int count,
        String aux,
        boolean tagMode,
        boolean consume
) {

    public enum Type {
        ITEM, MINE, KILL, CRAFT, PLACE, TAME, BREED, OBSERVE,
        DIMENSION, ADVANCEMENT, STAT, CHECKMARK, TRIGGER;

        public Type next() { return values()[(ordinal() + 1) % values().length]; }
        public boolean usesCount() { return this != DIMENSION && this != ADVANCEMENT && this != CHECKMARK; }
        public boolean usesAux() { return this == STAT; }
        public boolean supportsTag() { return this == ITEM || this == CRAFT; }
        public boolean supportsConsume() { return this == ITEM; }

        public String defaultValue() {
            return switch (this) {
                case ITEM, CRAFT -> "minecraft:stone";
                case MINE, PLACE -> "minecraft:stone";
                case KILL, TAME, BREED -> "minecraft:zombie";
                case OBSERVE -> "minecraft:bedrock";
                case DIMENSION -> "minecraft:the_nether";
                case ADVANCEMENT -> "minecraft:story/mine_diamond";
                case STAT -> "minecraft:custom";
                case CHECKMARK -> "Acknowledge";
                case TRIGGER -> "soa_additions:my_trigger";
            };
        }
        public String defaultAux() {
            return this == STAT ? "minecraft:play_time" : "";
        }
    }

    public static TaskDraft blank() { return new TaskDraft(Type.ITEM, Type.ITEM.defaultValue(), 1, "", false, false); }

    public TaskDraft withType(Type t) {
        boolean idLike = t != Type.CHECKMARK;
        boolean wasIdLike = type != Type.CHECKMARK;
        String newValue = (idLike == wasIdLike) ? value : t.defaultValue();
        String newAux = t.usesAux() ? (aux == null || aux.isEmpty() ? t.defaultAux() : aux) : "";
        return new TaskDraft(t, newValue, count, newAux, tagMode && t.supportsTag(), consume && t.supportsConsume());
    }

    public static void encode(TaskDraft d, FriendlyByteBuf buf) {
        buf.writeEnum(d.type);
        buf.writeUtf(d.value);
        buf.writeVarInt(d.count);
        buf.writeUtf(d.aux == null ? "" : d.aux);
        buf.writeBoolean(d.tagMode);
        buf.writeBoolean(d.consume);
    }

    public static TaskDraft decode(FriendlyByteBuf buf) {
        return new TaskDraft(
                buf.readEnum(Type.class),
                buf.readUtf(),
                buf.readVarInt(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readBoolean());
    }

    /** Build the live {@link QuestTask} instance this draft represents. */
    public QuestTask toTask() {
        try {
            int c = Math.max(1, count);
            return switch (type) {
                case ITEM -> tagMode
                        ? new ItemTask(null, new ResourceLocation(value), null, c, consume)
                        : new ItemTask(new ResourceLocation(value), null, null, c, consume);
                case CRAFT -> tagMode
                        ? new CraftTask(null, new ResourceLocation(value), null, c)
                        : new CraftTask(new ResourceLocation(value), null, null, c);
                case MINE -> new MineTask(new ResourceLocation(value), c);
                case KILL -> new KillTask(new ResourceLocation(value), c);
                case PLACE -> new PlaceTask(new ResourceLocation(value), c);
                case TAME -> new TameTask(new ResourceLocation(value), c);
                case BREED -> new BreedTask(new ResourceLocation(value), c);
                case OBSERVE -> new ObserveTask(new ResourceLocation(value), c, ObserveTask.DEFAULT_REACH);
                case DIMENSION -> new DimensionTask(new ResourceLocation(value));
                case ADVANCEMENT -> new AdvancementTask(new ResourceLocation(value));
                case STAT -> new StatTask(new ResourceLocation(value), new ResourceLocation(aux), c);
                case CHECKMARK -> new CheckmarkTask(value);
                case TRIGGER -> new com.soul.soa_additions.quest.task.CustomTriggerTask(
                        new ResourceLocation(value), c, "");
            };
        } catch (Exception e) {
            return new CheckmarkTask("(invalid: " + value + ")");
        }
    }

    /** Round-trip a real task into a draft for the editor. Tasks the editor
     *  doesn't know how to express are surfaced as a read-only checkmark with
     *  a label so they aren't silently dropped on save. */
    public static TaskDraft fromTask(QuestTask t) {
        if (t instanceof ItemTask it) {
            if (it.tag() != null) return new TaskDraft(Type.ITEM, it.tag().toString(), it.count(), "", true, it.consume());
            if (it.item() != null) return new TaskDraft(Type.ITEM, it.item().toString(), it.count(), "", false, it.consume());
        }
        if (t instanceof CraftTask ct) {
            if (ct.tag() != null) return new TaskDraft(Type.CRAFT, ct.tag().toString(), ct.count(), "", true, false);
            if (ct.item() != null) return new TaskDraft(Type.CRAFT, ct.item().toString(), ct.count(), "", false, false);
        }
        if (t instanceof MineTask mt) return new TaskDraft(Type.MINE, mt.block().toString(), mt.count(), "", false, false);
        if (t instanceof KillTask kt) return new TaskDraft(Type.KILL, kt.entity().toString(), kt.count(), "", false, false);
        if (t instanceof PlaceTask pt) return new TaskDraft(Type.PLACE, pt.block().toString(), pt.count(), "", false, false);
        if (t instanceof TameTask tt) return new TaskDraft(Type.TAME, tt.entity().toString(), tt.count(), "", false, false);
        if (t instanceof BreedTask bt) return new TaskDraft(Type.BREED, bt.entity().toString(), bt.count(), "", false, false);
        if (t instanceof ObserveTask ot) return new TaskDraft(Type.OBSERVE, ot.id().toString(), ot.count(), "", false, false);
        if (t instanceof DimensionTask dt) return new TaskDraft(Type.DIMENSION, dt.dimension().toString(), 1, "", false, false);
        if (t instanceof AdvancementTask at) return new TaskDraft(Type.ADVANCEMENT, at.advancement().toString(), 1, "", false, false);
        if (t instanceof StatTask st) return new TaskDraft(Type.STAT, st.statType().toString(), st.threshold(), st.statValue().toString(), false, false);
        if (t instanceof CheckmarkTask ck) return new TaskDraft(Type.CHECKMARK, ck.text(), 1, "", false, false);
        if (t instanceof com.soul.soa_additions.quest.task.CustomTriggerTask ct)
            return new TaskDraft(Type.TRIGGER, ct.triggerId().toString(), ct.count(), "", false, false);
        // Unknown (e.g. ItemTask with an NBT filter the GUI can't express) —
        // preserve as a checkmark labeled with describe() so the user notices
        // it isn't editable here.
        return new TaskDraft(Type.CHECKMARK, t.describe(), 1, "", false, false);
    }
}
