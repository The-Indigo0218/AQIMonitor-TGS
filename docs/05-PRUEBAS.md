# Pruebas Unitarias

> Este documento está dirigido principalmente a **integrantes técnicos**. Explica qué se verifica, por qué importa y cómo interpretar los resultados.

---

## Filosofía de Pruebas

Las pruebas se concentran en el **dominio**: la capa que contiene la lógica matemática y las reglas de negocio. Esta es la capa más crítica del sistema porque un error en la fórmula AQI produce valores incorrectos que pueden llevar a tomar decisiones equivocadas sobre la salud pública.

La infraestructura (hilos, consola) no se prueba unitariamente porque su comportamiento correcto requiere de una simulación en ejecución, que es una prueba de integración o sistema. Lo que sí se prueba es que las interfaces y contratos del dominio se respetan.

---

## Cómo Correr los Tests

```bash
mvn test
```

Salida esperada al final:

```
[INFO] Tests run: 58, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Para ver el reporte detallado por clase:

```bash
cat target/surefire-reports/com.aqimonitor.domain.AqiCalculatorTest.txt
```

---

## Archivo 1: `AqiCalculatorTest`

Contiene **6 clases anidadas** (grupos de tests), totalizando ~50 casos de prueba.

### Grupo `Pm25Tests` — Tests de PM2.5

Verifica que la fórmula EPA produce el AQI correcto para PM2.5 en los **18 puntos de control** más relevantes: los valores exactos en los extremos de cada breakpoint y algunos valores intermedios.

Ejemplo de caso de prueba:

| Concentración PM2.5 | AQI esperado | Lógica |
|---|---|---|
| 0.0 µg/m³ | 0 | Mínimo absoluto = AQI 0 |
| 6.0 µg/m³ | 25 | Punto medio del primer rango |
| 12.0 µg/m³ | 50 | Extremo superior del rango Buena |
| 12.1 µg/m³ | 51 | Primer punto del rango Moderada |
| 35.4 µg/m³ | 100 | Extremo superior del rango Moderada |
| 55.5 µg/m³ | 151 | Primer punto del rango Dañina |
| 500.4 µg/m³ | 500 | Máximo absoluto |

Estos puntos de frontera son especialmente importantes porque un error en la interpolación cerca de los límites puede producir saltos o inconsistencias.

### Grupo `Pm10Tests` — Tests de PM10

Igual estructura que PM2.5 pero con 18 casos para PM10 (en µg/m³).

### Grupo `No2Tests` — Tests de NO₂

18 casos para NO₂ (en ppb). Verifica que el sistema maneja correctamente las distintas unidades.

### Grupo `EdgeCases` — Casos Límite y Manejo de Errores

| Test | Qué verifica |
|---|---|
| `testNegativeConcentration` | Concentración negativa → lanza `IllegalArgumentException` |
| `testAboveMaxBreakpoint` | Concentración > 500.4 µg/m³ → lanza `IllegalArgumentException` |
| `testOverallAqiReturnsMax` | Con PM2.5=100, PM10=100, NO₂=150 → AQI global = 150 |
| `testEmptyConcentrations` | Mapa vacío → AQI = 0 (sin crashear) |

> **Nota sobre precisión y la normalización:** Los tests de PM10 y NO₂ usan valores enteros y los de PM2.5 usan valores de 1 decimal, que coinciden exactamente con los extremos de los breakpoints. En la simulación real, los sensores generan flotantes de doble precisión (ej. `54.867 µg/m³` para PM10) que pueden caer en el hueco entre rangos enteros consecutivos. Esto causaba un `IllegalArgumentException` en tiempo de ejecución. `AqiCalculator.calculateAqi` normaliza la concentración antes de la búsqueda (floor para PM10/NO₂, redondeo a 1 decimal para PM2.5), siguiendo la precisión que especifica la EPA para cada contaminante.

### Grupo `CategoryTests` — Clasificación por Categorías

18 casos que verifican que un AQI numérico se mapea a la categoría correcta. Incluye todos los umbrales:

```
AQI 50  → GOOD
AQI 51  → MODERATE
AQI 100 → MODERATE
AQI 101 → UNHEALTHY_SENSITIVE
AQI 150 → UNHEALTHY_SENSITIVE
AQI 151 → UNHEALTHY           ← umbral de alerta
AQI 201 → VERY_UNHEALTHY
AQI 301 → HAZARDOUS
AQI 500 → HAZARDOUS
```

También verifica el método `requiresUrgentMitigation()`:
- `GOOD`, `MODERATE`, `UNHEALTHY_SENSITIVE` → `false`
- `UNHEALTHY`, `VERY_UNHEALTHY`, `HAZARDOUS` → `true`

### Grupo `SensorReadingTests` — Integración con SensorReading

| Test | Qué verifica |
|---|---|
| `testSensorReadingOverallAqi` | AQI global calculado correctamente desde un `SensorReading` |
| `testSensorReadingIndividualAqi` | AQI por contaminante individual desde `SensorReading` |
| `testMissingPollutant` | Contaminante sin medición en el mapa → AQI = 0 (sin crashear) |

---

## Archivo 2: `BreakpointTableTest`

Cuatro tests que validan la **integridad estructural** de la tabla de breakpoints.

### `testAllPollutantsHaveBreakpoints`

Itera los 3 `Pollutant` y verifica que cada uno tenga exactamente **6 breakpoints** definidos. Si alguien agrega un contaminante al enum sin agregar sus datos a la tabla, este test falla.

### `testBreakpointsAreContiguous`

Verifica que no haya "huecos" entre rangos consecutivos. Para PM10 y NO₂ (valores enteros), el siguiente rango debe comenzar en `concentrationHigh + 1`. Para PM2.5 (decimal), debe ser `+ 0.1`.

Esto es crítico porque un hueco en la tabla haría que ciertas concentraciones reales no encuentren su breakpoint y el sistema lance una excepción en producción.

### `testBreakpointRanges`

Valida que dentro de cada breakpoint:
- `concentrationLow <= concentrationHigh` (el rango no está invertido)
- `aqiLow <= aqiHigh` (el rango AQI no está invertido)
- `aqiLow >= 0` (no hay AQI negativo)
- `aqiHigh <= 500` (no supera el máximo de la escala)

### `testFindBreakpoint`

Prueba la función de búsqueda con valores conocidos:
- PM2.5 = 35.4 µg/m³ debe encontrar el breakpoint [12.1–35.4, AQI 51–100]
- PM2.5 = 35.5 µg/m³ debe encontrar el breakpoint [35.5–55.4, AQI 101–150]
- Valores fuera de rango deben lanzar `IllegalArgumentException`

---

## Por Qué Estos Tests son Suficientes

La estrategia de pruebas cubre:

1. **Correctitud matemática**: los 50+ casos parametrizados con valores conocidos garantizan que la interpolación lineal es exacta.
2. **Robustez**: los edge cases garantizan que el sistema falla limpiamente (con excepción) en lugar de producir resultados silenciosamente incorrectos.
3. **Integridad de configuración**: los tests de `BreakpointTable` son una red de seguridad para cambios futuros en la tabla de datos.

La cobertura del dominio es **completa**: todas las rutas de código del dominio son ejercidas por al menos un test.
