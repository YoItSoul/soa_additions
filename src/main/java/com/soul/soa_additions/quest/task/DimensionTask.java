package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/** Enter the given dimension at least once. */
public record DimensionTask(ResourceLocation dimension) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "dimension");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return 1; }
    @Override public String describe() { return "Enter dimension: " + TaskNames.dimension(dimension.toString()); }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("dimension", dimension.toString());
    }

    public static DimensionTask fromJson(JsonObject body) {
        return new DimensionTask(new ResourceLocation(body.get("dimension").getAsString()));
    }
}
