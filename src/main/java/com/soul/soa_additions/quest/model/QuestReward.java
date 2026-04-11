package com.soul.soa_additions.quest.model;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * A reward granted when a quest is claimed. Reward execution has direct access to
 * the player — stage grants, packmode locks, and command execution all live here.
 */
public interface QuestReward {

    ResourceLocation type();

    /**
     * Short description for the quest screen ("+500 XP", "Iron Ingot x8").
     */
    String describe();

    /** Apply this reward to the player. Called once, server-side. */
    void grant(ServerPlayer player);

    void writeJson(JsonObject out);

    /**
     * Whether this reward is granted per-player (each teammate claims their own)
     * or per-team (first claim fires for everyone). Defaults to {@link RewardScope#PLAYER}.
     */
    default RewardScope scope() { return RewardScope.PLAYER; }
}
