package com.aqimonitor.infrastructure;

import com.aqimonitor.domain.Pollutant;

import java.util.EnumMap;
import java.util.Map;

/**
 * Perfil de emisión de una zona urbana. Define el nivel base y la variabilidad
 * (desviación estándar gaussiana) de cada contaminante según el uso del suelo.
 *
 * <p>Representa la <b>entrada</b> del sistema: cada zona inyecta concentraciones
 * con un comportamiento distinto, de modo que la red de estaciones refleje la
 * heterogeneidad real de una ciudad (industria, vivienda, tráfico).</p>
 */
public enum ZoneProfile {

    INDUSTRIAL(
        "Zona Industrial",
        "Predominan partículas finas y gruesas por procesos de combustión y manufactura.",
        38.0, 18.0,    // PM2.5: base, varianza
        120.0, 60.0,   // PM10 : base, varianza
        50.0, 40.0),   // NO₂  : base, varianza

    RESIDENCIAL(
        "Zona Residencial",
        "Bajo nivel de contaminación: tráfico ligero y calefacción doméstica.",
        8.0, 6.0,
        20.0, 15.0,
        12.0, 10.0),

    AUTOPISTA(
        "Corredor de Autopistas",
        "Alto NO₂ por tráfico vehicular intenso; partículas por desgaste de frenos y neumáticos.",
        22.0, 12.0,
        60.0, 30.0,
        200.0, 120.0);

    private final String label;
    private final String description;
    /** Cada entrada almacena el par {base, varianza} del contaminante. */
    private final Map<Pollutant, double[]> profile;

    ZoneProfile(String label, String description,
                double pm25Base, double pm25Variance,
                double pm10Base, double pm10Variance,
                double no2Base, double no2Variance) {
        this.label = label;
        this.description = description;
        this.profile = new EnumMap<>(Pollutant.class);
        profile.put(Pollutant.PM25, new double[]{pm25Base, pm25Variance});
        profile.put(Pollutant.PM10, new double[]{pm10Base, pm10Variance});
        profile.put(Pollutant.NO2, new double[]{no2Base, no2Variance});
    }

    /** Nivel base (media) del contaminante en esta zona. */
    public double base(Pollutant pollutant) {
        return profile.get(pollutant)[0];
    }

    /** Variabilidad (desviación estándar) del contaminante en esta zona. */
    public double variance(Pollutant pollutant) {
        return profile.get(pollutant)[1];
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
