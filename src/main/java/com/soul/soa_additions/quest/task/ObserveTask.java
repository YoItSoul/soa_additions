package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Look at a block or entity. Auto-detects which registry the {@code id}
 * id belongs to — if it's a known block, the task is satisfied by raytracing
 * a block of that type; otherwise the entity registry is checked. The
 * {@link com.soul.soa_additions.quest.events.ObserveTaskPoller poller} runs
 * once per second per player and skips entirely if no active quest references
 * an observe task, so the per-tick overhead is zero for most players.
 *
 * <p>{@code reach} is the raytrace distance in blocks (default 32) — bumped
 * above the vanilla interaction reach so players can spot bedrock from a
 * distance without standing on top of it.</p>
 */
public record ObserveTask(ResourceLocation id, int count, double reach) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "observe");
    public static final double DEFAULT_REACH = 32.0;

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    public boolean isBlock() { return BuiltInRegistries.BLOCK.containsKey(id); }
    public boolean isEntity() { return !isBlock() && BuiltInRegistries.ENTITY_TYPE.containsKey(id); }

    @Override public String describe() {
        String pretty = isBlock() ? TaskNames.block(id.toString()) : TaskNames.entity(id.toString());
        String what = isBlock() ? "block" : "creature";
        return count > 1
                ? "Observe " + count + "x " + pretty + " " + what
                : "Observe a " + pretty;
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("target", id.toString());
        if (count != 1) out.addProperty("count", count);
        if (reach != DEFAULT_REACH) out.addProperty("reach", reach);
    }

    public static ObserveTask fromJson(JsonObject body) {
        ResourceLocation id = new ResourceLocation(body.get("target").getAsString());
        int c = body.has("count") ? body.get("count").getAsInt() : 1;
        double r = body.has("reach") ? body.get("reach").getAsDouble() : DEFAULT_REACH;
        return new ObserveTask(id, c, r);
    }
}
