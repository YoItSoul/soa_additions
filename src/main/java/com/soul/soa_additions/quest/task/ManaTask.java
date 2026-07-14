package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Channel Botania mana into a Task Collector — point a Mana Spreader at the
 * block (it is a burst receiver), or right-click it with a mana-holding item
 * (Mana Tablet) to drain it.
 *
 * <p>JSON: {@code { "type": "soa_additions:mana", "value": 10000000 }}</p>
 */
public record ManaTask(long value) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "mana");

    @Override public ResourceLocation type() { return TYPE; }

    @Override public int target() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, value));
    }

    @Override public String describe() {
        return "Channel " + String.format("%,d", value) + " Mana into a Task Collector";
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("value", value);
    }

    public static ManaTask fromJson(JsonObject body) {
        return new ManaTask(body.get("value").getAsLong());
    }
}
