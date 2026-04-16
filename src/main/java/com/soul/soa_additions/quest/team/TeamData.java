package com.soul.soa_additions.quest.team;

import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete {@link TeamManager} backed by {@link SavedData}. Stores every
 * multi-member team and a per-player "current team" pointer; solo players are
 * synthesized on demand and never hit disk — they don't need to. This keeps
 * the data file small and means a server full of soloists writes nothing.
 *
 * <p>The owner-transfer rule on leave is deliberate: quest rewards that
 * target the owner (future feature — "deliver to leader's enderchest" and
 * similar) shouldn't silently brick when the owner quits. Transferring to
 * the next-joined member keeps the team alive; disbanding only happens on
 * the last member leaving or an explicit {@code /soa team disband}.</p>
 */
public final class TeamData extends SavedData implements TeamManager {

    private static final String DATA_NAME = "soa_quest_teams";

    /** How long a pending invite stays valid (ms). */
    private static final long INVITE_TTL_MS = 5 * 60 * 1000L;
    /** Minimum gap between invites from the same inviter to the same invitee (ms). */
    private static final long INVITE_COOLDOWN_MS = 60 * 1000L;
    /** Hard cap on team-name length after sanitization. */
    private static final int MAX_TEAM_NAME_LENGTH = 32;

    private final Map<UUID, StoredTeam> teams = new HashMap<>();
    private final Map<UUID, UUID> playerToTeam = new HashMap<>();

    /** invitee UUID → (teamId → expiresAtMs). Transient, not persisted. */
    private final Map<UUID, Map<UUID, Long>> pendingInvites = new HashMap<>();
    /** inviter UUID → (invitee UUID → lastInviteMs). Transient, not persisted. */
    private final Map<UUID, Map<UUID, Long>> inviteCooldowns = new HashMap<>();

    public TeamData() {}

    // ---------- TeamManager ----------

    @Override
    public QuestTeam teamOf(ServerPlayer player) {
        UUID teamId = playerToTeam.get(player.getUUID());
        if (teamId != null) {
            StoredTeam st = teams.get(teamId);
            if (st != null) return st.toRecord(false);
        }
        // Solo fallback — never persisted.
        return QuestTeam.soloFor(player.getUUID(), player.getGameProfile().getName());
    }

    /**
     * Resolve the team id for a player UUID without needing a ServerPlayer.
     * Returns the player's own UUID for solo players (solo teams are keyed
     * on the player UUID by convention), matching {@link #teamOf}'s behavior.
     */
    public UUID teamIdOf(UUID playerUuid) {
        UUID teamId = playerToTeam.get(playerUuid);
        return teamId != null ? teamId : playerUuid;
    }

    @Override
    public Optional<QuestTeam> team(UUID teamId) {
        StoredTeam st = teams.get(teamId);
        return st == null ? Optional.empty() : Optional.of(st.toRecord(false));
    }

    @Override
    public QuestTeam createTeam(ServerPlayer owner, String name) {
        String sanitized = sanitizeTeamName(name);
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be empty after stripping formatting.");
        }
        leaveTeam(owner); // drop any prior team membership first

