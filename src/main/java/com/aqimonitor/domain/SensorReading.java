package com.aqimonitor.domain;

import java.time.Instant;
import java.util.Map;
import java.util.EnumMap;

public record SensorReading(
    String stationId,
    Instant timestamp,
    Map<Pollutant, Double> concentrations
) {
    public SensorReading(String stationId, Instant timestamp, Map<Pollutant, Double> concentrations) {
        this.stationId = stationId;
        this.timestamp = timestamp;
        this.concentrations = Map.copyOf(concentrations);
    }

    public int getAqi(Pollutant pollutant) {
        Double conc = concentrations.get(pollutant);
        return conc != null ? AqiCalculator.calculateAqi(pollutant, conc) : 0;
    }

    public int getOverallAqi() {
        return AqiCalculator.calculateOverallAqi(concentrations);
    }

    public AirQualityCategory getCategory() {
        return AqiCalculator.categorize(getOverallAqi());
    }

    public static Builder builder(String stationId) {
        return new Builder(stationId);
    }

    public static class Builder {
        private final String stationId;
        private Instant timestamp;
        private final EnumMap<Pollutant, Double> concentrations = new EnumMap<>(Pollutant.class);

        private Builder(String stationId) {
            this.stationId = stationId;
            this.timestamp = Instant.now();
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder concentration(Pollutant pollutant, double value) {
            concentrations.put(pollutant, value);
            return this;
        }

        public SensorReading build() {
            return new SensorReading(stationId, timestamp, concentrations);
        }
    }
}