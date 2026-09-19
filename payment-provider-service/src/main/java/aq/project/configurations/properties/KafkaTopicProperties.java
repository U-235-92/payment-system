package aq.project.configurations.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter @Setter
@ConfigurationProperties(prefix = "service.kafka")
public class KafkaTopicProperties {

    private Map<String, KafkaTopicConfiguration> topics = new HashMap<>();

    @Getter @Setter
    public static class KafkaTopicConfiguration {

        private String name;
        private int partitions;
        private int replicas;
        private int minInsyncReplicas;
    }
}
