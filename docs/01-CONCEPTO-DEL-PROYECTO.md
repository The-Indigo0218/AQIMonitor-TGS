# Concepto del Proyecto: ¿Qué es y para qué sirve?

> Este documento está escrito para **todos los integrantes del equipo**, sin importar su nivel técnico.

---

## El Problema que Resuelve

La contaminación del aire es invisible pero real. En ciudades industriales o con alto tráfico, los niveles de partículas y gases nocivos pueden subir rápidamente a rangos peligrosos para la salud, especialmente para niños, ancianos y personas con enfermedades respiratorias.

El problema central es: **¿cómo saber cuándo el aire es peligroso y qué hacer al respecto?**

Este proyecto responde a esa pregunta construyendo un sistema de monitoreo automatizado que:

1. **Mide** concentraciones de tres contaminantes de forma continua.
2. **Calcula** un número único (el AQI) que resume la calidad del aire.
3. **Clasifica** ese número en una categoría comprensible ("Buena", "Moderada", "Peligrosa"…).
4. **Alerta** automáticamente y sugiere acciones concretas cuando el nivel es crítico.

---

## Conexión con la Teoría General de Sistemas (TGS)

La Teoría General de Sistemas estudia cómo distintos componentes interactúan y se retroalimentan para producir un comportamiento emergente. Este proyecto es una ilustración directa de esos principios:

| Concepto TGS | Cómo aparece en el proyecto |
|---|---|
| **Sistema abierto** | El sistema recibe entradas del entorno (lecturas de sensores) y produce salidas (alertas, registros) |
| **Retroalimentación negativa** | Cuando el AQI supera 150, el sistema reacciona con acciones de mitigación para reducir el problema |
| **Jerarquía de subsistemas** | Existe una capa de sensores, una capa de cálculo y una capa de decisión, cada una con su función |
| **Emergencia** | El AQI global no viene de un solo sensor, sino del comportamiento colectivo de las tres zonas |
| **Homeostasis** | El objetivo del sistema es mantener la calidad del aire dentro de rangos aceptables |
| **Entropía** | Sin el monitoreo, el conocimiento sobre el estado del aire sería desorganizado e inútil |

---

## ¿Cómo Funciona en Términos Simples?

Imagina una ciudad con tres estaciones de monitoreo, una por cada tipo de zona (industrial, residencial y un corredor de autopistas). Cada estación tiene sensores que, cada hora, envían tres mediciones:

- Cuántas partículas finas flotan en el aire (PM2.5 y PM10)
- Cuánto dióxido de nitrógeno hay (NO₂), un gas producido por motores y fábricas

Un monitor central recibe todas esas mediciones, aplica una fórmula matemática estandarizada y obtiene un número entre 0 y 500 llamado **AQI** (Índice de Calidad del Aire). Dependiendo de ese número, el sistema puede:

- Solo registrar la lectura (si el aire está bien)
- Imprimir una alerta urgente en pantalla con acciones específicas (si el aire es peligroso)

**La simulación comprime el tiempo:** 1 segundo de computadora equivale a 1 hora de tiempo real, lo que permite observar 24 "horas" de datos en menos de medio minuto.

---

## Diagrama Conceptual del Sistema

```
       ENTORNO (Atmósfera)
              │
    ┌─────────▼──────────┐
    │   5 ESTACIONES     │  ← Subsistema de Captura
    │  (sensores PM2.5,  │    cada una en su propio
    │   PM10, NO₂)       │    hilo de ejecución
    └─────────┬──────────┘
              │  lecturas cada segundo (= 1 hora sim.)
    ┌─────────▼──────────┐
    │  MONITOR CENTRAL   │  ← Subsistema de Procesamiento
    │  (cálculo AQI,     │    recolecta, calcula y
    │   clasificación)   │    detecta anomalías
    └────────┬───────────┘
             │
     ┌───────┴────────┐
     │                │
     ▼                ▼
 Registro         ALERTA URGENTE    ← Salidas del sistema
 en consola       + Mitigación
```

---

## El Bucle de Retroalimentación

Este es el corazón de la lógica TGS del proyecto:

```
     AQI > 150?
         │
        SÍ
         │
         ▼
  Generar AlertEvent
         │
         ▼
  Acciones de Mitigación:
  - "Restringir tráfico vehicular"
  - "Alerta industrial: reducir emisiones"
  - "Suspender clases al aire libre"
         │
         └──► Retroalimentación al entorno
              (reducir la causa del problema)
```

Cuando el AQI supera 150 (categoría "Dañina"), el sistema no simplemente lo registra: dispara un conjunto de respuestas proporcionales a la gravedad. Este mecanismo de **retroalimentación negativa** es el que busca llevar el sistema de regreso a un estado de equilibrio.

---

## ¿Quién Puede Entender Este Sistema?

- **Ciudadanos**: ven el número AQI y saben si pueden salir a correr.
- **Autoridades de salud**: reciben alertas automáticas con acciones concretas.
- **Ingenieros ambientales**: analizan los datos por estación y contaminante.
- **Operadores industriales**: son notificados cuando deben reducir emisiones.
- **Desarrolladores de software**: pueden extender el sistema con nuevas estaciones, contaminantes o canales de notificación.