        UUID teamId = UUID.randomUUID();
        StoredTeam st = new StoredTeam(teamId, sanitized, owner.getUUID());
        st.members.add(owner.getUUID());
        teams.put(teamId, st);
        playerToTeam.put(owner.getUUID(), teamId);
        setDirty();
        return st.toRecord(false);
    }

    /** Strip Minecraft formatting codes and control characters, trim, and cap
     *  at {@link #MAX_TEAM_NAME_LENGTH}. The name is used in chat messages
     *  sent to other players (invite notification), so any unsanitized input
     *  could spoof server messages or corrupt other players' chat. */
    public static String sanitizeTeamName(String raw) {
        if (raw == null) return "";
        String cleaned = raw.replaceAll("[\u00A7\u0000-\u001F\u007F]", "").trim();
        if (cleaned.length() > MAX_TEAM_NAME_LENGTH) {
            cleaned = cleaned.substring(0, MAX_TEAM_NAME_LENGTH);
        }
        return cleaned;
    }

    // ---------- invite allowlist ----------

    /** True if {@code inviter} may invite {@code invitee} right now (not on cooldown). */
    public boolean canInvite(UUID inviter, UUID invitee) {
        Map<UUID, Long> cds = inviteCooldowns.get(inviter);
        if (cds == null) return true;
        Long last = cds.get(invitee);
        return last == null || (System.currentTimeMillis() - last) >= INVITE_COOLDOWN_MS;
    }

    /** Record that {@code inviter} invited {@code invitee} to {@code teamId}. */
    public void recordInvite(UUID teamId, UUID inviter, UUID invitee) {
        long now = System.currentTimeMillis();
        pendingInvites.computeIfAbsent(invitee, k -> new HashMap<>())
                .put(teamId, now + INVITE_TTL_MS);
        inviteCooldowns.computeIfAbsent(inviter, k -> new HashMap<>())
                .put(invitee, now);
    }

    /** Returns true iff {@code invitee} had an unexpired invite for {@code teamId};
     *  the invite is removed either way (so expired entries self-clean). */
    public boolean consumeInvite(UUID invitee, UUID teamId) {
        Map<UUID, Long> invites = pendingInvites.get(invitee);
        if (invites == null) return false;
        Long exp = invites.remove(teamId);
        if (invites.isEmpty()) pendingInvites.remove(invitee);
        return exp != null && exp > System.currentTimeMillis();
    }

    @Override
    public QuestTeam joinTeam(UUID teamId, ServerPlayer joiner) {
        StoredTeam st = teams.get(teamId);
        if (st == null) throw new IllegalArgumentException("Unknown team: " + teamId);
        leaveTeam(joiner);
        if (!st.members.contains(joiner.getUUID())) st.members.add(joiner.getUUID());
        playerToTeam.put(joiner.getUUID(), teamId);
        setDirty();
        return st.toRecord(false);
    }

    @Override
    public QuestTeam leaveTeam(ServerPlayer player) {
        UUID teamId = playerToTeam.remove(player.getUUID());
        if (teamId == null) {
            // Was solo — "leaving" a solo team is a no-op that returns the same solo team.
            return QuestTeam.soloFor(player.getUUID(), player.getGameProfile().getName());
        }
        StoredTeam st = teams.get(teamId);
        if (st == null) return QuestTeam.soloFor(player.getUUID(), player.getGameProfile().getName());

        // Snapshot team progress into the leaving player's solo bucket so
        // they keep everything they earned as a member.
        QuestProgressData pdata = QuestProgressData.get(player.server);
        TeamQuestProgress teamProg = pdata.peekTeam(teamId);
        if (teamProg != null) {
            pdata.forTeam(player.getUUID()).copyFrom(teamProg);
            pdata.touch();
        }

        st.members.remove(player.getUUID());
        if (st.members.isEmpty()) {
            teams.remove(teamId);
        } else if (st.ownerUuid.equals(player.getUUID())) {
            st.ownerUuid = st.members.get(0); // transfer to next-joined
        }
        setDirty();
        return QuestTeam.soloFor(player.getUUID(), player.getGameProfile().getName());
    }

    @Override
    public void disband(UUID teamId) {
        StoredTeam st = teams.remove(teamId);
        if (st == null) return;
        for (UUID m : st.members) playerToTeam.remove(m);
        setDirty();
    }

    @Override
    public List<UUID> membersOf(UUID teamId) {
        StoredTeam st = teams.get(teamId);
        return st == null ? Collections.emptyList() : new ArrayList<>(st.members);
    }

    @Override
    public List<ServerPlayer> onlineMembers(UUID teamId) {
        // Resolved by the caller with a server reference; see onlineMembers(server, teamId).
        return Collections.emptyList();
    }

    public List<ServerPlayer> onlineMembers(MinecraftServer server, UUID teamId) {
        StoredTeam st = teams.get(teamId);
        if (st == null) return Collections.emptyList();
        List<ServerPlayer> out = new ArrayList<>();
        for (UUID m : st.members) {
            ServerPlayer p = server.getPlayerList().getPlayer(m);
            if (p != null) out.add(p);
        }
        return out;
    }

    // ---------- SavedData ----------

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (StoredTeam st : teams.values()) list.add(st.save());
        tag.put("teams", list);
        return tag;
    }

    public static TeamData load(CompoundTag tag) {
        TeamData data = new TeamData();
        ListTag list = tag.getList("teams", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            StoredTeam st = StoredTeam.load(list.getCompound(i));
            data.teams.put(st.id, st);
            for (UUID m : st.members) data.playerToTeam.put(m, st.id);
        }
        return data;
    }

    public static TeamData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                TeamData::load,
                TeamData::new,
                DATA_NAME
        );
    }

    // ---------- internal storage shape ----------

    private static final class StoredTeam {
        final UUID id;
        String name;
        UUID ownerUuid;
        final List<UUID> members = new ArrayList<>();

        StoredTeam(UUID id, String name, UUID ownerUuid) {
            this.id = id;
            this.name = name;
            this.ownerUuid = ownerUuid;
        }

        QuestTeam toRecord(boolean solo) {
            return new QuestTeam(id, name, ownerUuid, Collections.unmodifiableList(new ArrayList<>(members)), solo);
        }

        CompoundTag save() {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", id);
            t.putString("name", name);
            t.putUUID("owner", ownerUuid);
            ListTag ml = new ListTag();
            for (UUID m : members) {
                CompoundTag c = new CompoundTag();
                c.putUUID("id", m);
                ml.add(c);
            }
            t.put("members", ml);
            return t;
        }

        static StoredTeam load(CompoundTag t) {
            StoredTeam st = new StoredTeam(t.getUUID("id"), t.getString("name"), t.getUUID("owner"));
            ListTag ml = t.getList("members", Tag.TAG_COMPOUND);
            for (int i = 0; i < ml.size(); i++) st.members.add(ml.getCompound(i).getUUID("id"));
            return st;
        }
    }
}
