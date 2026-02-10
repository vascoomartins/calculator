
package rest.config;

import messaging.dto.CalculationRequest;
import messaging.dto.CalculationResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.results}")
    private String resultsTopic;

    private final String replyGroupId = "rest-reply-group-" + UUID.randomUUID().toString();

    @Bean
    public ProducerFactory<String, CalculationRequest> calculationRequestProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory<String, CalculationResponse> calculationReplyConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, replyGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CalculationResponse.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, CalculationResponse> calculationResultsListenerContainer(
            ConsumerFactory<String, CalculationResponse> consumerFactory) {
        ContainerProperties containerProps = new ContainerProperties(resultsTopic);
        containerProps.setGroupId(replyGroupId);
        containerProps.setMissingTopicsFatal(false);

        ConcurrentMessageListenerContainer<String, CalculationResponse> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);
        container.setAutoStartup(true);
        return container;
    }

    @Bean
    public ReplyingKafkaTemplate<String, CalculationRequest, CalculationResponse> calculationRequestReplyKafkaTemplate(
            ProducerFactory<String, CalculationRequest> producerFactory,
            ConcurrentMessageListenerContainer<String, CalculationResponse> replyContainer) {

        ReplyingKafkaTemplate<String, CalculationRequest, CalculationResponse> template =
                new ReplyingKafkaTemplate<>(producerFactory, replyContainer);
        template.setDefaultReplyTimeout(Duration.ofSeconds(10));
        template.setSharedReplyTopic(true);
        return template;
    }
}