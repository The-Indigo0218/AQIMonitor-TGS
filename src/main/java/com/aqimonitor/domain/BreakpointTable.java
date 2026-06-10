package com.aqimonitor.domain;

import java.util.List;
import java.util.Map;
import java.util.EnumMap;

public final class BreakpointTable {
    private static final Map<Pollutant, List<Breakpoint>> TABLE = new EnumMap<>(Pollutant.class);

    static {
        TABLE.put(Pollutant.PM25, List.of(
            new Breakpoint(0.0, 12.0, 0, 50),
            new Breakpoint(12.1, 35.4, 51, 100),
            new Breakpoint(35.5, 55.4, 101, 150),
            new Breakpoint(55.5, 150.4, 151, 200),
            new Breakpoint(150.5, 250.4, 201, 300),
            new Breakpoint(250.5, 500.4, 301, 500)
        ));

        TABLE.put(Pollutant.PM10, List.of(
            new Breakpoint(0, 54, 0, 50),
            new Breakpoint(55, 154, 51, 100),
            new Breakpoint(155, 254, 101, 150),
            new Breakpoint(255, 354, 151, 200),
            new Breakpoint(355, 424, 201, 300),
            new Breakpoint(425, 604, 301, 500)
        ));

        TABLE.put(Pollutant.NO2, List.of(
            new Breakpoint(0, 53, 0, 50),
            new Breakpoint(54, 100, 51, 100),
            new Breakpoint(101, 360, 101, 150),
            new Breakpoint(361, 649, 151, 200),
            new Breakpoint(650, 1249, 201, 300),
            new Breakpoint(1250, 2049, 301, 500)
        ));
    }

    private BreakpointTable() {}

    public static List<Breakpoint> getBreakpoints(Pollutant pollutant) {
        return TABLE.getOrDefault(pollutant, List.of());
    }

    public static Breakpoint findBreakpoint(Pollutant pollutant, double concentration) {
        return getBreakpoints(pollutant).stream()
            .filter(bp -> bp.contains(concentration))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Concentration " + concentration + " out of range for " + pollutant));
    }
}