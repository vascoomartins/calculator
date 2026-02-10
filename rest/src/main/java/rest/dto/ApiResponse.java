package rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Unified API response DTO.
 * Uses JSON inclusion rules to only show non-null fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private BigDecimal result;
    private String error;

    private ApiResponse() {
    }

    /**
     * Creates a successful response with a result.
     */
    public static ApiResponse success(BigDecimal result) {
        ApiResponse response = new ApiResponse();
        response.result = result;
        return response;
    }

    /**
     * Creates an error response with an error message.
     */
    public static ApiResponse error(String errorMessage) {
        ApiResponse response = new ApiResponse();
        response.error = errorMessage;
        return response;
    }

    public BigDecimal getResult() {
        return result;
    }

    public String getError() {
        return error;
    }
}

