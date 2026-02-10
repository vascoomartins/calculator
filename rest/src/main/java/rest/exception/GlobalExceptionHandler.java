package rest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import rest.dto.ApiResponse;

import static rest.constants.AppConstants.*;

/**
 * Global exception handler for the REST API.
 * Provides centralized error handling and consistent error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles calculation errors (e.g., division by zero).
     */
    @ExceptionHandler(CalculationException.class)
    public ResponseEntity<ApiResponse> handleCalculationException(CalculationException ex) {
        log.warn("Calculation error: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles timeout errors when the calculator service doesn't respond.
     */
    @ExceptionHandler(CalculatorTimeoutException.class)
    public ResponseEntity<ApiResponse> handleTimeoutException(CalculatorTimeoutException ex) {
        log.error("Calculator timeout: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles missing required request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = String.format(ERROR_MISSING_PARAM, ex.getParameterName());
        log.warn("Bad request: {}", message);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(message));
    }

    /**
     * Handles invalid parameter types (e.g., non-numeric values).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(ERROR_INVALID_PARAM, ex.getName());
        log.warn("Bad request: {}", message);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(message));
    }

    /**
     * Handles all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ERROR_UNEXPECTED));
    }
}

