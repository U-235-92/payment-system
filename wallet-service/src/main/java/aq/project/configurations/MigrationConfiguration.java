package aq.project.configurations;

import aq.project.properties.DatasourceProperties;
import aq.project.utils.migration.ShardFlywayMigrator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aq.project.utils.migration.ShardFlywayMigrator.*;

@Configuration
@RequiredArgsConstructor
@Profile({ "dev", "prod" })
class MigrationConfiguration {

    @Value("${spring.flyway.create-schemas}")
    private String flywayCreateSchema;
    @Value("${spring.flyway.default-schema}")
    private String flywayDefaultSchema;
    @Value("${spring.flyway.locations}")
    private String flywayLocation;
    @Value("${spring.flyway.baseline-version}")
    private String flywayBaselineVersion;
    @Value("${spring.flyway.baseline-on-migrate}")
    private String flywayBaselineOnMigrate;
    @Value("${spring.flyway.driver-class-name}")
    private String flywayDriverClassName;
    @Value("${spring.flyway.clean-disabled}")
    private String flywayCleanDisabled;

    private final DatasourceProperties datasourceProperties;

    @Bean("migrator")
    public ShardFlywayMigrator shardFlywayMigrator() {
        List<String[]> dataSources = datasourceProperties.getDatasource()
                .stream()
                .map(ds -> ds.split(","))
                .toList();

        Map<String, String> flywayMigrationProperties = new HashMap<>();

        flywayMigrationProperties.put(FLYWAY_DB_DRIVER_CLASS_NAME, flywayDriverClassName);
        flywayMigrationProperties.put(FLYWAY_LOCATIONS, flywayLocation);
        flywayMigrationProperties.put(FLYWAY_CREATE_SCHEMAS, flywayCreateSchema);
        flywayMigrationProperties.put(FLYWAY_DEFAULT_SCHEMA, flywayDefaultSchema);
        flywayMigrationProperties.put(FLYWAY_BASELINE_VERSION, flywayBaselineVersion);
        flywayMigrationProperties.put(FLYWAY_BASE_LINE_ON_MIGRATE, flywayBaselineOnMigrate);
        flywayMigrationProperties.put(FLYWAY_CLEAN_DISABLED, flywayCleanDisabled);

        return new ShardFlywayMigrator(flywayMigrationProperties, dataSources);
    }
}
