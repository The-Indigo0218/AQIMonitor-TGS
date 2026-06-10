package com.aqimonitor.domain;

public record Breakpoint(
    double concentrationLow,
    double concentrationHigh,
    int aqiLow,
    int aqiHigh
) {
    public boolean contains(double concentration) {
        return concentration >= concentrationLow && concentration <= concentrationHigh;
    }
}