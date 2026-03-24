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

## Continuous Integration/Deployment (CI/CD)

This project includes a GitHub Actions workflow for automated testing, building, and deployment.

### Workflow Overview

The pipeline is defined in `.github/workflows/ci-cd.yml` and includes the following jobs:

1. **Build and Test**: Runs on every push and pull request.
   - Compiles the Java source code with Maven.
   - Executes unit and integration tests.
   - Verifies code coverage with JaCoCo (minimum 80% line coverage).
   - Uploads test results and coverage reports as artifacts.

2. **Docker Build**: Runs on pushes to main/master/develop branches and version tags.
   - Builds a Docker image using the multi‑stage Dockerfile.
   - Tags the image with the Git SHA, branch name, and version (if applicable).
   - Pushes the image to GitHub Container Registry (GHCR) automatically.

3. **Deploy (Example)**: Illustrates how to deploy the built image to a server via SSH.
   - Pulls the latest image on the target server.
   - Stops the existing container and starts the new one using Docker Compose.
   - **Note**: This job is a template and requires you to set up SSH secrets and adjust the script to match your environment.

### How to Enable the Pipeline

1. Ensure your repository has GitHub Actions enabled (enabled by default).
2. The workflow uses the following secrets for registry login and deployment (optional):
   - `GITHUB_TOKEN` (automatically provided) – for pushing to GHCR.
   - `SSH_HOST`, `SSH_USER`, `SSH_PRIVATE_KEY` – if you want to use the example deployment.

3. To push to a different container registry (e.g., Docker Hub), modify the `REGISTRY` and `IMAGE_NAME` environment variables in the workflow file and add the corresponding login secrets.

### Manual Trigger

You can also trigger the workflow manually from the GitHub Actions tab.

## API Documentation

Once the service is running, OpenAPI documentation is available at:

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI spec: http://localhost:8081/v3/api-docs

## License

Proprietary