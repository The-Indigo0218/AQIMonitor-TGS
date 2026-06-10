package com.aqimonitor.domain;

import java.util.Map;

public final class AqiCalculator {

    private AqiCalculator() {}

    public static int calculateAqi(Pollutant pollutant, double concentration) {
        if (concentration < 0) {
            throw new IllegalArgumentException("Concentration cannot be negative: " + concentration);
        }

        Breakpoint bp = BreakpointTable.findBreakpoint(pollutant, concentration);

        double cLow = bp.concentrationLow();
        double cHigh = bp.concentrationHigh();
        int iLow = bp.aqiLow();
        int iHigh = bp.aqiHigh();

        double aqi = ((double) (iHigh - iLow) / (cHigh - cLow)) * (concentration - cLow) + iLow;
        return (int) Math.round(aqi);
    }

    public static int calculateOverallAqi(Map<Pollutant, Double> concentrations) {
        return concentrations.entrySet().stream()
            .mapToInt(entry -> calculateAqi(entry.getKey(), entry.getValue()))
            .max()
            .orElse(0);
    }

    public static AirQualityCategory categorize(int aqi) {
        if (aqi <= 50) return AirQualityCategory.GOOD;
        if (aqi <= 100) return AirQualityCategory.MODERATE;
        if (aqi <= 150) return AirQualityCategory.UNHEALTHY_SENSITIVE;
        if (aqi <= 200) return AirQualityCategory.UNHEALTHY;
        if (aqi <= 300) return AirQualityCategory.VERY_UNHEALTHY;
        return AirQualityCategory.HAZARDOUS;
    }
}