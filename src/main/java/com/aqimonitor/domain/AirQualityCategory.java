package com.aqimonitor.domain;

public enum AirQualityCategory {
    GOOD(0, 50, "Buena", "Calidad del aire satisfactoria, riesgo mínimo"),
    MODERATE(51, 100, "Moderada", "Calidad del aire aceptable, riesgo moderado para personas sensibles"),
    UNHEALTHY_SENSITIVE(101, 150, "Dañina para grupos sensibles", "Personas con enfermedades respiratorias/cardiacas, niños y ancianos deben limitar esfuerzos"),
    UNHEALTHY(151, 200, "Dañina", "Todos pueden experimentar efectos; grupos sensibles efectos más graves"),
    VERY_UNHEALTHY(201, 300, "Muy Dañina", "Alerta de salud: todos pueden experimentar efectos graves"),
    HAZARDOUS(301, 500, "Peligrosa", "Emergencia de salud: toda la población afectada");

    private final int aqiLow;
    private final int aqiHigh;
    private final String label;
    private final String description;

    AirQualityCategory(int aqiLow, int aqiHigh, String label, String description) {
        this.aqiLow = aqiLow;
        this.aqiHigh = aqiHigh;
        this.label = label;
        this.description = description;
    }

    public int getAqiLow() { return aqiLow; }
    public int getAqiHigh() { return aqiHigh; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }

    public boolean requiresUrgentMitigation() {
        return this == UNHEALTHY || this == VERY_UNHEALTHY || this == HAZARDOUS;
    }
}