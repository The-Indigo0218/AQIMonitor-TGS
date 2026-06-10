package com.aqimonitor.application;

import com.aqimonitor.domain.AlertEvent;
import java.util.function.Consumer;

public final class AlertService implements AlertNotifier {
    private final Consumer<String> alertConsumer;

    public AlertService(Consumer<String> alertConsumer) {
        this.alertConsumer = alertConsumer;
    }

    @Override
    public void notify(AlertEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 ALERTA URGENTE 🚨\n");
        sb.append(String.format("Estación: %s | AQI: %d | Categoría: %s\n",
            event.stationId(), event.aqi(), event.category().getLabel()));
        sb.append("Acciones de mitigación:\n");
        event.mitigationActions().forEach(action ->
            sb.append(String.format("  - %s\n", action)));
        alertConsumer.accept(sb.toString());
    }
}