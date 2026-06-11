# Arquitectura del Sistema

> Este documento está dirigido a **integrantes técnicos** del equipo. Explica cómo está organizado el código, por qué se tomaron las decisiones de diseño y qué hace cada clase.

---

## Principio de Diseño: Arquitectura Hexagonal (Puertos y Adaptadores)

El proyecto aplica **Arquitectura Limpia** (también llamada Hexagonal o de Puertos y Adaptadores). La idea central es que las reglas de negocio (dominio) no deben depender de nada externo: ni de hilos, ni de la consola, ni de bases de datos. Los detalles externos dependen del dominio, nunca al revés.

```
┌─────────────────────────────────────────────────┐
│                INFRASTRUCTURE                   │
│  (hilos, consola, Main — detalles del mundo)    │
│  ┌─────────────────────────────────────────┐    │
│  │           APPLICATION                   │    │
│  │  (orquestación, casos de uso)           │    │
│  │  ┌───────────────────────────────────┐  │    │
│  │  │            DOMAIN                 │  │    │
│  │  │  (fórmula AQI, categorías,        │  │    │
│  │  │   reglas puras — sin dependencias)│  │    │
│  │  └───────────────────────────────────┘  │    │
│  └─────────────────────────────────────────┘    │
└─────────────────────────────────────────────────┘
```

**Las dependencias siempre apuntan hacia adentro.** El dominio no importa nada de la aplicación ni de la infraestructura. La infraestructura importa de la aplicación y el dominio, pero nunca al revés.

---

## Capa 1 — Dominio (`com.aqimonitor.domain`)

Esta capa contiene la lógica pura del negocio. No tiene `Thread.sleep`, no escribe en consola, no conoce Maven ni JUnit. Solo matemáticas y reglas.

### `Pollutant` (enum)

Representa los tres contaminantes monitoreados. Cada constante lleva su nombre para mostrar y su unidad de medida.

```
PM25  →  "PM2.5"  →  "µg/m³"
PM10  →  "PM10"   →  "µg/m³"
NO2   →  "NO₂"    →  "ppb"
```

### `Breakpoint` (record)

Un segmento de la tabla de la EPA: rango de concentración ↔ rango AQI.

```java
record Breakpoint(
    double concentrationLow,
    double concentrationHigh,
    int aqiLow,
    int aqiHigh
)
```

Contiene `contains(double)` que devuelve `true` si una concentración cae dentro del segmento.

### `BreakpointTable`

Clase estática que almacena los 6 breakpoints de cada uno de los 3 contaminantes en un `EnumMap`. Expone dos métodos:
- `getBreakpoints(Pollutant)` → lista completa de breakpoints
- `findBreakpoint(Pollutant, double)` → el breakpoint específico para una concentración dada (lanza `IllegalArgumentException` si está fuera de rango)

### `AqiCalculator`

Clase de utilidad estática que implementa la fórmula EPA. Antes de buscar el breakpoint y aplicar la interpolación, normaliza la concentración a la precisión que exige la EPA para cada contaminante:

```java
// Normalización de precisión por contaminante (EPA)
double c = switch (pollutant) {
    case PM25 -> Math.round(concentration * 10.0) / 10.0; // 1 decimal
    case PM10, NO2 -> Math.floor(concentration);           // entero
};

// Fórmula de interpolación lineal sobre el valor normalizado
double aqi = ((iHigh - iLow) / (cHigh - cLow)) * (c - cLow) + iLow;
```

Sin esta normalización, valores flotantes del sensor como `54.867 µg/m³` (PM10) caen en el hueco entre los rangos enteros `[0, 54]` y `[55, 154]`, lanzando una excepción en tiempo de ejecución.

Tres métodos públicos:
| Método | Descripción |
|---|---|
| `calculateAqi(Pollutant, double)` | AQI de un contaminante a una concentración dada |
| `calculateOverallAqi(Map<Pollutant, Double>)` | Máximo AQI entre todos los contaminantes |
| `categorize(int)` | Convierte un número AQI en una `AirQualityCategory` |

