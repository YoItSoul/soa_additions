package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/** Kill {@code count} of {@code entity}. Matched on {@code LivingDeathEvent}. */
public record KillTask(ResourceLocation entity, int count) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "kill");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    @Override public String describe() {
        return "Kill " + count + "x " + TaskNames.entity(entity.toString());
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("entity", entity.toString());
        out.addProperty("count", count);
    }

    public static KillTask fromJson(JsonObject body) {
        return new KillTask(
                new ResourceLocation(body.get("entity").getAsString()),
                body.has("count") ? body.get("count").getAsInt() : 1);
    }
}
