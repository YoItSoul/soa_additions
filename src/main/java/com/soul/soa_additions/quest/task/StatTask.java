package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Reach a value in a vanilla stat. {@code statType} is a stat type id like
 * {@code minecraft:mined}, {@code statValue} is the stat value id (an item/block/entity).
 */
public record StatTask(ResourceLocation statType, ResourceLocation statValue, int threshold) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "stat");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return threshold; }
    @Override public String describe() {
        return "Reach " + threshold + " " + statType + "/" + statValue;
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("stat_type", statType.toString());
        out.addProperty("stat_value", statValue.toString());
        out.addProperty("threshold", threshold);
    }

    public static StatTask fromJson(JsonObject body) {
        return new StatTask(
                new ResourceLocation(body.get("stat_type").getAsString()),
                new ResourceLocation(body.get("stat_value").getAsString()),
                body.get("threshold").getAsInt());
    }
}
