package com.soul.soa_additions.quest.reward;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.PackModeData;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.RewardScope;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Locks the world's packmode permanently. Place this on the first meaningful
 * quest in Getting Started. Always TEAM-scoped — the mode applies to the whole
 * world, so the concept of a per-player lock is nonsensical.
 */
public record LockPackmodeReward() implements QuestReward {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "lock_packmode");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public String describe() { return "Lock the world's difficulty in"; }
    @Override public RewardScope scope() { return RewardScope.TEAM; }

    @Override public void grant(ServerPlayer player) {
        PackModeData data = PackModeData.get(player.server);
        boolean wasLocked = data.locked();
        data.lock();
        if (!wasLocked) {
            player.sendSystemMessage(Component.literal("⚑ Pack mode has been locked for this world.")
                    .withStyle(ChatFormatting.GOLD));
            org.slf4j.LoggerFactory.getLogger("soa_additions/quest")
                    .info("Pack mode locked by {} via quest reward (mode={})",
                            player.getGameProfile().getName(), data.mode());
        }
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
    }

    public static LockPackmodeReward fromJson(JsonObject body) {
        return new LockPackmodeReward();
    }
}
