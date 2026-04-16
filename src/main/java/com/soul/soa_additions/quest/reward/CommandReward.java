package com.soul.soa_additions.quest.reward;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.RewardScope;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record CommandReward(String command, RewardScope scope) implements QuestReward {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "command");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public String describe() { return "Run: " + command + (scope == RewardScope.TEAM ? " (team)" : ""); }

    @Override public void grant(ServerPlayer player) {
        String cmd = command
                .replace("{player}", player.getGameProfile().getName())
                .replace("{uuid}", player.getUUID().toString());
        // Defer to the next tick so a reward command that re-enters the quest
        // pipeline (e.g. /soa quests claim, /soa quests task complete) can't
        // recurse through the dispatcher inside the current claim frame.
        var server = player.getServer();
        CommandSourceStack src = server.createCommandSourceStack().withPermission(4);
        server.execute(() -> server.getCommands().performPrefixedCommand(src, cmd));
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("command", command);
        if (scope != RewardScope.PLAYER) out.addProperty("scope", scope.lower());
    }

    public static CommandReward fromJson(JsonObject body) {
        return new CommandReward(
                body.get("command").getAsString(),
                body.has("scope") ? RewardScope.fromString(body.get("scope").getAsString()) : RewardScope.PLAYER);
    }
}
