package com.soul.soa_additions.quest.i18n;

import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import net.minecraft.network.chat.Component;

/**
 * Turns the raw strings stored on {@link Chapter} and {@link Quest} into
 * translatable {@link Component}s using an auto-derived key convention, with
 * the original string as the English fallback.
 *
 * <p>Key scheme:</p>
 * <ul>
 *   <li>Chapter title: {@code soa_additions.quest.<chapter>.title}</li>
 *   <li>Chapter description line N: {@code soa_additions.quest.<chapter>.desc.N}</li>
 *   <li>Quest title: {@code soa_additions.quest.<chapter>.<quest>.title}</li>
 *   <li>Quest description line N: {@code soa_additions.quest.<chapter>.<quest>.desc.N}</li>
 * </ul>
 *
 * <p>No change to JSON format. Authors who only ship English keep writing
 * {@code "title": "The Basics"} and get exactly that on screen. Authors who
 * ship multiple languages add the derived key to their {@code lang/*.json}
 * files; when the client's active language has the key, the translation is
 * used, otherwise {@link Component#translatableWithFallback} uses the raw
 * string. This leans on vanilla's translation dispatch entirely — nothing
 * in the server payload or the quest model changes.</p>
 *
 * <p>Why derive the key instead of asking authors to write it explicitly?
 * Two reasons: (a) keeps the common "English-only pack" case zero-effort,
 * (b) any format that says "this field is a key" makes every author think
 * about it on every quest. Convention wins for a content authoring tool
 * where most strings are literal and a minority want localization.</p>
 */
public final class QuestText {

    private QuestText() {}

    public static Component chapterTitle(Chapter chapter) {
        return Component.translatableWithFallback(
                "soa_additions.quest." + chapter.id() + ".title",
                chapter.title());
    }

    public static Component chapterDescLine(Chapter chapter, int index) {
        String raw = safeLine(chapter.description(), index);
        return Component.translatableWithFallback(
                "soa_additions.quest." + chapter.id() + ".desc." + index,
                raw);
    }

    public static Component questTitle(Quest quest) {
        return Component.translatableWithFallback(
                "soa_additions.quest." + quest.chapterId() + "." + quest.id() + ".title",
                quest.title());
    }

    public static Component questDescLine(Quest quest, int index) {
        String raw = safeLine(quest.description(), index);
        return Component.translatableWithFallback(
                "soa_additions.quest." + quest.chapterId() + "." + quest.id() + ".desc." + index,
                raw);
    }

    private static String safeLine(java.util.List<String> lines, int index) {
        return (lines == null || index < 0 || index >= lines.size()) ? "" : lines.get(index);
    }
}
