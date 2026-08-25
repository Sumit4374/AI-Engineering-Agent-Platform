# ============================================================
# Stage 1: Build & Package JAR
# ============================================================
FROM eclipse-temurin:25-jdk-noble AS builder

WORKDIR /build

# Copy Maven wrapper and POM first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies offline
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and resources
COPY src/ src/

# Build application package (skip tests during container build; tests run in CI)
RUN ./mvnw clean package -DskipTests -B

# ============================================================
# Stage 2: Production Runtime
# ============================================================
FROM eclipse-temurin:25-jre-noble AS runner

WORKDIR /app

# Create a non-privileged application user for security
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /sbin/nologin appuser

# Copy the compiled executable JAR from builder stage
COPY --from=builder /build/target/ai_engineering-*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose HTTP port
EXPOSE 8080

# Configure JVM optimization flags for containerized environment
ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck against Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Launch application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
