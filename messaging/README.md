# Messaging Module

## Overview

The **Messaging Module** is a shared library that provides the data transfer objects (DTOs) and enums used for communication between the REST API and Calculator services via Apache Kafka.

This module has **no dependencies** on Spring or Kafka - it's a pure Java library that can be used by any module that needs to exchange calculation messages.

## Package Structure

```
messaging/
├── pom.xml
└── src/
    ├── main/java/messaging/
    │   ├── dto/
    │   │   ├── CalculationRequest.java    # Request DTO with Builder pattern
    │   │   └── CalculationResponse.java   # Response DTO with Factory methods
    │   └── enums/
    │       └── OperationType.java         # Enum with symbols and descriptions
    └── test/java/messaging/
        ├── dto/
        │   ├── CalculationRequestTest.java
        │   └── CalculationResponseTest.java
        └── enums/
            └── OperationTypeTest.java
```

## Components

### 1. CalculationRequest

The request DTO sent from the REST service to the Calculator service.

#### Fields

| Field | Type | Description |
|-------|------|-------------|
| `requestId` | `String` | Unique identifier for request tracing (UUID) |
| `operation` | `OperationType` | The mathematical operation to perform |
| `a` | `BigDecimal` | First operand |
| `b` | `BigDecimal` | Second operand |

#### Design Patterns Used

**Builder Pattern**
```java
CalculationRequest request = CalculationRequest.builder()
    .requestId("req-123")
    .operation(OperationType.SUM)
    .a(new BigDecimal("10"))
    .b(new BigDecimal("5"))
    .build();
```

The builder supports multiple input types for convenience:
- `a(BigDecimal value)` - Direct BigDecimal
- `a(String value)` - Parse from string
- `a(long value)` - Convert from long

**Validation**
```java
// Throws IllegalStateException if operation, a, or b is null
request.validate();
```

#### Utility Methods

| Method | Description |
|--------|-------------|
| `toExpression()` | Returns formatted string like `"10 + 5"` |
| `validate()` | Validates all required fields are present |
| `equals()/hashCode()` | Proper implementations for comparisons |
| `toString()` | Human-readable representation |

---

### 2. CalculationResponse

The response DTO sent from the Calculator service back to the REST service.

#### Fields

| Field | Type | Description |
|-------|------|-------------|
| `requestId` | `String` | Original request ID for correlation |
| `result` | `BigDecimal` | The calculation result (null if error) |
| `error` | `String` | Error message (null if success) |

#### Design Patterns Used

**Factory Methods**
```java
// Success response
CalculationResponse response = CalculationResponse.success("req-123", new BigDecimal("15"));

// Error response
CalculationResponse response = CalculationResponse.error("req-123", "Division by zero is not allowed");
```

#### Utility Methods

| Method | Description |
|--------|-------------|
| `isSuccess()` | Returns true if result is present and no error |
| `isError()` | Returns true if error message is present |
| `equals()/hashCode()` | Proper implementations for comparisons |
| `toString()` | Context-aware representation (shows result or error) |

---

### 3. OperationType

Enum representing supported mathematical operations.

#### Values

| Value | Symbol | Description |
|-------|--------|-------------|
| `SUM` | `+` | Addition |
| `SUBTRACT` | `-` | Subtraction |
| `MULTIPLY` | `*` | Multiplication |
| `DIVIDE` | `/` | Division |

#### Design Choices

**Rich Enum Pattern**
Instead of a simple enum, `OperationType` includes:
- `getSymbol()` - Returns mathematical symbol
- `getDescription()` - Returns human-readable name
- `toString()` - Returns formatted string like `"Addition (+)"`

```java
OperationType.SUM.getSymbol();       // "+"
OperationType.SUM.getDescription();  // "Addition"
OperationType.SUM.toString();        // "Addition (+)"
```

This enables:
- Cleaner logging and debugging
- Easy expression formatting (`request.toExpression()`)
- UI-friendly descriptions

---

## Design Decisions

### 1. Why BigDecimal?

