package messaging.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Response DTO for calculation operations.
 * Contains either a successful result or an error message.
 */
public class CalculationResponse {

    private String requestId;
    private BigDecimal result;
    private String error;

    /**
     * Default constructor for JSON deserialization.
     */
    public CalculationResponse() {}

    /**
     * Full constructor.
     */
    public CalculationResponse(String requestId, BigDecimal result, String error) {
        this.requestId = requestId;
        this.result = result;
        this.error = error;
    }

    /**
     * Factory method for a successful calculation response.
     *
     * @param requestId the original request ID
     * @param result the calculation result
     * @return a successful response
     */
    public static CalculationResponse success(String requestId, BigDecimal result) {
        return new CalculationResponse(requestId, result, null);
    }

    /**
     * Factory method for an error calculation response.
     *
     * @param requestId the original request ID
     * @param errorMessage the error message
     * @return an error response
     */
    public static CalculationResponse error(String requestId, String errorMessage) {
        return new CalculationResponse(requestId, null, errorMessage);
    }

    /**
     * Checks if this response represents a successful calculation.
     *
     * @return true if successful, false if error
     */
    public boolean isSuccess() {
        return error == null && result != null;
    }

    /**
     * Checks if this response represents an error.
     *
     * @return true if error, false if successful
     */
    public boolean isError() {
        return error != null;
    }

    public String getRequestId() {
        return requestId;
    }

    public BigDecimal getResult() {
        return result;
    }

    public String getError() {
        return error;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setResult(BigDecimal result) {
        this.result = result;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculationResponse that = (CalculationResponse) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(result, that.result) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, result, error);
    }

    @Override
    public String toString() {
        if (isSuccess()) {
            return String.format("CalculationResponse{requestId='%s', result=%s}", requestId, result);
        } else {
            return String.format("CalculationResponse{requestId='%s', error='%s'}", requestId, error);
        }
    }
}
