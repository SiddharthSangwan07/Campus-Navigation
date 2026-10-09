/**
 * A node (vertex) of the campus graph: one building / facility.
 */
public class Location {

    public enum Type {
        ENTRANCE, ADMIN, ACADEMIC, RESEARCH, FOOD, HOSTEL, SPORTS, MEDICAL, SERVICE, EVENT
    }

    private final String code;   // short unique id, e.g. "LIB"
    private final String name;   // human readable name
    private final Type type;     // category (used for "nearest facility" queries)

    public Location(String code, String name, Type type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public Type getType()   { return type; }

    @Override
    public String toString() {
        return String.format("%-6s %-28s [%s]", code, name, type);
    }
}
