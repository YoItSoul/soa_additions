package com.soul.soa_additions.quest.net;

import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.progress.QuestStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-side mirror of the active team's progress. Populated by
 * {@link QuestSyncPacket} on login and after every claim. The quest book
 * screen reads exclusively from this cache — it never directly queries
 * server data — so there's one obvious source of truth on the client and
 * one obvious place to invalidate when a new sync arrives.
 *
 * <p>Thread-safety: sync packets are applied on the client main thread via
 * {@code enqueueWork}, and every screen render also runs on the main
 * thread, so plain HashMaps are fine. If any async consumer ever appears,
 * swap to ConcurrentHashMap.</p>
 */
public final class ClientQuestState {

    private static volatile PackMode packMode = PackMode.ADVENTURE;
    private static volatile boolean serverEnforced = false;
    private static volatile Map<String, QuestSnapshotEntry> byId = Collections.emptyMap();
    private static volatile boolean editMode = false;

    public static boolean editMode() { return editMode; }
    public static void setEditMode(boolean v) { editMode = v; }

    private ClientQuestState() {}

    public static void apply(QuestSyncPacket pkt) {
        Map<String, QuestSnapshotEntry> fresh = new HashMap<>();
        for (QuestSnapshotEntry e : pkt.entries()) fresh.put(e.fullId(), e);
        byId = fresh;
        packMode = pkt.packMode();
        serverEnforced = pkt.serverEnforced();
    }

    public static PackMode packMode() { return packMode; }

    /** True when the server admin pre-set the pack mode via config. */
    public static boolean serverEnforced() { return serverEnforced; }

    public static QuestSnapshotEntry get(String fullId) { return byId.get(fullId); }

    /**
     * Default for unknown quests: LOCKED with empty counters. Keeps the GUI
     * from having to null-check on every lookup — a brand-new quest that
     * hasn't been touched by the team still has a sensible rendering.
     */
    public static QuestStatus statusOf(String fullId) {
        QuestSnapshotEntry e = byId.get(fullId);
        return e == null ? QuestStatus.LOCKED : e.status();
    }

    public static int taskCount(String fullId, int index) {
        QuestSnapshotEntry e = byId.get(fullId);
        if (e == null || index >= e.taskCounts().size()) return 0;
        return e.taskCounts().get(index);
    }

    public static boolean localClaimed(String fullId) {
        QuestSnapshotEntry e = byId.get(fullId);
        return e != null && e.localClaimed();
    }

    public static int size() { return byId.size(); }
}
