package com.soul.soa_additions.bloodarsenal.modifier;

import net.minecraft.nbt.CompoundTag;

/**
 * Tracks modifier progression — counter, level, and ready-to-upgrade status.
 * Each modifier on a stasis tool has its own tracker.
 *
 * <p>Ported from: arcaratus.bloodarsenal.modifier.ModifierTracker</p>
 */
public class ModifierTracker {

    private final Modifier modifier;
    private double counter;
    private int level;
    private boolean readyToUpgrade;
    private boolean isDirty;

    public ModifierTracker(Modifier modifier) {
        this(modifier, 0, 0, false);
    }

    public ModifierTracker(Modifier modifier, int level, double counter, boolean readyToUpgrade) {
        this.modifier = modifier;
        this.level = level;
        this.counter = counter;
        this.readyToUpgrade = readyToUpgrade;
    }

    /**
     * Increments the usage counter. When the threshold for the next level
     * is reached, marks the modifier as ready to upgrade.
     */
    public void incrementCounter(double amount) {
        if (level >= modifier.getMaxLevel()) return;

        counter += amount;
        double threshold = modifier.getCounterThreshold(level + 1);
        if (counter >= threshold) {
            readyToUpgrade = true;
        }
        isDirty = true;
    }

    /** Called after the modifier is upgraded via Sanguine Infusion or tome */
    public void onModifierUpgraded() {
        level++;
        counter = 0;
        readyToUpgrade = false;
        isDirty = true;
    }

    // ── NBT ─────────────────────────────────────────────────────────────

    private static final String TAG_COUNTER = "counter";
    private static final String TAG_LEVEL = "level";
    private static final String TAG_READY = "readyToUpgrade";

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(TAG_COUNTER, counter);
        tag.putInt(TAG_LEVEL, level);
        tag.putBoolean(TAG_READY, readyToUpgrade);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        counter = tag.getDouble(TAG_COUNTER);
        level = tag.getInt(TAG_LEVEL);
        readyToUpgrade = tag.getBoolean(TAG_READY);
    }

    // ── Getters ─────────────────────────────────────────────────────────

    public double getCounter() { return counter; }
    public int getLevel() { return level; }
    public boolean isReadyToUpgrade() { return readyToUpgrade; }
    public boolean isDirty() { return isDirty; }
    public void setDirty(boolean dirty) { this.isDirty = dirty; }
    public Modifier getModifier() { return modifier; }
}
