# Multi-stage Docker build for optimal image size
FROM maven:3.9-eclipse-temurin-23 AS builder

WORKDIR /app

# Copy parent POM first
COPY pom.xml ./pom.xml

# Copy location-service module
COPY location-service/pom.xml ./location-service/pom.xml
COPY location-service/src ./location-service/src

# Build the application from the location-service module
WORKDIR /app/location-service
RUN mvn clean package -DskipTests

# Production image
FROM eclipse-temurin:23-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 jumbo && \
    adduser -D -s /bin/sh -u 1001 -G jumbo jumbo

WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/location-service/target/location-service-*.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R jumbo:jumbo /app

# Switch to non-root user
USER jumbo

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

# Expose port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
