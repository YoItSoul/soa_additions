package com.soul.soa_additions.nyx.event;

import com.soul.soa_additions.nyx.NyxConfig;
import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class BloodMoonEvent extends LunarEvent {

    public final Counter counter = new Counter(
            () -> NyxConfig.BLOOD_MOON_CHANCE.get(),
            () -> NyxConfig.BLOOD_MOON_START_NIGHT.get(),
            () -> NyxConfig.BLOOD_MOON_GRACE.get(),
            () -> NyxConfig.BLOOD_MOON_INTERVAL.get());

    public BloodMoonEvent(NyxWorldData data) { super("blood_moon", data); }

    @Override
    public Component getStartMessage() {
        return Component.translatable("info.soa_additions.blood_moon")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED).withItalic(true));
    }

    @Override
    public boolean shouldStart(boolean lastDaytime) {
        if (NyxConfig.BLOOD_MOON_ON_FULL.get() && level.getMoonBrightness() < 1.0f) return false;
        return lastDaytime && !NyxWorldData.isDaytime(level) && counter.canStart(data, this);
    }

    @Override
    public boolean shouldStop(boolean lastDaytime) { return NyxWorldData.isDaytime(level); }

    @Override public int getSkyColor() { return 4328707; }

    @Override public void update(boolean lastDaytime) { counter.update(data, this, lastDaytime); }
    @Override public CompoundTag serialize() { return counter.serialize(); }
    @Override public void deserialize(CompoundTag tag) { counter.deserialize(tag); }
}
