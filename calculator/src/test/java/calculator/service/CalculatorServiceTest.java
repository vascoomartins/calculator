package calculator.service;

import calculator.exception.DivisionByZeroException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {
    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorService();
    }

    @Test
    void testSum() {
        BigDecimal result = calculatorService.sum(new BigDecimal("2.5"), new BigDecimal("3.5"));
        assertEquals(new BigDecimal("6"), result.stripTrailingZeros());
    }

    @Test
    void testSubtract() {
        BigDecimal result = calculatorService.subtract(new BigDecimal("5.5"), new BigDecimal("2.0"));
        assertEquals(new BigDecimal("3.5"), result.stripTrailingZeros());
    }

    @Test
    void testMultiply() {
        BigDecimal result = calculatorService.multiply(new BigDecimal("2"), new BigDecimal("3.5"));
        assertEquals(new BigDecimal("7"), result.stripTrailingZeros());
    }

    @Test
    void testDivide() {
        BigDecimal result = calculatorService.divide(new BigDecimal("7"), new BigDecimal("2"));
        assertEquals(new BigDecimal("3.5"), result.stripTrailingZeros());
    }

    @Test
    void testDivideByZero() {
        DivisionByZeroException exception = assertThrows(DivisionByZeroException.class, () ->
                calculatorService.divide(new BigDecimal("5"), BigDecimal.ZERO)
        );
        assertEquals("Division by zero is not allowed", exception.getMessage());
    }

    @Test
    void testSumWithNegativeNumbers() {
        BigDecimal result = calculatorService.sum(new BigDecimal("-5"), new BigDecimal("3"));
        assertEquals(new BigDecimal("-2"), result.stripTrailingZeros());
    }

    @Test
    void testDivideWithDecimals() {
        BigDecimal result = calculatorService.divide(new BigDecimal("10"), new BigDecimal("3"));
        assertTrue(result.toString().startsWith("3.333333"));
    }
}