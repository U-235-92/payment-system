package aq.project.configurations.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
@ConfigurationProperties(prefix = "service.kafka.backoff")
public class KafkaBackoffProperties {

    private int maxAttempts;
    private long waitDuration;
    private double multiplier;
}
