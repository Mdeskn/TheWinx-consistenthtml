package com.winx.booking.api.ui;

import java.util.List;

/**
 * Static list of named demo locations (≈ Dortmund / Fleet's area) for the search dropdown.
*/
public final class DemoLocations {

    public record Location(String key, String displayName, double latitude, double longitude) {
    }

    private static final List<Location> LOCATIONS = List.of(
            new Location("DORTMUND_HBF", "Dortmund Hauptbahnhof", 51.5178, 7.4590),
            new Location("CITY_CENTER", "City Center (Reinoldikirche)", 51.5142, 7.4660),
            new Location("FH_DORTMUND", "TU Dortmund", 51.4928, 7.4140),
            new Location("WESTFALENPARK", "Westfalenpark", 51.4920, 7.4730),
            new Location("PHOENIX_SEE", "Phoenix See (Hörde)", 51.4900, 7.5100)
    );

    private DemoLocations() {
    }

    public static List<Location> all() {
        return LOCATIONS;
    }

    /** Resolves a location key to its coordinates, or {@code null} if unknown. */
    public static Location find(String key) {
        if (key == null) {
            return null;
        }
        return LOCATIONS.stream().filter(l -> l.key().equals(key)).findFirst().orElse(null);
    }
}
