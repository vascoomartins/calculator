package rest.service;

import messaging.dto.CalculationRequest;
import messaging.dto.CalculationResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import rest.exception.CalculationException;
import rest.exception.CalculatorTimeoutException;

import java.math.BigDecimal;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceTest {

    @Mock
    private ReplyingKafkaTemplate<String, CalculationRequest, CalculationResponse> replyingKafkaTemplate;

    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorService(replyingKafkaTemplate, "calculation-requests");
    }

    @Test
    void testSum() throws Exception {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("5");
        mockSuccessfulResponse(new BigDecimal("15"));

        BigDecimal result = calculatorService.sum(a, b);

        assertEquals(new BigDecimal("15"), result);
    }

    @Test
    void testSubtract() throws Exception {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("5");
        mockSuccessfulResponse(new BigDecimal("5"));

        BigDecimal result = calculatorService.subtract(a, b);

        assertEquals(new BigDecimal("5"), result);
    }

    @Test
    void testMultiply() throws Exception {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("5");
        mockSuccessfulResponse(new BigDecimal("50"));

        BigDecimal result = calculatorService.multiply(a, b);

        assertEquals(new BigDecimal("50"), result);
    }

    @Test
    void testDivide() throws Exception {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("2");
        mockSuccessfulResponse(new BigDecimal("5"));

        BigDecimal result = calculatorService.divide(a, b);

        assertEquals(new BigDecimal("5"), result);
    }

    @Test
    void testDivideByZero() throws Exception {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = BigDecimal.ZERO;
        mockErrorResponse("Division by zero is not allowed");

        CalculationException exception = assertThrows(
                CalculationException.class,
                () -> calculatorService.divide(a, b)
        );

        assertEquals("Division by zero is not allowed", exception.getMessage());
    }

    @Test
    void testTimeout() throws Exception {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("5");

        RequestReplyFuture<String, CalculationRequest, CalculationResponse> mockFuture = mock(RequestReplyFuture.class);
        when(replyingKafkaTemplate.sendAndReceive(any(ProducerRecord.class))).thenReturn(mockFuture);
        when(mockFuture.get(anyLong(), any())).thenThrow(new TimeoutException("Timeout"));

        assertThrows(CalculatorTimeoutException.class, () -> calculatorService.sum(a, b));
    }

    private void mockSuccessfulResponse(BigDecimal result) throws Exception {
        CalculationResponse response = new CalculationResponse("test-id", result, null);
        mockResponse(response);
    }

    private void mockErrorResponse(String error) throws Exception {
        CalculationResponse response = new CalculationResponse("test-id", null, error);
        mockResponse(response);
    }

    private void mockResponse(CalculationResponse response) throws Exception {
        RequestReplyFuture<String, CalculationRequest, CalculationResponse> mockFuture = mock(RequestReplyFuture.class);
        ConsumerRecord<String, CalculationResponse> consumerRecord =
                new ConsumerRecord<>("topic", 0, 0L, "key", response);

        when(replyingKafkaTemplate.sendAndReceive(any(ProducerRecord.class))).thenReturn(mockFuture);
        when(mockFuture.get(anyLong(), any())).thenReturn(consumerRecord);
    }
}

