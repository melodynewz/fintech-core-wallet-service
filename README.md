# Wallet Service

A Spring Boot-based wallet management service with JWT authentication, rate limiting, and H2 database.

## Features

- User authentication and authorization via JWT
- Wallet balance management
- Transaction history
- Rate limiting per user
- H2 database with Flyway migrations
- OpenAPI documentation

## Quick Start with Docker

### Prerequisites

- Docker and Docker Compose installed
- Java 21 (optional, for local development)

### Building the Docker Image

```bash
docker build -t wallet-service .
```

### Running with Docker Compose

1. Copy the environment example file:
   ```bash
   cp .env.example .env
   ```
   Edit `.env` to set your JWT secret and other variables.

2. Start the service:
   ```bash
   docker-compose up -d
   ```

3. Check logs:
   ```bash
   docker-compose logs -f
   ```

4. Access the application:
   - API: http://localhost:8081/api
   - H2 Console: http://localhost:8081/h2-console
     - JDBC URL: `jdbc:h2:file:./data/fintech_db`
     - Username: `sa`
     - Password: (leave empty)

### Environment Variables

See `.env.example` for all available variables. Key variables:

- `JWT_SECRET`: Secret key for signing JWT tokens (minimum 32 characters)
- `JWT_EXPIRATION_MS`: Token expiration in milliseconds (default 86400000)
- `SPRING_DATASOURCE_URL`: Database URL (default embedded H2)
- `SERVER_PORT`: Port the service listens on (default 8081)

### Persistent Data

The H2 database file is stored in `./data` directory, which is mounted as a volume in Docker. This ensures data persists across container restarts.

### Health Check

The service includes a health endpoint at `/actuator/health`. Docker Compose uses this for health checks.

## Local Development (without Docker)

### Build and Run

```bash
./mvnw clean package
java -jar target/*.jar
```

### Running Tests

```bash
./mvnw test
```

## API Documentation

Once the service is running, OpenAPI documentation is available at:

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI spec: http://localhost:8081/v3/api-docs

## License

Proprietary