### `AirQualityCategory` (enum)

Las 6 categorías de calidad del aire. Cada constante lleva su rango AQI, etiqueta en español y descripción de impacto en salud.

Método clave: `requiresUrgentMitigation()` → devuelve `true` para `UNHEALTHY`, `VERY_UNHEALTHY` y `HAZARDOUS`.

### `SensorReading` (record)

Captura inmutable de los datos emitidos por una estación en un instante. Contiene:
- `stationId` — identificador de la estación
- `timestamp` — momento de la lectura (`Instant`)
- `concentrations` — mapa `Pollutant → Double` (copia inmutable con `Map.copyOf`)

Tiene un Builder fluido para construirlo campo a campo.

### `AlertEvent` (record)

Evento de dominio que se crea cuando una lectura supera el umbral de mitigación. Incluye el listado de acciones de mitigación apropiado según la categoría:

| Categoría | Acciones |
|---|---|
| UNHEALTHY (151–200) | Restringir tráfico, limitar actividades al aire libre |
| VERY_UNHEALTHY (201–300) | + Alerta industrial, suspender clases al aire libre |
| HAZARDOUS (301+) | Evacuación, cierre de industrias, alerta máxima |

---

## Capa 2 — Aplicación (`com.aqimonitor.application`)

Orquesta el flujo de datos. Define los **puertos** (interfaces) que desacoplan la aplicación de sus implementaciones concretas.

### `ReadingCollector` (interfaz — puerto de entrada)

```java
@FunctionalInterface
interface ReadingCollector {
    void collect(SensorReading reading);
}
```

Las estaciones no saben que existe un `CentralMonitor`. Solo saben que hay alguien que implementa `ReadingCollector`.

### `AlertNotifier` (interfaz — puerto de salida)

```java
@FunctionalInterface
interface AlertNotifier {
    void notify(AlertEvent event);
}
```

El `CentralMonitor` no sabe cómo se formatean las alertas ni adónde van. Solo llama a `AlertNotifier`.

### `CentralMonitor` (implementa `ReadingCollector`)

El núcleo de la aplicación. Cuando recibe una lectura:

1. La almacena en un `ConcurrentHashMap<String, SensorReading>` (thread-safe porque llegan de múltiples hilos).
2. Incrementa los contadores de lecturas totales y, si aplica, de alertas emitidas.
3. La formatea y delega al `logConsumer` para imprimirla.
4. Si la categoría requiere mitigación urgente, crea un `AlertEvent` y llama al `alertNotifier`.

Métodos de consulta disponibles para el resumen final:

| Método | Descripción |
|---|---|
| `getGlobalAqi()` | Peor AQI entre todas las estaciones |
| `getGlobalCategory()` | Categoría correspondiente al AQI global |
| `getAllLatestReadings()` | Mapa estación → última lectura (vista no modificable) |
| `getTotalReadings()` | Total acumulado de lecturas procesadas |
| `getTotalAlerts()` | Total de alertas urgentes emitidas durante la sesión |

### `AlertService` (implementa `AlertNotifier`)

Formatea el `AlertEvent` como un bloque de texto legible y lo pasa al `alertConsumer` (que en producción escribe a `System.err`).

---

## Capa 3 — Infraestructura (`com.aqimonitor.infrastructure`)

Adapta el mundo externo (hilos del OS, consola) al sistema.

### `SensorStation` (implementa `Runnable`)

Cada estación es una tarea ejecutable independiente. En cada iteración del bucle:

1. Genera concentraciones usando una distribución normal gaussiana (`random.nextGaussian()`).
2. Construye un `SensorReading` con esos valores.
3. Llama a `collector.collect(reading)`.
4. Duerme 1 000 ms (1 segundo real = 1 hora de simulación).

