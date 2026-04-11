package com.soul.soa_additions.quest.editor;

import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;

import java.util.List;
import java.util.Optional;

/**
 * Write-back path for in-game edits.
 *
 * <p>The editor never mutates a loaded {@link Chapter} directly — it constructs
 * a new instance and hands it here. Implementations are expected to serialize
 * it to {@code <world>/soa_additions/quest_edits/<chapter>.json} and trigger a
 * {@code /reload} so the main {@code QuestLoader} picks it up via
 * {@link com.soul.soa_additions.quest.QuestRegistry#worldEditChapters()}.</p>
 *
 * <p>This interface exists now so every editor call site can be written
 * against a stable API. The concrete file-backed impl lands with the editor
 * GUI work.</p>
 */
public interface QuestOverrideStorage {

    /**
     * Save a chapter override. If a chapter with the same id already exists in
     * world edits, it is replaced entirely — this is a full-object write, not a
     * patch. Callers should read {@link #loadChapter}, mutate the result, and
     * write it back.
     */
    void saveChapter(Chapter chapter, EditTarget target);

    /** Convenience: save to {@link EditTarget#WORLD_OVERRIDE}. */
    default void saveChapter(Chapter chapter) { saveChapter(chapter, EditTarget.WORLD_OVERRIDE); }

    /**
     * Delete a chapter override. The base chapter (from jar/datapack) becomes
     * the effective chapter again. Has no effect on base chapters themselves.
     */
    void deleteChapterOverride(String chapterId);

    /**
     * Load the current override of a chapter, or empty if no override exists.
     * The base chapter — if any — is <i>not</i> returned; callers that want the
     * "effective" chapter should query {@link com.soul.soa_additions.quest.QuestRegistry}.
     */
    Optional<Chapter> loadChapter(String chapterId);

    /** List every chapter id that currently has a world-edit override. */
    List<String> listOverriddenChapterIds();

    /**
     * Convenience: update a single quest inside a chapter. Loads the effective
     * chapter, replaces the quest with the new one (or appends if missing),
     * saves the resulting chapter as an override.
     */
    void saveQuest(String chapterId, Quest quest, EditTarget target);

    default void saveQuest(String chapterId, Quest quest) {
        saveQuest(chapterId, quest, EditTarget.WORLD_OVERRIDE);
    }

    /**
     * Whether the given chapter can be written back at the requested target.
     * {@code PROGRAMMATIC} chapters always return false for {@code AUTHOR_SOURCE}
     * (no source file to write to — they live in {@code .java}). Used by the
     * editor GUI to surface "this quest is source-only, fork into an override?"
     * instead of silently dropping the save.
     */
    boolean canWrite(Chapter chapter, EditTarget target);
}
