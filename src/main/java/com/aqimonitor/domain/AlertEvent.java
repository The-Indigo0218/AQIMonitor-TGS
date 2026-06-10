package com.aqimonitor.domain;

import java.time.Instant;
import java.util.List;

public record AlertEvent(
    String stationId,
    Instant timestamp,
    AirQualityCategory category,
    int aqi,
    List<String> mitigationActions
) {
    public static AlertEvent createUrgentAlert(String stationId, SensorReading reading) {
        List<String> actions = switch (reading.getCategory()) {
            case UNHEALTHY -> List.of("Restringir tráfico vehicular", "Limitar actividades al aire libre");
            case VERY_UNHEALTHY -> List.of("Restringir tráfico vehicular", "Alerta industrial: reducir emisiones", "Suspender clases al aire libre");
            case HAZARDOUS -> List.of("Evacuación de zonas críticas", "Cierre de industrias contaminantes", "Alerta máxima: permanecer en interiores");
            default -> List.of();
        };
        return new AlertEvent(stationId, reading.timestamp(), reading.getCategory(), reading.getOverallAqi(), actions);
    }
}