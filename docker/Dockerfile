# ================================================================
# Multi-stage Dockerfile — Practice Service
# ================================================================
# Stage 1: Build
# ================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build
COPY docker/settings.xml /root/.m2/settings.xml

COPY pom.xml .
RUN mvn dependency:go-offline -B -q -s /root/.m2/settings.xml

COPY src ./src
RUN mvn package -DskipTests -B -q -s /root/.m2/settings.xml

# ================================================================
# Stage 2: Runtime
# ================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S dts && adduser -S dts -G dts

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

RUN chown -R dts:dts /app
USER dts

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=40s \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
