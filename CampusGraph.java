import java.util.*;

/**
 * Undirected, weighted graph stored as an ADJACENCY LIST.
 *   Vertex = Location (building)       Edge = walkway between two buildings
 *   Weight = distance in metres        Extra attributes: accessible (ramp, no stairs), blocked (closed)
 */
public class CampusGraph {

    /** A directed half of an undirected walkway. */
    public static class Edge {
        public final String from;
        public final String to;
        public final int distance;
        public final boolean accessible;  // false => stairs / steep path (not wheelchair friendly)
        public boolean blocked;           // true  => closed for construction etc.

        Edge(String from, String to, int distance, boolean accessible) {
            this.from = from;
            this.to = to;
            this.distance = distance;
            this.accessible = accessible;
        }
    }

    private final Map<String, Location> locations = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adj = new LinkedHashMap<>();

    // ---------- construction ----------

    public void addLocation(Location loc) {
        locations.put(loc.getCode(), loc);
        adj.putIfAbsent(loc.getCode(), new ArrayList<>());
    }

    public void addRoad(String a, String b, int distance, boolean accessible) {
        if (!has(a) || !has(b)) throw new IllegalArgumentException("Unknown location: " + a + " or " + b);
        adj.get(a).add(new Edge(a, b, distance, accessible));
        adj.get(b).add(new Edge(b, a, distance, accessible));
    }

    // ---------- queries ----------

    public boolean has(String code)            { return locations.containsKey(code); }
    public Location get(String code)           { return locations.get(code); }
    public Collection<Location> locations()    { return locations.values(); }
    public int size()                          { return locations.size(); }

    public int roadCount() {
        int total = 0;
        for (List<Edge> list : adj.values()) total += list.size();
        return total / 2;
    }

    public List<Edge> edgesOf(String code) { return adj.get(code); }

    /** Edges that may actually be walked right now (not blocked, and optionally step-free only). */
    public List<Edge> usableEdges(String code, boolean accessibleOnly) {
        List<Edge> out = new ArrayList<>();
        for (Edge e : adj.get(code)) {
            if (e.blocked) continue;
            if (accessibleOnly && !e.accessible) continue;
            out.add(e);
        }
        return out;
    }

    public Edge findEdge(String a, String b, boolean accessibleOnly) {
        Edge best = null;
        for (Edge e : usableEdges(a, accessibleOnly))
            if (e.to.equals(b) && (best == null || e.distance < best.distance)) best = e;
        return best;
    }

    /** Close or re-open a walkway. Returns false if no such road exists. */
    public boolean setBlocked(String a, String b, boolean blocked) {
        boolean changed = false;
        for (Edge e : adj.getOrDefault(a, List.of())) if (e.to.equals(b)) { e.blocked = blocked; changed = true; }
        for (Edge e : adj.getOrDefault(b, List.of())) if (e.to.equals(a)) { e.blocked = blocked; changed = true; }
        return changed;
    }

    /** Resolve user input (code or part of a name) to a location code; null if not found. */
    public String resolve(String input) {
        if (input == null) return null;
        String s = input.trim().toUpperCase();
        if (has(s)) return s;
        for (Location l : locations.values())
            if (l.getName().toUpperCase().contains(s) && !s.isEmpty()) return l.getCode();
        return null;
    }

    public void printAdjacencyList() {
        System.out.println("\n=== ADJACENCY LIST (neighbour : metres, * = stairs, X = blocked) ===");
        for (Map.Entry<String, List<Edge>> en : adj.entrySet()) {
            StringBuilder sb = new StringBuilder(String.format("%-6s -> ", en.getKey()));
            for (Edge e : en.getValue())
                sb.append(e.to).append(':').append(e.distance)
                  .append(e.accessible ? "" : "*").append(e.blocked ? "X" : "").append("  ");
            System.out.println(sb);
        }
    }
}
