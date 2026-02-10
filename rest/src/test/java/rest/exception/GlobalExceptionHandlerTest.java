package rest.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import rest.dto.ApiResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleCalculationException() {
        CalculationException ex = new CalculationException("Division by zero");

        ResponseEntity<ApiResponse> response = handler.handleCalculationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Division by zero", response.getBody().getError());
    }

    @Test
    void testHandleTimeoutException() {
        CalculatorTimeoutException ex = new CalculatorTimeoutException("Service timeout");

        ResponseEntity<ApiResponse> response = handler.handleTimeoutException(ex);

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Service timeout", response.getBody().getError());
    }

    @Test
    void testHandleMissingParams() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("a", "BigDecimal");

        ResponseEntity<ApiResponse> response = handler.handleMissingParams(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Missing required parameter: a", response.getBody().getError());
    }

    @Test
    void testHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("a");

        ResponseEntity<ApiResponse> response = handler.handleTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid value for parameter 'a': must be a valid number", response.getBody().getError());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ApiResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getError());
    }
}

