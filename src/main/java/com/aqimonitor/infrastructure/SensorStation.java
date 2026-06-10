package com.aqimonitor.infrastructure;

import com.aqimonitor.application.ReadingCollector;
import com.aqimonitor.domain.Pollutant;
import com.aqimonitor.domain.SensorReading;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SensorStation implements Runnable {
    private final String stationId;
    private final ReadingCollector collector;
    private final Random random = new Random();
    private final AtomicBoolean running = new AtomicBoolean(true);

    private static final double PM25_BASE = 15.0;
    private static final double PM10_BASE = 40.0;
    private static final double NO2_BASE = 30.0;

    private static final double PM25_VARIANCE = 25.0;
    private static final double PM10_VARIANCE = 60.0;
    private static final double NO2_VARIANCE = 80.0;

    public SensorStation(String stationId, ReadingCollector collector) {
        this.stationId = stationId;
        this.collector = collector;
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

        double pm25 = Math.max(0, PM25_BASE + random.nextGaussian() * PM25_VARIANCE);
        double pm10 = Math.max(0, PM10_BASE + random.nextGaussian() * PM10_VARIANCE);
        double no2 = Math.max(0, NO2_BASE + random.nextGaussian() * NO2_VARIANCE);

        builder.concentration(Pollutant.PM25, pm25)
               .concentration(Pollutant.PM10, pm10)
               .concentration(Pollutant.NO2, no2);

        return builder.build();
    }
}