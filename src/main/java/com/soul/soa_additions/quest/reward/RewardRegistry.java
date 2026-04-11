package com.soul.soa_additions.quest.reward;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestReward;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Mirror of {@link com.soul.soa_additions.quest.task.TaskRegistry} for rewards. */
public final class RewardRegistry {

    private static final Map<ResourceLocation, Function<JsonObject, QuestReward>> DESERIALIZERS = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private RewardRegistry() {}

    public static void register(String path, Function<JsonObject, QuestReward> fn) {
        register(new ResourceLocation(SoaAdditions.MODID, path), fn);
    }

    public static void register(ResourceLocation id, Function<JsonObject, QuestReward> fn) {
        DESERIALIZERS.put(id, fn);
    }

    public static QuestReward deserialize(JsonObject body) {
        bootstrap();
        String type = body.get("type").getAsString();
        ResourceLocation id = type.contains(":") ? new ResourceLocation(type)
                : new ResourceLocation(SoaAdditions.MODID, type);
        Function<JsonObject, QuestReward> fn = DESERIALIZERS.get(id);
        if (fn == null) throw new IllegalArgumentException("Unknown reward type: " + id);
        return fn.apply(body);
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        register("item", ItemReward::fromJson);
        register("xp", XpReward::fromJson);
        register("command", CommandReward::fromJson);
        register("grant_stage", GrantStageReward::fromJson);
        register("lock_packmode", LockPackmodeReward::fromJson);
    }
}
