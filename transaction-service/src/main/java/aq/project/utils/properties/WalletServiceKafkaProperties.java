package aq.project.utils.properties;

import aq.project.exceptions.NotFoundTopicException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Getter @Setter
@ConfigurationProperties(prefix = "service.kafka.services.wallet-service")
public class WalletServiceKafkaProperties {

    private Set<String> topics;

    public String getTopic(String topic) {
        if(topics.contains(topic))
            return topic;
        throw new NotFoundTopicException(topic, String.format("Topic %s not found", topic));
    }
}
