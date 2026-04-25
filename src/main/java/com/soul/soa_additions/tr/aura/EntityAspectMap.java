package com.soul.soa_additions.tr.aura;

import com.soul.soa_additions.tr.core.AspectStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity → aspect composition lookup. Mirrors {@link com.soul.soa_additions.tr.core.AspectMap}
 * for items/blocks but keyed by entity-type ResourceLocation. Read by
 * {@link AuraImpartHandler} on mob death to figure out what aspects to
 * impart into the chunk's aura, and (eventually) by the Monocle's mob-scan
 * tooltip to display a creature's aspect makeup.
 *
 * <p>Loaded by {@link com.soul.soa_additions.tr.data.AspectMapLoader}'s
 * EntityListener from {@code data/<ns>/tr_aspects/entity/<path>.json}, same
 * JSON shape as items.
 */
public final class EntityAspectMap {

    private static final Map<ResourceLocation, List<AspectStack>> ENTRIES = new HashMap<>();
    private static final List<AspectStack> EMPTY = Collections.emptyList();

    private EntityAspectMap() {}

    public static void put(ResourceLocation entityId, List<AspectStack> stacks) {
        ENTRIES.put(entityId, List.copyOf(stacks));
    }

    public static void clear() { ENTRIES.clear(); }

    public static int size() { return ENTRIES.size(); }

    public static List<AspectStack> forEntity(@Nullable Entity entity) {
        if (entity == null) return EMPTY;
        return forType(entity.getType());
    }

    public static List<AspectStack> forType(@Nullable EntityType<?> type) {
        if (type == null) return EMPTY;
        return forId(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static List<AspectStack> forId(@Nullable ResourceLocation id) {
        if (id == null) return EMPTY;
        return ENTRIES.getOrDefault(id, EMPTY);
    }
}
