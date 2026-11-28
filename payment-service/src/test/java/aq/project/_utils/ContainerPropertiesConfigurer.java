package aq.project._utils;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

public class ContainerPropertiesConfigurer {

    public static void registerApplicationContextKafkaContainerProperties(
            DynamicPropertyRegistry registry,
            KafkaContainer container
    ) {
        registry.add("spring.kafka.bootstrapServers", container::getBootstrapServers);
    }


    public static void registerApplicationContextPostgresqlContainerProperties(
            DynamicPropertyRegistry registry,
            PostgreSQLContainer container
    ) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}