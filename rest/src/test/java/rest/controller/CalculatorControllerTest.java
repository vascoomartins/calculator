package rest.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rest.dto.ApiResponse;
import rest.service.CalculatorService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculatorControllerTest {

    @Mock
    private CalculatorService calculatorService;

    @InjectMocks
    private CalculatorController calculatorController;

    @Test
    void testSum() {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("5");
        BigDecimal expectedResult = new BigDecimal("15");

        when(calculatorService.sum(a, b)).thenReturn(expectedResult);

        ResponseEntity<ApiResponse> result = calculatorController.sum(a, b);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(expectedResult, result.getBody().getResult());
        verify(calculatorService).sum(a, b);
    }

    @Test
    void testSubtract() {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("5");
        BigDecimal expectedResult = new BigDecimal("5");

        when(calculatorService.subtract(a, b)).thenReturn(expectedResult);

        ResponseEntity<ApiResponse> result = calculatorController.subtract(a, b);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(expectedResult, result.getBody().getResult());
        verify(calculatorService).subtract(a, b);
    }

    @Test
    void testMultiply() {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("5");
        BigDecimal expectedResult = new BigDecimal("50");

        when(calculatorService.multiply(a, b)).thenReturn(expectedResult);

        ResponseEntity<ApiResponse> result = calculatorController.multiply(a, b);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(expectedResult, result.getBody().getResult());
        verify(calculatorService).multiply(a, b);
    }

    @Test
    void testDivide() {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("2");
        BigDecimal expectedResult = new BigDecimal("5");

        when(calculatorService.divide(a, b)).thenReturn(expectedResult);

        ResponseEntity<ApiResponse> result = calculatorController.divide(a, b);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(expectedResult, result.getBody().getResult());
        verify(calculatorService).divide(a, b);
    }
}