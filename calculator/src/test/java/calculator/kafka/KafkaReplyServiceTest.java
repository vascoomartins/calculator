package calculator.kafka;

import messaging.dto.CalculationResponse;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaReplyServiceTest {

    @Mock
    private KafkaTemplate<String, CalculationResponse> kafkaTemplate;

    @Captor
    private ArgumentCaptor<ProducerRecord<String, CalculationResponse>> recordCaptor;

    private KafkaReplyService replyService;

    @BeforeEach
    void setUp() {
        replyService = new KafkaReplyService(kafkaTemplate);
    }

    @Test
    void testSendReply() {
        String replyTopic = "calculation-results";
        byte[] correlationId = "test-correlation".getBytes(StandardCharsets.UTF_8);
        CalculationResponse response = CalculationResponse.success("req-1", new BigDecimal("42"));

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        replyService.sendReply(replyTopic, correlationId, response);

        verify(kafkaTemplate).send(recordCaptor.capture());
        ProducerRecord<String, CalculationResponse> record = recordCaptor.getValue();

        assertEquals(replyTopic, record.topic());
        assertEquals(response, record.value());

        byte[] headerValue = record.headers().lastHeader(KafkaHeaders.CORRELATION_ID).value();
        assertArrayEquals(correlationId, headerValue);
    }

    @Test
    void testSendReplyWithError() {
        String replyTopic = "calculation-results";
        byte[] correlationId = "test-correlation".getBytes(StandardCharsets.UTF_8);
        CalculationResponse response = CalculationResponse.error("req-2", "Division by zero");

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        replyService.sendReply(replyTopic, correlationId, response);

        verify(kafkaTemplate).send(recordCaptor.capture());
        ProducerRecord<String, CalculationResponse> record = recordCaptor.getValue();

        assertNull(record.value().getResult());
        assertEquals("Division by zero", record.value().getError());
    }
}
