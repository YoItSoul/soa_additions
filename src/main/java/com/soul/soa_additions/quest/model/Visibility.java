package com.soul.soa_additions.quest.model;

/**
 * Visibility policy for a quest or chapter, applied client-side when the
 * quest book is rendered. The actual progression logic is unaffected — these
 * states only control whether the entry is drawn for the player.
 *
 * <ul>
 *   <li>{@link #NORMAL}: always visible. The default.</li>
 *   <li>{@link #HIDDEN_UNTIL_DEPS}: hidden until every declared dependency
 *       has been claimed. For chapters, the dependencies are the entries in
 *       {@code requires_chapters} (every non-optional quest in those chapters
 *       must be claimed) plus {@code requires_quests} (those exact quests
 *       must be claimed). For quests, the dependencies are
 *       {@code dependencies} on the quest itself.</li>
 *   <li>{@link #INVISIBLE}: never shown to players. Useful for retired or
 *       in-progress entries that should still load on the server but stay
 *       out of the book. Editors with edit mode active still see them.</li>
 * </ul>
 *
 * <p>The legacy {@code "hidden": true} JSON flag maps to {@link #HIDDEN_UNTIL_DEPS}
 * on load so existing chapter files still work without migration.</p>
 */
public enum Visibility {
    NORMAL,
    HIDDEN_UNTIL_DEPS,
    INVISIBLE;

    public static Visibility fromString(String s) {
        if (s == null) return NORMAL;
        try { return Visibility.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return NORMAL; }
    }

    public String lower() { return name().toLowerCase(); }

    public Visibility next() {
        Visibility[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
