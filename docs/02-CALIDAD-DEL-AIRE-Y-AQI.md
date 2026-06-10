# Calidad del Aire y el Índice AQI

> Este documento está escrito para **todos los integrantes del equipo**. Explica en detalle qué se mide, con qué unidades y cómo se interpreta el resultado.

---

## ¿Qué es el AQI?

El **AQI** (Air Quality Index, o Índice de Calidad del Aire) es un número adimensional (sin unidades propias) en una escala de **0 a 500** que resume de forma unificada el nivel de contaminación del aire.

Fue desarrollado por la **Agencia de Protección Ambiental de los Estados Unidos (EPA)** y es adoptado por organismos de salud de todo el mundo. La ventaja del AQI es que convierte varias mediciones técnicas (en distintas unidades, de distintos contaminantes) en un único número fácil de comunicar.

> Un AQI de **0** significa aire perfectamente limpio.
> Un AQI de **500** significa una emergencia ambiental de máxima gravedad.

---

## Los Tres Contaminantes que Medimos

### 1. PM2.5 — Partículas Finas

| Atributo | Valor |
|---|---|
| Nombre completo | Material Particulado de diámetro ≤ 2.5 micrómetros |
| Unidad de medida | **µg/m³** (microgramos por metro cúbico) |
| Origen | Humo de vehículos, quemas, industrias, cocción de alimentos |
| Por qué es peligroso | Son tan pequeñas que penetran profundo en los pulmones e incluso entran al torrente sanguíneo |
| Referencia de escala | Un cabello humano mide ~70 µm; una PM2.5 mide ≤ 2.5 µm |

**Tabla de breakpoints PM2.5 (µg/m³)**

| Concentración mínima | Concentración máxima | AQI mínimo | AQI máximo | Categoría |
|---|---|---|---|---|
| 0.0 | 12.0 | 0 | 50 | Buena |
| 12.1 | 35.4 | 51 | 100 | Moderada |
| 35.5 | 55.4 | 101 | 150 | Dañina para grupos sensibles |
| 55.5 | 150.4 | 151 | 200 | Dañina |
| 150.5 | 250.4 | 201 | 300 | Muy Dañina |
| 250.5 | 500.4 | 301 | 500 | Peligrosa |

---

### 2. PM10 — Partículas Gruesas

| Atributo | Valor |
|---|---|
| Nombre completo | Material Particulado de diámetro ≤ 10 micrómetros |
| Unidad de medida | **µg/m³** (microgramos por metro cúbico) |
| Origen | Polvo de construcción, tierra, polen, ceniza |
| Por qué es peligroso | Irritan las vías respiratorias superiores; afectan ojos y garganta |

**Tabla de breakpoints PM10 (µg/m³)**

| Concentración mínima | Concentración máxima | AQI mínimo | AQI máximo | Categoría |
|---|---|---|---|---|
| 0 | 54 | 0 | 50 | Buena |
| 55 | 154 | 51 | 100 | Moderada |
| 155 | 254 | 101 | 150 | Dañina para grupos sensibles |
| 255 | 354 | 151 | 200 | Dañina |
| 355 | 424 | 201 | 300 | Muy Dañina |
| 425 | 604 | 301 | 500 | Peligrosa |

---

### 3. NO₂ — Dióxido de Nitrógeno

| Atributo | Valor |
|---|---|
| Nombre completo | Dióxido de Nitrógeno |
| Unidad de medida | **ppb** (partes por billón en volumen) |
| Origen | Motores de combustión interna, centrales eléctricas, industria química |
| Por qué es peligroso | Daña el sistema respiratorio, contribuye a la formación de smog fotoquímico y lluvia ácida |

> **¿Por qué ppb y no µg/m³?** Porque el NO₂ es un gas, y para los gases es más conveniente expresar la concentración como proporción de volumen (cuántos "pedacitos" de NO₂ hay por cada billón de pedacitos de aire).

**Tabla de breakpoints NO₂ (ppb)**

| Concentración mínima | Concentración máxima | AQI mínimo | AQI máximo | Categoría |
|---|---|---|---|---|
| 0 | 53 | 0 | 50 | Buena |
| 54 | 100 | 51 | 100 | Moderada |
| 101 | 360 | 101 | 150 | Dañina para grupos sensibles |
| 361 | 649 | 151 | 200 | Dañina |
| 650 | 1249 | 201 | 300 | Muy Dañina |
| 1250 | 2049 | 301 | 500 | Peligrosa |

---

## Las 6 Categorías de Calidad del Aire

| AQI | Color | Categoría | ¿Qué significa? |
|---|---|---|---|
| 0 – 50 | Verde | **Buena** | Calidad satisfactoria. Sin riesgo para la salud. |
| 51 – 100 | Amarillo | **Moderada** | Aceptable. Riesgo moderado para personas extremadamente sensibles. |
| 101 – 150 | Naranja | **Dañina para grupos sensibles** | Personas con asma, enfermedades cardíacas, niños y ancianos deben limitar esfuerzos prolongados al aire libre. |
| 151 – 200 | Rojo | **Dañina** | Toda la población puede experimentar efectos. Grupos sensibles, efectos más graves. **Se disparan alertas.** |
| 201 – 300 | Morado | **Muy Dañina** | Alerta de salud general. Todos pueden sufrir efectos graves. **Se disparan alertas.** |
| 301 – 500 | Marrón | **Peligrosa** | Emergencia de salud. Toda la población está en riesgo. **Se disparan alertas de máxima urgencia.** |

> El sistema dispara alertas automáticas cuando el AQI supera **150** (categorías Dañina, Muy Dañina y Peligrosa).

---

## La Fórmula Oficial de la EPA

El AQI se calcula mediante **interpolación lineal** dentro del rango que corresponde a la concentración medida. La fórmula es:

```
AQI = [(AQI_alto - AQI_bajo) / (C_alto - C_bajo)] × (C - C_bajo) + AQI_bajo
```

Donde:
- **C** = concentración medida del contaminante
- **C_bajo** = concentración mínima del rango (breakpoint inferior)
- **C_alto** = concentración máxima del rango (breakpoint superior)
- **AQI_bajo** = AQI mínimo del rango
- **AQI_alto** = AQI máximo del rango

### Ejemplo Paso a Paso

Supongamos que medimos **PM2.5 = 45.0 µg/m³**.

1. Buscamos en la tabla el rango que contiene 45.0 µg/m³: es el rango [35.5 – 55.4], que corresponde al AQI [101 – 150].
2. Aplicamos la fórmula:

```
AQI = [(150 - 101) / (55.4 - 35.5)] × (45.0 - 35.5) + 101
    = [49 / 19.9] × 9.5 + 101
    = 2.4623 × 9.5 + 101
    = 23.39 + 101
    ≈ 124
```

3. El AQI resultante es **124** → Categoría: **Dañina para grupos sensibles** (naranja).

---

## AQI Global: La Regla del "Peor Caso"

Cuando se miden varios contaminantes simultáneamente, el **AQI global** de una estación es el **máximo** de los AQI individuales de cada contaminante. Esto sigue el principio de protección máxima: si cualquier contaminante es peligroso, el aire se considera peligroso.

```
AQI_global = max(AQI_PM2.5, AQI_PM10, AQI_NO2)
```

Y el **AQI de la ciudad** es el máximo entre todas las estaciones activas:

```
AQI_ciudad = max(AQI_EST-001, AQI_EST-002, AQI_EST-003, AQI_EST-004, AQI_EST-005)
```
