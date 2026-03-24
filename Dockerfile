# Build stage
FROM eclipse-temurin:21-jdk AS builder

# Set working directory
WORKDIR /app

# Copy maven wrapper and pom.xml
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

# Download dependencies (cache layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Create a volume for H2 database persistence (optional)
VOLUME /app/data

# Expose the application port (matches server.port)
EXPOSE 8081

# Environment variables (can be overridden)
ENV SPRING_DATASOURCE_URL=jdbc:h2:file:./data/fintech_db;DB_CLOSE_DELAY=-1
ENV SPRING_H2_CONSOLE_ENABLED=true
ENV JWT_SECRET=mySecretKeyForJWT12345678901234567890
ENV JWT_EXPIRATION_MS=86400000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]