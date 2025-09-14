# Jumbo Store Location Service

A professional REST API service for finding the nearest Jumbo stores based on geographical coordinates.

## 🚀 Features

- **Fast Location Search**: Find the 5 closest Jumbo stores to any coordinate
- **Multiple Search Strategies**: Choose between QuadTree (optimized) and In-Memory search
- **Open Store Filtering**: Option to filter only currently open stores
- **Professional API**: RESTful design with comprehensive validation and error handling
- **Interactive Documentation**: Swagger UI for easy API exploration
- **Health Monitoring**: Built-in health checks and metrics
- **Production Ready**: Comprehensive logging, error handling, and configuration

## 🏗️ Architecture

The service follows clean architecture principles with:

- **Controller Layer**: REST API endpoints with validation
- **Service Layer**: Business logic with pluggable search strategies
- **Model Layer**: Domain objects with proper encapsulation
- **Configuration**: Externalized configuration for different environments

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL (optional, for database features)

## 🛠️ Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd jumbo/location-service
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

### 3. Explore the API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Documentation**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/api/v1/health

## 📖 API Usage

### Find Nearby Stores

```bash
GET /api/v1/stores/nearby?latitude=52.3702&longitude=4.8952&limit=5&onlyOpen=false
```

**Parameters:**
- `latitude` (required): Latitude coordinate (-90 to 90)
- `longitude` (required): Longitude coordinate (-180 to 180)
- `limit` (optional): Maximum number of stores to return (1-50, default: 5)
- `onlyOpen` (optional): Filter only open stores (default: false)

**Example Response:**
```json
[
  {
    "uuid": "gVOTFx8cACn8BnUCltyf",
    "city": "Amsterdam",
    "postalCode": "1012 AB",
    "street": "Damrak 123",
    "latitude": 52.3702,
    "longitude": 4.8952,
    "todayOpen": "08:00",
    "todayClose": "22:00",
    "distance": 0.0,
    "locationType": "supermarket",
    "collectionPoint": false
  }
]
```

## ⚙️ Configuration

Key configuration options in `application.yaml`:

```yaml
jumbo:
  location:
    search:
      strategy: quadtree  # Options: quadtree, in-memory
      max-distance-km: 50
    stores:
      data-file: classpath:stores.json
      cache-enabled: true
      cache-ttl-minutes: 60
```

## 🧪 Testing

Run the test suite:

```bash
mvn test
```

The project includes:
- Unit tests for controllers
- Integration tests for services
- API validation tests
- Performance tests for different search strategies

## 📊 Monitoring

The service exposes several monitoring endpoints:

- `/actuator/health` - Health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

## 🏷️ Search Strategies

### QuadTree Strategy (Recommended)
- **Performance**: O(log n) average case
- **Best for**: Large datasets, frequent searches
- **Memory**: Higher memory usage for tree structure

### In-Memory Strategy
- **Performance**: O(n) linear search
- **Best for**: Small datasets, simple implementation
- **Memory**: Lower memory footprint

Configure via `jumbo.location.search.strategy` property.

## 🔧 Production Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t jumbo-location-service .

# Run container
docker run -p 8080:8080 jumbo-location-service
```

### Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=production
export JUMBO_LOCATION_SEARCH_STRATEGY=quadtree
export LOGGING_LEVEL_COM_JUMBO=INFO
```

## 📝 API Design Principles

1. **RESTful Design**: Following REST conventions
2. **Comprehensive Validation**: Input validation with clear error messages
3. **Error Handling**: Consistent error responses with proper HTTP status codes
4. **Documentation**: Self-documenting API with OpenAPI/Swagger
5. **Versioning**: API versioning support (`/api/v1/`)
6. **Performance**: Optimized search algorithms for fast response times

## 🚀 Additional Features for Production

- **Rate Limiting**: Protect against abuse
- **Caching**: Redis caching for frequently accessed data
- **Security**: API authentication and authorization
- **Monitoring**: Application performance monitoring (APM)
- **Logging**: Structured logging with correlation IDs

## 🤝 Contributing

1. Follow the existing code style and patterns
2. Add tests for new functionality
3. Update documentation as needed
4. Ensure all tests pass before submitting

## 📞 Support

For questions or issues, please contact the development team at developer@jumbo.com
