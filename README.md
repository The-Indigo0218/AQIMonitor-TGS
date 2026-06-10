# Sistema de Monitoreo y Alerta de Calidad del Aire (AQI Monitor)

**Proyecto para la materia: Teoría General de Sistemas**

Este sistema simula una red de estaciones de monitoreo atmosférico que miden la calidad del aire en tiempo real, calculan el Índice de Calidad del Aire (AQI) según la metodología oficial de la EPA de EE.UU. y disparan alertas automáticas de mitigación cuando los niveles son peligrosos.

---

## Tabla de Contenidos

| Documento | Audiencia | Descripción |
|---|---|---|
| [Concepto del Proyecto](docs/01-CONCEPTO-DEL-PROYECTO.md) | Todos | Qué es, por qué existe, cómo encaja en TGS |
| [Calidad del Aire y el AQI](docs/02-CALIDAD-DEL-AIRE-Y-AQI.md) | Todos | Contaminantes, unidades, fórmula y categorías |
| [Arquitectura del Sistema](docs/03-ARQUITECTURA.md) | Técnicos | Estructura de capas, clases y decisiones de diseño |
| [Cómo Ejecutar la Simulación](docs/04-SIMULACION.md) | Todos | Compilar, correr y leer la salida por consola |
| [Pruebas Unitarias](docs/05-PRUEBAS.md) | Técnicos | Qué se verifica y cómo correr los tests |
| [Historial de Commits](GIT_COMMANDS.md) | Técnicos | Comandos Git y convención de mensajes usada |

---

## Inicio Rápido

### Requisitos

- Java 21 o superior (con soporte para `--enable-preview`)
- Apache Maven 3.9+

### Compilar y Ejecutar

```bash
# Compilar el proyecto
mvn compile

# Correr la simulación
mvn exec:java -Dexec.mainClass="com.aqimonitor.infrastructure.Main" \
  -Dexec.args="" -Dexec.vmArgs="--enable-preview"

# Correr las pruebas
mvn test
```

> Presiona `Ctrl+C` para detener la simulación. Se imprimirá un resumen final.

---

## Estructura del Proyecto

```
aqi-monitor/
├── src/
│   ├── main/java/com/aqimonitor/
│   │   ├── domain/          # Reglas puras: fórmula AQI, categorías, alertas
│   │   ├── application/     # Orquestación: monitor central, servicio de alertas
│   │   └── infrastructure/  # Hilos, consola, punto de entrada (Main)
│   └── test/java/com/aqimonitor/domain/
│       ├── AqiCalculatorTest.java
│       └── BreakpointTableTest.java
├── docs/                    # Documentación detallada
├── pom.xml                  # Configuración Maven
└── README.md
```

---

## Resumen Técnico

| Característica | Detalle |
|---|---|
| Lenguaje | Java 21 (preview features) |
| Concurrencia | Virtual Threads — Project Loom (`newVirtualThreadPerTaskExecutor`) |
| Arquitectura | Hexagonal / Clean Architecture (3 capas) |
| Build tool | Apache Maven |
| Testing | JUnit 5.10.2 (parametrizado, anidado) |
| Contaminantes | PM2.5 (µg/m³), PM10 (µg/m³), NO₂ (ppb) |
| Metodología AQI | EPA — interpolación lineal sobre tabla de breakpoints |
| Escala de tiempo | 1 segundo real = 1 hora de simulación |
| Estaciones activas | 5 (CENTRO, NORTE, SUR, ESTE, OESTE) |