**Requirement**: Support arbitrary precision decimal numbers.

**Problem with `double`**:
```java
double result = 0.1 + 0.2;  // Returns 0.30000000000000004
```

**Solution with `BigDecimal`**:
```java
BigDecimal result = new BigDecimal("0.1").add(new BigDecimal("0.2"));  // Returns 0.3
```

BigDecimal provides:
- Exact decimal representation
- Arbitrary precision (limited only by memory)
- Correct handling of financial/scientific calculations

### 2. Why Builder Pattern for Request?

**Benefits**:
1. **Readability** - Clear what each parameter represents
2. **Flexibility** - Optional parameters handled cleanly
3. **Validation** - Build-time validation ensures valid objects
4. **Immutability-friendly** - Easy to create immutable objects

**Alternative Considered**: Telescoping constructors
```java
// Hard to read - which number is a? which is b?
new CalculationRequest("id", OperationType.SUM, new BigDecimal("10"), new BigDecimal("5"));
```

### 3. Why Factory Methods for Response?

**Benefits**:
1. **Intent clarity** - `success()` vs `error()` makes code self-documenting
2. **Prevents invalid states** - Can't accidentally create response with both result and error
3. **Simpler API** - No need to pass null explicitly

**Alternative Considered**: Single constructor with nulls
```java
// Unclear intent, error-prone
new CalculationResponse("id", result, null);  // Is this success or incomplete?
```

### 4. Why Setters for JSON Deserialization?

Even though we encourage immutable usage via builder/factory methods, setters are needed for:
- Jackson JSON deserialization (default behavior)
- Framework compatibility
- Test utilities

The design encourages immutable usage while maintaining framework compatibility.

### 5. Why No Spring Dependencies?

**Benefits**:
1. **Lightweight** - Can be used in any Java project
2. **Fast compilation** - No transitive dependencies
3. **Reusable** - Could be published as a standalone library
4. **Testable** - No Spring context needed for unit tests

---

## Testing

### Unit Tests

```bash
cd messaging
mvn test
```

### Test Coverage

| Class | Tests | Coverage |
|-------|-------|----------|
| `CalculationRequest` | 12 | Builder, validation, equals, toString |
| `CalculationResponse` | 10 | Factory methods, isSuccess/isError, equals |
| `OperationType` | 12 | Symbols, descriptions, valueOf, values |

### Example Tests

```java
@Test
void testBuilderWithValidation() {
    assertThrows(IllegalStateException.class, () -> 
        CalculationRequest.builder()
            .a(new BigDecimal("10"))
            // Missing operation and b
            .build()
    );
}

@Test
void testSuccessResponse() {
    CalculationResponse response = CalculationResponse.success("req-1", new BigDecimal("42"));
    
    assertTrue(response.isSuccess());
    assertFalse(response.isError());
    assertEquals(new BigDecimal("42"), response.getResult());
}
```

---

## Usage Examples

### Creating a Request

```java
// Using builder (recommended)
CalculationRequest request = CalculationRequest.builder()
    .requestId(UUID.randomUUID().toString())
    .operation(OperationType.DIVIDE)
    .a("100")
    .b("4")
    .build();

// Get expression for logging
log.info("Processing: {}", request.toExpression());  // "100 / 4"
```

### Handling a Response

```java
CalculationResponse response = calculatorService.calculate(request);

if (response.isSuccess()) {
    log.info("Result: {}", response.getResult());
} else {
    log.error("Error: {}", response.getError());
}
```

### Serialization (Jackson)

```java
ObjectMapper mapper = new ObjectMapper();

// Serialize
String json = mapper.writeValueAsString(request);
// {"requestId":"req-1","operation":"SUM","a":10,"b":5}

// Deserialize
CalculationRequest parsed = mapper.readValue(json, CalculationRequest.class);
```

---

## Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Note**: No runtime dependencies! This module is pure Java.

---

## Version History

| Version | Changes |
|---------|---------|
| 1.0.0 | Initial release with CalculationRequest, CalculationResponse, OperationType |

