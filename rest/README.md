# REST Module

## Overview

The **REST Module** is a Spring Boot web application that exposes the calculator functionality via a RESTful API. It acts as the entry point for client requests, communicates with the Calculator service via Apache Kafka, and returns results to clients.

## Package Structure

```
rest/
├── Dockerfile
├── pom.xml
└── src/
    ├── main/
    │   ├── java/rest/
    │   │   ├── RestApplication.java           # Spring Boot entry point
    │   │   ├── config/
    │   │   │   ├── KafkaConfig.java           # Kafka producer/consumer config
    │   │   │   ├── KafkaTopicConfig.java      # Topic creation config
    │   │   │   └── RequestIdFilter.java       # Unique request ID filter
    │   │   ├── constants/
    │   │   │   └── AppConstants.java          # Application constants
    │   │   ├── controller/
    │   │   │   ├── CalculatorController.java  # REST endpoints (thin)
    │   │   │   └── HealthController.java      # Health check endpoint
    │   │   ├── dto/
    │   │   │   └── ApiResponse.java           # Unified API response
    │   │   ├── exception/
    │   │   │   ├── CalculationException.java
    │   │   │   ├── CalculatorTimeoutException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   └── service/
    │   │       └── CalculatorService.java     # Kafka communication logic
    │   └── resources/
    │       ├── application.properties
    │       └── logback-spring.xml
    └── test/java/rest/
        ├── controller/
        │   └── CalculatorControllerTest.java
        ├── exception/
        │   └── GlobalExceptionHandlerTest.java
        └── service/
            └── CalculatorServiceTest.java
```

## API Endpoints

### Calculator Operations

| Method | Endpoint | Parameters | Description |
|--------|----------|------------|-------------|
| GET | `/sum` | `a`, `b` | Adds two numbers |
| GET | `/subtract` | `a`, `b` | Subtracts b from a |
| GET | `/multiply` | `a`, `b` | Multiplies two numbers |
| GET | `/divide` | `a`, `b` | Divides a by b |
| GET | `/health` | - | Basic health check |
| GET | `/health/info` | - | Detailed health info |

### Request Format

```
GET /sum?a=10&b=5
```

### Response Format

**Success**:
```json
{
    "result": 15
}
```

**Error**:
```json
{
    "error": "Division by zero is not allowed"
}
```

### Response Headers

| Header | Description |
|--------|-------------|
| `X-Request-ID` | Unique identifier for request tracing |
| `Content-Type` | `application/json` |

---

## Components

### 1. CalculatorController

**Purpose**: Thin REST controller - handles HTTP concerns only.

**Location**: `rest.controller.CalculatorController`

```java
@RestController
public class CalculatorController {
    
    private final CalculatorService calculatorService;
    
    @GetMapping("/sum")
    public ResponseEntity<ApiResponse> sum(
            @RequestParam("a") BigDecimal a,
            @RequestParam("b") BigDecimal b) {
        BigDecimal result = calculatorService.sum(a, b);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
```

**Design Pattern**: Thin Controller

**What it does**:
- Parse request parameters
- Call service layer
- Return response

**What it doesn't do**:
- Business logic
- Kafka communication
- Error handling (delegated to GlobalExceptionHandler)

---

### 2. CalculatorService

**Purpose**: Service layer handling Kafka communication.

**Location**: `rest.service.CalculatorService`

```java
@Service
public class CalculatorService {
    
    private final ReplyingKafkaTemplate<String, CalculationRequest, CalculationResponse> 
        replyingKafkaTemplate;
    
    public BigDecimal sum(BigDecimal a, BigDecimal b) {
        return calculate(OperationType.SUM, a, b);
    }
    
    private BigDecimal calculate(OperationType operation, BigDecimal a, BigDecimal b) {
        CalculationRequest request = new CalculationRequest(requestId, operation, a, b);
        
        RequestReplyFuture<String, CalculationRequest, CalculationResponse> future =
            replyingKafkaTemplate.sendAndReceive(record);
        
        CalculationResponse response = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        if (response.getError() != null) {
            throw new CalculationException(response.getError());
        }
        
        return response.getResult();
    }
}
```

**Key features**:
- Request-Reply pattern with Kafka
- Timeout handling (10 seconds)
- Error conversion to exceptions

---

### 3. GlobalExceptionHandler

**Purpose**: Centralized error handling for consistent API responses.

**Location**: `rest.exception.GlobalExceptionHandler`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CalculationException.class)
    public ResponseEntity<ApiResponse> handleCalculationException(CalculationException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(CalculatorTimeoutException.class)
    public ResponseEntity<ApiResponse> handleTimeoutException(CalculatorTimeoutException ex) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Missing required parameter: " + ex.getParameterName()));
    }
}
```

**Handled exceptions**:

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `CalculationException` | 400 Bad Request | Business logic errors |
| `CalculatorTimeoutException` | 504 Gateway Timeout | Kafka timeout |
| `MissingServletRequestParameterException` | 400 Bad Request | Missing a or b |
| `MethodArgumentTypeMismatchException` | 400 Bad Request | Invalid number format |
| `Exception` | 500 Internal Server Error | Unexpected errors |

---

### 4. ApiResponse

**Purpose**: Unified response DTO for all API responses.

**Location**: `rest.dto.ApiResponse`

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    
    private BigDecimal result;
    private String error;
    
    public static ApiResponse success(BigDecimal result) {
        ApiResponse response = new ApiResponse();
        response.result = result;
        return response;
    }
    
    public static ApiResponse error(String errorMessage) {
        ApiResponse response = new ApiResponse();
        response.error = errorMessage;
        return response;
    }
}
```

