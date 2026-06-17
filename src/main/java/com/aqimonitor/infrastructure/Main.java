package com.aqimonitor.infrastructure;

import com.aqimonitor.application.CentralMonitor;
import com.aqimonitor.application.AlertService;
import com.aqimonitor.application.ReadingCollector;
import com.aqimonitor.domain.AqiCalculator;
import com.aqimonitor.domain.SensorReading;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║     SISTEMA DE MONITOREO Y ALERTA DE CALIDAD DEL AIRE        ║");
        System.out.println("║                    Teoría General de Sistemas                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("  3 zonas monitoreadas  |  escala: 1 segundo = 1 hora simulada");
        System.out.println("  Presiona Ctrl+C para detener y ver el resumen final.");
        System.out.println();

        Consumer<String> logConsumer = ConsoleOutput.createLogConsumer();
        Consumer<String> alertConsumer = ConsoleOutput.createAlertConsumer();

        AlertService alertService = new AlertService(alertConsumer);
        CentralMonitor centralMonitor = new CentralMonitor(alertService, logConsumer);
        ReadingCollector collector = centralMonitor;

        List<SensorStation> stations = List.of(
            new SensorStation("EST-001-INDUSTRIAL", ZoneProfile.INDUSTRIAL, collector),
            new SensorStation("EST-002-RESIDENCIAL", ZoneProfile.RESIDENCIAL, collector),
            new SensorStation("EST-003-AUTOPISTA", ZoneProfile.AUTOPISTA, collector)
        );

        Instant startTime = Instant.now();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (SensorStation station : stations) {
                executor.submit(station);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n  Deteniendo simulación...");
                stations.forEach(SensorStation::stop);
                try {
                    executor.shutdown();
                    executor.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                printSummary(centralMonitor, stations, startTime);
            }));

            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    private static void printSummary(CentralMonitor monitor, List<SensorStation> stations,
                                     Instant startTime) {
        long elapsedSeconds = Duration.between(startTime, Instant.now()).toSeconds();
        int globalAqi = monitor.getGlobalAqi();
        String sep = "─".repeat(82);

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                              RESUMEN FINAL DE SIMULACIÓN                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.printf("  Duración real    : %d segundos%n", elapsedSeconds);
        System.out.printf("  Tiempo simulado  : ~%d horas  (escala 1 s = 1 h)%n", elapsedSeconds);
        System.out.printf("  Lecturas totales : %d  (%d por estación)%n",
            monitor.getTotalReadings(), elapsedSeconds);
        System.out.printf("  Alertas emitidas : %d  (AQI > 150 en alguna estación)%n",
            monitor.getTotalAlerts());
        System.out.println();
        System.out.println("  Estado final por estación:");
        System.out.println("  " + sep);
        System.out.printf("  %-20s │ %4s │ %-31s │ %s%n",
            "Estación", "AQI", "Categoría", "Contaminante dominante");
        System.out.println("  " + sep);

        Map<String, SensorReading> readings = monitor.getAllLatestReadings();
        stations.forEach(station -> {
            String id = station.getStationId();
            SensorReading r = readings.get(id);
            if (r == null) {
                System.out.printf("  %-20s │  --- │ %-31s │ %s%n", id, "(sin datos)", "");
                return;
            }
            String dominant = r.concentrations().entrySet().stream()
                .max(Comparator.comparingInt(e ->
                    AqiCalculator.calculateAqi(e.getKey(), e.getValue())))
                .map(e -> String.format("%s: %.1f %s",
                    e.getKey().getDisplayName(), e.getValue(), e.getKey().getUnit()))
                .orElse("-");
            System.out.printf("  %-20s │ %4d │ %-31s │ %s%n",
                id, r.getOverallAqi(), r.getCategory().getLabel(), dominant);
        });

        System.out.println("  " + sep);
        System.out.printf("  %-20s │ %4d │ %-31s │%n",
            "AQI GLOBAL (ciudad)", globalAqi, monitor.getGlobalCategory().getLabel());
        System.out.println("  " + sep);
        System.out.println();
        System.out.println("  " + monitor.getGlobalCategory().getDescription());
        System.out.println();
    }
}
