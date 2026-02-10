package rest.constants;

/**
 * Application constants for the REST module.
 * Centralizes magic strings and configuration values.
 */
public final class AppConstants {

    private AppConstants() {
    }

    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    public static final String MDC_REQUEST_ID_KEY = "requestId";

    public static final String ERROR_MISSING_PARAM = "Missing required parameter: %s";
    public static final String ERROR_INVALID_PARAM = "Invalid value for parameter '%s': must be a valid number";
    public static final String ERROR_UNEXPECTED = "An unexpected error occurred";

    public static final String API_VERSION = "1.0.0";
}

