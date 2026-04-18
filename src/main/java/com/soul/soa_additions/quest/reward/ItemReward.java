package com.soul.soa_additions.quest.reward;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.RewardScope;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemReward(ResourceLocation item, int count, RewardScope scope) implements QuestReward {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "item");

    @Override public ResourceLocation type() { return TYPE; }

    @Override public String describe() {
        return "Receive " + count + "x " + com.soul.soa_additions.quest.task.TaskNames.item(item.toString())
                + (scope == RewardScope.TEAM ? " (team)" : "");
    }

    @Override public void grant(ServerPlayer player) {
        Item it = BuiltInRegistries.ITEM.get(item);
        if (it == null || it == net.minecraft.world.item.Items.AIR) return;
        ItemStack stack = new ItemStack(it, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("item", item.toString());
        out.addProperty("count", count);
        if (scope != RewardScope.PLAYER) out.addProperty("scope", scope.lower());
    }

    public static ItemReward fromJson(JsonObject body) {
        return new ItemReward(
                new ResourceLocation(body.get("item").getAsString()),
                body.has("count") ? body.get("count").getAsInt() : 1,
                body.has("scope") ? RewardScope.fromString(body.get("scope").getAsString()) : RewardScope.PLAYER);
    }
}
