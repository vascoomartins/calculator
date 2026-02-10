package rest.service;

import messaging.dto.CalculationRequest;
import messaging.dto.CalculationResponse;
import messaging.enums.OperationType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;
import rest.constants.AppConstants;
import rest.exception.CalculationException;
import rest.exception.CalculatorTimeoutException;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service layer responsible for handling calculator operations.
 * Communicates with the calculator module via Kafka.
 */
@Service
public class CalculatorService {

    private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);

    @Value("${kafka.reply.timeout-seconds}")
    private int TIMEOUT_SECONDS;

    private final ReplyingKafkaTemplate<String, CalculationRequest, CalculationResponse> replyingKafkaTemplate;
    private final String requestTopic;

    public CalculatorService(
            ReplyingKafkaTemplate<String, CalculationRequest, CalculationResponse> replyingKafkaTemplate,
            @Value("${kafka.topic.requests}") String requestTopic) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.requestTopic = requestTopic;
    }

    /**
     * Performs a sum operation.
     */
    public BigDecimal sum(BigDecimal a, BigDecimal b) {
        return calculate(OperationType.SUM, a, b);
    }

    /**
     * Performs a subtraction operation.
     */
    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return calculate(OperationType.SUBTRACT, a, b);
    }

    /**
     * Performs a multiplication operation.
     */
    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return calculate(OperationType.MULTIPLY, a, b);
    }

    /**
     * Performs a division operation.
     */
    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        return calculate(OperationType.DIVIDE, a, b);
    }

    /**
     * Sends a calculation request to the calculator module via Kafka and waits for the response.
     *
     * @param operation the operation type
     * @param a first operand
     * @param b second operand
     * @return the calculation result
     * @throws CalculatorTimeoutException if the calculator doesn't respond in time
     * @throws CalculationException if the calculation fails
     */
    private BigDecimal calculate(OperationType operation, BigDecimal a, BigDecimal b) {
        String requestId = MDC.get(AppConstants.MDC_REQUEST_ID_KEY);

        log.info("Processing calculation request: operation={}, a={}, b={}", operation, a, b);

        CalculationRequest request = new CalculationRequest(requestId, operation, a, b);
        ProducerRecord<String, CalculationRequest> record = new ProducerRecord<>(requestTopic, requestId, request);

        log.debug("Sending calculation request to Kafka: topic={}, requestId={}", requestTopic, requestId);

        try {
            RequestReplyFuture<String, CalculationRequest, CalculationResponse> future =
                    replyingKafkaTemplate.sendAndReceive(record);

            ConsumerRecord<String, CalculationResponse> responseRecord = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            CalculationResponse response = responseRecord.value();

            log.debug("Received calculation response: result={}, error={}", response.getResult(), response.getError());

            if (response.getError() != null) {
                log.warn("Calculation failed: operation={}, error={}", operation, response.getError());
                throw new CalculationException(response.getError());
            }

            log.info("Calculation successful: operation={}, result={}", operation, response.getResult());
            return response.getResult();

        } catch (TimeoutException e) {
            log.error("Timeout waiting for calculator response: operation={}, requestId={}", operation, requestId);
            throw new CalculatorTimeoutException("Calculator service did not respond in time");
        } catch (CalculationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during calculation: operation={}", operation, e);
            throw new CalculationException("Failed to process calculation: " + e.getMessage());
        }
    }
}

