package calculator.exception;

/**
 * Base exception for calculator operations.
 */
public class CalculationException extends RuntimeException {

    public CalculationException(String message) {
        super(message);
    }

    public CalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}

