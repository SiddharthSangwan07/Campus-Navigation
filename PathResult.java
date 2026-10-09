import java.util.*;

/**
 * Result of a route query: ordered list of location codes plus total distance.
 */
public class PathResult {
    private final List<String> path;
    private final int distance;      // metres
    private final boolean found;

    private PathResult(List<String> path, int distance, boolean found) {
        this.path = path;
        this.distance = distance;
        this.found = found;
    }

    public static PathResult of(List<String> path, int distance) {
        return new PathResult(path, distance, true);
    }

    public static PathResult notFound() {
        return new PathResult(Collections.emptyList(), -1, false);
    }

    public boolean isFound()      { return found; }
    public List<String> getPath() { return path; }
    public int getDistance()      { return distance; }
    public int getHops()          { return Math.max(0, path.size() - 1); }

    /** Walking time in minutes at an average speed of 1.4 m/s. */
    public double walkingMinutes() { return distance / 1.4 / 60.0; }
}
