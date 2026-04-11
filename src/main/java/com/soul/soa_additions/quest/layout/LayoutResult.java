package com.soul.soa_additions.quest.layout;

import java.util.List;
import java.util.Map;

/**
 * Output of {@link QuestLayout#compute}.
 *
 * @param positions map of quest full-id → grid position (column, row)
 * @param columns   number of columns used (= depth of longest dep chain + 1)
 * @param rows      max rows in any single column
 * @param edges     flattened list of dependency edges for edge-routing code
 * @param orphans   quests not reachable from any root — placed in their own trailing column
 */
public record LayoutResult(
        Map<String, GridPosition> positions,
        int columns,
        int rows,
        List<Edge> edges,
        List<String> orphans
) {
    /**
     * Column-row coordinate in the virtual grid plus the resolved pixel offset.
     * Pixel values are content-relative (screen adds its own origin padding).
     * For quests with manual positions the column/row are still populated
     * (derived from the manual pixel coords) so crossing-reduction and edge
     * routing continue to work.
     */
    public record GridPosition(int column, int row, int pixelX, int pixelY) {}

    /**
     * Directed edge in the dependency graph.
     *
     * @param from   parent quest full-id
     * @param to     child quest full-id
     * @param orGroup true if this edge belongs to an OR-group on the child
     *               (child has {@code depsAll=false}). Rendered dashed.
     */
    public record Edge(String from, String to, boolean orGroup) {}
}
