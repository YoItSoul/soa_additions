package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Transfer EMC into a Task Collector by right-clicking it with any ProjectE
 * EMC-holding item (Klein Stars). The EMC is drained from the item.
 *
 * <p>JSON: {@code { "type": "soa_additions:emc", "value": 80000000 }}</p>
 */
public record EmcTask(long value) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "emc");

    @Override public ResourceLocation type() { return TYPE; }

    @Override public int target() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, value));
    }

    @Override public String describe() {
        return "Transfer " + String.format("%,d", value) + " EMC into a Task Collector (Klein Star)";
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("value", value);
    }

    public static EmcTask fromJson(JsonObject body) {
        return new EmcTask(body.get("value").getAsLong());
    }
}
