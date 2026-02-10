package calculator.exception;

/**
 * Exception thrown when attempting to divide by zero.
 */
public class DivisionByZeroException extends CalculationException {

    public DivisionByZeroException() {
        super("Division by zero is not allowed");
    }

    public DivisionByZeroException(String message) {
        super(message);
    }
}

