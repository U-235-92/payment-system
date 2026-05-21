package aq.project.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlywayMigration implements ApplicationRunner {

    private final Flyway flyway;
    private final ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting flyway migration");
        try {
            flyway.migrate();
            log.info("Successfully finished flyway migration");
            System.exit(SpringApplication.exit(applicationContext, () -> 0));
        } catch (Exception e) {
            log.error("Error occurred during flyway migration", e);
            System.exit(SpringApplication.exit(applicationContext, () -> -1));
        }
    }
}
