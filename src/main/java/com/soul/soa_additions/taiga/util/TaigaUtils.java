package com.soul.soa_additions.taiga.util;

import net.minecraft.world.level.Level;

/**
 * Shared helpers for TAIGA trait ports. Carries over the handful of static
 * utilities that 1.12.2 com.sosnitzka.taiga.util.Utils exposed — mainly
 * day/night detection against the world daytime clock.
 */
public final class TaigaUtils {

    private TaigaUtils() {}

    public static boolean isNight(long dayTime) {
        long t = dayTime % 24000L;
        return t >= 13000L && t < 23000L;
    }

    public static boolean isNight(Level level) {
        return isNight(level.getDayTime());
    }
}
