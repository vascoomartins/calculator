package messaging.dto;

import messaging.enums.OperationType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculationRequestTest {

    @Test
    void testConstructor() {
        CalculationRequest request = new CalculationRequest(
                "req-1", OperationType.SUM, new BigDecimal("10"), new BigDecimal("5"));

        assertEquals("req-1", request.getRequestId());
        assertEquals(OperationType.SUM, request.getOperation());
        assertEquals(new BigDecimal("10"), request.getA());
        assertEquals(new BigDecimal("5"), request.getB());
    }

    @Test
    void testBuilderWithBigDecimal() {
        CalculationRequest request = CalculationRequest.builder()
                .requestId("req-1")
                .operation(OperationType.MULTIPLY)
                .a(new BigDecimal("6"))
                .b(new BigDecimal("7"))
                .build();

        assertEquals("req-1", request.getRequestId());
        assertEquals(OperationType.MULTIPLY, request.getOperation());
        assertEquals(new BigDecimal("6"), request.getA());
        assertEquals(new BigDecimal("7"), request.getB());
    }

    @Test
    void testBuilderWithStrings() {
        CalculationRequest request = CalculationRequest.builder()
                .requestId("req-2")
                .operation(OperationType.DIVIDE)
                .a("100")
                .b("4")
                .build();

        assertEquals(new BigDecimal("100"), request.getA());
        assertEquals(new BigDecimal("4"), request.getB());
    }

    @Test
    void testBuilderWithLongs() {
        CalculationRequest request = CalculationRequest.builder()
                .requestId("req-3")
                .operation(OperationType.SUBTRACT)
                .a(20L)
                .b(8L)
                .build();

        assertEquals(BigDecimal.valueOf(20), request.getA());
        assertEquals(BigDecimal.valueOf(8), request.getB());
    }

    @Test
    void testValidateMissingOperation() {
        assertThrows(IllegalStateException.class, () ->
                CalculationRequest.builder()
                        .a(new BigDecimal("10"))
                        .b(new BigDecimal("5"))
                        .build()
        );
    }

    @Test
    void testValidateMissingOperandA() {
        assertThrows(IllegalStateException.class, () ->
                CalculationRequest.builder()
                        .operation(OperationType.SUM)
                        .b(new BigDecimal("5"))
                        .build()
        );
    }

    @Test
    void testValidateMissingOperandB() {
        assertThrows(IllegalStateException.class, () ->
                CalculationRequest.builder()
                        .operation(OperationType.SUM)
                        .a(new BigDecimal("10"))
                        .build()
        );
    }

    @Test
    void testToExpression() {
        CalculationRequest request = new CalculationRequest(
                "req-1", OperationType.SUM, new BigDecimal("10"), new BigDecimal("5"));

        assertEquals("10 + 5", request.toExpression());
    }

    @Test
    void testToExpressionWithDifferentOperations() {
        assertEquals("10 - 5", new CalculationRequest(
                null, OperationType.SUBTRACT, new BigDecimal("10"), new BigDecimal("5")).toExpression());
        assertEquals("10 * 5", new CalculationRequest(
                null, OperationType.MULTIPLY, new BigDecimal("10"), new BigDecimal("5")).toExpression());
        assertEquals("10 / 5", new CalculationRequest(
                null, OperationType.DIVIDE, new BigDecimal("10"), new BigDecimal("5")).toExpression());
    }

    @Test
    void testEquals() {
        CalculationRequest request1 = new CalculationRequest(
                "req-1", OperationType.SUM, new BigDecimal("10"), new BigDecimal("5"));
        CalculationRequest request2 = new CalculationRequest(
                "req-1", OperationType.SUM, new BigDecimal("10"), new BigDecimal("5"));

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testNotEquals() {
        CalculationRequest request1 = new CalculationRequest(
                "req-1", OperationType.SUM, new BigDecimal("10"), new BigDecimal("5"));
        CalculationRequest request2 = new CalculationRequest(
                "req-2", OperationType.SUM, new BigDecimal("10"), new BigDecimal("5"));

        assertNotEquals(request1, request2);
    }

    @Test
    void testToString() {
        CalculationRequest request = new CalculationRequest(
                "req-1", OperationType.SUM, new BigDecimal("10"), new BigDecimal("5"));

        String str = request.toString();
        assertTrue(str.contains("req-1"));
        assertTrue(str.contains("SUM"));
        assertTrue(str.contains("10 + 5"));
    }
}

