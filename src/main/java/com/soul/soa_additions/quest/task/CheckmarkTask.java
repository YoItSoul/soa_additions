package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/** Manual completion: player clicks a button on the quest screen. Good for story beats. */
public record CheckmarkTask(String text) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "checkmark");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return 1; }
    @Override public String describe() { return text.isEmpty() ? "Acknowledge" : text; }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        if (!text.isEmpty()) out.addProperty("text", text);
    }

    public static CheckmarkTask fromJson(JsonObject body) {
        return new CheckmarkTask(body.has("text") ? body.get("text").getAsString() : "");
    }
}
