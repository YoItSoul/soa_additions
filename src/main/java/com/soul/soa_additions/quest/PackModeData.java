package com.soul.soa_additions.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-world packmode state. Stores the current mode, whether it's locked
 * (either by explicit command, the first quest completion, or a
 * {@code lock_packmode} reward), and the real-world timestamp of creation
 * so the 30-minute soft-lock window can be enforced.
 *
 * <p>Packmode is intentionally a single global value per world — it affects
 * loot, recipes, quest visibility, and scaling difficulty. Allowing it to
 * differ per player would create incoherent progression (player A sees an
 * expert recipe gate that doesn't exist for player B). The lock flag
 * prevents the usual FTBQ-style mid-run mode flip that strands progress.</p>
 */
public final class PackModeData extends SavedData {

    private static final String DATA_NAME = "soa_packmode";

    private PackMode mode = PackMode.ADVENTURE;
    private boolean locked = false;
    private boolean serverEnforced = false;
    private long worldCreatedMillis = 0L;

    public PackModeData() {}

    public PackMode mode() { return mode; }

    public boolean locked() { return locked; }

    /** True when the mode was set by server config rather than player choice. */
    public boolean serverEnforced() { return serverEnforced; }

    public long worldCreatedMillis() { return worldCreatedMillis; }

    /** True only when the mode has been explicitly locked — by command or by
     *  a {@code lock_packmode} reward firing on a completed quest. The old
     *  30-minute soft window was removed because it reported "locked" before
     *  any actual lock event had occurred, which surprised players who were
     *  waiting on the gamestage that triggers the real lock. */
    public boolean isClosedForChange() {
        return locked;
    }

    /** Explicit change. Throws if already closed — caller must check first. */
    public void setMode(PackMode newMode) {
        if (isClosedForChange()) {
            throw new IllegalStateException("Packmode is locked for this world");
        }
        this.mode = newMode;
        setDirty();
        PackModeBridge.remember(newMode);
    }

    /** Forced set — bypasses the lock. Used only by console + op-level commands. */
    public void forceMode(PackMode newMode) {
        this.mode = newMode;
        PackModeBridge.remember(newMode);
        setDirty();
    }

    public void lock() {
        if (!locked) {
            locked = true;
            setDirty();
        }
    }

    /**
     * In singleplayer the mode belongs to the world, not the session.
     *
     * <p>The choice is made once, on the create-world screen, and is fixed from then on — a world
     * cannot be started on casual and finished on expert. Applied on every load rather than only at
     * creation, so worlds made before this existed are covered too.
     *
     * <p>Servers are left alone deliberately: which mode a server runs is the admin's call, and
     * changing it later is a decision they are entitled to make for their own players.
     * {@code /soa packmode force} still overrides this, at permission 4, and the anti-cheat already
     * treats it as a cheat command.
     */
    public void enforceSingleplayerLock(MinecraftServer server) {
        if (!server.isDedicatedServer() && !locked) {
            locked = true;
            setDirty();
        }
    }

    /** Called once, on first access in a new world, to stamp creation time. */
    public void ensureStamped() {
        if (worldCreatedMillis == 0L) {
            worldCreatedMillis = System.currentTimeMillis();
            setDirty();
        }
    }

    // ---------- SavedData ----------

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("mode", mode.name());
        tag.putBoolean("locked", locked);
        tag.putBoolean("serverEnforced", serverEnforced);
        tag.putLong("createdAt", worldCreatedMillis);
        return tag;
    }

    public static PackModeData load(CompoundTag tag) {
        PackModeData d = new PackModeData();
        try { d.mode = PackMode.valueOf(tag.getString("mode")); }
        catch (IllegalArgumentException ignored) { d.mode = PackMode.ADVENTURE; }
        d.locked = tag.getBoolean("locked");
        d.serverEnforced = tag.getBoolean("serverEnforced");
        d.worldCreatedMillis = tag.getLong("createdAt");
        return d;
    }

    public static PackModeData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        PackModeData data = overworld.getDataStorage().computeIfAbsent(
                PackModeData::load,
                () -> {
                    // Fresh world: seed the mode from whatever the player
                    // picked on the create-world screen, falling back to the
                    // default. Consumed once so subsequent worlds aren't
                    // infected by a stale choice.
                    PackModeData d = new PackModeData();
                    PackMode pending = PendingPackMode.consume();
                    if (pending != null) d.mode = pending;
                    return d;
                },
                DATA_NAME
        );
        data.ensureStamped();
        data.applyServerConfig();
        data.enforceSingleplayerLock(server);
        // Keep the KubeJS-facing mirror in step with the authoritative value,
        // so a cold start (where no server/level exists during datapack load)
        // still resolves this world's mode. See PackModeBridge.
        PackModeBridge.remember(data.mode);
        return data;
    }

    /**
     * If the server config specifies a pack mode, enforce it. Re-checks every
     * startup so admins can change the config value and restart to switch modes.
     * Clears enforcement when the config is removed (empty string).
     */
    private boolean configAppliedThisSession = false;

    private void applyServerConfig() {
        if (configAppliedThisSession) return;
        configAppliedThisSession = true;

        String cfg = com.soul.soa_additions.config.ModConfigs.SERVER_PACKMODE.get();
        if (cfg == null || cfg.isBlank()) {
            // Config cleared — remove server enforcement so players/commands
            // can change the mode again (if it wasn't also manually locked).
            if (serverEnforced) {
                serverEnforced = false;
                locked = false;
                setDirty();
                org.slf4j.LoggerFactory.getLogger("soa_additions/packmode")
                        .info("Server pack mode config cleared — mode is now editable");
            }
            return;
        }
        // parseStrict answers null for invalid input — only enforce if the config string
        // actually names a mode, rather than silently enforcing ADVENTURE on a typo.
        PackMode target = PackMode.parseStrict(cfg);
        if (target == null) return;

        if (serverEnforced && this.mode == target) return; // already correct

        this.mode = target;
        this.locked = true;
        this.serverEnforced = true;
        setDirty();
        org.slf4j.LoggerFactory.getLogger("soa_additions/packmode")
                .info("Pack mode enforced by server config: {}", target.lower());
    }
}
