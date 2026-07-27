# Builder stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q
COPY src ./src
RUN ./mvnw package -DskipTests -B -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S dts && adduser -S dts -G dts
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R dts:dts /app
USER dts
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=40s \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
