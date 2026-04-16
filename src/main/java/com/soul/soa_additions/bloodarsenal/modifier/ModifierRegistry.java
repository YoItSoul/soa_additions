package com.soul.soa_additions.bloodarsenal.modifier;

import java.util.*;

/**
 * Static registry of all stasis tool modifiers and their incompatibility rules.
 * Modifiers are registered during {@link com.soul.soa_additions.bloodarsenal.BloodArsenalPlugin#init}.
 * Full population in Phase 5.
 */
public final class ModifierRegistry {

    private static final Map<String, Modifier> MODIFIERS = new LinkedHashMap<>();
    private static final Map<String, Set<String>> INCOMPATIBLE = new HashMap<>();

    private ModifierRegistry() {}

    public static void register(Modifier modifier) {
        MODIFIERS.put(modifier.getKey(), modifier);
    }

    public static void addIncompatibility(String key1, String key2) {
        INCOMPATIBLE.computeIfAbsent(key1, k -> new HashSet<>()).add(key2);
        INCOMPATIBLE.computeIfAbsent(key2, k -> new HashSet<>()).add(key1);
    }

    public static Modifier get(String key) {
        return MODIFIERS.get(key);
    }

    public static Collection<Modifier> getAll() {
        return Collections.unmodifiableCollection(MODIFIERS.values());
    }

    public static Set<String> getIncompatible(String key) {
        return INCOMPATIBLE.getOrDefault(key, Collections.emptySet());
    }

    public static boolean areIncompatible(String key1, String key2) {
        Set<String> set = INCOMPATIBLE.get(key1);
        return set != null && set.contains(key2);
    }
}
