package com.soul.soa_additions.quest.reward;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.RewardScope;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record XpReward(int amount, boolean levels, RewardScope scope) implements QuestReward {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "xp");

    @Override public ResourceLocation type() { return TYPE; }

    @Override public String describe() {
        return "Gain +" + amount + (levels ? " levels" : " XP") + (scope == RewardScope.TEAM ? " (team)" : "");
    }

    @Override public void grant(ServerPlayer player) {
        if (levels) player.giveExperienceLevels(amount);
        else player.giveExperiencePoints(amount);
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("amount", amount);
        if (levels) out.addProperty("levels", true);
        if (scope != RewardScope.PLAYER) out.addProperty("scope", scope.lower());
    }

    public static XpReward fromJson(JsonObject body) {
        return new XpReward(
                body.get("amount").getAsInt(),
                body.has("levels") && body.get("levels").getAsBoolean(),
                body.has("scope") ? RewardScope.fromString(body.get("scope").getAsString()) : RewardScope.PLAYER);
    }
}
