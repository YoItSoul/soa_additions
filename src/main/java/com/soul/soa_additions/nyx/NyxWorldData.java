package com.soul.soa_additions.nyx;

import com.soul.soa_additions.nyx.event.BloodMoonEvent;
import com.soul.soa_additions.nyx.event.FullMoonEvent;
import com.soul.soa_additions.nyx.event.HarvestMoonEvent;
import com.soul.soa_additions.nyx.event.LunarEvent;
import com.soul.soa_additions.nyx.event.StarShowerEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-level Nyx state. Attached as SavedData (1.20 replacement for the world capability).
 * Tracks the current/forced lunar event, visited dimensions for meteor gating,
 * cached meteor positions in unloaded chunks, and per-chunk player-present ticks.
 */
public class NyxWorldData extends SavedData {

    public static final String NAME = "soa_nyx_world";

    public static float moonPhaseMultiplier = 0.0f;

    public final ServerLevel level;
    public final List<LunarEvent> lunarEvents = new ArrayList<>();
    public final Set<BlockPos> cachedMeteorPositions = new HashSet<>();
    public final Map<ChunkPos, int[]> playersPresentTicks = new HashMap<>();
    public final Set<BlockPos> meteorLandingSites = new HashSet<>();
    public final Set<String> visitedDimensions = new HashSet<>();
    public LunarEvent currentEvent;
    public LunarEvent forcedEvent;
    private boolean wasDaytime;

    public NyxWorldData(ServerLevel level) {
        this.level = level;
        this.lunarEvents.add(new HarvestMoonEvent(this));
        this.lunarEvents.add(new StarShowerEvent(this));
        this.lunarEvents.add(new BloodMoonEvent(this));
        this.lunarEvents.add(new FullMoonEvent(this));
    }

