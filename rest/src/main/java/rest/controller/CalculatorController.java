package rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rest.dto.ApiResponse;
import rest.service.CalculatorService;

import java.math.BigDecimal;

/**
 * REST controller exposing calculator operations.
 * Follows the thin controller pattern - delegates all business logic to the service layer.
 */
@RestController
public class CalculatorController {

    private static final Logger log = LoggerFactory.getLogger(CalculatorController.class);

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /**
     * Adds two numbers.
     * Example: GET /sum?a=10&b=5 returns {"result": 15}
     */
    @GetMapping("/sum")
    public ResponseEntity<ApiResponse> sum(
            @RequestParam("a") BigDecimal a,
            @RequestParam("b") BigDecimal b) {
        log.debug("REST request: sum({}, {})", a, b);
        BigDecimal result = calculatorService.sum(a, b);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Subtracts two numbers.
     * Example: GET /subtract?a=20&b=8 returns {"result": 12}
     */
    @GetMapping("/subtract")
    public ResponseEntity<ApiResponse> subtract(
            @RequestParam("a") BigDecimal a,
            @RequestParam("b") BigDecimal b) {
        log.debug("REST request: subtract({}, {})", a, b);
        BigDecimal result = calculatorService.subtract(a, b);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Multiplies two numbers.
     * Example: GET /multiply?a=6&b=7 returns {"result": 42}
     */
    @GetMapping("/multiply")
    public ResponseEntity<ApiResponse> multiply(
            @RequestParam("a") BigDecimal a,
            @RequestParam("b") BigDecimal b) {
        log.debug("REST request: multiply({}, {})", a, b);
        BigDecimal result = calculatorService.multiply(a, b);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Divides two numbers.
     * Example: GET /divide?a=100&b=4 returns {"result": 25}
     */
    @GetMapping("/divide")
    public ResponseEntity<ApiResponse> divide(
            @RequestParam("a") BigDecimal a,
            @RequestParam("b") BigDecimal b) {
        log.debug("REST request: divide({}, {})", a, b);
        BigDecimal result = calculatorService.divide(a, b);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}