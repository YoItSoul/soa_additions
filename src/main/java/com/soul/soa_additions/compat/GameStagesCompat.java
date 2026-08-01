package com.soul.soa_additions.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

/**
 * Reflective bridge to Darkhax's GameStages. The mod is a soft dependency and
 * is not on the compile classpath, so every call goes through
 * {@code GameStageHelper} by name and degrades to a no-op when it is absent.
 */
public final class GameStagesCompat {

    private static final String HELPER = "net.darkhax.gamestages.GameStageHelper";

    private GameStagesCompat() {}

    public static boolean hasStage(Player player, String stage) {
        try {
            Class<?> helper = Class.forName(HELPER);
            return (boolean) helper.getMethod("hasStage", Player.class, String.class)
                    .invoke(null, player, stage);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /** GameStages 1.20.1 takes varargs, reflected as {@code String[].class}. */
    public static boolean addStage(Player player, String stage) {
        return invokeStage("addStage", player, stage);
    }

    public static boolean removeStage(Player player, String stage) {
        return invokeStage("removeStage", player, stage);
    }

    private static boolean invokeStage(String name, Player player, String stage) {
        if (!(player instanceof ServerPlayer sp)) return false;
        try {
            Class<?> helper = Class.forName(HELPER);
            Method m = helper.getMethod(name, ServerPlayer.class, String[].class);
            m.invoke(null, sp, new String[]{stage});
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            org.slf4j.LoggerFactory.getLogger("soa_additions/gamestages")
                    .warn("GameStages {} failed for stage {}: {}", name, stage, e.getMessage());
            return false;
        }
    }
}