**Design Pattern**: Factory Methods

**Key feature**: `@JsonInclude(JsonInclude.Include.NON_NULL)`
- Success response: `{"result": 15}` (no error field)
- Error response: `{"error": "..."}` (no result field)

---

### 5. RequestIdFilter

**Purpose**: Generate unique request IDs for tracing.

**Location**: `rest.config.RequestIdFilter`

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends HttpFilter {
    
    @Override
    protected void doFilter(HttpServletRequest request, 
                            HttpServletResponse response,
                            FilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        
        MDC.put(AppConstants.MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(AppConstants.REQUEST_ID_HEADER, requestId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(AppConstants.MDC_REQUEST_ID_KEY);
        }
    }
}
```

**Benefits**:
- Every request has unique ID
- ID in response headers for client correlation
- ID in MDC for log correlation
- ID propagated to Calculator via Kafka

---

### 6. AppConstants

**Purpose**: Centralize magic strings and configuration values.

**Location**: `rest.constants.AppConstants`

```java
public final class AppConstants {
    
    private AppConstants() {} // Prevent instantiation
    
    // HTTP Headers
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    // MDC Keys
    public static final String MDC_REQUEST_ID_KEY = "requestId";
    
    // Kafka Topics (default values)
    public static final String DEFAULT_REQUEST_TOPIC = "calculation-requests";
    public static final String DEFAULT_RESULT_TOPIC = "calculation-results";
    
    // Timeouts
    public static final long KAFKA_REPLY_TIMEOUT_SECONDS = 10;
    
    // Error Messages
    public static final String ERROR_TIMEOUT = "Timeout waiting for calculator response";
    public static final String ERROR_MISSING_PARAM = "Missing required parameter: %s";
}
```

**Benefits**:
- No magic strings scattered in code
- Easy to find and update constants
- Consistent naming across codebase

---

## Configuration

### application.properties

```properties
# Server
server.port=8080

# Kafka
spring.kafka.bootstrap-servers=kafka:9092
kafka.topic.requests=calculation-requests
kafka.topic.results=calculation-results

# Logging
logging.level.rest=INFO
```

### Kafka Configuration

**ReplyingKafkaTemplate** (`KafkaConfig.java`):

```java
@Bean
public ReplyingKafkaTemplate<String, CalculationRequest, CalculationResponse> 
        calculationRequestReplyKafkaTemplate() {
    
    ReplyingKafkaTemplate<...> template = new ReplyingKafkaTemplate<>(
        producerFactory, 
        replyContainer
    );
    template.setDefaultReplyTimeout(Duration.ofSeconds(10));
    template.setSharedReplyTopic(true);
    return template;
}
```

**Key settings**:
- `setDefaultReplyTimeout(10s)`: Prevents indefinite waiting
- `setSharedReplyTopic(true)`: Multiple REST instances share reply topic

### Topic Configuration

**KafkaTopicConfig.java**:

```java
@Bean
public NewTopic calculationRequestsTopic() {
    return TopicBuilder.name("calculation-requests")
            .partitions(1)
            .replicas(1)
            .build();
}

@Bean
public NewTopic calculationResultsTopic() {
    return TopicBuilder.name("calculation-results")
            .partitions(1)
            .replicas(1)
            .build();
}
```

**Why 1 partition?**
- Simplifies development/testing
- Ensures message ordering
- For production: increase for parallelism

---

## Design Decisions

### 1. Thin Controller Pattern

**Problem**: Fat controllers mix HTTP handling with business logic.

**Solution**:
```java
// Controller - only HTTP concerns
@GetMapping("/sum")
public ResponseEntity<ApiResponse> sum(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
    return ResponseEntity.ok(ApiResponse.success(calculatorService.sum(a, b)));
}

// Service - business logic and Kafka
public BigDecimal sum(BigDecimal a, BigDecimal b) {
    return calculate(OperationType.SUM, a, b);
}
```

**Benefits**:
- Controllers are trivial to test
- Service can be tested without HTTP
- Clear separation of concerns

### 2. Global Exception Handler

**Problem**: Error handling scattered across controllers.

**Solution**: `@RestControllerAdvice` catches all exceptions:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CalculationException.class)
    public ResponseEntity<ApiResponse> handleCalculationException(CalculationException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }
}
```

**Benefits**:
- Consistent error format
- Controllers don't need try-catch
- Easy to add new exception types
- Centralized logging

### 3. Request-Reply with Kafka

**Problem**: Kafka is async, but REST is sync.

**Solution**: Spring Kafka's `ReplyingKafkaTemplate`:

```java
// Send and wait for reply
RequestReplyFuture<...> future = replyingKafkaTemplate.sendAndReceive(record);
CalculationResponse response = future.get(10, TimeUnit.SECONDS);
```

**How it works**:
1. REST sends request with correlation ID
2. Calculator processes and sends reply with same correlation ID
3. REST matches reply to original request
4. REST returns HTTP response

### 4. Unified ApiResponse

**Problem**: Inconsistent response formats (sometimes result, sometimes error object).

**Solution**: Single DTO with factory methods:

```java
// Success
ApiResponse.success(new BigDecimal("15"));  // {"result": 15}

// Error
ApiResponse.error("Division by zero");       // {"error": "..."}
```

**Benefits**:
- Client always knows response structure
- `@JsonInclude(NON_NULL)` removes null fields
- Factory methods prevent invalid states

### 5. Request ID for Tracing

**Bonus Feature** (+3 points)

**Implementation**:
1. Filter generates UUID
2. UUID added to response header
3. UUID added to MDC for logs
4. UUID sent in Kafka message
5. Calculator logs include UUID

**Trace example**:
```
REST:       [abc-123] Received request: sum(10, 5)
REST:       [abc-123] Sending to Kafka
Calculator: [abc-123] Processing calculation
Calculator: [abc-123] Result: 15
REST:       [abc-123] Returning result
```

---

## Testing

### Running Tests

```bash
cd rest
mvn test
```

### Test Coverage

| Class | Tests | Focus |
|-------|-------|-------|
| `CalculatorControllerTest` | 4 | HTTP handling, delegation |
| `CalculatorServiceTest` | 6 | Kafka communication, errors |
| `GlobalExceptionHandlerTest` | 5 | All exception types |

### Example Tests

**Controller Test** (mocked service):
```java
@Test
void testSum() {
    when(calculatorService.sum(a, b)).thenReturn(new BigDecimal("15"));
    
    ResponseEntity<ApiResponse> result = calculatorController.sum(a, b);
    
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(new BigDecimal("15"), result.getBody().getResult());
    verify(calculatorService).sum(a, b);
}
```

**Service Test** (mocked Kafka):
```java
@Test
void testDivideByZero() {
    mockErrorResponse("Division by zero");
    
    assertThrows(CalculationException.class, 
        () -> calculatorService.divide(new BigDecimal("10"), BigDecimal.ZERO));
}
```

---

## Docker

### Dockerfile

```dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY messaging ./messaging
COPY rest ./rest
RUN mvn clean package -DskipTests -pl rest -am

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y curl  # For health checks
COPY --from=build /workspace/rest/target/rest-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Health Check

```yaml
# docker-compose.yml
healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
    interval: 10s
    timeout: 5s
    retries: 5
```

---

## Logging

### Logback Configuration

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level [%X{requestId}] %logger{36} - %msg%n</pattern>
```

**Output**:
```
2026-02-10 02:05:55 INFO [dbb39acf-8251-4975-b4d8-2ec0e05e3c9c] r.service.CalculatorService - Processing calculation
```

### Log Files

| File | Content |
|------|---------|
| `logs/rest.log` | REST module logs |
| `logs/rest-error.log` | ERROR level only |

---

## Error Handling

### HTTP Status Codes

| Status | Condition |
|--------|-----------|
| 200 OK | Successful calculation |
| 400 Bad Request | Invalid input, division by zero |
| 504 Gateway Timeout | Calculator didn't respond in time |
| 500 Internal Server Error | Unexpected errors |

### Error Response Format

```json
{
    "error": "Human-readable error message"
}
```

---

## Dependencies

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <!-- JSON -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-json</artifactId>
    </dependency>
    
    <!-- Shared DTOs -->
    <dependency>
        <groupId>wit.calculator.project</groupId>
        <artifactId>messaging</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Usage Examples

### cURL

```bash
# Sum
curl "http://localhost:8080/sum?a=10&b=5"
# {"result":15}

# Divide
curl "http://localhost:8080/divide?a=10&b=3"
# {"result":3.333333333333333333333333333333333}

# Error
curl "http://localhost:8080/divide?a=10&b=0"
# {"error":"Division by zero is not allowed"}

# Health Check
curl "http://localhost:8080/health"
# {"status":"UP"}

# Get Request ID
curl -I "http://localhost:8080/sum?a=1&b=1"
# X-Request-ID: abc-123-def-456
```

### JavaScript (Fetch)

```javascript
const response = await fetch('http://localhost:8080/sum?a=10&b=5');
const data = await response.json();

if (data.result !== undefined) {
    console.log('Result:', data.result);
} else {
    console.error('Error:', data.error);
}
```

---

## Version History

| Version | Changes |
|---------|---------|
| 1.0.0 | Initial release with REST endpoints |
| 1.0.1 | Refactored with thin controller, global exception handler, unified ApiResponse |

