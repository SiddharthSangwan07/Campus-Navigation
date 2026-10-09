import java.util.*;

/**
 * Campus Navigation & Shortest Path System  --  console application.
 *   java Main          -> interactive menu
 *   java Main demo     -> scripted demonstration of every feature
 */
public class Main {

    private static final CampusGraph graph = CampusData.build();
    private static boolean accessibleOnly = false;
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   HORIZON INSTITUTE OF TECHNOLOGY - CAMPUS NAVIGATOR");
        System.out.printf ("   %d buildings | %d walkways%n", graph.size(), graph.roadCount());
        System.out.println("==============================================================");

        if (args.length > 0 && args[0].equalsIgnoreCase("demo")) { demo(); return; }

        while (true) {
            printMenu();
            String choice = in.nextLine().trim().toUpperCase();
            try {
                switch (choice) {
                    case "1":  listLocations();      break;
                    case "2":  graph.printAdjacencyList(); break;
                    case "3":  shortestRoute();      break;
                    case "4":  fewestHopsRoute();    break;
                    case "5":  alternativeRoutes();  break;
                    case "6":  dfsTour();            break;
                    case "7":  nearestFacility();    break;
                    case "8":  withinHops();         break;
                    case "9":  toggleRoad();         break;
                    case "10": connectivity();       break;
                    case "11": compareAlgorithms();  break;
                    case "A":  accessibleOnly = !accessibleOnly;
                               System.out.println("Step-free (accessible) routing: " + (accessibleOnly ? "ON" : "OFF")); break;
                    case "0":  System.out.println("Goodbye!"); return;
                    default:   System.out.println("Invalid choice.");
                }
            } catch (NoSuchElementException ex) { return; }
        }
    }

    // ------------------------------------------------------------------ menu

    private static void printMenu() {
        System.out.println("\n----------------------------- MENU -----------------------------");
        System.out.println(" 1  List all locations           7  Nearest facility (by type)");
        System.out.println(" 2  Show adjacency list          8  What is within N hops? (BFS)");
        System.out.println(" 3  Shortest route (Dijkstra)    9  Block / unblock a walkway");
        System.out.println(" 4  Fewest-hops route (BFS)     10  Connectivity check");
        System.out.println(" 5  All alternative routes (DFS)11  Compare BFS vs Dijkstra");
        System.out.println(" 6  DFS campus tour              A  Toggle step-free routing [" + (accessibleOnly ? "ON" : "OFF") + "]");
        System.out.println(" 0  Exit");
        System.out.print("Choice: ");
    }

    // -------------------------------------------------------------- features

    private static void listLocations() {
        System.out.println("\nCODE   NAME                         [TYPE]");
        for (Location l : graph.locations()) System.out.println(l);
    }

    private static void shortestRoute() {
        String[] od = askOriginDestination();
        if (od == null) return;
        System.out.println("\n>> Dijkstra: shortest DISTANCE route");
        printRoute(PathFinder.dijkstra(graph, od[0], od[1], accessibleOnly));
    }

    private static void fewestHopsRoute() {
        String[] od = askOriginDestination();
        if (od == null) return;
        System.out.println("\n>> BFS: route through the FEWEST walkways");
        printRoute(PathFinder.bfs(graph, od[0], od[1], accessibleOnly));
    }

    private static void alternativeRoutes() {
        String[] od = askOriginDestination();
        if (od == null) return;
        List<PathResult> all = PathFinder.dfsAllPaths(graph, od[0], od[1], accessibleOnly, 7, 500);
        System.out.println("\n>> DFS backtracking found " + all.size() + " route(s) with at most 7 hops (sorted by distance).");
        int shown = Math.min(5, all.size());
        for (int i = 0; i < shown; i++) {
            PathResult r = all.get(i);
            System.out.printf("  #%d  %4d m  %2d hops  %s%n", i + 1, r.getDistance(), r.getHops(), pathNames(r.getPath()));
        }
        if (all.size() > shown) System.out.println("  ... and " + (all.size() - shown) + " more.");
    }

    private static void dfsTour() {
        String s = askLocation("Start location: ");
        if (s == null) return;
        System.out.println("\nRecursive DFS : " + String.join(" -> ", PathFinder.dfsOrderRecursive(graph, s, accessibleOnly)));
        System.out.println("Iterative DFS : " + String.join(" -> ", PathFinder.dfsOrderIterative(graph, s, accessibleOnly)));
    }

    private static void nearestFacility() {
        String s = askLocation("You are at: ");
        if (s == null) return;
        System.out.print("Facility type " + Arrays.toString(Location.Type.values()) + ": ");
        try {
            Location.Type t = Location.Type.valueOf(in.nextLine().trim().toUpperCase());
            System.out.println("\n>> Nearest " + t + " from " + graph.get(s).getName());
            printRoute(PathFinder.nearestOfType(graph, s, t, accessibleOnly));
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown facility type.");
        }
    }

    private static void withinHops() {
        String s = askLocation("Start location: ");
        if (s == null) return;
        System.out.print("Max hops: ");
        int n;
        try { n = Integer.parseInt(in.nextLine().trim()); } catch (NumberFormatException e) { System.out.println("Not a number."); return; }
        printLevels(s, n);
    }

    private static void toggleRoad() {
        String a = askLocation("Walkway end 1: ");
        String b = a == null ? null : askLocation("Walkway end 2: ");
        if (b == null) return;
        System.out.print("Block (B) or Unblock (U)? ");
        boolean block = in.nextLine().trim().equalsIgnoreCase("B");
        if (graph.setBlocked(a, b, block)) System.out.println("Walkway " + a + " <-> " + b + (block ? " is now CLOSED." : " is now OPEN."));
        else System.out.println("There is no direct walkway between those two locations.");
    }

    private static void connectivity() {
        printComponents();
    }

    private static void compareAlgorithms() {
        String[] od = askOriginDestination();
        if (od == null) return;
        compare(od[0], od[1]);
    }

    // --------------------------------------------------------- shared output

    private static void compare(String a, String b) {
        PathResult bfs = PathFinder.bfs(graph, a, b, accessibleOnly);
        PathResult dij = PathFinder.dijkstra(graph, a, b, accessibleOnly);
        System.out.println("\n>> " + graph.get(a).getName() + "  to  " + graph.get(b).getName());
        if (!bfs.isFound()) { System.out.println("   No route available."); return; }
        System.out.printf("   BFS      : %2d hops, %4d m  %s%n", bfs.getHops(), bfs.getDistance(), pathNames(bfs.getPath()));
        System.out.printf("   Dijkstra : %2d hops, %4d m  %s%n", dij.getHops(), dij.getDistance(), pathNames(dij.getPath()));
        if (bfs.getDistance() == dij.getDistance()) System.out.println("   -> Both found an equally short walk here.");
        else System.out.println("   -> BFS minimises hops, Dijkstra minimises metres: Dijkstra saves "
                + (bfs.getDistance() - dij.getDistance()) + " m.");
    }

    private static void printLevels(String s, int n) {
        Map<Integer, List<String>> levels = PathFinder.withinHops(graph, s, n, accessibleOnly);
        System.out.println("\n>> BFS levels from " + graph.get(s).getName());
        if (levels.isEmpty()) System.out.println("   (nothing reachable)");
        for (Map.Entry<Integer, List<String>> e : levels.entrySet())
            System.out.println("   " + e.getKey() + " hop(s): " + String.join(", ", e.getValue()));
    }

    private static void printComponents() {
        List<List<String>> comps = PathFinder.connectedComponents(graph, accessibleOnly);
        System.out.println("\n>> Connectivity: " + comps.size() + " connected region(s)");
        int i = 1;
        for (List<String> c : comps) System.out.println("   Region " + i++ + " (" + c.size() + "): " + String.join(", ", c));
        if (comps.size() > 1) System.out.println("   WARNING: some places cannot be reached from others!");
    }

    private static void printRoute(PathResult r) {
        if (!r.isFound()) { System.out.println("   No route available (blocked or inaccessible)."); return; }
        System.out.println("   Route    : " + pathNames(r.getPath()));
        System.out.printf ("   Distance : %d m   |   Hops: %d   |   Walking time: %.1f min%n",
                r.getDistance(), r.getHops(), r.walkingMinutes());
        System.out.println("   Directions:");
        List<String> p = r.getPath();
        for (int i = 0; i + 1 < p.size(); i++) {
            CampusGraph.Edge e = graph.findEdge(p.get(i), p.get(i + 1), accessibleOnly);
            System.out.printf("     %d. %s -> %s  (%d m)%s%n", i + 1,
                    graph.get(p.get(i)).getName(), graph.get(p.get(i + 1)).getName(), e.distance,
                    e.accessible ? "" : "  [stairs]");
        }
        if (p.size() == 1) System.out.println("     You are already there.");
    }

    private static String pathNames(List<String> codes) {
        return String.join(" > ", codes);
    }

    // ------------------------------------------------------------- input

    private static String askLocation(String prompt) {
        System.out.print(prompt);
        String code = graph.resolve(in.nextLine());
        if (code == null) System.out.println("Location not found. Use a code (e.g. LIB) or part of a name.");
        return code;
    }

    private static String[] askOriginDestination() {
        String a = askLocation("From: ");
        if (a == null) return null;
        String b = askLocation("To  : ");
        return b == null ? null : new String[]{a, b};
    }

    // -------------------------------------------------------------- demo

    private static void demo() {
        System.out.println("\n##### 1. Adjacency list #####");
        graph.printAdjacencyList();

        System.out.println("\n##### 2. Dijkstra: Main Gate -> Sports Complex #####");
        printRoute(PathFinder.dijkstra(graph, "GATE", "SPORT", false));

        System.out.println("\n##### 3. BFS vs Dijkstra: Main Gate -> Boys Hostel #####");
        compare("GATE", "HOS1");

        System.out.println("\n##### 4. BFS vs Dijkstra: Auditorium -> Gymnasium #####");
        compare("AUD", "GYM");

        System.out.println("\n##### 5. DFS: alternative routes Library -> Food Court #####");
        List<PathResult> all = PathFinder.dfsAllPaths(graph, "LIB", "FC", false, 7, 500);
        System.out.println("   " + all.size() + " routes; best three:");
        for (int i = 0; i < Math.min(3, all.size()); i++)
            System.out.printf("   #%d %4d m  %s%n", i + 1, all.get(i).getDistance(), pathNames(all.get(i).getPath()));

        System.out.println("\n##### 6. DFS tour from Main Gate #####");
        System.out.println("   Recursive: " + String.join(" -> ", PathFinder.dfsOrderRecursive(graph, "GATE", false)));
        System.out.println("   Iterative: " + String.join(" -> ", PathFinder.dfsOrderIterative(graph, "GATE", false)));

        System.out.println("\n##### 7. Nearest FOOD from Computer Science Block #####");
        printRoute(PathFinder.nearestOfType(graph, "CSE", Location.Type.FOOD, false));

        System.out.println("\n##### 8. BFS levels: within 2 hops of Library #####");
        printLevels("LIB", 2);

        System.out.println("\n##### 9. Step-free routing: Library -> Auditorium #####");
        System.out.println("  Normal:");
        printRoute(PathFinder.dijkstra(graph, "LIB", "AUD", false));
        System.out.println("  Step-free only:");
        printRoute(PathFinder.dijkstra(graph, "LIB", "AUD", true));

        System.out.println("\n##### 10. Road closure: Research Labs <-> Observatory #####");
        printComponents();
        graph.setBlocked("LAB", "OBS", true);
        System.out.println("   (walkway LAB <-> OBS closed)");
        printComponents();
        printRoute(PathFinder.dijkstra(graph, "GATE", "OBS", false));
        graph.setBlocked("LAB", "OBS", false);

        System.out.println("\n##### 11. Closure forces a detour: block LIB <-> CAF #####");
        System.out.println("  Before:"); printRoute(PathFinder.dijkstra(graph, "LIB", "CAF", false));
        graph.setBlocked("LIB", "CAF", true);
        System.out.println("  After :"); printRoute(PathFinder.dijkstra(graph, "LIB", "CAF", false));
    }
}
