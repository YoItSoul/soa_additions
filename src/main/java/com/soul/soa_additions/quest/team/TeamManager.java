package com.soul.soa_additions.quest.team;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side lookup and mutation surface for quest teams. The concrete
 * implementation lives in SavedData (upcoming pass) — this interface is what
 * the quest engine, reward system, and commands depend on, so the storage
 * layer can be swapped without touching consumers.
 *
 * <p>Design notes:</p>
 * <ul>
 *   <li>Every online player resolves to a {@link QuestTeam} — solo players get
 *       an implicit team-of-one, so the quest engine never sees a null team.</li>
 *   <li>Teams are keyed on their own UUID, not the owner's, so transferring
 *       ownership doesn't break progress tracking.</li>
 *   <li>Join/leave/disband operations return the resulting team states so
 *       callers don't have to re-query; this also lets network packets batch
 *       team-update payloads cleanly.</li>
 * </ul>
 */
public interface TeamManager {

    /** Team for this player (solo team if they haven't joined one). Never null. */
    QuestTeam teamOf(ServerPlayer player);

    /** Team by id, or empty if disbanded. */
    Optional<QuestTeam> team(UUID teamId);

    /** Create a new team with this player as owner and sole initial member. */
    QuestTeam createTeam(ServerPlayer owner, String name);

    /** Add a player to an existing team. Returns the updated team. */
    QuestTeam joinTeam(UUID teamId, ServerPlayer joiner);

    /** Remove a player from their current team. If they're the owner and the */
    /** team has members left, ownership transfers to the next-joined member. */
    /** If they're the last member, the team disbands and its progress is archived. */
    QuestTeam leaveTeam(ServerPlayer player);

    /** Disband a team outright. Progress archived for audit. */
    void disband(UUID teamId);

    /** List every player currently on this team, online or not. */
    List<UUID> membersOf(UUID teamId);

    /**
     * Online members only — useful for reward fan-out where offline players
     * get queued grants on next login rather than dropping rewards on the
     * ground of an empty slot.
     */
    List<ServerPlayer> onlineMembers(UUID teamId);
}
