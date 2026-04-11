package com.soul.soa_additions.quest.model;

import com.soul.soa_additions.quest.PackMode;
import com.soul.soa_additions.quest.QuestSource;
import com.soul.soa_additions.quest.model.RewardScope;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record Quest(
        String id,
        String chapterId,
        String title,
        List<String> description,
        String icon,
        Visibility visibility,
        boolean optional,
        List<String> dependencies,
        boolean depsAll,
        List<QuestTask> tasks,
        List<QuestReward> rewards,
        Set<PackMode> modes,
        QuestSource source,
        boolean autoClaim,
        NodeShape shape,
        // Manual position overrides. -1 means "let the auto layout decide".
        // Values are pixel offsets relative to the layout's content origin,
        // so they compose with whatever padding the screen applies.
        int posX,
        int posY,
        // Whether dependency edges into this quest should be drawn in the
        // graph view. Defaults true; turning it off lets editors keep a clean
        // visual without removing the underlying logical dependency.
        boolean showDeps,
        // Per-quest node size in pixels (square). Scales both the background
        // shape and the icon proportionally. {@link #DEFAULT_SIZE} matches the
        // legacy fixed 32×32 node.
        int size,
        // Repeatable flag + reset scope. When true, claiming this quest resets
        // its progress so it can be completed again. {@code repeatScope} picks
        // between TEAM (full reset of tasks + all claim markers) and PLAYER
        // (only clear claim markers so each team member can re-collect personal
        // rewards without re-grinding team-shared tasks). Downstream quests stay
        // unlocked after the first claim — tracked via {@code everClaimed} on
        // the progress row, not on the quest itself.
        boolean repeatable,
        RewardScope repeatScope,
        // Mutual-exclusion list: quest ids (bare or chapter/quest) that lock
        // this quest out permanently once they've been completed. Distinct
        // from {@link #dependencies} — exclusions don't draw edges and they
        // *block* rather than unlock. Used to model branching paths where
        // choosing one option shuts the door on the other.
        List<String> exclusions
) {
    public static final int DEFAULT_SIZE = 32;

    public boolean availableIn(PackMode mode) { return modes.contains(mode); }

    public String fullId() { return chapterId + "/" + id; }

    public boolean isEditable() { return source.isEditable(); }

    public boolean hasManualPosition() { return posX >= 0 && posY >= 0; }

    public int sizeOrDefault() { return size <= 0 ? DEFAULT_SIZE : size; }

    public Quest withPosition(int x, int y) {
        return new Quest(id, chapterId, title, description, icon, visibility, optional,
                dependencies, depsAll, tasks, rewards, modes, source, autoClaim, shape, x, y, showDeps, size, repeatable, repeatScope, exclusions);
    }

    public static EnumSet<PackMode> allModes() { return EnumSet.allOf(PackMode.class); }
}
