package aq.project.utils.migration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("migrator")
@RequiredArgsConstructor
@Profile({ "dev-shard", "prod" })
class Migrator {

    private static final String URL = "flyway.url";
    private static final String USER = "flyway.user";
    private static final String PASSWORD = "flyway.password";
    private static final String LOCATIONS = "flyway.locations";
    private static final String DRIVER_CLASS_NAME = "flyway.driver";
    private static final String CREATE_SCHEMAS = "flyway.createSchemas";
    private static final String DEFAULT_SCHEMA = "flyway.defaultSchema";
    private static final String CLEAN_DISABLED = "flyway.cleanDisabled";
    private static final String BASELINE_VERSION = "flyway.baselineVersion";
    private static final String BASE_LINE_ON_MIGRATE = "flyway.baselineOnMigrate";

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
    @Value("${db.driver.class.name}")
    private String flywayDriverClassName;
    @Value("${spring.flyway.clean-disabled}")
    private String flywayCleanDisabled;

    private final Datasource datasource;

    private final ApplicationContext applicationContext;

    @PostConstruct
    public void migrate() {
        final List<String[]> datasourceMetaDataList = getDatasourceMetaData();
        final List<String[]> completedMigrationList = new ArrayList<>();
        final List<String> shardList = datasourceMetaDataList.stream().map(arr -> arr[0]).toList();
        log.info("Starting flyway migration for shards: {}", shardList);
        for(String[] metadata : datasourceMetaDataList) {
            try {
                getFlyway(metadata[0], metadata[1], metadata[2]).migrate();
                completedMigrationList.add(metadata);
                log.info("Successfully finished flyway migration for shard: [{}]", metadata[0]);
            } catch (Exception migrationException) {
                log.error("Error occurred during flyway migration for shard: [{}]", metadata[0], migrationException);
                log.info("Beginning to roll back successfully migrated shards");
                int rollBackCount = 0;
                for(String[] migration : completedMigrationList) {
                    try {
                        getFlyway(migration[0], migration[1], migration[2]).clean();
                        log.info("Successfully rolled back flyway migration for shard: [{}]", migration[0]);
                        rollBackCount++;
                    } catch (Exception rollbackException) {
                        log.error("Error occurred during rollback of flyway migration for shard: [{}]", migration[0], migrationException);
                    }
                }
                if(rollBackCount == completedMigrationList.size()) {
                    log.info("Rolling back of migration successfully completed");
                } else {
                    log.warn("Rolling back of migration completed with errors");
                }
                System.exit(SpringApplication.exit(applicationContext, () -> -1));
            }
        }
        log.info("Flyway migration completed successfully for shards: {}", shardList);
    }

    private List<String[]> getDatasourceMetaData() {
        return datasource.getDatasource()
                .stream()
                .map(ds -> ds.split(","))
                .toList();
    }

    private Flyway getFlyway(String url, String user, String password) {
        Map<String, String> properties = new HashMap<>();
        properties.put(URL, url);
        properties.put(USER, user);
        properties.put(PASSWORD, password);
        properties.put(LOCATIONS, flywayLocation);
        properties.put(CREATE_SCHEMAS, flywayCreateSchema);
        properties.put(DEFAULT_SCHEMA, flywayDefaultSchema);
        properties.put(BASELINE_VERSION, flywayBaselineVersion);
        properties.put(DRIVER_CLASS_NAME, flywayDriverClassName);
        properties.put(BASE_LINE_ON_MIGRATE, flywayBaselineOnMigrate);
        properties.put(CLEAN_DISABLED, flywayCleanDisabled);
        return Flyway.configure()
                .configuration(properties)
                .load();
    }
}
