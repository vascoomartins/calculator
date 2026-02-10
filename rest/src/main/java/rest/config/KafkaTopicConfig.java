package rest.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.requests}")
    private String requestsTopic;

    @Value("${kafka.topic.results}")
    private String resultsTopic;

    @Bean
    public NewTopic calculationRequestsTopic() {
        return TopicBuilder.name(requestsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic calculationResultsTopic() {
        return TopicBuilder.name(resultsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
