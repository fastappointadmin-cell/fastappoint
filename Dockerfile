# ── Stage 1: build the fat JAR ────────────────────────────────────────────────
FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app

# Cache Gradle wrapper and dependencies separately from source
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
# Download dependencies (layer-cached as long as build.gradle doesn't change)
RUN ./gradlew dependencies --no-daemon -q

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: minimal runtime image ────────────────────────────────────────────
FROM eclipse-temurin:26-jdk-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]
