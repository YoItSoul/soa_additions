package com.soul.soa_additions.quest.model;

import com.google.gson.JsonObject;
import com.soul.soa_additions.quest.task.TaskRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * A single requirement on a quest. Task instances are pure data — progress is
 * tracked per-player elsewhere (see {@code PlayerQuestData}). Subclasses register
 * their deserializer in {@link TaskRegistry}.
 */
public interface QuestTask {

    /** Stable type id like {@code soa_additions:kill}. Written to JSON. */
    ResourceLocation type();

    /**
     * Target value the player's progress counter must reach to complete this task.
     * A kill task returning 5 means "kill 5 of the entity".
     */
    int target();

    /**
     * Human-readable one-line summary for the quest screen ("Kill 5 Zombies").
     */
    String describe();

    /**
     * Serializes the task body (not the type field) to JSON for programmatic export
     * and the in-game editor (v2).
     */
    void writeJson(JsonObject out);
}
