package rest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check controller for monitoring and container orchestration.
 */
@RestController
public class HealthController {

    @Value("${spring.application.name:rest}")
    private String applicationName;

    /**
     * Basic health check endpoint.
     * Returns UP if the service is running. Used by Docker health checks.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    /**
     * Detailed health info endpoint.
     * Returns detailed information about the service including version and uptime.
     */
    @GetMapping("/health/info")
    public ResponseEntity<Map<String, Object>> healthInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("service", applicationName);
        info.put("version", "1.0.0");
        info.put("timestamp", Instant.now().toString());
        info.put("java", System.getProperty("java.version"));
        return ResponseEntity.ok(info);
    }
}

