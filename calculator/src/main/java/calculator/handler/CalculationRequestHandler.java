package calculator.handler;

import calculator.service.CalculatorService;
import messaging.dto.CalculationRequest;
import messaging.dto.CalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handler responsible for processing calculation requests.
 * Acts as a facade between the messaging layer and the calculator service.
 */
@Component
public class CalculationRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(CalculationRequestHandler.class);

    private final CalculatorService calculatorService;

    public CalculationRequestHandler(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /**
     * Processes a calculation request and returns a response.
     * Handles all exceptions and converts them to error responses.
     *
     * @param request the calculation request
     * @return the calculation response (either success or error)
     */
    public CalculationResponse handle(CalculationRequest request) {
        log.info("Processing calculation: operation={}, a={}, b={}",
                request.getOperation(), request.getA(), request.getB());

        try {
            BigDecimal result = executeOperation(request);
            log.info("Calculation successful: result={}", result);
            return CalculationResponse.success(request.getRequestId(), result);
        } catch (Exception e) {
            log.warn("Calculation failed: error={}", e.getMessage());
            return CalculationResponse.error(request.getRequestId(), e.getMessage());
        }
    }

    /**
     * Executes the calculation operation based on the request type.
     */
    private BigDecimal executeOperation(CalculationRequest request) {
        return switch (request.getOperation()) {
            case SUM -> calculatorService.sum(request.getA(), request.getB());
            case SUBTRACT -> calculatorService.subtract(request.getA(), request.getB());
            case MULTIPLY -> calculatorService.multiply(request.getA(), request.getB());
            case DIVIDE -> calculatorService.divide(request.getA(), request.getB());
        };
    }
}