    public static NyxWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> loadFrom(tag, level),
                () -> new NyxWorldData(level),
                NAME);
    }

    public static float getMoonPhaseMultiplier() { return moonPhaseMultiplier; }

    public static boolean isDaytime(Level level) {
        long time = level.getDayTime() % 24000L;
        return time < 12541L || time >= 23458L;
    }

    public LunarEvent findEventByName(String name) {
        for (LunarEvent e : lunarEvents) if (e.name.equals(name)) return e;
        return null;
    }

    public void tick() {
        String dim = level.dimension().location().toString();
        List<? extends String> allowed = NyxConfig.ALLOWED_DIMENSIONS.get();
        if (level.getGameTime() % 200L == 0L) {
            for (ServerPlayer sp : level.getServer().getPlayerList().getPlayers()) {
                visitedDimensions.add(sp.level().dimension().location().toString());
            }
            setDirty();
        }
        if (NyxConfig.METEORS.get() && level.getGameTime() % 100L == 0L) {
            updatePlayersPresentTicks();
            setDirty();
        }
        if (!allowed.contains(dim)) return;

        moonPhaseMultiplier = level.getMoonBrightness();

        for (LunarEvent e : lunarEvents) e.update(wasDaytime);

        boolean dirty = false;
        if (currentEvent == null) {
            if (forcedEvent != null && forcedEvent.shouldStart(wasDaytime)) {
                currentEvent = forcedEvent;
                forcedEvent = null;
            } else {
                for (LunarEvent e : lunarEvents) {
                    if (e.shouldStart(wasDaytime)) { currentEvent = e; break; }
                }
            }
            if (currentEvent != null) {
                dirty = true;
                if (NyxConfig.EVENT_NOTIFICATIONS.get()) {
                    Component msg = currentEvent.getStartMessage();
                    for (ServerPlayer sp : level.players()) sp.sendSystemMessage(msg);
                }
            }
        }
        if (currentEvent != null && currentEvent.shouldStop(wasDaytime)) {
            currentEvent = null;
            dirty = true;
        }
        if (dirty) setDirty();
        wasDaytime = isDaytime(level);
    }

    private void updatePlayersPresentTicks() {
        int interval = 100;
        int radius = NyxConfig.METEOR_DISALLOW_RADIUS.get();
        Set<ChunkPos> remaining = new HashSet<>(playersPresentTicks.keySet());
        for (ServerPlayer p : level.players()) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    ChunkPos pos = new ChunkPos(
                            Mth.floor(p.getX() / 16.0) + x,
                            Mth.floor(p.getZ() / 16.0) + z);
                    int[] t = playersPresentTicks.computeIfAbsent(pos, k -> new int[]{0});
                    t[0] += interval;
                    remaining.remove(pos);
                }
            }
        }
        for (ChunkPos pos : remaining) {
            int[] t = playersPresentTicks.get(pos);
            t[0] -= interval;
            if (t[0] <= 0) playersPresentTicks.remove(pos);
        }
    }

    public double getMeteorChance() {
        ResourceLocation dim = level.dimension().location();
        if (dim.toString().equals("minecraft:the_end")) return NyxConfig.METEOR_CHANCE_END.get();
        if (!NyxConfig.ALLOWED_DIMENSIONS.get().contains(dim.toString())) return 0.0;
        boolean visitedGate = visitedDimensions.contains(NyxConfig.METEOR_GATE_DIMENSION.get());
        if (!isDaytime(level)) {
            if (currentEvent instanceof StarShowerEvent) return NyxConfig.METEOR_CHANCE_STAR_SHOWER.get();
            return visitedGate ? NyxConfig.METEOR_CHANCE_AFTER_GATE_NIGHT.get() : NyxConfig.METEOR_CHANCE_NIGHT.get();
        }
        return visitedGate ? NyxConfig.METEOR_CHANCE_AFTER_GATE.get() : NyxConfig.METEOR_CHANCE.get();
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        if (currentEvent != null) root.putString("event", currentEvent.name);
        root.putBoolean("was_daytime", wasDaytime);
        for (LunarEvent e : lunarEvents) root.put(e.name, e.serialize());
        ListTag landings = new ListTag();
        for (BlockPos p : meteorLandingSites) landings.add(LongTag.valueOf(p.asLong()));
        root.put("meteor_landings", landings);
        ListTag meteors = new ListTag();
        for (BlockPos p : cachedMeteorPositions) meteors.add(LongTag.valueOf(p.asLong()));
        root.put("cached_meteors", meteors);
        ListTag ticks = new ListTag();
        for (Map.Entry<ChunkPos, int[]> e : playersPresentTicks.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putInt("x", e.getKey().x);
            c.putInt("z", e.getKey().z);
            c.putInt("ticks", e.getValue()[0]);
            ticks.add(c);
        }
        root.put("players_present_ticks", ticks);
        ListTag dims = new ListTag();
        for (String d : visitedDimensions) dims.add(StringTag.valueOf(d));
        root.put("visited_dims", dims);
        return root;
    }

    private static NyxWorldData loadFrom(CompoundTag tag, ServerLevel level) {
        NyxWorldData data = new NyxWorldData(level);
        String eventName = tag.getString("event");
        data.currentEvent = data.findEventByName(eventName);
        data.wasDaytime = tag.getBoolean("was_daytime");
        for (LunarEvent e : data.lunarEvents) e.deserialize(tag.getCompound(e.name));
        for (Tag t : tag.getList("meteor_landings", Tag.TAG_LONG)) {
            data.meteorLandingSites.add(BlockPos.of(((LongTag) t).getAsLong()));
        }
        for (Tag t : tag.getList("cached_meteors", Tag.TAG_LONG)) {
            data.cachedMeteorPositions.add(BlockPos.of(((LongTag) t).getAsLong()));
        }
        for (Tag t : tag.getList("players_present_ticks", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            data.playersPresentTicks.put(
                    new ChunkPos(c.getInt("x"), c.getInt("z")),
                    new int[]{c.getInt("ticks")});
        }
        for (Tag t : tag.getList("visited_dims", Tag.TAG_STRING)) {
            data.visitedDimensions.add(t.getAsString());
        }
        return data;
    }
}
