package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Deposit experience <em>points</em> (not levels) by right-clicking a Task
 * Collector block with an empty hand. Matches GreedyCraft's FTBQ {@code xp}
 * task semantics: the XP is consumed.
 *
 * <p>JSON: {@code { "type": "soa_additions:xp", "value": 2500000 }}</p>
 */
public record XpTask(long value) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "xp");

    @Override public ResourceLocation type() { return TYPE; }

    @Override public int target() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, value));
    }

    @Override public String describe() {
        return "Deposit " + String.format("%,d", value) + " XP at a Task Collector";
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("value", value);
    }

    public static XpTask fromJson(JsonObject body) {
        return new XpTask(body.get("value").getAsLong());
    }
}
