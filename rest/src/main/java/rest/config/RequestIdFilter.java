package rest.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import rest.constants.AppConstants;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that generates and propagates unique request IDs for tracing.
 * - Checks for existing X-Request-ID header (allows external correlation)
 * - Generates UUID if not present
 * - Adds to MDC for logging
 * - Returns in response header for client correlation
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestId = request.getHeader(AppConstants.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(AppConstants.MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(AppConstants.REQUEST_ID_HEADER, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(AppConstants.MDC_REQUEST_ID_KEY);
        }
    }
}
