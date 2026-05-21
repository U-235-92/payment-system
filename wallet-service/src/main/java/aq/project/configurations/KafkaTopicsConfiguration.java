package aq.project.configurations;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfiguration {

    @Value("${service.kafka.topics.wallet_operation_response.name}")
    private String walletOperationResponseTopicName;
    @Value("${service.kafka.topics.wallet_operation_response.replicas}")
    private String walletOperationResponseTopicReplicas;
    @Value("${service.kafka.topics.wallet_operation_response.partitions}")
    private String walletOperationResponseTopicPartitions;
    @Value("${service.kafka.topics.wallet_operation_response.min_insync_replicas}")
    private String walletOperationResponseTopicMinInSyncReplicas;

    @Value("${service.kafka.topics.wallet_operation_request.name}")
    private String walletOperationRequestTopicName;
    @Value("${service.kafka.topics.wallet_operation_request.replicas}")
    private String walletOperationRequestTopicReplicas;
    @Value("${service.kafka.topics.wallet_operation_request.partitions}")
    private String walletOperationRequestTopicPartitions;
    @Value("${service.kafka.topics.wallet_operation_request.min_insync_replicas}")
    private String walletOperationRequestTopicMinInSyncReplicas;

    @Bean
    public NewTopic walletOperationResponseTopic() {
        return TopicBuilder.name(walletOperationResponseTopicName)
                .replicas(Integer.parseInt(walletOperationResponseTopicReplicas))
                .partitions(Integer.parseInt(walletOperationResponseTopicPartitions))
                .config("min.insync.replicas", walletOperationResponseTopicMinInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic walletOperationRequestTopic() {
        return TopicBuilder.name(walletOperationRequestTopicName)
                .replicas(Integer.parseInt(walletOperationRequestTopicReplicas))
                .partitions(Integer.parseInt(walletOperationRequestTopicPartitions))
                .config("min.insync.replicas", walletOperationRequestTopicMinInSyncReplicas)
                .build();
    }
}
