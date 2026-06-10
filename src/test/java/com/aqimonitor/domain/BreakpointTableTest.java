package com.aqimonitor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Breakpoint Table Tests")
class BreakpointTableTest {

    @Test
    @DisplayName("All pollutants should have breakpoints defined")
    void testAllPollutantsHaveBreakpoints() {
        for (Pollutant pollutant : Pollutant.values()) {
            List<Breakpoint> breakpoints = BreakpointTable.getBreakpoints(pollutant);
            assertFalse(breakpoints.isEmpty(), pollutant + " should have breakpoints defined");
            assertEquals(6, breakpoints.size(), pollutant + " should have exactly 6 breakpoints");
        }
    }

    @Test
    @DisplayName("Breakpoints should be contiguous (no gaps)")
    void testBreakpointsAreContiguous() {
        for (Pollutant pollutant : Pollutant.values()) {
            List<Breakpoint> breakpoints = BreakpointTable.getBreakpoints(pollutant);
            for (int i = 0; i < breakpoints.size() - 1; i++) {
                Breakpoint current = breakpoints.get(i);
                Breakpoint next = breakpoints.get(i + 1);

                double expectedNextLow = pollutant == Pollutant.PM25
                    ? current.concentrationHigh() + 0.1
                    : current.concentrationHigh() + 1.0;

                assertEquals(expectedNextLow, next.concentrationLow(), 0.01,
                    "Gap found in " + pollutant + " between " + current + " and " + next);

                assertEquals(current.aqiHigh() + 1, next.aqiLow(),
                    "AQI gap found in " + pollutant + " between " + current + " and " + next);
            }
        }
    }

    @Test
    @DisplayName("Breakpoints should have valid ranges")
    void testBreakpointRanges() {
        for (Pollutant pollutant : Pollutant.values()) {
            List<Breakpoint> breakpoints = BreakpointTable.getBreakpoints(pollutant);
            for (Breakpoint bp : breakpoints) {
                assertTrue(bp.concentrationLow() <= bp.concentrationHigh(),
                    "Invalid range in " + pollutant + ": " + bp);
                assertTrue(bp.aqiLow() <= bp.aqiHigh(),
                    "Invalid AQI range in " + pollutant + ": " + bp);
                assertTrue(bp.aqiLow() >= 0, "AQI low should be non-negative");
                assertTrue(bp.aqiHigh() <= 500, "AQI high should not exceed 500");
            }
        }
    }

    @Test
    @DisplayName("findBreakpoint should return correct breakpoint for known values")
    void testFindBreakpoint() {
        Breakpoint bp = BreakpointTable.findBreakpoint(Pollutant.PM25, 35.4);
        assertEquals(35.4, bp.concentrationHigh(), 0.01);
        assertEquals(100, bp.aqiHigh());

        bp = BreakpointTable.findBreakpoint(Pollutant.PM25, 35.5);
        assertEquals(35.5, bp.concentrationLow(), 0.01);
        assertEquals(101, bp.aqiLow());
    }

    @Test
    @DisplayName("findBreakpoint should throw for out of range values")
    void testFindBreakpointOutOfRange() {
        assertThrows(IllegalArgumentException.class, () ->
            BreakpointTable.findBreakpoint(Pollutant.PM25, -1.0));

        assertThrows(IllegalArgumentException.class, () ->
            BreakpointTable.findBreakpoint(Pollutant.PM25, 600.0));
    }
}