# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src src
RUN mvn package -DskipTests -B

# Stage 2: Runtime stage
FROM ibm-semeru-runtimes:open-21-jre AS runtime
WORKDIR /app
RUN groupadd -r dts && useradd -r -g dts -s /bin/false dts
COPY --from=builder /app/target/*.jar app.jar
RUN chown dts:dts app.jar
USER dts
EXPOSE 8087
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=90s \
    CMD curl -sf http://localhost:8087/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
