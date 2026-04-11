package com.soul.soa_additions.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-world packmode state. Stores the current mode, whether it's locked
 * (either by explicit command, the first quest completion, or a
 * {@code lock_packmode} reward), and the real-world timestamp of creation
 * so the 30-minute soft-lock window can be enforced.
 *
 * <p>Packmode is intentionally a single global value per world — it affects
 * loot, recipes, quest visibility, and scaling difficulty. Allowing it to
 * differ per player would create incoherent progression (player A sees an
 * expert recipe gate that doesn't exist for player B). The lock flag
 * prevents the usual FTBQ-style mid-run mode flip that strands progress.</p>
 */
public final class PackModeData extends SavedData {

    private static final String DATA_NAME = "soa_packmode";

    private PackMode mode = PackMode.ADVENTURE;
    private boolean locked = false;
    private long worldCreatedMillis = 0L;

    public PackModeData() {}

    public PackMode mode() { return mode; }

    public boolean locked() { return locked; }

    public long worldCreatedMillis() { return worldCreatedMillis; }

    /** True only when the mode has been explicitly locked — by command or by
     *  a {@code lock_packmode} reward firing on a completed quest. The old
     *  30-minute soft window was removed because it reported "locked" before
     *  any actual lock event had occurred, which surprised players who were
     *  waiting on the gamestage that triggers the real lock. */
    public boolean isClosedForChange() {
        return locked;
    }

    /** Explicit change. Throws if already closed — caller must check first. */
    public void setMode(PackMode newMode) {
        if (isClosedForChange()) {
            throw new IllegalStateException("Packmode is locked for this world");
        }
        this.mode = newMode;
        setDirty();
    }

    /** Forced set — bypasses the lock. Used only by console + op-level commands. */
    public void forceMode(PackMode newMode) {
        this.mode = newMode;
        setDirty();
    }

    public void lock() {
        if (!locked) {
            locked = true;
            setDirty();
        }
    }

    /** Called once, on first access in a new world, to stamp creation time. */
    public void ensureStamped() {
        if (worldCreatedMillis == 0L) {
            worldCreatedMillis = System.currentTimeMillis();
            setDirty();
        }
    }

    // ---------- SavedData ----------

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("mode", mode.name());
        tag.putBoolean("locked", locked);
        tag.putLong("createdAt", worldCreatedMillis);
        return tag;
    }

    public static PackModeData load(CompoundTag tag) {
        PackModeData d = new PackModeData();
        try { d.mode = PackMode.valueOf(tag.getString("mode")); }
        catch (IllegalArgumentException ignored) { d.mode = PackMode.ADVENTURE; }
        d.locked = tag.getBoolean("locked");
        d.worldCreatedMillis = tag.getLong("createdAt");
        return d;
    }

    public static PackModeData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        PackModeData data = overworld.getDataStorage().computeIfAbsent(
                PackModeData::load,
                PackModeData::new,
                DATA_NAME
        );
        data.ensureStamped();
        return data;
    }
}
