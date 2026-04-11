package com.soul.soa_additions.quest.progress;

import net.minecraft.nbt.CompoundTag;

/**
 * Per-task counter. Kept deliberately dumb — just an integer count against a
 * target that the task itself knows about. Richer task types (e.g. "collect
 * one of each of these 5 items") still only surface a single completion ratio
 * to progress; internal structure stays inside the task's own NBT blob.
 *
 * <p>Mutable on purpose: progress is hot-path. Every kill, every item pickup,
 * every stat tick potentially bumps one of these. Allocating a new record per
 * increment would be wasteful. The enclosing {@link TeamQuestProgress} is
 * still the single source of truth for persistence.</p>
 */
public final class TaskProgress {

    private int count;
    private CompoundTag extra;

    public TaskProgress() {}

    public TaskProgress(int count) { this.count = count; }

    public int count() { return count; }

    public void setCount(int count) { this.count = Math.max(0, count); }

    /** Increment and return the new value. */
    public int add(int delta) {
        count = Math.max(0, count + delta);
        return count;
    }

    /** Optional per-task scratch NBT — used by tasks that track structured state. */
    public CompoundTag extra() {
        if (extra == null) extra = new CompoundTag();
        return extra;
    }

    public boolean hasExtra() { return extra != null && !extra.isEmpty(); }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("count", count);
        if (hasExtra()) tag.put("extra", extra);
        return tag;
    }

    public static TaskProgress load(CompoundTag tag) {
        TaskProgress p = new TaskProgress(tag.getInt("count"));
        if (tag.contains("extra")) p.extra = tag.getCompound("extra").copy();
        return p;
    }
}
