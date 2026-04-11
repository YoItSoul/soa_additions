package com.soul.soa_additions.quest.editor;

/**
 * Where an in-game edit should be written.
 *
 * <p>{@link #WORLD_OVERRIDE} is the default for servers: edits land in
 * {@code <world>/soa_additions/quest_edits/} and layer on top of base content,
 * leaving the mod jar / datapack / Java source untouched. Safe for live ops
 * fixing a typo — nothing desyncs from the shipped content.</p>
 *
 * <p>{@link #AUTHOR_SOURCE} is for pack authors working on their own modpack
 * or server content. Edits to JSON-backed chapters are written directly back
 * to the source file (resource pack / datapack) so changes can be committed
 * to source control. {@code PROGRAMMATIC} chapters still cannot be edited in
 * this mode — their definitions live in {@code .java} and a rebuild is
 * required. Attempting to edit one is surfaced to the user as "fork to
 * override?" instead of silently failing.</p>
 *
 * <p>Mode is a per-player choice on top of edit mode and has no bearing on
 * permissions — you still need op + {@code /soa quests editmode true} to
 * edit anything. Defaults to {@link #WORLD_OVERRIDE} on first activation;
 * {@link #AUTHOR_SOURCE} must be opted into explicitly via the command so
 * there's an audit trail of "player X started writing to source files".</p>
 */
public enum EditTarget {
    WORLD_OVERRIDE,
    AUTHOR_SOURCE
}
