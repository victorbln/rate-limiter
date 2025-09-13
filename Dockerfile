# Multi-stage build for optimal image size and security
# Build stage - handles compilation and packaging
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy dependency configuration first for better layer caching
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Runtime stage - minimal runtime environment
FROM eclipse-temurin:17-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -D -s /bin/sh -u 1001 -G appgroup appuser

WORKDIR /app

# Copy application jar from build stage
COPY --from=build /app/target/rate-limiter-*.jar app.jar

# Set ownership for non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Configure JVM for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Application entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]