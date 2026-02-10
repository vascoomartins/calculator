package calculator.service;

import calculator.exception.DivisionByZeroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Service providing core calculator operations.
 * All operations support arbitrary precision using BigDecimal.
 */
@Service
public class CalculatorService {

    private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    /**
     * Adds two numbers.
     */
    public BigDecimal sum(BigDecimal a, BigDecimal b) {
        log.debug("Performing sum: {} + {}", a, b);
        BigDecimal result = a.add(b, MATH_CONTEXT);
        log.debug("Sum result: {}", result);
        return result;
    }

    /**
     * Subtracts second number from first.
     */
    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        log.debug("Performing subtraction: {} - {}", a, b);
        BigDecimal result = a.subtract(b, MATH_CONTEXT);
        log.debug("Subtraction result: {}", result);
        return result;
    }

    /**
     * Multiplies two numbers.
     */
    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        log.debug("Performing multiplication: {} * {}", a, b);
        BigDecimal result = a.multiply(b, MATH_CONTEXT);
        log.debug("Multiplication result: {}", result);
        return result;
    }

    /**
     * Divides first number by second.
     *
     * @throws DivisionByZeroException if b is zero
     */
    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        log.debug("Performing division: {} / {}", a, b);
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Division by zero attempted: a={}", a);
            throw new DivisionByZeroException();
        }
        BigDecimal result = a.divide(b, MATH_CONTEXT);
        log.debug("Division result: {}", result);
        return result;
    }
}
