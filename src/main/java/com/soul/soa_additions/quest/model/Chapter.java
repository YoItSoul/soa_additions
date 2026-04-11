package com.soul.soa_additions.quest.model;

import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.QuestSource;

import java.util.List;
import java.util.Set;

/**
 * A group of related quests. Chapters can form a hierarchy via
 * {@link #parentChapter} — a chapter whose parent is non-empty is rendered
 * as a sub-category in the quest book side pane, indented and with smaller
 * heading text based on its nesting depth.
 *
 * @param parentChapter id of the parent chapter, or empty string for top-level
 * @param source        where this chapter was loaded from (governs editor permissions)
 */
public record Chapter(
        String id,
        String title,
        List<String> description,
        String icon,
        int sortOrder,
        List<String> requiresChapters,
        List<String> requiresQuests,
        Visibility visibility,
        Set<PackMode> modes,
        List<Quest> quests,
        QuestSource source,
        String parentChapter
) {
    /** Compact constructor — normalise null parent to empty string. */
    public Chapter {
        if (parentChapter == null) parentChapter = "";
    }

    public boolean availableIn(PackMode mode) { return modes.contains(mode); }

    public boolean isEditable() { return source.isEditable(); }

    /** True if this chapter is a sub-category of another chapter. */
    public boolean hasParent() { return !parentChapter.isEmpty(); }
}
