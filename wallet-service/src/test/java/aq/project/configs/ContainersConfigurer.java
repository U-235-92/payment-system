package aq.project.configs;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class ContainersConfigurer {

    public static void configurePostgreSqlProperties(DynamicPropertyRegistry registry, PostgreSQLContainer container) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    public static void configureKafkaProperties(DynamicPropertyRegistry registry, KafkaContainer container) {
        registry.add("spring.kafka.bootstrapServers", container::getBootstrapServers);
    }
}
