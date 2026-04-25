package com.soul.soa_additions.tr.knowledge;

import com.soul.soa_additions.tr.core.Aspect;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Client-side mirror of the player's known-aspect set, populated by
 * {@link com.soul.soa_additions.tr.network.KnownAspectsSyncPacket}. Read by
 * client UI and JEI integration to gate visibility.
 */
public final class ClientKnownAspects {

    private static final Set<ResourceLocation> KNOWN = new LinkedHashSet<>();

    /** Listeners for discovery-set changes — JEI plugin subscribes to refresh
     *  its hidden-ingredient list when a new aspect comes in. */
    private static final java.util.List<Runnable> LISTENERS = new java.util.concurrent.CopyOnWriteArrayList<>();

    private ClientKnownAspects() {}

    public static void replace(Set<ResourceLocation> next) {
        KNOWN.clear();
        KNOWN.addAll(next);
        for (Runnable l : LISTENERS) {
            try { l.run(); } catch (Throwable ignored) {}
        }
    }

    public static boolean has(Aspect a) { return KNOWN.contains(a.id()); }
    public static boolean has(ResourceLocation id) { return KNOWN.contains(id); }
    public static Set<ResourceLocation> snapshot() { return Collections.unmodifiableSet(new LinkedHashSet<>(KNOWN)); }
    public static int size() { return KNOWN.size(); }

    public static void addListener(Runnable r) { LISTENERS.add(r); }
}
