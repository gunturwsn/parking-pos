# syntax=docker/dockerfile:1

# --- Build stage: compile the Spring Boot jar ---
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory inside the builder image
WORKDIR /app

# Copy Maven descriptor and warm dependency cache
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Copy the backend source code
COPY src ./src

# Package the application (tests already run in CI)
RUN mvn -q -B package -DskipTests

# --- Runtime stage: slim JRE-only image ---
FROM eclipse-temurin:21-jre

# Set working directory in the runtime container
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# JVM options placeholder (overridable)
ENV JAVA_OPTS=""

# Start the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
