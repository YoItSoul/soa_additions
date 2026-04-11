package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Place {@code count} of a specific block. Driven by
 * {@code BlockEvent.EntityPlaceEvent} on the server — no polling needed.
 */
public record PlaceTask(ResourceLocation block, int count) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "place");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    @Override public String describe() {
        return "Place " + count + "x " + TaskNames.block(block.toString());
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("block", block.toString());
        out.addProperty("count", count);
    }

    public static PlaceTask fromJson(JsonObject body) {
        return new PlaceTask(
                new ResourceLocation(body.get("block").getAsString()),
                body.has("count") ? body.get("count").getAsInt() : 1);
    }
}