Los valores base y varianza configurados:

| Contaminante | Base | Varianza (σ) |
|---|---|---|
| PM2.5 | 15.0 µg/m³ | 25.0 |
| PM10 | 40.0 µg/m³ | 60.0 |
| NO₂ | 30.0 ppb | 80.0 |

La varianza alta produce ocasionalmente picos que cruzan el umbral de alerta, simulando episodios de contaminación.

### `ConsoleOutput`

Fábrica de dos `Consumer<String>`: uno para logs normales (`System.out`) y otro para alertas urgentes (`System.err`). Ambos sincronizan sobre un objeto compartido (`LOCK`) para evitar entrelazado de caracteres cuando varios hilos escriben simultáneamente.

### `Main`

Punto de entrada del programa. Responsabilidades:

1. Imprimir banner de inicio.
2. Crear el grafo de dependencias: `ConsoleOutput` → `AlertService` → `CentralMonitor` → `SensorStation[]`.
3. Abrir un `ExecutorService` con `newVirtualThreadPerTaskExecutor()` y enviar cada estación.
4. Registrar un `ShutdownHook` que detiene las estaciones limpiamente al recibir `Ctrl+C` e imprime el resumen final.

---

## Virtual Threads (Project Loom)

Java 21 introdujo los **Virtual Threads**: hilos ligeros gestionados por la JVM, no por el sistema operativo. Permiten crear miles de hilos sin el overhead de los OS threads.

```java
// Sin Virtual Threads (OS threads — costoso a escala)
ExecutorService executor = Executors.newFixedThreadPool(5);

// Con Virtual Threads (JVM threads — ligeros)
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

Cada estación corre en su propio Virtual Thread. El `Thread.sleep(1000)` dentro de `SensorStation.run()` libera el portador (carrier thread del OS) durante la espera, en lugar de bloquearlo, lo que hace la simulación eficiente incluso si escalamos a cientos de estaciones.

---

## Diagrama de Clases Simplificado

```
domain/
  Pollutant (enum)
  Breakpoint (record)
  BreakpointTable
      └── usa → Breakpoint, Pollutant
  AqiCalculator
      └── usa → BreakpointTable, AirQualityCategory
  AirQualityCategory (enum)
  SensorReading (record)
      └── usa → AqiCalculator, Pollutant
  AlertEvent (record)
      └── usa → AirQualityCategory, SensorReading

application/
  ReadingCollector (interface)  ← puerto de entrada
  AlertNotifier (interface)     ← puerto de salida
  CentralMonitor
      └── implementa → ReadingCollector
      └── usa → AlertNotifier, SensorReading, AlertEvent
  AlertService
      └── implementa → AlertNotifier
      └── usa → AlertEvent

infrastructure/
  SensorStation
      └── implementa → Runnable
      └── usa → ReadingCollector, SensorReading, Pollutant
  ConsoleOutput
  Main
      └── ensambla todo
```

---

## Decisiones de Diseño Relevantes

| Decisión | Alternativa descartada | Razón |
|---|---|---|
| `record` para `Breakpoint` y `SensorReading` | Clases mutables | Inmutabilidad garantiza seguridad en entorno concurrente sin sincronización |
| `@FunctionalInterface` para `ReadingCollector` y `AlertNotifier` | Clases abstractas | Permite pasar lambdas como implementaciones; facilita las pruebas |
| `ConcurrentHashMap` en `CentralMonitor` | `HashMap` con `synchronized` | Mejor rendimiento concurrente; ninguna escritura bloquea todo el mapa |
| Distribución gaussiana en `SensorStation` | Valores aleatorios uniformes | Más realista: la mayoría de lecturas son normales, con picos ocasionales |
| `Consumer<String>` como parámetro de I/O | Dependencia directa a `System.out` | Permite sustituir la consola por un logger, archivo, o red sin tocar la aplicación |
