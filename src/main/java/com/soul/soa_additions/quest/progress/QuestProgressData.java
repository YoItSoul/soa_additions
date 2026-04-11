package com.soul.soa_additions.quest.progress;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World-attached storage for every team's quest progress. Lives on the
 * overworld's {@code DimensionDataStorage} under {@code soa_quest_progress}
 * and persists across server restarts. One file for the whole pack — quest
 * progress is global to the world, not per-dimension.
 *
 * <p>This is the <i>only</i> writable progress store. Anything that mutates
 * counters or status goes through an instance acquired via {@link #get}. The
 * {@code setDirty()} calls on each mutation are intentional: they mark the
 * SavedData for re-serialization on the next save tick without us having to
 * hand-roll save scheduling.</p>
 */
public final class QuestProgressData extends SavedData {

    private static final String DATA_NAME = "soa_quest_progress";

    private final Map<UUID, TeamQuestProgress> byTeam = new HashMap<>();

    public QuestProgressData() {}

    public TeamQuestProgress forTeam(UUID teamId) {
        TeamQuestProgress t = byTeam.get(teamId);
        if (t == null) {
            t = new TeamQuestProgress(teamId);
            byTeam.put(teamId, t);
            setDirty();
        }
        return t;
    }

    public TeamQuestProgress peekTeam(UUID teamId) {
        return byTeam.get(teamId);
    }

    public void dropTeam(UUID teamId) {
        if (byTeam.remove(teamId) != null) setDirty();
    }

    /** Mark dirty after an external mutation to a borrowed team/quest/task. */
    public void touch() { setDirty(); }

    // ---------- SavedData ----------

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (TeamQuestProgress t : byTeam.values()) list.add(t.save());
        tag.put("teams", list);
        return tag;
    }

    public static QuestProgressData load(CompoundTag tag) {
        QuestProgressData data = new QuestProgressData();
        ListTag list = tag.getList("teams", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            TeamQuestProgress t = TeamQuestProgress.load(list.getCompound(i));
            data.byTeam.put(t.teamId(), t);
        }
        return data;
    }

    public static QuestProgressData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                QuestProgressData::load,
                QuestProgressData::new,
                DATA_NAME
        );
    }
}
