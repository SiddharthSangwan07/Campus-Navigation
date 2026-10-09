import java.util.*;

/**
 * All graph algorithms of the project.
 *
 *   BFS       -> fewest walkways (hops), level-by-level exploration, connectivity
 *   DFS       -> reachability, campus tour order, ALL alternative routes (backtracking)
 *   Dijkstra  -> true shortest DISTANCE on the weighted graph (and nearest facility)
 */
public class PathFinder {

    // =====================================================================
    //  BFS  --  O(V + E)
    // =====================================================================

    /** Path with the minimum NUMBER OF HOPS (ignores distances). */
    public static PathResult bfs(CampusGraph g, String src, String dst, boolean accessibleOnly) {
        Map<String, String> parent = new HashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        parent.put(src, null);
        queue.add(src);

        while (!queue.isEmpty()) {
            String u = queue.poll();
            if (u.equals(dst)) break;
            for (CampusGraph.Edge e : g.usableEdges(u, accessibleOnly)) {
                if (!parent.containsKey(e.to)) {
                    parent.put(e.to, u);
                    queue.add(e.to);
                }
            }
        }
        return buildPath(g, parent, src, dst, accessibleOnly);
    }

    /** Locations grouped by number of hops from src (BFS levels), up to maxHops. */
    public static Map<Integer, List<String>> withinHops(CampusGraph g, String src, int maxHops, boolean accessibleOnly) {
        Map<String, Integer> level = new HashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        level.put(src, 0);
        queue.add(src);
        Map<Integer, List<String>> out = new TreeMap<>();

        while (!queue.isEmpty()) {
            String u = queue.poll();
            int d = level.get(u);
            if (d > 0) out.computeIfAbsent(d, k -> new ArrayList<>()).add(u);
            if (d == maxHops) continue;
            for (CampusGraph.Edge e : g.usableEdges(u, accessibleOnly)) {
                if (!level.containsKey(e.to)) {
                    level.put(e.to, d + 1);
                    queue.add(e.to);
                }
            }
        }
        return out;
    }

    /** Connected components of the currently usable campus (BFS flood fill). */
    public static List<List<String>> connectedComponents(CampusGraph g, boolean accessibleOnly) {
        Set<String> seen = new HashSet<>();
        List<List<String>> comps = new ArrayList<>();
        for (Location l : g.locations()) {
            if (seen.contains(l.getCode())) continue;
            List<String> comp = new ArrayList<>();
            Deque<String> queue = new ArrayDeque<>();
            queue.add(l.getCode());
            seen.add(l.getCode());
            while (!queue.isEmpty()) {
                String u = queue.poll();
                comp.add(u);
                for (CampusGraph.Edge e : g.usableEdges(u, accessibleOnly))
                    if (seen.add(e.to)) queue.add(e.to);
            }
            comps.add(comp);
        }
        return comps;
    }

    // =====================================================================
    //  DFS  --  O(V + E) for traversal, exponential for "all paths"
    // =====================================================================

    /** Recursive DFS visiting order (a "walking tour" of everything reachable). */
    public static List<String> dfsOrderRecursive(CampusGraph g, String src, boolean accessibleOnly) {
        List<String> order = new ArrayList<>();
        dfsVisit(g, src, new HashSet<>(), order, accessibleOnly);
        return order;
    }

    private static void dfsVisit(CampusGraph g, String u, Set<String> visited, List<String> order, boolean acc) {
        visited.add(u);
        order.add(u);
        for (CampusGraph.Edge e : g.usableEdges(u, acc))
            if (!visited.contains(e.to)) dfsVisit(g, e.to, visited, order, acc);
    }

    /** Iterative DFS using an explicit stack (same order as the recursive version). */
    public static List<String> dfsOrderIterative(CampusGraph g, String src, boolean accessibleOnly) {
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(src);
        while (!stack.isEmpty()) {
            String u = stack.pop();
            if (!visited.add(u)) continue;
            order.add(u);
            List<CampusGraph.Edge> edges = g.usableEdges(u, accessibleOnly);
            for (int i = edges.size() - 1; i >= 0; i--)   // reverse so first neighbour is explored first
                if (!visited.contains(edges.get(i).to)) stack.push(edges.get(i).to);
        }
        return order;
    }

    /** Is dst reachable from src? (DFS) */
    public static boolean isReachable(CampusGraph g, String src, String dst, boolean accessibleOnly) {
        return dfsOrderRecursive(g, src, accessibleOnly).contains(dst);
    }

