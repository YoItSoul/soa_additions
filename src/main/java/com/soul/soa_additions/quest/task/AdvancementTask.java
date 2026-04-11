package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/** Earn the given advancement. Driven by {@code AdvancementEvent.AdvancementEarnEvent}. */
public record AdvancementTask(ResourceLocation advancement) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "advancement");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return 1; }
    @Override public String describe() { return "Earn advancement: " + TaskNames.advancement(advancement.toString()); }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("advancement", advancement.toString());
    }

    public static AdvancementTask fromJson(JsonObject body) {
        return new AdvancementTask(new ResourceLocation(body.get("advancement").getAsString()));
    }
}
