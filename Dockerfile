# Multi-stage Dockerfile for Spring Boot Kotlin API

# Stage 1: Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace/app

# Copy Gradle wrapper and configuration files first for caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Ensure Gradle wrapper is executable
RUN chmod +x gradlew

# Pre-fetch dependencies (optional cache layer)
RUN ./gradlew dependencies --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx384m -XX:MaxMetaspaceSize=256m"

# Copy source code and build the executable jar
COPY src src
RUN ./gradlew bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx384m -XX:MaxMetaspaceSize=256m" -Pkotlin.compiler.execution.strategy=in-process -x test

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy built artifact from builder stage
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# Dynamic port binding (default 8080)
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=deploy
EXPOSE 8080

# Launch application
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60.0", "-XX:+UseSerialGC", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
