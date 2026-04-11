package com.soul.soa_additions.quest.progress;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * All quest progress for a single team, keyed by {@code chapter/quest} full id.
 * Solo players are a team-of-one, so this type is the <i>only</i> progress
 * container — there is no parallel per-player container. This keeps the
 * completion-propagation rule ("if A finishes, B and C also finish") from
 * being a special case: anything that writes progress writes it here.
 */
public final class TeamQuestProgress {

    private final UUID teamId;
    private final Map<String, QuestProgress> byQuestId = new HashMap<>();

    public TeamQuestProgress(UUID teamId) {
        this.teamId = teamId;
    }

    public UUID teamId() { return teamId; }

    /** Get-or-create the progress row for a quest. */
    public QuestProgress get(String fullQuestId) {
        return byQuestId.computeIfAbsent(fullQuestId, QuestProgress::new);
    }

    public QuestProgress peek(String fullQuestId) {
        return byQuestId.get(fullQuestId);
    }

    public Collection<QuestProgress> all() { return byQuestId.values(); }

    public int size() { return byQuestId.size(); }

    /**
     * Deep-copy every quest row from {@code other} into this container,
     * replacing any existing rows with the same id. Used when a player
     * leaves a team — their new solo team gets a fresh copy of the former
     * team's progress so they keep everything they earned as a member.
     *
     * <p>Implementation uses the NBT save/load round-trip rather than
     * hand-written copy constructors so there's one authoritative
     * serialization path that stays in sync with future field additions.
     * A few microseconds on a rare event is fine.</p>
     */
    public void copyFrom(TeamQuestProgress other) {
        for (QuestProgress qp : other.byQuestId.values()) {
            QuestProgress clone = QuestProgress.load(qp.save());
            byQuestId.put(clone.fullId(), clone);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("team", teamId);
        ListTag quests = new ListTag();
        for (QuestProgress qp : byQuestId.values()) quests.add(qp.save());
        tag.put("quests", quests);
        return tag;
    }

    public static TeamQuestProgress load(CompoundTag tag) {
        TeamQuestProgress t = new TeamQuestProgress(tag.getUUID("team"));
        ListTag list = tag.getList("quests", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            QuestProgress qp = QuestProgress.load(list.getCompound(i));
            t.byQuestId.put(qp.fullId(), qp);
        }
        return t;
    }
}
