package calculator.handler;

import calculator.exception.DivisionByZeroException;
import calculator.service.CalculatorService;
import messaging.dto.CalculationRequest;
import messaging.dto.CalculationResponse;
import messaging.enums.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculationRequestHandlerTest {

    @Mock
    private CalculatorService calculatorService;

    private CalculationRequestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CalculationRequestHandler(calculatorService);
    }

    @Test
    void testHandleSum() {
        CalculationRequest request = new CalculationRequest("req-1", OperationType.SUM,
                new BigDecimal("10"), new BigDecimal("5"));
        when(calculatorService.sum(new BigDecimal("10"), new BigDecimal("5")))
                .thenReturn(new BigDecimal("15"));

        CalculationResponse response = handler.handle(request);

        assertEquals("req-1", response.getRequestId());
        assertEquals(new BigDecimal("15"), response.getResult());
        assertNull(response.getError());
    }

    @Test
    void testHandleSubtract() {
        CalculationRequest request = new CalculationRequest("req-2", OperationType.SUBTRACT,
                new BigDecimal("10"), new BigDecimal("3"));
        when(calculatorService.subtract(new BigDecimal("10"), new BigDecimal("3")))
                .thenReturn(new BigDecimal("7"));

        CalculationResponse response = handler.handle(request);

        assertEquals("req-2", response.getRequestId());
        assertEquals(new BigDecimal("7"), response.getResult());
        assertNull(response.getError());
    }

    @Test
    void testHandleMultiply() {
        CalculationRequest request = new CalculationRequest("req-3", OperationType.MULTIPLY,
                new BigDecimal("6"), new BigDecimal("7"));
        when(calculatorService.multiply(new BigDecimal("6"), new BigDecimal("7")))
                .thenReturn(new BigDecimal("42"));

        CalculationResponse response = handler.handle(request);

        assertEquals("req-3", response.getRequestId());
        assertEquals(new BigDecimal("42"), response.getResult());
        assertNull(response.getError());
    }

    @Test
    void testHandleDivide() {
        CalculationRequest request = new CalculationRequest("req-4", OperationType.DIVIDE,
                new BigDecimal("20"), new BigDecimal("4"));
        when(calculatorService.divide(new BigDecimal("20"), new BigDecimal("4")))
                .thenReturn(new BigDecimal("5"));

        CalculationResponse response = handler.handle(request);

        assertEquals("req-4", response.getRequestId());
        assertEquals(new BigDecimal("5"), response.getResult());
        assertNull(response.getError());
    }

    @Test
    void testHandleDivisionByZero() {
        CalculationRequest request = new CalculationRequest("req-5", OperationType.DIVIDE,
                new BigDecimal("10"), BigDecimal.ZERO);
        when(calculatorService.divide(new BigDecimal("10"), BigDecimal.ZERO))
                .thenThrow(new DivisionByZeroException());

        CalculationResponse response = handler.handle(request);

        assertEquals("req-5", response.getRequestId());
        assertNull(response.getResult());
        assertEquals("Division by zero is not allowed", response.getError());
    }

    @Test
    void testHandleGenericError() {
        CalculationRequest request = new CalculationRequest("req-6", OperationType.SUM,
                new BigDecimal("10"), new BigDecimal("5"));
        when(calculatorService.sum(new BigDecimal("10"), new BigDecimal("5")))
                .thenThrow(new RuntimeException("Unexpected error"));

        CalculationResponse response = handler.handle(request);

        assertEquals("req-6", response.getRequestId());
        assertNull(response.getResult());
        assertEquals("Unexpected error", response.getError());
    }
}

