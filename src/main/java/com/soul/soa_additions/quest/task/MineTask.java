package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Break {@code count} of {@code block}. Lifetime counter driven by
 * {@code BlockEvent.BreakEvent}. Silk-touch breaks still count — we match
 * on the block id, not the dropped item, so "mine 10 iron ore" completes
 * whether the player smelts the drops or keeps the ore block.
 */
public record MineTask(ResourceLocation block, int count) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "mine");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    @Override public String describe() {
        return "Mine " + count + "x " + TaskNames.block(block.toString());
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("block", block.toString());
        out.addProperty("count", count);
    }

    public static MineTask fromJson(JsonObject body) {
        return new MineTask(
                new ResourceLocation(body.get("block").getAsString()),
                body.has("count") ? body.get("count").getAsInt() : 1);
    }
}