    /**
     * DFS with BACKTRACKING: every simple (cycle-free) route from src to dst.
     * maxHops and maxPaths keep the exponential search under control.
     */
    public static List<PathResult> dfsAllPaths(CampusGraph g, String src, String dst, boolean accessibleOnly,
                                               int maxHops, int maxPaths) {
        List<PathResult> results = new ArrayList<>();
        Deque<String> current = new ArrayDeque<>();
        Set<String> onPath = new HashSet<>();
        current.addLast(src);
        onPath.add(src);
        backtrack(g, src, dst, accessibleOnly, maxHops, maxPaths, current, onPath, 0, results);
        results.sort(Comparator.comparingInt(PathResult::getDistance));
        return results;
    }

    private static void backtrack(CampusGraph g, String u, String dst, boolean acc, int maxHops, int maxPaths,
                                  Deque<String> current, Set<String> onPath, int dist, List<PathResult> out) {
        if (out.size() >= maxPaths) return;
        if (u.equals(dst)) {
            out.add(PathResult.of(new ArrayList<>(current), dist));
            return;
        }
        if (current.size() - 1 >= maxHops) return;
        for (CampusGraph.Edge e : g.usableEdges(u, acc)) {
            if (onPath.contains(e.to)) continue;
            current.addLast(e.to);                 // choose
            onPath.add(e.to);
            backtrack(g, e.to, dst, acc, maxHops, maxPaths, current, onPath, dist + e.distance, out);
            onPath.remove(e.to);                   // un-choose (backtrack)
            current.removeLast();
        }
    }

    // =====================================================================
    //  Dijkstra  --  O((V + E) log V)
    // =====================================================================

    /** Shortest-path tree from one source: distance and parent of every reachable location. */
    public static class ShortestPathTree {
        public final Map<String, Integer> dist = new HashMap<>();
        public final Map<String, String> parent = new HashMap<>();
    }

    public static ShortestPathTree dijkstraTree(CampusGraph g, String src, boolean accessibleOnly) {
        ShortestPathTree t = new ShortestPathTree();
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        t.dist.put(src, 0);
        t.parent.put(src, null);
        pq.add(new AbstractMap.SimpleEntry<>(src, 0));

        while (!pq.isEmpty()) {
            Map.Entry<String, Integer> cur = pq.poll();
            String u = cur.getKey();
            if (cur.getValue() > t.dist.get(u)) continue;      // stale queue entry
            for (CampusGraph.Edge e : g.usableEdges(u, accessibleOnly)) {
                int nd = t.dist.get(u) + e.distance;
                if (nd < t.dist.getOrDefault(e.to, Integer.MAX_VALUE)) {
                    t.dist.put(e.to, nd);
                    t.parent.put(e.to, u);
                    pq.add(new AbstractMap.SimpleEntry<>(e.to, nd));
                }
            }
        }
        return t;
    }

    public static PathResult dijkstra(CampusGraph g, String src, String dst, boolean accessibleOnly) {
        ShortestPathTree t = dijkstraTree(g, src, accessibleOnly);
        return buildPath(g, t.parent, src, dst, accessibleOnly);
    }

    /** Closest location of a given type (e.g. nearest FOOD) by walking distance. */
    public static PathResult nearestOfType(CampusGraph g, String src, Location.Type type, boolean accessibleOnly) {
        ShortestPathTree t = dijkstraTree(g, src, accessibleOnly);
        String best = null;
        for (Location l : g.locations()) {
            String c = l.getCode();
            if (c.equals(src) || l.getType() != type || !t.dist.containsKey(c)) continue;
            if (best == null || t.dist.get(c) < t.dist.get(best)) best = c;
        }
        return best == null ? PathResult.notFound() : buildPath(g, t.parent, src, best, accessibleOnly);
    }

    // =====================================================================
    //  helper
    // =====================================================================

    /** Walk the parent map backwards from dst to src, then sum the edge lengths. */
    private static PathResult buildPath(CampusGraph g, Map<String, String> parent, String src, String dst, boolean acc) {
        if (!parent.containsKey(dst)) return PathResult.notFound();
        LinkedList<String> path = new LinkedList<>();
        for (String at = dst; at != null; at = parent.get(at)) path.addFirst(at);
        int total = 0;
        for (int i = 0; i + 1 < path.size(); i++)
            total += g.findEdge(path.get(i), path.get(i + 1), acc).distance;
        return PathResult.of(path, total);
    }
}
