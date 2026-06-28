package aq.project.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = "aq.project.client")
@EnableSchedulerLock(defaultLockAtLeastFor = "30s", defaultLockAtMostFor = "60s")
public class ApplicationConfiguration {

    @Bean
    public LockProvider lockProvider(JdbcTemplate jdbcTemplate) {
        JdbcTemplateLockProvider.Configuration configuration = JdbcTemplateLockProvider.Configuration
                .builder()
                .withJdbcTemplate(jdbcTemplate)
                .withTableName("shedlock")
                .usingDbTime()
                .build();
        return new JdbcTemplateLockProvider(configuration);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
