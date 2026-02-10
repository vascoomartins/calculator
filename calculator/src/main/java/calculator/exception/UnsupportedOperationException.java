package calculator.exception;

import messaging.enums.OperationType;

/**
 * Exception thrown when an unsupported operation is requested.
 */
public class UnsupportedOperationException extends CalculationException {

    public UnsupportedOperationException(OperationType operation) {
        super("Unsupported operation: " + operation);
    }

    public UnsupportedOperationException(String message) {
        super(message);
    }
}

