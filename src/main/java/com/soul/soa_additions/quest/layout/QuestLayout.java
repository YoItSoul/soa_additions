package com.soul.soa_additions.quest.layout;

import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sugiyama-style layered graph layout for a chapter's quest dependencies.
 *
 * <p>No per-quest hand-placement. Given a chapter, this class walks the dependency
 * DAG and produces {@link LayoutResult.GridPosition} values that the GUI turns
 * into pixel coordinates. Re-runs on every reload — authors add quests, layout
 * updates automatically.</p>
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li><b>Layer assignment (longest path):</b> each quest's column is
 *       {@code max(parent columns) + 1}. Quests with no parents are column 0.</li>
 *   <li><b>Barycenter ordering:</b> within each column, order quests to minimize
 *       edge crossings by placing each at the mean row of its parents. Iterate
 *       forward and backward a few sweeps to converge.</li>
 *   <li><b>Row compaction:</b> collapse gaps so rows are 0..n-1 contiguous.</li>
 * </ol>
 *
 * <p>Cycles aren't legal in a dependency graph; if one is detected we log and
 * drop the offending edge so layout still produces something rather than hanging.</p>
 */
public final class QuestLayout {

    private static final int BARYCENTER_SWEEPS = 6;

    // Pixel spacing used to pixelize grid coords. Kept here (rather than in
    // the screen) so drag-to-edit can quantize manual positions against the
    // same scale the auto layout uses.
    public static final int COL_SPACING = 72;
    public static final int ROW_SPACING = 48;

    private QuestLayout() {}

    public static LayoutResult compute(Chapter chapter) {
        List<Quest> quests = chapter.quests();
        if (quests.isEmpty()) {
            return new LayoutResult(Collections.emptyMap(), 0, 0, Collections.emptyList(), Collections.emptyList());
        }

        // Build lookup by *local* id (chapter's own dep refs use unqualified ids)
        Map<String, Quest> byLocalId = new LinkedHashMap<>();
        for (Quest q : quests) byLocalId.put(q.id(), q);

        // Edges: parent local-id → child local-id
        Map<String, List<String>> childrenOf = new HashMap<>();
        Map<String, List<String>> parentsOf = new HashMap<>();
        List<LayoutResult.Edge> edges = new ArrayList<>();
        for (Quest q : quests) {
            parentsOf.computeIfAbsent(q.id(), k -> new ArrayList<>());
            for (String dep : q.dependencies()) {
                if (!byLocalId.containsKey(dep)) continue; // external dep — ignored for layout
                childrenOf.computeIfAbsent(dep, k -> new ArrayList<>()).add(q.id());
                parentsOf.computeIfAbsent(q.id(), k -> new ArrayList<>()).add(dep);
                edges.add(new LayoutResult.Edge(q.chapterId() + "/" + dep, q.fullId(), !q.depsAll()));
            }
        }

        // Detect and break cycles defensively
        breakCycles(quests, parentsOf, childrenOf);

        // 1. Layer assignment via longest path from roots
        Map<String, Integer> column = new HashMap<>();
        List<String> order = topologicalOrder(quests, parentsOf);
        for (String id : order) {
            int col = 0;
            for (String p : parentsOf.getOrDefault(id, Collections.emptyList())) {
                col = Math.max(col, column.getOrDefault(p, 0) + 1);
            }
            column.put(id, col);
        }

        // Group by column
        int maxCol = column.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<List<String>> columns = new ArrayList<>();
        for (int i = 0; i <= maxCol; i++) columns.add(new ArrayList<>());
        for (String id : order) columns.get(column.get(id)).add(id);

        // 2. Barycenter ordering to reduce crossings.
        //    Initialize each column by insertion order, then alternate forward/backward sweeps.
        Map<String, Integer> row = new HashMap<>();
        for (List<String> col : columns) {
            for (int i = 0; i < col.size(); i++) row.put(col.get(i), i);
        }
        for (int sweep = 0; sweep < BARYCENTER_SWEEPS; sweep++) {
            boolean forward = sweep % 2 == 0;
            if (forward) {
                for (int c = 1; c <= maxCol; c++) {
                    reorderByBarycenter(columns.get(c), row, parentsOf);
                    for (int i = 0; i < columns.get(c).size(); i++) row.put(columns.get(c).get(i), i);
                }
            } else {
                for (int c = maxCol - 1; c >= 0; c--) {
                    reorderByBarycenter(columns.get(c), row, childrenOf);
                    for (int i = 0; i < columns.get(c).size(); i++) row.put(columns.get(c).get(i), i);
                }
            }
        }

        // 3. Build final positions keyed on FULL id so the GUI doesn't have to care about local vs full.
        //    Manual positions (Quest#hasManualPosition) override the auto-computed
        //    pixel coords but still keep their grid column/row for edge routing.
        Map<String, LayoutResult.GridPosition> positions = new LinkedHashMap<>();
        int maxRows = 0;
        for (int c = 0; c <= maxCol; c++) {
            List<String> col = columns.get(c);
            maxRows = Math.max(maxRows, col.size());
            for (int r = 0; r < col.size(); r++) {
                Quest q = byLocalId.get(col.get(r));
                int px, py;
                if (q.hasManualPosition()) {
                    px = q.posX();
                    py = q.posY();
                } else {
                    px = c * COL_SPACING;
                    py = r * ROW_SPACING;
                }
                positions.put(q.fullId(), new LayoutResult.GridPosition(c, r, px, py));
            }
        }

        // Orphans = quests not in any column (shouldn't happen after full walk, but safety net)
        List<String> orphans = new ArrayList<>();
        for (Quest q : quests) {
            if (!positions.containsKey(q.fullId())) orphans.add(q.fullId());
        }

        return new LayoutResult(positions, maxCol + 1, maxRows, edges, orphans);
    }

