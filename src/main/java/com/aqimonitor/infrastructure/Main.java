package com.aqimonitor.infrastructure;

import com.aqimonitor.application.CentralMonitor;
import com.aqimonitor.application.AlertService;
import com.aqimonitor.application.ReadingCollector;

import java.util.ArrayList;
import java.util.List;
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
        System.out.println();

        Consumer<String> logConsumer = ConsoleOutput.createLogConsumer();
        Consumer<String> alertConsumer = ConsoleOutput.createAlertConsumer();

        AlertService alertService = new AlertService(alertConsumer);
        CentralMonitor centralMonitor = new CentralMonitor(alertService, logConsumer);
        ReadingCollector collector = centralMonitor;

        List<SensorStation> stations = List.of(
            new SensorStation("EST-001-CENTRO", collector),
            new SensorStation("EST-002-NORTE", collector),
            new SensorStation("EST-003-SUR", collector),
            new SensorStation("EST-004-ESTE", collector),
            new SensorStation("EST-005-OESTE", collector)
        );

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (SensorStation station : stations) {
                executor.submit(station);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n⏹️  Deteniendo simulación...");
                stations.forEach(SensorStation::stop);
                try {
                    executor.shutdown();
                    executor.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                printSummary(centralMonitor);
            }));

            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    private static void printSummary(CentralMonitor monitor) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      RESUMEN FINAL                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.printf("AQI Global: %d (%s)%n",
            monitor.getGlobalAqi(), monitor.getGlobalCategory().getLabel());
    }
}