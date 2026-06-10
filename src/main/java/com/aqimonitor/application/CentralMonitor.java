package com.aqimonitor.application;

import com.aqimonitor.domain.AlertEvent;
import com.aqimonitor.domain.SensorReading;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class CentralMonitor implements ReadingCollector {
    private final AlertNotifier alertNotifier;
    private final Map<String, SensorReading> latestReadings = new ConcurrentHashMap<>();
    private final Consumer<String> logConsumer;

    public CentralMonitor(AlertNotifier alertNotifier, Consumer<String> logConsumer) {
        this.alertNotifier = alertNotifier;
        this.logConsumer = logConsumer;
    }

    @Override
    public void collect(SensorReading reading) {
        latestReadings.put(reading.stationId(), reading);
        logConsumer.accept(formatReading(reading));

        if (reading.getCategory().requiresUrgentMitigation()) {
            AlertEvent alert = AlertEvent.createUrgentAlert(reading.stationId(), reading);
            alertNotifier.notify(alert);
        }
    }

    public SensorReading getLatestReading(String stationId) {
        return latestReadings.get(stationId);
    }

    public int getGlobalAqi() {
        return latestReadings.values().stream()
            .mapToInt(SensorReading::getOverallAqi)
            .max()
            .orElse(0);
    }

    public com.aqimonitor.domain.AirQualityCategory getGlobalCategory() {
        return com.aqimonitor.domain.AqiCalculator.categorize(getGlobalAqi());
    }

    private String formatReading(SensorReading reading) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] Station: %s | AQI: %d (%s)",
            reading.timestamp().toString().substring(11, 19),
            reading.stationId(),
            reading.getOverallAqi(),
            reading.getCategory().getLabel()));
        reading.concentrations().forEach((p, c) ->
            sb.append(String.format(" | %s: %.1f %s", p.getDisplayName(), c, p.getUnit())));
        return sb.toString();
    }
}