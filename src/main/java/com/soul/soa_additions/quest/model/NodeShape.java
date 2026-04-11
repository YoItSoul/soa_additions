package com.soul.soa_additions.quest.model;

/**
 * Background shape drawn behind a quest's icon in the quest book graph.
 * Independent of quest status — status is applied as a tint color on top
 * of whichever shape the author chose, so a rhombus VISIBLE quest and a
 * rhombus CLAIMED quest share the same silhouette but different color.
 *
 * <ul>
 *   <li>{@link #SQUARE}  — flat axis-aligned square (default).</li>
 *   <li>{@link #CIRCLE}  — filled circle, same footprint as SQUARE.</li>
 *   <li>{@link #RHOMBUS} — square rotated 45°; diamond silhouette.</li>
 *   <li>{@link #ICON}    — the quest's own item icon rendered at 2× size
 *       and tinted with the status color, forming the background itself.
 *       The normal 1× icon is drawn on top, centered.</li>
 * </ul>
 */
public enum NodeShape {
    SQUARE,
    CIRCLE,
    RHOMBUS,
    ICON;

    public static NodeShape fromString(String s) {
        if (s == null) return ICON;
        try { return NodeShape.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return ICON; }
    }
}
