package com.soul.soa_additions.quest.team;

import java.util.List;
import java.util.UUID;

/**
 * A group of players who share quest progress. Progress in the quest engine is
 * stored keyed on {@link #id()}, not on individual player UUIDs — when any
 * teammate advances a task, the whole team's counter goes up, and every
 * teammate sees the quest as complete when the counter hits the target.
 *
 * <p>Solo players are represented by a single-member team whose id equals the
 * player's UUID. This collapses the "solo vs team" code path into one case:
 * there is only ever team progress.</p>
 *
 * @param id       stable team id (player UUID for solo, random UUID for real teams)
 * @param name     display name ("Soloist" for solo, user-provided for real teams)
 * @param ownerUuid player who created the team (first solo player for solo teams)
 * @param members  every member UUID. Always non-empty.
 * @param solo     true if this team represents a single player who hasn't joined a real team
 */
public record QuestTeam(UUID id, String name, UUID ownerUuid, List<UUID> members, boolean solo) {

    public static QuestTeam soloFor(UUID playerUuid, String playerName) {
        return new QuestTeam(playerUuid, playerName, playerUuid, List.of(playerUuid), true);
    }

    public boolean contains(UUID playerUuid) {
        return members.contains(playerUuid);
    }

    public int size() { return members.size(); }
}
