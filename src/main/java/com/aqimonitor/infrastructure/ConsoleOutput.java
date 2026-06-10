package com.aqimonitor.infrastructure;

import java.util.function.Consumer;

public final class ConsoleOutput {
    private static final Object LOCK = new Object();

    public static Consumer<String> createLogConsumer() {
        return message -> {
            synchronized (LOCK) {
                System.out.println(message);
            }
        };
    }

    public static Consumer<String> createAlertConsumer() {
        return message -> {
            synchronized (LOCK) {
                System.err.println(message);
            }
        };
    }
}