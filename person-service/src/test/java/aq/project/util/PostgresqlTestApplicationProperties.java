package aq.project.util;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class PostgresqlTestApplicationProperties {

    public static void configureProperties(DynamicPropertyRegistry registry, PostgreSQLContainer container) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.jpa.generate-ddl", () -> true);
        registry.add("spring.jpa.show-sql", () -> true);
    }
}
