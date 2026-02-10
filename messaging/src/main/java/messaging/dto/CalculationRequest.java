package messaging.dto;

import messaging.enums.OperationType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Request DTO for calculation operations.
 * Represents a calculation request with two operands and an operation type.
 */
public class CalculationRequest {

    private String requestId;
    private OperationType operation;
    private BigDecimal a;
    private BigDecimal b;

    /**
     * Default constructor for JSON deserialization.
     */
    public CalculationRequest() {}

    /**
     * Full constructor.
     */
    public CalculationRequest(String requestId, OperationType operation, BigDecimal a, BigDecimal b) {
        this.requestId = requestId;
        this.operation = operation;
        this.a = a;
        this.b = b;
    }

    /**
     * Creates a new builder for CalculationRequest.
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getRequestId() {
        return requestId;
    }

    public OperationType getOperation() {
        return operation;
    }

    public BigDecimal getA() {
        return a;
    }

    public BigDecimal getB() {
        return b;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public void setA(BigDecimal a) {
        this.a = a;
    }

    public void setB(BigDecimal b) {
        this.b = b;
    }

    /**
     * Validates that all required fields are present.
     *
     * @throws IllegalStateException if any required field is missing
     */
    public void validate() {
        if (operation == null) {
            throw new IllegalStateException("Operation is required");
        }
        if (a == null) {
            throw new IllegalStateException("Operand 'a' is required");
        }
        if (b == null) {
            throw new IllegalStateException("Operand 'b' is required");
        }
    }

    /**
     * Returns a formatted string representation of the calculation.
     * Example: "10 + 5"
     */
    public String toExpression() {
        return String.format("%s %s %s", a, operation.getSymbol(), b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculationRequest that = (CalculationRequest) o;
        return Objects.equals(requestId, that.requestId) &&
                operation == that.operation &&
                Objects.equals(a, that.a) &&
                Objects.equals(b, that.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, operation, a, b);
    }

    @Override
    public String toString() {
        return String.format("CalculationRequest{requestId='%s', operation=%s, expression='%s'}",
                requestId, operation, toExpression());
    }

    /**
     * Builder class for CalculationRequest.
     */
    public static class Builder {
        private String requestId;
        private OperationType operation;
        private BigDecimal a;
        private BigDecimal b;

        private Builder() {}

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder operation(OperationType operation) {
            this.operation = operation;
            return this;
        }

        public Builder a(BigDecimal a) {
            this.a = a;
            return this;
        }

        public Builder b(BigDecimal b) {
            this.b = b;
            return this;
        }

        public Builder a(String a) {
            this.a = new BigDecimal(a);
            return this;
        }

        public Builder b(String b) {
            this.b = new BigDecimal(b);
            return this;
        }

        public Builder a(long a) {
            this.a = BigDecimal.valueOf(a);
            return this;
        }

        public Builder b(long b) {
            this.b = BigDecimal.valueOf(b);
            return this;
        }

        public CalculationRequest build() {
            CalculationRequest request = new CalculationRequest(requestId, operation, a, b);
            request.validate();
            return request;
        }
    }
}
