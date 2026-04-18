package com.soul.soa_additions.quest.reward;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.RewardScope;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Grants a GameStages stage. Defaults to {@link RewardScope#TEAM} because a
 * progression stage applies to the whole party — solo stage grants on one
 * teammate would break co-op progression.
 */
public record GrantStageReward(String stage, RewardScope scope) implements QuestReward {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "grant_stage");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public String describe() { return "Unlock stage \"" + stage + "\""; }

    @Override public void grant(ServerPlayer player) {
        try {
            Class<?> helperCls = Class.forName("net.darkhax.gamestages.GameStageHelper");
            // GameStages 1.20.1: addStage(ServerPlayer, String...) — varargs,
            // reflected as String[].class.
            helperCls.getMethod("addStage", ServerPlayer.class, String[].class)
                    .invoke(null, player, new String[]{stage});
        } catch (ClassNotFoundException ignored) {
            // GameStages not installed: silently skip.
        } catch (ReflectiveOperationException e) {
            org.slf4j.LoggerFactory.getLogger("soa_additions/quest")
                    .warn("Failed to grant stage {}: {}", stage, e.getMessage());
        }
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("stage", stage);
        if (scope != RewardScope.TEAM) out.addProperty("scope", scope.lower());
    }

    public static GrantStageReward fromJson(JsonObject body) {
        return new GrantStageReward(
                body.get("stage").getAsString(),
                body.has("scope") ? RewardScope.fromString(body.get("scope").getAsString()) : RewardScope.TEAM);
    }
}
