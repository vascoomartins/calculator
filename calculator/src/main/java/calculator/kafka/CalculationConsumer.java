package calculator.kafka;

import calculator.handler.CalculationRequestHandler;
import messaging.dto.CalculationRequest;
import messaging.dto.CalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Kafka consumer for calculation requests.
 * Follows the thin consumer pattern - handles only messaging concerns,
 * delegates business logic to the handler.
 */
@Component
public class CalculationConsumer {

    private static final Logger log = LoggerFactory.getLogger(CalculationConsumer.class);
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    private final CalculationRequestHandler requestHandler;
    private final KafkaReplyService replyService;

    public CalculationConsumer(CalculationRequestHandler requestHandler, KafkaReplyService replyService) {
        this.requestHandler = requestHandler;
        this.replyService = replyService;
    }

    /**
     * Listens for calculation requests and processes them.
     */
    @KafkaListener(
            topics = "${kafka.topic.requests}",
            groupId = "calculator-group",
            containerFactory = "calculationRequestListenerContainerFactory"
    )
    public void consume(
            @Payload CalculationRequest request,
            @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTopicBytes,
            @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {

        String replyTopic = new String(replyTopicBytes, StandardCharsets.UTF_8);

        setupMdc(request.getRequestId());
        try {
            log.info("Received calculation request: operation={}, a={}, b={}",
                    request.getOperation(), request.getA(), request.getB());

            CalculationResponse response = requestHandler.handle(request);
            replyService.sendReply(replyTopic, correlationId, response);

        } finally {
            clearMdc();
        }
    }

    private void setupMdc(String requestId) {
        if (requestId != null) {
            MDC.put(MDC_REQUEST_ID_KEY, requestId);
        }
    }

    private void clearMdc() {
        MDC.remove(MDC_REQUEST_ID_KEY);
    }
}