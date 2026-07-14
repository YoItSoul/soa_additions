package com.soul.soa_additions.oresight;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-player capability that records every ore the player currently has
 * ore-sight for and the absolute game-time tick when each entry expires.
 *
 * <p>The capability is the source of truth on the server. Each insertion,
 * removal, or expiry triggers a {@link OreSightSyncPacket} to the owning
 * player so the client renderer knows what to scan and outline.</p>
 *
 * <p>NBT format on the player save:
 * <pre>
 * "soa_oresight": {
 *   "entries": [
 *     {"block": "minecraft:gold_ore", "until": 1234567L},
 *     ...
 *   ]
 * }
 * </pre></p>
 */
public final class OreSightTracker {

    public static final Capability<OreSightTracker> CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation CAP_ID =
            new ResourceLocation("soa_additions", "ore_sight");

    private final Map<Block, Long> expiryByBlock = new LinkedHashMap<>();

    /** Master ore-sight expiry tick (absolute game-time). 0 = inactive. */
    private long masterExpiry = 0L;

    public OreSightTracker() {}

    /** Add or refresh an ore entry. {@code expiryGameTime} is absolute level
     *  game-time when the sight should end. Returns true if the map changed. */
    public boolean addOrRefresh(Block block, long expiryGameTime) {
        if (block == null) return false;
        Long prev = expiryByBlock.get(block);
        if (prev != null && prev >= expiryGameTime) return false;
        expiryByBlock.put(block, expiryGameTime);
        return true;
    }

    /** Set/refresh the master ore-sight expiry. Returns true if it changed. */
    public boolean setMaster(long expiryGameTime) {
        if (masterExpiry >= expiryGameTime) return false;
        masterExpiry = expiryGameTime;
        return true;
    }

    public long masterExpiry() { return masterExpiry; }

    public boolean isMasterActive(long currentGameTime) {
        return masterExpiry > currentGameTime;
    }

    /** Drop expired entries. Returns true if any were removed. */
    public boolean pruneExpired(long currentGameTime) {
        boolean changed = expiryByBlock.entrySet().removeIf(e -> e.getValue() <= currentGameTime);
        if (masterExpiry != 0L && masterExpiry <= currentGameTime) {
            masterExpiry = 0L;
            changed = true;
        }
        return changed;
    }

    public Map<Block, Long> snapshot() {
        return new LinkedHashMap<>(expiryByBlock);
    }

    public boolean isEmpty() {
        return expiryByBlock.isEmpty() && masterExpiry == 0L;
    }

    public CompoundTag serializeNBT() {
        CompoundTag out = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<Block, Long> e : expiryByBlock.entrySet()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(e.getKey());
            if (id == null) continue;
            CompoundTag entry = new CompoundTag();
            entry.putString("block", id.toString());
            entry.putLong("until", e.getValue());
            list.add(entry);
        }
        out.put("entries", list);
        if (masterExpiry != 0L) out.putLong("master", masterExpiry);
        return out;
    }

    public void deserializeNBT(CompoundTag tag) {
        expiryByBlock.clear();
        masterExpiry = tag.getLong("master"); // returns 0 if missing
        ListTag list = tag.getList("entries", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getString("block")));
            if (block != null) {
                expiryByBlock.put(block, entry.getLong("until"));
            }
        }
    }

    /** Resolve the capability for a player. Returns an empty optional if missing
     *  (which shouldn't happen post-attach but is safe to guard). */
    public static LazyOptional<OreSightTracker> get(Player player) {
        return player.getCapability(CAP);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Capability provider — attached to every player via OreSightEvents.
    // ─────────────────────────────────────────────────────────────────────

    public static final class Provider
            implements ICapabilitySerializable<CompoundTag>, ICapabilityProvider {
        private final OreSightTracker instance = new OreSightTracker();
        private final LazyOptional<OreSightTracker> handle = LazyOptional.of(() -> instance);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == CAP ? handle.cast() : LazyOptional.empty();
        }

        @Override public CompoundTag serializeNBT() { return instance.serializeNBT(); }
        @Override public void deserializeNBT(CompoundTag tag) { instance.deserializeNBT(tag); }

        public void invalidate() { handle.invalidate(); }
    }
}