    // ---------- helpers ----------

    private static List<String> topologicalOrder(List<Quest> quests, Map<String, List<String>> parentsOf) {
        // Kahn's algorithm
        Map<String, Integer> indeg = new HashMap<>();
        for (Quest q : quests) indeg.put(q.id(), parentsOf.getOrDefault(q.id(), Collections.emptyList()).size());
        List<String> ready = new ArrayList<>();
        for (Map.Entry<String, Integer> e : indeg.entrySet()) if (e.getValue() == 0) ready.add(e.getKey());
        List<String> out = new ArrayList<>();
        Map<String, List<String>> childrenOf = new HashMap<>();
        for (Quest q : quests) {
            for (String p : parentsOf.getOrDefault(q.id(), Collections.emptyList())) {
                childrenOf.computeIfAbsent(p, k -> new ArrayList<>()).add(q.id());
            }
        }
        while (!ready.isEmpty()) {
            String n = ready.remove(0);
            out.add(n);
            for (String c : childrenOf.getOrDefault(n, Collections.emptyList())) {
                int d = indeg.get(c) - 1;
                indeg.put(c, d);
                if (d == 0) ready.add(c);
            }
        }
        // Append any survivors (shouldn't exist after cycle break)
        for (Quest q : quests) if (!out.contains(q.id())) out.add(q.id());
        return out;
    }

    private static void reorderByBarycenter(List<String> layer, Map<String, Integer> rowOf,
                                            Map<String, List<String>> neighbors) {
        // Each node's barycenter = mean row of its neighbors in the adjacent layer.
        Map<String, Double> bary = new HashMap<>();
        for (String id : layer) {
            List<String> ns = neighbors.getOrDefault(id, Collections.emptyList());
            if (ns.isEmpty()) { bary.put(id, (double) rowOf.getOrDefault(id, 0)); continue; }
            double sum = 0;
            int seen = 0;
            for (String n : ns) {
                Integer r = rowOf.get(n);
                if (r != null) { sum += r; seen++; }
            }
            bary.put(id, seen == 0 ? rowOf.getOrDefault(id, 0).doubleValue() : sum / seen);
        }
        layer.sort((a, b) -> Double.compare(bary.get(a), bary.get(b)));
    }

    private static void breakCycles(List<Quest> quests,
                                     Map<String, List<String>> parentsOf,
                                     Map<String, List<String>> childrenOf) {
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        for (Quest q : quests) {
            if (!visited.contains(q.id())) dfsBreak(q.id(), visited, stack, parentsOf, childrenOf);
        }
    }

    private static void dfsBreak(String id, Set<String> visited, Set<String> stack,
                                 Map<String, List<String>> parentsOf,
                                 Map<String, List<String>> childrenOf) {
        visited.add(id);
        stack.add(id);
        List<String> kids = childrenOf.getOrDefault(id, Collections.emptyList());
        List<String> toRemove = new ArrayList<>();
        for (String child : kids) {
            if (stack.contains(child)) {
                // back-edge — drop it
                toRemove.add(child);
                org.slf4j.LoggerFactory.getLogger("soa_additions/quest-layout")
                        .warn("Dependency cycle detected: {} → {}. Dropping edge for layout.", id, child);
            } else if (!visited.contains(child)) {
                dfsBreak(child, visited, stack, parentsOf, childrenOf);
            }
        }
        for (String rm : toRemove) {
            kids.remove(rm);
            parentsOf.getOrDefault(rm, Collections.emptyList()).remove(id);
        }
        stack.remove(id);
    }
}
