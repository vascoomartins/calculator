package calculator.kafka;

import calculator.handler.CalculationRequestHandler;
import messaging.dto.CalculationRequest;
import messaging.dto.CalculationResponse;
import messaging.enums.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculationConsumerTest {

    @Mock
    private CalculationRequestHandler requestHandler;

    @Mock
    private KafkaReplyService replyService;

    private CalculationConsumer consumer;

    private static final byte[] REPLY_TOPIC = "calculation-results".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CORRELATION_ID = "test-correlation".getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        consumer = new CalculationConsumer(requestHandler, replyService);
    }

    @Test
    void testConsumeSuccess() {
        CalculationRequest request = new CalculationRequest("test-id", OperationType.SUM,
                new BigDecimal("10"), new BigDecimal("5"));
        CalculationResponse expectedResponse = CalculationResponse.success("test-id", new BigDecimal("15"));

        when(requestHandler.handle(any())).thenReturn(expectedResponse);

        consumer.consume(request, REPLY_TOPIC, CORRELATION_ID);

        verify(requestHandler).handle(request);
        verify(replyService).sendReply("calculation-results", CORRELATION_ID, expectedResponse);
    }

    @Test
    void testConsumeError() {
        CalculationRequest request = new CalculationRequest("test-id", OperationType.DIVIDE,
                new BigDecimal("10"), new BigDecimal("0"));
        CalculationResponse expectedResponse = CalculationResponse.error("test-id", "Division by zero");

        when(requestHandler.handle(any())).thenReturn(expectedResponse);

        consumer.consume(request, REPLY_TOPIC, CORRELATION_ID);

        verify(requestHandler).handle(request);
        verify(replyService).sendReply("calculation-results", CORRELATION_ID, expectedResponse);
    }

    @Test
    void testConsumeDelegatesAllOperations() {
        for (OperationType operation : OperationType.values()) {
            CalculationRequest request = new CalculationRequest("test-id", operation,
                    new BigDecimal("10"), new BigDecimal("5"));
            CalculationResponse response = CalculationResponse.success("test-id", new BigDecimal("15"));

            when(requestHandler.handle(any())).thenReturn(response);

            consumer.consume(request, REPLY_TOPIC, CORRELATION_ID);

            verify(requestHandler, atLeastOnce()).handle(request);
        }
    }
}
