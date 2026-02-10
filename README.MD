# Calculator Project

A distributed RESTful API Calculator built with **Spring Boot** and **Apache Kafka** for inter-module communication. Supports arbitrary precision decimal arithmetic using `BigDecimal`.

## Project Structure

```
calculator-project/
├── calculator/          # Calculator service (Kafka consumer)
│   └── README.md        # Detailed calculator documentation
├── messaging/           # Shared DTOs and enums
│   └── README.md        # Detailed messaging documentation
├── rest/               # REST API (Kafka producer)
│   └── README.md        # Detailed REST documentation
├── docker-compose.yml  # Docker orchestration
├── test-api.sh         # API test script
└── pom.xml            # Parent POM
```

> **See individual module READMEs for detailed documentation:**
> - [Messaging Module](messaging/README.md) - DTOs, enums, builder patterns
> - [Calculator Module](calculator/README.md) - Calculation logic, Kafka consumer
> - [REST Module](rest/README.md) - REST API, Kafka producer, error handling

## Features

| Feature | Description |
|---------|-------------|
| **Arbitrary Precision** | Uses `BigDecimal` with 34 digits of precision |
| **RESTful API** | Clean endpoints: `/sum`, `/subtract`, `/multiply`, `/divide` |
| **Async Processing** | Kafka-based communication between modules |
| **Request Tracing** | Unique `X-Request-ID` header for each request |
| **MDC Propagation** | Request ID tracked across all services |
| **Error Handling** | Consistent error responses with proper HTTP codes |
| **Health Check** | `/health` endpoint for monitoring |
| **Dockerized** | Ready-to-run with Docker Compose |

## Quick Start

### Prerequisites

- Docker & Docker Compose
- (Optional) Java 21+ and Maven 3.6+ for local development

### Running with Docker Compose

```bash
# Clone and start
git clone <repository>
cd calculator-project
docker-compose up --build

# API available at http://localhost:8080
```

### Test the API

```bash
# Run test script
./test-api.sh

# Or manually:
curl "http://localhost:8080/sum?a=10&b=5"        # {"result":15}
curl "http://localhost:8080/divide?a=10&b=3"    # {"result":3.333...}
curl "http://localhost:8080/divide?a=10&b=0"    # {"error":"Division by zero is not allowed"}
```

## API Endpoints

| Operation | Endpoint | Example | Response |
|-----------|----------|---------|----------|
| Sum | `GET /sum?a={a}&b={b}` | `/sum?a=10&b=5` | `{"result":15}` |
| Subtract | `GET /subtract?a={a}&b={b}` | `/subtract?a=20&b=8` | `{"result":12}` |
| Multiply | `GET /multiply?a={a}&b={b}` | `/multiply?a=6&b=7` | `{"result":42}` |
| Divide | `GET /divide?a={a}&b={b}` | `/divide?a=100&b=4` | `{"result":25}` |
| Health | `GET /health` | `/health` | `{"status":"UP"}` |
| Health Info | `GET /health/info` | `/health/info` | `{"status":"UP","service":"rest",...}` |

### Response Format

**Success:**
```json
{"result": 15}
```

**Error:**
```json
{"error": "Division by zero is not allowed"}
```

### Response Headers

| Header | Description |
|--------|-------------|
| `X-Request-ID` | Unique UUID for request tracing |
| `Content-Type` | `application/json` |

## Module Overview

### Messaging Module
- **Purpose**: Shared DTOs and enums
- **Dependencies**: None (pure Java)
- **Contents**: `CalculationRequest`, `CalculationResponse`, `OperationType`

### Calculator Module
- **Purpose**: Perform calculations
- **Role**: Kafka consumer
- **Key Features**: BigDecimal arithmetic, custom exceptions

### REST Module
- **Purpose**: HTTP API gateway
- **Role**: Kafka producer
- **Key Features**: Request-Reply pattern, request ID generation, centralized constants

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Kafka broker address |
| `SERVER_PORT` | `8080` | REST API port |

### Kafka Topics

| Topic | Purpose |
|-------|---------|
| `calculation-requests` | Requests from REST to Calculator |
| `calculation-results` | Responses from Calculator to REST |

## Running Tests

```bash
# All modules
mvn test

# Specific module
cd calculator && mvn test
cd rest && mvn test
cd messaging && mvn test
```

### Test Coverage

| Module | Test Classes | Coverage Focus |
|--------|--------------|----------------|
| Messaging | 3 | DTOs, enums, validation |
| Calculator | 4 | Service, handler, consumer, reply |
| REST | 3 | Controller, service, exceptions |

## Docker Commands

```bash
# Start services
docker-compose up --build

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs calculator
docker-compose logs rest

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Rebuild specific service
docker-compose build calculator
```

## Logging


### Log Files

| Module | File | Content |
|--------|------|---------|
| REST | `logs/rest.log` | REST module logs |
| Calculator | `logs/calculator.log` | Calculator module logs |

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 3.2.6 | Framework |
| Apache Kafka | 3.7.0 | Messaging |
| Maven | 3.9.6 | Build tool |
| Docker | - | Containerization |
| JUnit 5 | 5.10.2 | Testing |
| Mockito | 5.7.0 | Mocking |
| Logback | 1.4.14 | Logging |

## License

This project is for educational purposes.

