package com.soul.soa_additions.nyx.event;

import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.function.IntSupplier;
import java.util.function.DoubleSupplier;

public abstract class LunarEvent {

    public final String name;
    protected final NyxWorldData data;
    protected final ServerLevel level;

    public LunarEvent(String name, NyxWorldData data) {
        this.name = name;
        this.data = data;
        this.level = data.level;
    }

    public abstract Component getStartMessage();
    public abstract boolean shouldStart(boolean lastDaytime);
    public abstract boolean shouldStop(boolean lastDaytime);

    public int getSkyColor() { return 0; }
    public void update(boolean lastDaytime) {}

    public CompoundTag serialize() { return new CompoundTag(); }
    public void deserialize(CompoundTag tag) {}

    /** Counter helper mirroring nyx's ConfigImpl - tracks start-night, grace, and interval. */
    public static class Counter {
        public final DoubleSupplier chance;
        public final IntSupplier startNight;
        public final IntSupplier graceDays;
        public final IntSupplier nightInterval;
        public int daysSinceLast;
        public int startDays;
        public int grace;

        public Counter(DoubleSupplier chance, IntSupplier startNight, IntSupplier graceDays, IntSupplier nightInterval) {
            this.chance = chance;
            this.startNight = startNight;
            this.graceDays = graceDays;
            this.nightInterval = nightInterval;
        }

        public void update(NyxWorldData data, LunarEvent owner, boolean lastDaytime) {
            if (data.currentEvent == owner) { daysSinceLast = 0; grace = 0; }
            if (!lastDaytime && NyxWorldData.isDaytime(data.level)) {
                daysSinceLast++;
                if (startDays < startNight.getAsInt()) startDays++;
                if (grace < graceDays.getAsInt()) grace++;
            }
        }

        public boolean canStart(NyxWorldData data, LunarEvent owner) {
            if (data.forcedEvent == owner) return true;
            if (startDays < startNight.getAsInt()) return false;
            if (grace < graceDays.getAsInt()) return false;
            if (nightInterval.getAsInt() > 0) return daysSinceLast >= nightInterval.getAsInt();
            return data.level.random.nextDouble() <= chance.getAsDouble();
        }

        public CompoundTag serialize() {
            CompoundTag c = new CompoundTag();
            c.putInt("days_since_last", daysSinceLast);
            c.putInt("start_days", startDays);
            c.putInt("grace_days", grace);
            return c;
        }

        public void deserialize(CompoundTag c) {
            daysSinceLast = c.getInt("days_since_last");
            startDays = c.getInt("start_days");
            grace = c.getInt("grace_days");
        }
    }
}
