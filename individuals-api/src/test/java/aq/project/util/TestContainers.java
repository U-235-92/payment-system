package aq.project.util;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public abstract class TestContainers {

    private static final Network TEST_NETWORK = Network.newNetwork();

    public abstract static class Keycloak {

        private static final String KEYCLOAK_IMAGE_NAME = "quay.io/keycloak/keycloak:latest";

        public static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer(KEYCLOAK_IMAGE_NAME)
                .withRealmImportFile("realm-config.json")
                .withExposedPorts(8080)
                .withNetwork(TEST_NETWORK)
                .withNetworkAliases("keycloak")
                .waitingFor(Wait.forHttp("/").forPort(8080).withStartupTimeout(Duration.ofSeconds(120)));
    }

    public abstract static class PersonService {

        private static final String PERSON_SERVICE_IMAGE_NAME = "payment-system/test-person-service:1.0.0";

        public static final GenericContainer<?> PERSON_SERVICE_CONTAINER = new GenericContainer<>(DockerImageName.parse(PERSON_SERVICE_IMAGE_NAME))
                .withImagePullPolicy(PullPolicy.defaultPolicy())
                .withExposedPorts(8082)
                .withNetwork(TEST_NETWORK)
                .withNetworkAliases("person-service")
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("PERSON-SERVICE")))
                .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI", "http://keycloak:8080/realms/payment-system")
                .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI", "http://keycloak:8080/realms/payment-system/protocol/openid-connect/certs")
                .waitingFor(Wait.forHttp("/actuator/health").forPort(8082).withStartupTimeout(Duration.ofSeconds(120)));
    }
}
