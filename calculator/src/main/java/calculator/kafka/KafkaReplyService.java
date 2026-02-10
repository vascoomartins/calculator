package calculator.kafka;

import messaging.dto.CalculationResponse;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

/**
 * Service responsible for sending calculation responses back via Kafka.
 * Encapsulates the Kafka producer logic for reply messages.
 */
@Component
public class KafkaReplyService {

    private static final Logger log = LoggerFactory.getLogger(KafkaReplyService.class);

    private final KafkaTemplate<String, CalculationResponse> kafkaTemplate;

    public KafkaReplyService(KafkaTemplate<String, CalculationResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a calculation response to the specified reply topic.
     *
     * @param replyTopic the topic to send the reply to
     * @param correlationId the correlation ID for request-reply matching
     * @param response the calculation response
     */
    public void sendReply(String replyTopic, byte[] correlationId, CalculationResponse response) {
        log.debug("Sending reply to topic: {}, requestId: {}", replyTopic, response.getRequestId());

        ProducerRecord<String, CalculationResponse> record =
                new ProducerRecord<>(replyTopic, null, null, response);
        record.headers().add(KafkaHeaders.CORRELATION_ID, correlationId);

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send reply to {}: {}", replyTopic, ex.getMessage());
                    } else {
                        log.debug("Reply sent successfully to {}", replyTopic);
                    }
                });
    }
}

