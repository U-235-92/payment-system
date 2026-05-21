package aq.project.utils;

import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class Containers {

    public static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("apache/kafka:4.0.2"));
}
