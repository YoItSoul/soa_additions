package com.soul.soa_additions.quest.editor;

import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which ops currently have quest edit mode enabled. Transient — cleared
 * on server restart, so a forgotten edit flag doesn't survive a reboot. A
 * player with edit mode on sees the editor controls in the quest book GUI;
 * everyone else (including other ops who haven't enabled it) sees the normal
 * read-only view.
 *
 * <p>Edit mode is a <i>view</i> concept, not a <i>permission</i> concept. The
 * permission check (op level 2+) happens when the command is run; edit mode
 * just reflects whether the authorized user has opted into the editor UI.</p>
 */
public final class EditModeTracker {

    private static final Set<UUID> ACTIVE = Collections.synchronizedSet(new HashSet<>());
    private static final Map<UUID, EditTarget> TARGETS = new ConcurrentHashMap<>();

    private EditModeTracker() {}

    /** Target defaults to {@link EditTarget#WORLD_OVERRIDE} until explicitly changed. */
    public static EditTarget targetOf(UUID playerUuid) {
        return TARGETS.getOrDefault(playerUuid, EditTarget.WORLD_OVERRIDE);
    }

    /** @return true if the target actually changed */
    public static boolean setTarget(UUID playerUuid, EditTarget target) {
        EditTarget prev = TARGETS.put(playerUuid, target);
        return prev != target;
    }

    public static boolean isActive(UUID playerUuid) {
        return ACTIVE.contains(playerUuid);
    }

    public static boolean isActive(ServerPlayer player) {
        return isActive(player.getUUID());
    }

    /**
     * @return true if the state actually changed
     */
    public static boolean setActive(UUID playerUuid, boolean active) {
        return active ? ACTIVE.add(playerUuid) : ACTIVE.remove(playerUuid);
    }

    public static void clearAll() {
        ACTIVE.clear();
        TARGETS.clear();
    }

    public static int activeCount() {
        return ACTIVE.size();
    }
}
