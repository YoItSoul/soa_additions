package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Consume Forge Energy through a Task Collector block. Progress is tracked in
 * units of {@link #UNIT} FE (1 kFE) so pack-scale targets like GreedyCraft's
 * 150,000,000,000 FE fit the engine's int progress counters.
 *
 * <p>JSON: {@code { "type": "soa_additions:energy", "value": 150000000000 }}
 * — {@code value} is raw FE.</p>
 */
public record EnergyTask(long value) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "energy");

    /** Raw FE per progress point. */
    public static final long UNIT = 1000;

    @Override public ResourceLocation type() { return TYPE; }

    @Override public int target() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, value / UNIT));
    }

    @Override public String describe() {
        return "Insert " + String.format("%,d", value) + " FE into a Task Collector";
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("value", value);
    }

    public static EnergyTask fromJson(JsonObject body) {
        return new EnergyTask(body.get("value").getAsLong());
    }
}
