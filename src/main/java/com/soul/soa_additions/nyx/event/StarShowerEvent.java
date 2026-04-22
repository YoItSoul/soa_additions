package com.soul.soa_additions.nyx.event;

import com.soul.soa_additions.nyx.NyxConfig;
import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class StarShowerEvent extends LunarEvent {

    public final Counter counter = new Counter(
            () -> NyxConfig.STAR_SHOWER_CHANCE.get(),
            () -> NyxConfig.STAR_SHOWER_START_NIGHT.get(),
            () -> NyxConfig.STAR_SHOWER_GRACE.get(),
            () -> NyxConfig.STAR_SHOWER_INTERVAL.get());

    public StarShowerEvent(NyxWorldData data) { super("star_shower", data); }

    @Override
    public Component getStartMessage() {
        return Component.translatable("info.soa_additions.star_shower")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(true));
    }

    @Override
    public boolean shouldStart(boolean lastDaytime) {
        return lastDaytime && !NyxWorldData.isDaytime(level) && counter.canStart(data, this);
    }

    @Override
    public boolean shouldStop(boolean lastDaytime) { return NyxWorldData.isDaytime(level); }

    @Override public int getSkyColor() { return 14598751; }

    @Override public void update(boolean lastDaytime) { counter.update(data, this, lastDaytime); }
    @Override public CompoundTag serialize() { return counter.serialize(); }
    @Override public void deserialize(CompoundTag tag) { counter.deserialize(tag); }
}
