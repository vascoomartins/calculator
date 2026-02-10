package messaging.enums;

/**
 * Enumeration of supported calculator operations.
 */
public enum OperationType {
    SUM("+", "Addition"),
    SUBTRACT("-", "Subtraction"),
    MULTIPLY("*", "Multiplication"),
    DIVIDE("/", "Division");

    private final String symbol;
    private final String description;

    OperationType(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    /**
     * Returns the mathematical symbol for this operation.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns a human-readable description of this operation.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a formatted string representation of the operation.
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", description, symbol);
    }
}
