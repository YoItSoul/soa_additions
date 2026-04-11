package com.soul.soa_additions.quest.progress;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Per-quest progress for a single team. Holds the task counters, the current
 * lifecycle status, and a set of player UUIDs that have individually claimed
 * player-scoped rewards. Team-scoped rewards are tracked by the {@code
 * teamClaimed} flag — once the team has claimed them, no one else in the team
 * gets a second copy.
 *
 * <p>Claim tracking is split this way so a quest with, say, one team item and
 * one per-player XP reward works correctly: the first player claims the team
 * item (marks {@code teamClaimed}) and their own XP (adds their UUID), and
 * later players who hit claim only receive their XP.</p>
 */
public final class QuestProgress {

    private final String fullId;
    private final List<TaskProgress> tasks = new ArrayList<>();
    private QuestStatus status = QuestStatus.LOCKED;
    private boolean teamClaimed;
    private final Set<UUID> playerClaims = new HashSet<>();
    private long lastUpdateTick;
    /** Sticky "this quest has been claimed at least once" flag. Repeatable
     *  quests reset their status/tasks on claim, but downstream dependencies
     *  still need to see the original completion. */
    private boolean everClaimed;

    public QuestProgress(String fullId) {
        this.fullId = fullId;
    }

    public String fullId() { return fullId; }

    public QuestStatus status() { return status; }

    public void setStatus(QuestStatus status) { this.status = status; }

    public List<TaskProgress> tasks() { return tasks; }

    public TaskProgress task(int index) {
        while (tasks.size() <= index) tasks.add(new TaskProgress());
        return tasks.get(index);
    }

    public boolean teamClaimed() { return teamClaimed; }

    public void markTeamClaimed() { this.teamClaimed = true; }

    public boolean hasClaimed(UUID player) { return playerClaims.contains(player); }

    public void markPlayerClaimed(UUID player) { playerClaims.add(player); }

    public Set<UUID> playerClaims() { return Collections.unmodifiableSet(playerClaims); }

    public long lastUpdateTick() { return lastUpdateTick; }

    public void touch(long tick) { this.lastUpdateTick = tick; }

    public boolean everClaimed() { return everClaimed; }
    public void markEverClaimed() { this.everClaimed = true; }

    /** Full reset for TEAM-scoped repeatable quests. Keeps {@code everClaimed}
     *  so downstream deps stay satisfied. Caller must {@link #setStatus} to
     *  whatever the evaluator decides afterwards. */
    public void resetForRepeat() {
        for (TaskProgress tp : tasks) tp.setCount(0);
        teamClaimed = false;
        playerClaims.clear();
        status = QuestStatus.VISIBLE;
    }

    /** PLAYER-scoped repeat: clear only the claim markers so each team member
     *  can re-collect personal rewards; leave task counters intact. */
    public void clearClaimMarkers() {
        teamClaimed = false;
        playerClaims.clear();
        status = QuestStatus.VISIBLE;
    }

    // ---------- NBT ----------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", fullId);
        tag.putString("status", status.name());
        tag.putBoolean("teamClaimed", teamClaimed);
        tag.putBoolean("everClaimed", everClaimed);
        tag.putLong("tick", lastUpdateTick);

        ListTag taskList = new ListTag();
        for (TaskProgress tp : tasks) taskList.add(tp.save());
        tag.put("tasks", taskList);

        ListTag claims = new ListTag();
        for (UUID u : playerClaims) {
            CompoundTag c = new CompoundTag();
            c.putUUID("id", u);
            claims.add(c);
        }
        tag.put("claims", claims);
        return tag;
    }

    public static QuestProgress load(CompoundTag tag) {
        QuestProgress p = new QuestProgress(tag.getString("id"));
        try { p.status = QuestStatus.valueOf(tag.getString("status")); }
        catch (IllegalArgumentException ignored) { p.status = QuestStatus.LOCKED; }
        p.teamClaimed = tag.getBoolean("teamClaimed");
        p.everClaimed = tag.getBoolean("everClaimed");
        p.lastUpdateTick = tag.getLong("tick");

        ListTag taskList = tag.getList("tasks", Tag.TAG_COMPOUND);
        for (int i = 0; i < taskList.size(); i++) {
            p.tasks.add(TaskProgress.load(taskList.getCompound(i)));
        }

        ListTag claims = tag.getList("claims", Tag.TAG_COMPOUND);
        for (int i = 0; i < claims.size(); i++) {
            p.playerClaims.add(claims.getCompound(i).getUUID("id"));
        }
        return p;
    }
}
