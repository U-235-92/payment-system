package aq.project.configs;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.kafka.KafkaContainer;

public abstract class ContainersConfigurer {

    public static void configureKafkaProperties(DynamicPropertyRegistry registry, KafkaContainer container) {
        registry.add("spring.kafka.bootstrapServers", container::getBootstrapServers);
    }
}
