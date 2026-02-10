# Calculator Module

## Overview

The **Calculator Module** is a Spring Boot application that provides the core calculation logic. It consumes calculation requests from Apache Kafka, performs the mathematical operations, and sends back the results.

This module is designed to be **horizontally scalable** - multiple instances can run concurrently, each processing messages from the Kafka topic.

## Package Structure

```
calculator/
├── Dockerfile
├── pom.xml
└── src/
    ├── main/
    │   ├── java/calculator/
    │   │   ├── CalculatorApplication.java      # Spring Boot entry point
    │   │   ├── config/
    │   │   │   └── KafkaConfig.java            # Kafka consumer/producer config
    │   │   ├── exception/
    │   │   │   ├── CalculationException.java   # Base exception
    │   │   │   ├── DivisionByZeroException.java
    │   │   │   └── UnsupportedOperationException.java
    │   │   ├── handler/
    │   │   │   └── CalculationRequestHandler.java  # Request processing facade
    │   │   ├── kafka/
    │   │   │   ├── CalculationConsumer.java    # Kafka message consumer
    │   │   │   └── KafkaReplyService.java      # Kafka reply sender
    │   │   └── service/
    │   │       └── CalculatorService.java      # Core calculation logic
    │   └── resources/
    │       ├── application.properties
    │       └── logback-spring.xml
    └── test/java/calculator/
        ├── handler/
        │   └── CalculationRequestHandlerTest.java
        ├── kafka/
        │   ├── CalculationConsumerTest.java
        │   └── KafkaReplyServiceTest.java
        └── service/
            └── CalculatorServiceTest.java
```

## Components

### 1. CalculatorService

**Purpose**: Core calculation logic with arbitrary precision support.

**Location**: `calculator.service.CalculatorService`

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `sum(a, b)` | BigDecimal, BigDecimal | BigDecimal | Adds two numbers |
| `subtract(a, b)` | BigDecimal, BigDecimal | BigDecimal | Subtracts b from a |
| `multiply(a, b)` | BigDecimal, BigDecimal | BigDecimal | Multiplies two numbers |
| `divide(a, b)` | BigDecimal, BigDecimal | BigDecimal | Divides a by b |

#### Implementation Details

```java
private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

public BigDecimal divide(BigDecimal a, BigDecimal b) {
    if (b.compareTo(BigDecimal.ZERO) == 0) {
        throw new DivisionByZeroException();
    }
    return a.divide(b, MATH_CONTEXT);
}
```

**Why `MathContext.DECIMAL128`?**
- 34 digits of precision
- IEEE 754R standard
- Sufficient for financial and scientific calculations
- Consistent rounding behavior

---

### 2. CalculationRequestHandler

**Purpose**: Facade between Kafka messaging and business logic.

**Location**: `calculator.handler.CalculationRequestHandler`

#### Design Pattern: Facade

The handler:
1. Receives a `CalculationRequest`
2. Delegates to `CalculatorService`
3. Converts result/exception to `CalculationResponse`
4. Never throws exceptions - always returns a response

```java
public CalculationResponse handle(CalculationRequest request) {
    try {
        BigDecimal result = executeOperation(request);
        return CalculationResponse.success(request.getRequestId(), result);
    } catch (Exception e) {
        return CalculationResponse.error(request.getRequestId(), e.getMessage());
    }
}
```

**Why this pattern?**
- Separates messaging concerns from business logic
- Makes error handling consistent
- Simplifies testing (mock the handler, not Kafka)
- Single responsibility principle

---

### 3. CalculationConsumer

**Purpose**: Kafka message consumer - thin layer focused on messaging.

**Location**: `calculator.kafka.CalculationConsumer`

#### Responsibilities

1. Listen to Kafka topic
2. Extract headers (reply topic, correlation ID)
3. Set up MDC for logging
4. Delegate to handler
5. Send reply via KafkaReplyService

#### Does NOT Handle

- Business logic
- Error conversion
- Response creation

```java
@KafkaListener(
    topics = "${kafka.topic.requests}",
    groupId = "calculator-group",
    containerFactory = "calculationRequestListenerContainerFactory"
)
public void consume(
        @Payload CalculationRequest request,
        @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTopicBytes,
        @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
    
    setupMdc(request.getRequestId());
    try {
        CalculationResponse response = requestHandler.handle(request);
        replyService.sendReply(replyTopic, correlationId, response);
    } finally {
        clearMdc();
    }
}
```

---

### 4. KafkaReplyService

**Purpose**: Encapsulates Kafka reply sending logic.

**Location**: `calculator.kafka.KafkaReplyService`

```java
public void sendReply(String replyTopic, byte[] correlationId, CalculationResponse response) {
    ProducerRecord<String, CalculationResponse> record = 
            new ProducerRecord<>(replyTopic, null, null, response);
    record.headers().add(KafkaHeaders.CORRELATION_ID, correlationId);
    
    kafkaTemplate.send(record)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send reply: {}", ex.getMessage());
                }
            });
}
```

**Why separate service?**
- Testable in isolation
- Reusable if needed
- Async handling with proper logging
- Single responsibility

---

### 5. Exception Hierarchy

```
CalculationException (base)
├── DivisionByZeroException
└── UnsupportedOperationException
```

#### CalculationException

Base exception for all calculator-related errors.

```java
public class CalculationException extends RuntimeException {
    public CalculationException(String message) {
        super(message);
    }
}
```

#### DivisionByZeroException

Thrown when attempting to divide by zero.

