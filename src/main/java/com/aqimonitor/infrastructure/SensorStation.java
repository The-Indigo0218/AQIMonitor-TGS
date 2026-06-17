package com.aqimonitor.infrastructure;

import com.aqimonitor.application.ReadingCollector;
import com.aqimonitor.domain.Pollutant;
import com.aqimonitor.domain.SensorReading;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SensorStation implements Runnable {
    private final String stationId;
    private final ZoneProfile zone;
    private final ReadingCollector collector;
    private final Random random = new Random();
    private final AtomicBoolean running = new AtomicBoolean(true);

    public SensorStation(String stationId, ZoneProfile zone, ReadingCollector collector) {
        this.stationId = stationId;
        this.zone = zone;
        this.collector = collector;
    }

    public String getStationId() {
        return stationId;
    }

    public ZoneProfile getZone() {
        return zone;
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                SensorReading reading = generateReading();
                collector.collect(reading);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running.set(false);
    }

    private SensorReading generateReading() {
        var builder = SensorReading.builder(stationId)
            .timestamp(Instant.now());

        for (Pollutant pollutant : Pollutant.values()) {
            double value = Math.max(0,
                zone.base(pollutant) + random.nextGaussian() * zone.variance(pollutant));
            builder.concentration(pollutant, value);
        }

        return builder.build();
    }
}