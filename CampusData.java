
/**
 * Builds the hypothetical campus: "Horizon Institute of Technology" (HIT).
 * 18 buildings, 26 walkways. Distances are in metres.
 */
public class CampusData {

    public static CampusGraph build() {
        CampusGraph g = new CampusGraph();

        g.addLocation(new Location("GATE",  "Main Gate",              Location.Type.ENTRANCE));
        g.addLocation(new Location("PARK",  "Parking Lot",            Location.Type.SERVICE));
        g.addLocation(new Location("ATM",   "Bank & ATM",             Location.Type.SERVICE));
        g.addLocation(new Location("ADM",   "Administration Block",   Location.Type.ADMIN));
        g.addLocation(new Location("LIB",   "Central Library",        Location.Type.ACADEMIC));
        g.addLocation(new Location("CSE",   "Computer Science Block", Location.Type.ACADEMIC));
        g.addLocation(new Location("ECE",   "Electronics Block",      Location.Type.ACADEMIC));
        g.addLocation(new Location("MECH",  "Mechanical Workshop",    Location.Type.ACADEMIC));
        g.addLocation(new Location("LAB",   "Research Labs",          Location.Type.RESEARCH));
        g.addLocation(new Location("OBS",   "Observatory",            Location.Type.RESEARCH));
        g.addLocation(new Location("AUD",   "Auditorium",             Location.Type.EVENT));
        g.addLocation(new Location("CAF",   "Main Cafeteria",         Location.Type.FOOD));
        g.addLocation(new Location("FC",    "Food Court",             Location.Type.FOOD));
        g.addLocation(new Location("HOS1",  "Boys Hostel",            Location.Type.HOSTEL));
        g.addLocation(new Location("HOS2",  "Girls Hostel",           Location.Type.HOSTEL));
        g.addLocation(new Location("SPORT", "Sports Complex",         Location.Type.SPORTS));
        g.addLocation(new Location("GYM",   "Gymnasium",              Location.Type.SPORTS));
        g.addLocation(new Location("MED",   "Health Centre",          Location.Type.MEDICAL));

        // addRoad(from, to, metres, accessible?)   accessible=false -> stairs / steep slope
        g.addRoad("GATE", "PARK",  80, true);
        g.addRoad("GATE", "ADM",  150, true);
        g.addRoad("GATE", "ATM",  120, true);
        g.addRoad("ADM",  "ATM",  100, true);
        g.addRoad("ADM",  "LIB",  200, true);
        g.addRoad("ADM",  "AUD",  180, true);
        g.addRoad("ADM",  "MED",  170, true);
        g.addRoad("PARK", "MED",  160, true);
        g.addRoad("LIB",  "AUD",  140, false);
        g.addRoad("LIB",  "CSE",  120, true);
        g.addRoad("LIB",  "CAF",  160, true);
        g.addRoad("CSE",  "CAF",  190, false);
        g.addRoad("CSE",  "ECE",   90, true);
        g.addRoad("CSE",  "LAB",  130, true);
        g.addRoad("ECE",  "LAB",  100, false);
        g.addRoad("ECE",  "MECH", 110, true);
        g.addRoad("LAB",  "OBS",  300, true);
        g.addRoad("MECH", "SPORT",250, true);
        g.addRoad("CAF",  "FC",    70, true);
        g.addRoad("CAF",  "HOS1", 220, true);
        g.addRoad("FC",   "HOS2", 200, true);
        g.addRoad("AUD",  "FC",   210, true);
        g.addRoad("HOS1", "HOS2", 150, true);
        g.addRoad("HOS1", "GYM",  140, true);
        g.addRoad("GYM",  "SPORT",100, true);
        g.addRoad("MED",  "HOS2", 180, true);
        return g;
    }
}
