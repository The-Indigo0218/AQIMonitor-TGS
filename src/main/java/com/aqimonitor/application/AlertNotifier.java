package com.aqimonitor.application;

import com.aqimonitor.domain.AlertEvent;

@FunctionalInterface
public interface AlertNotifier {
    void notify(AlertEvent event);
}