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

    @Value("${service.kafka.topics.wallet_operation_response_dlt.name}")
    private String walletOperationResponseDltTopicName;
    @Value("${service.kafka.topics.wallet_operation_response_dlt.replicas}")
    private String walletOperationResponseDltTopicReplicas;
    @Value("${service.kafka.topics.wallet_operation_response_dlt.partitions}")
    private String walletOperationResponseDltTopicPartitions;
    @Value("${service.kafka.topics.wallet_operation_response_dlt.min_insync_replicas}")
    private String walletOperationResponseDltTopicMinInSyncReplicas;

    @Value("${service.kafka.topics.wallet_operation_response_exceptions.name}")
    private String walletOperationResponseExceptionsTopicName;
    @Value("${service.kafka.topics.wallet_operation_response_exceptions.replicas}")
    private String walletOperationResponseExceptionsTopicReplicas;
    @Value("${service.kafka.topics.wallet_operation_response_exceptions.partitions}")
    private String walletOperationResponseExceptionsTopicPartitions;
    @Value("${service.kafka.topics.wallet_operation_response_exceptions.min_insync_replicas}")
    private String walletOperationResponseExceptionsTopicMinInSyncReplicas;

    @Value("${service.kafka.topics.wallet_operation_request.name}")
    private String walletOperationRequestTopicName;
    @Value("${service.kafka.topics.wallet_operation_request.replicas}")
    private String walletOperationRequestTopicReplicas;
    @Value("${service.kafka.topics.wallet_operation_request.partitions}")
    private String walletOperationRequestTopicPartitions;
    @Value("${service.kafka.topics.wallet_operation_request.min_insync_replicas}")
    private String walletOperationRequestTopicMinInSyncReplicas;

    @Value("${service.kafka.topics.wallet_operation_request_dlt.name}")
    private String walletOperationRequestDltTopicName;
    @Value("${service.kafka.topics.wallet_operation_request_dlt.replicas}")
    private String walletOperationRequestDltTopicReplicas;
    @Value("${service.kafka.topics.wallet_operation_request_dlt.partitions}")
    private String walletOperationRequestDltTopicPartitions;
    @Value("${service.kafka.topics.wallet_operation_request_dlt.min_insync_replicas}")
    private String walletOperationRequestDltTopicMinInSyncReplicas;

    @Value("${service.kafka.topics.wallet_operation_request_exceptions.name}")
    private String walletOperationRequestExceptionsTopicName;
    @Value("${service.kafka.topics.wallet_operation_request_exceptions.replicas}")
    private String walletOperationRequestExceptionsTopicReplicas;
    @Value("${service.kafka.topics.wallet_operation_request_exceptions.partitions}")
    private String walletOperationRequestExceptionsTopicPartitions;
    @Value("${service.kafka.topics.wallet_operation_request_exceptions.min_insync_replicas}")
    private String walletOperationRequestExceptionsTopicMinInSyncReplicas;

    @Bean
    public NewTopic walletOperationResponseTopic() {
        return TopicBuilder.name(walletOperationResponseTopicName)
                .replicas(Integer.parseInt(walletOperationResponseTopicReplicas))
                .partitions(Integer.parseInt(walletOperationResponseTopicPartitions))
                .config("min.insync.replicas", walletOperationResponseTopicMinInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic walletOperationResponseDltTopic() {
        return TopicBuilder.name(walletOperationResponseDltTopicName)
                .replicas(Integer.parseInt(walletOperationResponseDltTopicReplicas))
                .partitions(Integer.parseInt(walletOperationResponseDltTopicPartitions))
                .config("min.insync.replicas", walletOperationResponseDltTopicMinInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic walletOperationResponseExceptionsTopic() {
        return TopicBuilder.name(walletOperationResponseExceptionsTopicName)
                .replicas(Integer.parseInt(walletOperationResponseExceptionsTopicReplicas))
                .partitions(Integer.parseInt(walletOperationResponseExceptionsTopicPartitions))
                .config("min.insync.replicas", walletOperationResponseExceptionsTopicMinInSyncReplicas)
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

    @Bean
    public NewTopic walletOperationRequestDltTopic() {
        return TopicBuilder.name(walletOperationRequestDltTopicName)
                .replicas(Integer.parseInt(walletOperationRequestDltTopicReplicas))
                .partitions(Integer.parseInt(walletOperationRequestDltTopicPartitions))
                .config("min.insync.replicas", walletOperationRequestDltTopicMinInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic walletOperationRequestExceptionsTopic() {
        return TopicBuilder.name(walletOperationRequestExceptionsTopicName)
                .replicas(Integer.parseInt(walletOperationRequestExceptionsTopicReplicas))
                .partitions(Integer.parseInt(walletOperationRequestExceptionsTopicPartitions))
                .config("min.insync.replicas", walletOperationRequestExceptionsTopicMinInSyncReplicas)
                .build();
    }
}
