package com.aqimonitor.domain;

public enum Pollutant {
    PM25("PM2.5", "µg/m³"),
    PM10("PM10", "µg/m³"),
    NO2("NO₂", "ppb");

    private final String displayName;
    private final String unit;

    Pollutant(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }
}