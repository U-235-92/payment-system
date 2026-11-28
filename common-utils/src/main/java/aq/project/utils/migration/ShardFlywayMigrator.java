package aq.project.utils.migration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ShardFlywayMigrator {

    public static final String FLYWAY_DB_URL = "flyway.url";

    public static final String FLYWAY_DB_USER = "flyway.user";

    public static final String FLYWAY_DB_PASSWORD = "flyway.password";

    public static final String FLYWAY_DB_DRIVER_CLASS_NAME = "flyway.driver";

    public static final String FLYWAY_LOCATIONS = "flyway.locations";

    public static final String FLYWAY_CREATE_SCHEMAS = "flyway.createSchemas";

    public static final String FLYWAY_DEFAULT_SCHEMA = "flyway.defaultSchema";

    public static final String FLYWAY_CLEAN_DISABLED = "flyway.cleanDisabled";

    public static final String FLYWAY_BASELINE_VERSION = "flyway.baselineVersion";

    public static final String FLYWAY_BASE_LINE_ON_MIGRATE = "flyway.baselineOnMigrate";

    private final Map<String, String> flywayMigrationProperties;

    private final List<String[]> shardDataSources;

    @PostConstruct
    public void migrate() {
        final List<String[]> completedMigrationList = new ArrayList<>();
        final List<String> shardList = shardDataSources.stream().map(arr -> arr[0]).toList();

        log.info("Starting flyway migration for shards: {}", shardList);

        for(String[] metadata : shardDataSources) {
            try {
                getFlyway(metadata[0], metadata[1], metadata[2]).migrate();

                completedMigrationList.add(metadata);

                log.info("Successfully finished flyway migration for shard: [{}]",
                        metadata[0]);

            } catch (Exception migrationException) {
                log.error("Error occurred during flyway migration for shard: [{}]",
                        metadata[0], migrationException);

                log.info("Beginning to roll back successfully migrated shards");

                int rollBackCount = 0;

                for(String[] migration : completedMigrationList) {
                    try {
                        getFlyway(migration[0], migration[1], migration[2]).clean();

                        log.info("Successfully rolled back flyway migration for shard: [{}]",
                                migration[0]);

                        rollBackCount++;

                    } catch (Exception rollbackException) {
                        log.error("Error occurred during rollback of flyway migration for shard: [{}]",
                                migration[0], migrationException);
                    }
                }

                if(rollBackCount == completedMigrationList.size()) {
                    log.info("Rolling back of migration successfully completed");
                } else {
                    log.warn("Rolling back of migration completed with errors");
                }

                System.exit(-1);
            }
        }
        log.info("Flyway migration completed successfully for shards: {}", shardList);
    }

    private Flyway getFlyway(String url, String user, String password) {
        flywayMigrationProperties.put(FLYWAY_DB_URL, url);
        flywayMigrationProperties.put(FLYWAY_DB_USER, user);
        flywayMigrationProperties.put(FLYWAY_DB_PASSWORD, password);

        return Flyway.configure()
                .configuration(flywayMigrationProperties)
                .load();
    }
}
