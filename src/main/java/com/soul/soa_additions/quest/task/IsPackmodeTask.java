package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

/**
 * Completes when the server's current pack mode matches the specified value.
 * Polled alongside dimension tasks (every 20 ticks) so it picks up the mode
 * almost immediately after it's set — whether by server config, command, or
 * player choice.
 *
 * <p>Use this to gate pack-mode-specific quest branches:</p>
 * <pre>{@code
 * {
 *   "type": "soa_additions:is_packmode",
 *   "mode": "expert"
 * }
 * }</pre>
 */
public record IsPackmodeTask(PackMode mode) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "is_packmode");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return 1; }
    @Override public String describe() { return "Pack mode: " + mode.lower(); }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("mode", mode.lower());
    }

    public static IsPackmodeTask fromJson(JsonObject body) {
        return new IsPackmodeTask(PackMode.fromString(body.get("mode").getAsString()));
    }
}
