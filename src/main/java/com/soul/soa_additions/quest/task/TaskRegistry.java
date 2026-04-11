package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry of quest task types. Add-ons or future contributors register new task
 * types with {@link #register(String, Function)} — no changes to the loader needed.
 *
 * <pre>{@code
 * TaskRegistry.register("forge_energy", body -> new ForgeEnergyTask(body.get("value").getAsInt()));
 * }</pre>
 *
 * The registry is populated in {@link #bootstrap()} from a static initializer on
 * first load, so built-in task types are always available.
 */
public final class TaskRegistry {

    private static final Map<ResourceLocation, Function<JsonObject, QuestTask>> DESERIALIZERS = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private TaskRegistry() {}

    /**
     * @param path path within the {@code soa_additions} namespace. Pass a full id
     *             (e.g. {@code othermod:my_task}) by calling {@link #register(ResourceLocation, Function)}.
     */
    public static void register(String path, Function<JsonObject, QuestTask> deserializer) {
        register(new ResourceLocation(SoaAdditions.MODID, path), deserializer);
    }

    public static void register(ResourceLocation id, Function<JsonObject, QuestTask> deserializer) {
        DESERIALIZERS.put(id, deserializer);
    }

    public static QuestTask deserialize(JsonObject body) {
        bootstrap();
        String type = body.get("type").getAsString();
        ResourceLocation id = type.contains(":") ? new ResourceLocation(type)
                : new ResourceLocation(SoaAdditions.MODID, type);
        Function<JsonObject, QuestTask> fn = DESERIALIZERS.get(id);
        if (fn == null) throw new IllegalArgumentException("Unknown task type: " + id);
        return fn.apply(body);
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        register("kill", KillTask::fromJson);
        register("item", ItemTask::fromJson);
        register("advancement", AdvancementTask::fromJson);
        register("dimension", DimensionTask::fromJson);
        register("stat", StatTask::fromJson);
        register("trigger", CustomTriggerTask::fromJson);
        register("checkmark", CheckmarkTask::fromJson);
        register("craft", CraftTask::fromJson);
        register("mine", MineTask::fromJson);
        register("place", PlaceTask::fromJson);
        register("tame", TameTask::fromJson);
        register("breed", BreedTask::fromJson);
        register("observe", ObserveTask::fromJson);
    }
}
