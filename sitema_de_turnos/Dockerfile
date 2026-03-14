# =============================================================================
# STAGE 1 — BUILD
# Usa la imagen oficial de Maven con JDK 21 para compilar el proyecto
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copiar sólo los descriptores de dependencias primero para aprovechar
# la caché de Docker: si pom.xml no cambia, Maven no vuelve a descargar.
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copiar el código fuente y compilar el JAR ejecutable
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# =============================================================================
# STAGE 2 — RUNTIME
# Imagen mínima: JRE 21 Alpine (~75 MB), sin compilador ni Maven
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuario sin privilegios para ejecutar la aplicación (seguridad)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar únicamente el JAR producido en el stage anterior
COPY --from=builder /build/target/*.jar app.jar

# Propietario correcto para el usuario de ejecución
RUN chown appuser:appgroup app.jar

USER appuser

# Spring Boot escucha en 8080 por defecto
EXPOSE 8080

# Opciones de JVM recomendadas para contenedores:
# -XX:+UseContainerSupport  → respeta los límites de CPU/RAM del contenedor
# -XX:MaxRAMPercentage=75.0 → usa como máximo el 75 % de la RAM asignada
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
