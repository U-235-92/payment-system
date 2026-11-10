package aq.project.configurations;

import aq.project.properties.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaTopicProperties kafkaTopicProperties;

    @Bean
    public NewTopic depositTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("deposit_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic depositTransactionCompleteResponseTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("deposit_transaction_complete_response");
        return createTopic(config);
    }

    @Bean
    public NewTopic depositTransactionFailResponseTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("deposit_transaction_fail_response");
        return createTopic(config);
    }

    @Bean
    public NewTopic withdrawalTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("withdrawal_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic withdrawalTransactionCompleteResponseTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("withdrawal_transaction_complete_response");
        return createTopic(config);
    }

    @Bean
    public NewTopic withdrawalTransactionFailResponseTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("withdrawal_transaction_fail_response");
        return createTopic(config);
    }

    private NewTopic createTopic(KafkaTopicProperties.KafkaTopicConfiguration config) {
        return TopicBuilder.name(config.getName())
                .partitions(config.getPartitions())
                .replicas(config.getReplicas())
                .config("min.insync.replicas", String.valueOf(config.getMinInsyncReplicas()))
                .build();
    }
}