```java
public class DivisionByZeroException extends CalculationException {
    public DivisionByZeroException() {
        super("Division by zero is not allowed");
    }
}
```

**Why custom exceptions?**
- More descriptive than generic `ArithmeticException`
- Can add additional context (future enhancement)
- Enables specific error handling if needed
- Clear API contract

---

## Configuration

### application.properties

```properties
# Application
spring.application.name=calculator

# Kafka
spring.kafka.bootstrap-servers=kafka:9092
kafka.topic.requests=calculation-requests
kafka.topic.results=calculation-results

# Logging
logging.level.calculator=INFO
```

### Kafka Configuration

**Consumer Configuration** (`KafkaConfig.java`):

```java
@Bean
public ConsumerFactory<String, CalculationRequest> calculationRequestConsumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "calculator-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    return new DefaultKafkaConsumerFactory<>(props);
}
```

**Key settings**:
- `GROUP_ID_CONFIG`: All calculator instances share the same group for load balancing
- `AUTO_OFFSET_RESET_CONFIG`: "earliest" ensures no messages are missed on startup
- `TRUSTED_PACKAGES`: Required for JSON deserialization

---

## Design Decisions

### 1. Thin Consumer Pattern

**Problem**: Fat consumers mix messaging and business logic, making them hard to test.

**Solution**: Consumer only handles:
- Message reception
- Header extraction
- MDC setup
- Delegation

**Benefits**:
- Unit test handler without Kafka
- Easy to mock in integration tests
- Clear separation of concerns

### 2. MDC for Request Tracing

**Implementation**:
```java
private void setupMdc(String requestId) {
    if (requestId != null) {
        MDC.put("requestId", requestId);
    }
}
```

**Benefits**:
- All log messages include request ID
- Easy to trace requests across services
- Works with distributed tracing systems

**Log output**:
```
2026-02-10 02:05:55 INFO [dbb39acf-8251-4975-b4d8-2ec0e05e3c9c] c.kafka.CalculationConsumer - Received calculation request
```

### 3. Async Reply with Logging

```java
kafkaTemplate.send(record)
    .whenComplete((result, ex) -> {
        if (ex != null) {
            log.error("Failed to send reply: {}", ex.getMessage());
        }
    });
```

**Why async?**
- Non-blocking
- Better throughput
- Errors logged but don't block processing

### 4. BigDecimal with MathContext

**Problem**: Division can result in infinite decimals (e.g., 10/3).

**Solution**: Use `MathContext.DECIMAL128`:
```java
a.divide(b, MathContext.DECIMAL128);
// 10 / 3 = 3.333333333333333333333333333333333
```

**Benefits**:
- 34 digits of precision
- IEEE 754R standard compliance
- Consistent rounding (HALF_EVEN)

---

## Testing

### Running Tests

```bash
cd calculator
mvn test
```

### Test Coverage

| Class | Tests | Focus |
|-------|-------|-------|
| `CalculatorServiceTest` | 7 | Calculation logic, division by zero |
| `CalculationRequestHandlerTest` | 6 | Request handling, error conversion |
| `CalculationConsumerTest` | 3 | Delegation verification |
| `KafkaReplyServiceTest` | 2 | Reply sending, correlation ID |

### Example Tests

**CalculatorService**:
```java
@Test
void testDivideByZero() {
    DivisionByZeroException ex = assertThrows(
        DivisionByZeroException.class,
        () -> calculatorService.divide(new BigDecimal("10"), BigDecimal.ZERO)
    );
    assertEquals("Division by zero is not allowed", ex.getMessage());
}
```

**Handler**:
```java
@Test
void testHandleDivisionByZero() {
    when(calculatorService.divide(any(), any()))
        .thenThrow(new DivisionByZeroException());
    
    CalculationResponse response = handler.handle(request);
    
    assertTrue(response.isError());
    assertEquals("Division by zero is not allowed", response.getError());
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
COPY calculator ./calculator
RUN mvn clean package -DskipTests -pl calculator -am

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/calculator/target/calculator-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

**Multi-stage build**:
1. Build stage: Compile with Maven
2. Runtime stage: Minimal JRE image

**Benefits**:
- Smaller final image (~300MB vs ~800MB)
- No build tools in production
- Faster deployment

---

## Logging

### Logback Configuration

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level [%X{requestId}] %logger{36} - %msg%n</pattern>
```

**Features**:
- Request ID in every log line
- File rotation (10MB, 7 days retention)
- Separate file for calculator module

### Log Levels

| Logger | Level | Purpose |
|--------|-------|---------|
| `calculator` | INFO | Application logs |
| `org.apache.kafka` | WARN | Reduce Kafka noise |
| `ROOT` | INFO | Default |

---

## Scalability

### Horizontal Scaling

Multiple calculator instances can run concurrently:

Remove:

```yaml
# docker-compose.yml
container-name: calculator
```

```yaml
# docker-compose scale
docker-compose up --scale calculator=3
```

**How it works**:
- All instances use same consumer group (`calculator-group`)
- Kafka automatically distributes partitions
- Each instance processes different messages

### Configuration for Scaling

With 1 partition (current):
- Only 1 instance active at a time
- Others standby for failover

To enable parallel processing:
```java
// KafkaTopicConfig in REST module
.partitions(3)  // Match number of instances
```

---

## Dependencies

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
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

## Version History

| Version | Changes |
|---------|---------|
| 1.0.0 | Initial release with basic calculator operations |
| 1.0.1 | Refactored with Handler pattern, custom exceptions, KafkaReplyService |

