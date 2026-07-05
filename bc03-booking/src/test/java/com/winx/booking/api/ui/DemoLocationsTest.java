package com.winx.booking.api.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DemoLocationsTest {

    @Test
    void findReturnsKnownLocation() {
        DemoLocations.Location loc = DemoLocations.find("DORTMUND_HBF");
        assertNotNull(loc);
        assertEquals("Dortmund Hauptbahnhof", loc.displayName());
    }

    @Test
    void findUnknownOrNullReturnsNull() {
        assertNull(DemoLocations.find("NOPE"));
        assertNull(DemoLocations.find(null));
    }
}
