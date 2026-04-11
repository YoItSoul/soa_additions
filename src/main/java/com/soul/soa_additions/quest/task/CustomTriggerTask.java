package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Fired by external code calling {@code QuestEngine.fireTrigger(player, triggerId)}.
 * Use this for boss deaths, structure entry, custom rituals, etc. — anything the
 * built-in task types don't cover. Set {@code count} if it must fire multiple times.
 */
public record CustomTriggerTask(ResourceLocation triggerId, int count, String label) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "trigger");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }
    @Override public String describe() {
        return (label.isEmpty() ? triggerId.toString() : label) + (count > 1 ? " x" + count : "");
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("trigger", triggerId.toString());
        if (count > 1) out.addProperty("count", count);
        if (!label.isEmpty()) out.addProperty("label", label);
    }

    public static CustomTriggerTask fromJson(JsonObject body) {
        return new CustomTriggerTask(
                new ResourceLocation(body.get("trigger").getAsString()),
                body.has("count") ? body.get("count").getAsInt() : 1,
                body.has("label") ? body.get("label").getAsString() : "");
    }
}
