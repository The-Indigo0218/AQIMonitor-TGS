# Cómo Ejecutar la Simulación

> Este documento está escrito para **todos los integrantes** del equipo. No necesitas saber programar para entender qué hace el sistema cuando corre.

---

## Requisitos Previos

Antes de ejecutar el proyecto necesitas tener instalado en tu computadora:

| Herramienta | Versión mínima | ¿Para qué sirve? |
|---|---|---|
| Java JDK | 21 | Ejecutar el código Java |
| Apache Maven | 3.9+ | Compilar y gestionar dependencias |

Verifica que los tengas con:

```bash
java -version
mvn -version
```

---

## Comandos Principales

Todos los comandos se ejecutan desde la raíz del proyecto (`aqi-monitor/`).

### Compilar

```bash
mvn compile
```

Traduce el código fuente Java a bytecode. Los archivos `.class` quedan en `target/classes/`.

### Ejecutar la Simulación

```bash
mvn exec:java -Dexec.mainClass="com.aqimonitor.infrastructure.Main" \
  -Dexec.vmArgs="--enable-preview"
```

> Para detener la simulación presiona `Ctrl + C`. El sistema imprimirá un resumen final antes de cerrarse.

### Correr las Pruebas

```bash
mvn test
```

Ejecuta todos los tests y muestra un reporte de cuáles pasaron y cuáles fallaron.

### Compilar todo en un JAR ejecutable

```bash
mvn package
```

Genera `target/aqi-monitor-1.0.0-SNAPSHOT.jar`. Se puede ejecutar con:

```bash
java --enable-preview -jar target/aqi-monitor-1.0.0-SNAPSHOT.jar
```

---

## Entendiendo la Salida en Consola

Cuando corres la simulación verás dos tipos de mensajes:

### Lecturas normales (salida estándar — negro/blanco)

```
[14:32:01] Station: EST-001-CENTRO | AQI: 47 (Buena) | PM2.5: 9.3 µg/m³ | PM10: 28.1 µg/m³ | NO₂: 41.2 ppb
[14:32:01] Station: EST-002-NORTE  | AQI: 83 (Moderada) | PM2.5: 22.7 µg/m³ | PM10: 91.4 µg/m³ | NO₂: 38.5 ppb
[14:32:01] Station: EST-003-SUR    | AQI: 112 (Dañina para grupos sensibles) | PM2.5: 40.1 µg/m³ | PM10: 195.0 µg/m³ | NO₂: 102.3 ppb
```

Cada línea representa una "hora" de simulación para cada estación. Los campos son:

| Campo | Descripción |
|---|---|
| `[HH:MM:SS]` | Hora real del sistema al momento de la lectura |
| `Station` | Identificador de la estación (EST-001 a EST-005) |
| `AQI` | Índice calculado (el peor contaminante marca el valor) |
| `(Categoría)` | Nombre de la categoría en español |
| `PM2.5: X µg/m³` | Concentración medida de partículas finas |
| `PM10: X µg/m³` | Concentración medida de partículas gruesas |
| `NO₂: X ppb` | Concentración medida de dióxido de nitrógeno |

### Alertas urgentes (salida de error — suelen aparecer en rojo)

```
🚨 ALERTA URGENTE 🚨
Estación: EST-004-ESTE | AQI: 174 | Categoría: Dañina
Acciones de mitigación:
  - Restringir tráfico vehicular
  - Limitar actividades al aire libre
```

Las alertas aparecen en `stderr` (la salida de error del proceso), que en la mayoría de terminales se muestra en rojo o separado del texto normal.

### Resumen final (al presionar Ctrl+C)

```
╔══════════════════════════════════════════════════════════════╗
║                      RESUMEN FINAL                           ║
╚══════════════════════════════════════════════════════════════╝
AQI Global: 98 (Moderada)
```

---

## La Escala de Tiempo

El sistema simula el tiempo acelerado:

```
1 segundo real  =  1 hora de simulación
60 segundos     =  2.5 días simulados
```

Esto permite observar el comportamiento de la red de estaciones a lo largo de "días" en cuestión de minutos, ideal para demostrar el sistema en una presentación.

---

## Las 5 Estaciones de Monitoreo

| ID | Ubicación |
|---|---|
| EST-001-CENTRO | Zona central de la ciudad |
| EST-002-NORTE | Zona norte |
| EST-003-SUR | Zona sur |
| EST-004-ESTE | Zona este |
| EST-005-OESTE | Zona oeste |

Cada estación corre en su **propio Virtual Thread** (hilo ligero de Java). Esto significa que las 5 estaciones funcionan en paralelo: cada una genera sus propias lecturas de forma independiente, como lo harían en la realidad sensores físicos distintos comunicándose por red.

---

## Generación de Datos: ¿Por Qué Hay Picos?

Los valores de concentración no son completamente aleatorios: siguen una **distribución normal (gaussiana)**, que es la que mejor describe fenómenos naturales y de tráfico.

```
Valor simulado = Base + (varianza × número_gaussiano_aleatorio)
```

| Contaminante | Valor base | Desviación estándar | Qué significa |
|---|---|---|---|
| PM2.5 | 15.0 µg/m³ | 25.0 | La mayoría de lecturas rondan 15 µg/m³ (Buena), pero picos ocasionales pueden superar 55 µg/m³ (Dañino) |
| PM10 | 40.0 µg/m³ | 60.0 | Base en zona Buena, picos posibles en zona Dañina |
| NO₂ | 30.0 ppb | 80.0 | Base en zona Buena, picos posibles en zona Dañina |

Los picos representan eventos episódicos: hora pico de tráfico, incendio industrial, quema de campos, etc. Esto hace que el sistema **sí dispare alertas** durante la simulación, permitiendo ver el bucle de retroalimentación en acción.

---

## Flujo de Datos Completo (Secuencia)

```
Cada ~1 segundo, para cada estación:

SensorStation (Virtual Thread)
    │
    │  genera SensorReading
    │  (PM2.5, PM10, NO₂ con distribución gaussiana)
    │
    ▼
CentralMonitor.collect(reading)
    │
    ├──► Almacena en ConcurrentHashMap
    │
    ├──► Formatea y envía a logConsumer (consola)
    │
    └──► ¿AQI > 150?
              │
             SÍ
              │
              ▼
         AlertEvent.createUrgentAlert(...)
              │
              ▼
         AlertService.notify(event)
              │
              ▼
         alertConsumer (System.err — alerta en rojo)
```
