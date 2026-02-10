package rest.exception;

/**
 * Exception thrown when the calculator service times out.
 */
public class CalculatorTimeoutException extends RuntimeException {

    public CalculatorTimeoutException(String message) {
        super(message);
    }

    public CalculatorTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

