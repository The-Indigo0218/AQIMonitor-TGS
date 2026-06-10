# Historial de Git y Convención de Commits

> Este documento explica cómo se construyó el historial de commits del proyecto y cómo seguir la misma convención si se realizan cambios futuros.

---

## Convención: Conventional Commits

Todos los commits siguen la especificación **Conventional Commits** (`https://www.conventionalcommits.org`). El formato es:

```
<tipo>(<alcance>): <descripción en imperativo, minúsculas>
```

### Tipos Utilizados

| Tipo | Cuándo usarlo |
|---|---|
| `feat` | Se agrega funcionalidad nueva |
| `test` | Se agregan o modifican pruebas |
| `chore` | Configuración, scaffolding, sin lógica de negocio |
| `fix` | Corrección de un bug |
| `docs` | Solo documentación |
| `refactor` | Reestructuración sin cambio de comportamiento |

### Alcances (scopes) del Proyecto

| Scope | Descripción |
|---|---|
| `domain` | Clases en `com.aqimonitor.domain` |
| `application` | Clases en `com.aqimonitor.application` |
| `infrastructure` | Clases en `com.aqimonitor.infrastructure` |

---

## Historial de Commits del Proyecto

Los commits se crearon en orden, de lo más interno hacia afuera, siguiendo la dirección de dependencias de la Arquitectura Limpia.

```
1dd42c7  chore: initialize Maven project with Java 21 and JUnit 5 configuration
c57ca08  feat(domain): add pollutant types, breakpoints table and air quality categories
c77f447  feat(domain): implement EPA AQI calculation with linear interpolation
30a83c3  feat(domain): add sensor reading model and urgent mitigation alert events
2b2541a  feat(application): add ports and central monitor for data collection and alerting
6137c51  feat(application): implement alert service with mitigation actions
c35e409  feat(infrastructure): implement SensorStation using Virtual Threads (Project Loom)
81b9d02  feat(infrastructure): add console output adapters and main entry point
78fdb80  test(domain): add comprehensive unit tests for AQI calculator with EPA breakpoints
0c36893  test(domain): add validation tests for breakpoint table integrity
```

### Orden y Razonamiento

| Paso | Commit | Por qué en ese orden |
|---|---|---|
| 1 | `chore: initialize...` | La configuración del proyecto siempre va primero |
| 2-4 | `feat(domain): ...` | El dominio no depende de nada; se construye primero |
| 5-6 | `feat(application): ...` | La aplicación depende del dominio; va después |
| 7-8 | `feat(infrastructure): ...` | La infra depende de todo; va al final |
| 9-10 | `test(domain): ...` | Los tests se agregan una vez que el código que prueban existe |

---

## Comandos para Reproducir el Repositorio desde Cero

Si necesitas inicializar el repositorio en otra máquina o recrear el historial:

```bash
# 1. Inicializar el repositorio
git init
git branch -M main

# 2. Crear .gitignore adecuado para Java/Maven
cat > .gitignore << 'EOF'
# Compiled output
target/
*.class
*.jar
*.war
*.ear

# Maven
.mvn/
!.mvn/wrapper/maven-wrapper.jar
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties

# IDEs
.idea/
*.iml
*.iws
*.ipr
.eclipse/
.classpath
.project
.settings/
*.sublime-workspace
*.sublime-project

# OS
.DS_Store
Thumbs.db

# Logs
*.log
EOF

# 3. Primer commit — configuración del proyecto
git add pom.xml .gitignore
git commit -m "chore: initialize Maven project with Java 21 and JUnit 5 configuration"

# 4. Dominio — tipos base
git add src/main/java/com/aqimonitor/domain/Pollutant.java \
        src/main/java/com/aqimonitor/domain/Breakpoint.java \
        src/main/java/com/aqimonitor/domain/BreakpointTable.java \
        src/main/java/com/aqimonitor/domain/AirQualityCategory.java
git commit -m "feat(domain): add pollutant types, breakpoints table and air quality categories"

# 5. Dominio — calculadora AQI
git add src/main/java/com/aqimonitor/domain/AqiCalculator.java
git commit -m "feat(domain): implement EPA AQI calculation with linear interpolation"

# 6. Dominio — modelo de lectura y evento de alerta
git add src/main/java/com/aqimonitor/domain/SensorReading.java \
        src/main/java/com/aqimonitor/domain/AlertEvent.java
git commit -m "feat(domain): add sensor reading model and urgent mitigation alert events"

# 7. Aplicación — puertos e interfaces
git add src/main/java/com/aqimonitor/application/ReadingCollector.java \
        src/main/java/com/aqimonitor/application/AlertNotifier.java \
        src/main/java/com/aqimonitor/application/CentralMonitor.java
git commit -m "feat(application): add ports and central monitor for data collection and alerting"

# 8. Aplicación — servicio de alertas
git add src/main/java/com/aqimonitor/application/AlertService.java
git commit -m "feat(application): implement alert service with mitigation actions"

# 9. Infraestructura — estación de sensores con Virtual Threads
git add src/main/java/com/aqimonitor/infrastructure/SensorStation.java
git commit -m "feat(infrastructure): implement SensorStation using Virtual Threads (Project Loom)"

# 10. Infraestructura — salida por consola y punto de entrada
git add src/main/java/com/aqimonitor/infrastructure/ConsoleOutput.java \
        src/main/java/com/aqimonitor/infrastructure/Main.java
git commit -m "feat(infrastructure): add console output adapters and main entry point"

# 11. Tests — calculadora AQI
git add src/test/java/com/aqimonitor/domain/AqiCalculatorTest.java
git commit -m "test(domain): add comprehensive unit tests for AQI calculator with EPA breakpoints"

# 12. Tests — tabla de breakpoints
git add src/test/java/com/aqimonitor/domain/BreakpointTableTest.java
git commit -m "test(domain): add validation tests for breakpoint table integrity"

# 13. Etiquetar la versión estable
git tag -a v1.0.0 -m "Version 1.0.0 - Sistema completo con simulación y pruebas"
```

---

## Buenas Prácticas para Futuros Commits

1. **Un commit = un cambio lógico.** No mezcles "agregar feature X" con "arreglar bug Y" en el mismo commit.
2. **El mensaje describe el porqué, no el qué.** Prefiere `feat(domain): support CO as measurable pollutant` sobre `feat(domain): add CO to Pollutant enum`.
3. **Usa el scope siempre** para que sea fácil filtrar el historial por capa.
4. **Verifica antes de hacer commit:**
   ```bash
   mvn test       # ¿Todos los tests pasan?
   git diff       # ¿Solo están los cambios que quiero?
   git status     # ¿No hay archivos de IDE o target/ accidentales?
   ```
