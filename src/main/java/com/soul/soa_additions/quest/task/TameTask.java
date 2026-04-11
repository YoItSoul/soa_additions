package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Tame {@code count} of a specific entity. Driven by Forge's
 * {@code AnimalTameEvent} — fires once per successful tame.
 */
public record TameTask(ResourceLocation entity, int count) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "tame");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    @Override public String describe() {
        return "Tame " + count + "x " + TaskNames.entity(entity.toString());
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("entity", entity.toString());
        out.addProperty("count", count);
    }

    public static TameTask fromJson(JsonObject body) {
        return new TameTask(
                new ResourceLocation(body.get("entity").getAsString()),
                body.has("count") ? body.get("count").getAsInt() : 1);
    }
}
