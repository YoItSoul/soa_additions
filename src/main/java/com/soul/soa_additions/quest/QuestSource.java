package com.soul.soa_additions.quest;

/**
 * Where a loaded chapter/quest originated from. Drives editor permissions —
 * only {@link #WORLD_EDITS} is writable in-game; everything else is source-
 * controlled and must be forked into an override if a player wants to change it.
 */
public enum QuestSource {
    /** Shipped inside soa_additions.jar at {@code resources/data/soa_additions/quests/...}. */
    BUILTIN_JSON,

    /** From a datapack on disk (vanilla datapacks, KubeJS, etc.). */
    DATAPACK,

    /** Authored in Java via {@link com.soul.soa_additions.quest.builder.Quests}. */
    PROGRAMMATIC,

    /**
     * Written by the in-game editor into the world save under
     * {@code <world>/soa_additions/quest_edits/}. Fully mutable at runtime.
     */
    WORLD_EDITS;

    public boolean isEditable() {
        return this == WORLD_EDITS;
    }
}
