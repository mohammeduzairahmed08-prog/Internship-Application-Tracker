# =========================================================================
# Stage 1: Build the Spring Boot application using Maven and Temurin JDK 21
# =========================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven wrapper and dependencies specification
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Ensure wrapper executable permission
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy project source code
COPY src ./src

# Build production jar skipping test phase for fast deployment
RUN ./mvnw clean package -DskipTests

# =========================================================================
# Stage 2: Minimal, production-ready Alpine JRE 21 runtime
# =========================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy compiled JAR from build stage
COPY --from=builder /workspace/target/*.jar app.jar

# Dynamic port binding
EXPOSE 8080
ENV PORT=8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
