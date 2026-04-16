package com.soul.soa_additions.anticheat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * World-scoped persistent set of flagged UUIDs. Lives next to
 * {@code soa_packmode} and {@code soa_quest_progress} on the overworld's
 * {@code DimensionDataStorage}.
 *
 * <p>Flags are per-player: one player cheating never taints another player
 * or the world as a whole. Each entry records the first reason that caused
 * the flag plus the real-world timestamp so admins can investigate.</p>
 *
 * <p>This is one of three redundant stores used by {@link CheaterManager}:
 * the SavedData (here), the player's persistent NBT, and the append-only
 * chained log at {@code <world>/soa_additions/cheaters.log}. {@link #logHead}
 * stores the latest hash from the chained log so we can detect tampering
 * even if someone deletes the log file entirely.</p>
 */
public final class CheaterData extends SavedData {

    private static final String DATA_NAME = "soa_cheaters";

    public record Entry(String reason, long flaggedAtMillis) {}

    private final Map<UUID, Entry> flagged = new HashMap<>();
    /** Hex-encoded SHA-256 of the latest cheater log entry, or "" for an empty chain. */
    private String logHead = "";

    public CheaterData() {}

    public boolean isFlagged(UUID uuid) {
        return flagged.containsKey(uuid);
    }

    public Entry entry(UUID uuid) {
        return flagged.get(uuid);
    }

    public Set<UUID> allFlagged() {
        return Collections.unmodifiableSet(flagged.keySet());
    }

    /** Returns true if the UUID was newly added. */
    public boolean flag(UUID uuid, String reason) {
        if (flagged.containsKey(uuid)) return false;
        flagged.put(uuid, new Entry(reason == null ? "unspecified" : reason, System.currentTimeMillis()));
        setDirty();
        return true;
    }

    public String logHead() { return logHead; }

    public void setLogHead(String head) {
        this.logHead = head == null ? "" : head;
        setDirty();
    }

    // ---------- SavedData ----------

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Entry> e : flagged.entrySet()) {
            CompoundTag row = new CompoundTag();
            row.putUUID("uuid", e.getKey());
            row.putString("reason", e.getValue().reason());
            row.putLong("flaggedAt", e.getValue().flaggedAtMillis());
            list.add(row);
        }
        tag.put("flagged", list);
        tag.putString("logHead", logHead);
        return tag;
    }

    public static CheaterData load(CompoundTag tag) {
        CheaterData d = new CheaterData();
        ListTag list = tag.getList("flagged", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag row = list.getCompound(i);
            d.flagged.put(row.getUUID("uuid"),
                    new Entry(row.getString("reason"), row.getLong("flaggedAt")));
        }
        d.logHead = tag.getString("logHead");
        return d;
    }

    public static CheaterData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                CheaterData::load,
                CheaterData::new,
                DATA_NAME
        );
    }
}
