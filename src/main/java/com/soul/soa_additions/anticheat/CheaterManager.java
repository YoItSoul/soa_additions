package com.soul.soa_additions.anticheat;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrator for the triple-redundant cheater store. Writes to all three
 * backends on flag and cross-checks them on login so a cheater can't undo
 * their flag by tampering with any single one.
 *
 * <h3>Backends</h3>
 * <ol>
 *   <li>{@link CheaterData} — world-scoped {@code SavedData}. Lives in
 *       {@code <world>/data/soa_cheaters.dat}.</li>
 *   <li>Forge-persisted NBT on the {@link ServerPlayer}. Survives death,
 *       dimension changes, and logout. Lives in the player's
 *       {@code <world>/playerdata/<uuid>.dat}.</li>
 *   <li>{@link CheaterLog} — append-only hash-chained log at
 *       {@code <world>/soa_additions/cheaters.log}.</li>
 * </ol>
 *
 * <p>On login, {@link #crossCheckOnLogin} reads all three and re-applies the
 * flag from whichever backend still has it. A mismatch is itself recorded
 * as a {@code tamper_detected:*} event in the log so admins can see what
 * was meddled with.</p>
 */
public final class CheaterManager {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/cheater-mgr");
    private static final String NBT_KEY = "soa_cheater_flagged";
    private static final String NBT_REASON_KEY = "soa_cheater_reason";

    public static final ResourceLocation CHEAT_ADVANCEMENT_ID =
            new ResourceLocation(SoaAdditions.MODID, "cheats_are_fun");

    /** Per-server log instance so we don't re-resolve the world path each call. */
    private static final ConcurrentHashMap<MinecraftServer, CheaterLog> LOGS = new ConcurrentHashMap<>();

    private CheaterManager() {}

    public static CheaterLog log(MinecraftServer server) {
        return LOGS.computeIfAbsent(server, CheaterLog::new);
    }

    public static void clearServer(MinecraftServer server) {
        LOGS.remove(server);
    }

    // ---------- core API ----------

    /**
     * Flag a player in all three backends. Idempotent — a second call for the
     * same player is a no-op in the SavedData but still re-applies the
     * advancement + NBT flag in case either was stripped.
     */
    public static void flag(ServerPlayer player, String reason) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        CheaterData data = CheaterData.get(server);
        boolean newlyFlagged = data.flag(player.getUUID(), reason);

        // Chain log only records the initial flag, not every re-apply from a
        // login check. Tamper events have their own explicit append path in
        // crossCheckOnLogin so we don't miss them.
        if (newlyFlagged) {
            String head = log(server).append(player.getUUID(), reason, data.logHead());
            if (!head.isEmpty()) data.setLogHead(head);
        }

        writePlayerNbt(player, reason);
        grantAdvancement(player);

        if (newlyFlagged) {
            LOG.warn("Flagged {} ({}): {}", player.getGameProfile().getName(), player.getUUID(), reason);
            com.soul.soa_additions.quest.telemetry.QuestTelemetry.postCheatFlag(player, reason);
        }
    }

    /** True iff this player is recorded as a cheater in the canonical SavedData. */
    public static boolean isFlagged(MinecraftServer server, UUID uuid) {
        return CheaterData.get(server).isFlagged(uuid);
    }

    public static boolean isFlagged(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return server != null && isFlagged(server, player.getUUID());
    }

    // ---------- login cross-check ----------

    /**
     * Verify every backend agrees on this player's flag state. Re-apply the
     * flag from whichever backend has it. Record a {@code tamper_detected}
     * entry if at least one backend disagrees.
     */
    public static void crossCheckOnLogin(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        CheaterData data = CheaterData.get(server);
        boolean savedDataSays = data.isFlagged(player.getUUID());
        boolean nbtSays = readPlayerNbt(player);
        boolean advancementSays = hasCheatAdvancement(player);

        // Chain verification — if it's broken, someone edited the log. That
        // alone is a tamper event, regardless of this player's own status.
        String logHeadActual = log(server).verifyAndGetHead();
        if (logHeadActual == null) {
            tamperEvent(player, "log_chain_broken");
        } else if (!logHeadActual.equals(data.logHead())) {
            // File's chain is intact but SavedData's head pointer disagrees —
            // log file was truncated or replaced with an older version.
            tamperEvent(player, "log_head_mismatch");
            data.setLogHead(logHeadActual);
        }

        // Majority/any flag wins — if any backend has it, flag is real.
        boolean anyFlagged = savedDataSays || nbtSays || advancementSays;
        if (!anyFlagged) return;

        if (!(savedDataSays && nbtSays && advancementSays)) {
            StringBuilder mismatch = new StringBuilder("tamper_detected:backend_mismatch");
            if (!savedDataSays) mismatch.append(":saveddata_missing");
            if (!nbtSays) mismatch.append(":nbt_missing");
            if (!advancementSays) mismatch.append(":advancement_missing");
            tamperEvent(player, mismatch.toString());
        }

        // Re-apply to every backend. Pull the reason from whichever backend
        // still has it so we don't overwrite the original flag reason.
        String reason = resolveReason(data, player);
        flag(player, reason);
    }

    private static String resolveReason(CheaterData data, ServerPlayer player) {
        CheaterData.Entry e = data.entry(player.getUUID());
        if (e != null) return e.reason();
        String nbt = readPlayerNbtReason(player);
        if (nbt != null) return nbt;
        return "restored_from_advancement";
    }

    private static void tamperEvent(ServerPlayer player, String label) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        CheaterData data = CheaterData.get(server);
        String head = log(server).append(player.getUUID(), label, data.logHead());
        if (!head.isEmpty()) data.setLogHead(head);
        LOG.warn("Tamper detected for {} ({}): {}", player.getGameProfile().getName(), player.getUUID(), label);
    }

    // ---------- advancement handling ----------

    public static boolean hasCheatAdvancement(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        Advancement adv = server.getAdvancements().getAdvancement(CHEAT_ADVANCEMENT_ID);
        return adv != null && player.getAdvancements().getOrStartProgress(adv).isDone();
    }

    public static void grantAdvancement(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Advancement adv = server.getAdvancements().getAdvancement(CHEAT_ADVANCEMENT_ID);
        if (adv == null) {
            LOG.error("cheats_are_fun advancement is missing from datapacks");
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(adv);
        if (progress.isDone()) return;
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(adv, criterion);
        }
    }

    // ---------- NBT mirror ----------

    private static CompoundTag persistedSubTag(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void writePlayerNbt(ServerPlayer player, String reason) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putBoolean(NBT_KEY, true);
        if (reason != null) persisted.putString(NBT_REASON_KEY, reason);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static boolean readPlayerNbt(ServerPlayer player) {
        return persistedSubTag(player).getBoolean(NBT_KEY);
    }

    private static String readPlayerNbtReason(ServerPlayer player) {
        CompoundTag sub = persistedSubTag(player);
        if (!sub.contains(NBT_REASON_KEY)) return null;
        String r = sub.getString(NBT_REASON_KEY);
        return r.isEmpty() ? null : r;
    }
}
