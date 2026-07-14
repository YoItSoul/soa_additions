package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;

/**
 * Completes when the player has a GameStages stage. Lets a quest surface a
 * pack-level gate (e.g. Malum's cluster_of_brilliance is item-staged behind
 * "skilled_wizard") instead of hiding it behind an unrelated dependency.
 *
 * Polled from QuestEventBridge alongside the dimension/packmode condition
 * tasks. GameStages is reached via reflection, mirroring GrantStageReward —
 * no hard dependency, silently never-completes when the mod is absent.
 */
public record GameStageTask(String stage, String label) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "gamestage");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return 1; }
    @Override public String describe() {
        return label.isEmpty() ? "Unlock game stage \"" + stage + "\"" : label;
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("stage", stage);
        if (!label.isEmpty()) out.addProperty("label", label);
    }

    public static GameStageTask fromJson(JsonObject body) {
        return new GameStageTask(
                body.get("stage").getAsString(),
                body.has("label") ? body.get("label").getAsString() : "");
    }

    // ── reflection bridge (resolved once) ──

    private static volatile Method hasStageMethod;
    private static volatile boolean resolved;

    public static boolean hasStage(ServerPlayer player, String stage) {
        if (!resolved) {
            synchronized (GameStageTask.class) {
                if (!resolved) {
                    try {
                        Class<?> helper = Class.forName("net.darkhax.gamestages.GameStageHelper");
                        for (Method m : helper.getMethods()) {
                            if (!m.getName().equals("hasStage")) continue;
                            Class<?>[] p = m.getParameterTypes();
                            if (p.length == 2 && p[0].isAssignableFrom(ServerPlayer.class)
                                    && p[1] == String.class) {
                                hasStageMethod = m;
                                break;
                            }
                        }
                    } catch (ClassNotFoundException ignored) {
                        // GameStages not installed: task can never complete.
                    }
                    resolved = true;
                }
            }
        }
        Method m = hasStageMethod;
        if (m == null) return false;
        try {
            return (Boolean) m.invoke(null, player, stage);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
