# Jumbo Store Locator - Docker Compose Setup

This Docker Compose configuration brings up the complete Jumbo Store Locator application with both the backend location service and frontend together.

## Services

- **location-service**: Spring Boot backend API running on port 8080
- **store-locator-frontend**: React frontend running on port 3000

## Quick Start

```bash
# Build and start all services
docker-compose up --build

# Run in background
docker-compose up -d --build

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f location-service
docker-compose logs -f store-locator-frontend
```

## Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/v1
- **API Health Check**: http://localhost:8080/api/v1/health

## Configuration

The frontend is configured to communicate with the backend at `http://localhost:8080/api/v1`. This can be customized by modifying the `API_BASE_URL` environment variable in the docker-compose.yml file.

## Development

For development, you can run individual services:

```bash
# Start only the backend
docker-compose up location-service

# Start only the frontend (requires backend running)
docker-compose up store-locator-frontend
```

## Troubleshooting

- Ensure ports 3000 and 8080 are not in use by other applications
- The frontend waits for the backend to be healthy before starting
- Check logs with `docker-compose logs` if services fail to start
