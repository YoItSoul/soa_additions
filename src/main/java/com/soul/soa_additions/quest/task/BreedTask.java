package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Breed {@code count} of a specific entity. Driven by Forge's
 * {@code BabyEntitySpawnEvent}, which fires after two parents successfully
 * produce a child — the parent entity type is used for matching.
 */
public record BreedTask(ResourceLocation entity, int count) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "breed");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    @Override public String describe() {
        return "Breed " + count + "x " + TaskNames.entity(entity.toString());
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("entity", entity.toString());
        out.addProperty("count", count);
    }

    public static BreedTask fromJson(JsonObject body) {
        return new BreedTask(
                new ResourceLocation(body.get("entity").getAsString()),
                body.has("count") ? body.get("count").getAsInt() : 1);
    }
}
