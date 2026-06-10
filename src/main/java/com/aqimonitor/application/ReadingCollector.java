package com.aqimonitor.application;

import com.aqimonitor.domain.SensorReading;

@FunctionalInterface
public interface ReadingCollector {
    void collect(SensorReading reading);
}