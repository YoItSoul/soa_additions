package com.soul.soa_additions.nyx.event;

import com.soul.soa_additions.nyx.NyxConfig;
import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class FullMoonEvent extends LunarEvent {
    public FullMoonEvent(NyxWorldData data) { super("full_moon", data); }

    @Override
    public Component getStartMessage() {
        return Component.translatable("info.soa_additions.full_moon")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
    }

    @Override
    public boolean shouldStart(boolean lastDaytime) {
        if (!NyxConfig.FULL_MOON.get()) return false;
        return lastDaytime && !NyxWorldData.isDaytime(level) && level.getMoonBrightness() >= 1.0f;
    }

    @Override
    public boolean shouldStop(boolean lastDaytime) { return NyxWorldData.isDaytime(level); }
}
