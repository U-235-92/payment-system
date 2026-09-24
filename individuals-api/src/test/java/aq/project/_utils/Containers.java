package aq.project._utils;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public abstract class Containers {

    private static final Network TEST_NETWORK = Network.newNetwork();

    private static final String KEYCLOAK_IMAGE_NAME = "quay.io/keycloak/keycloak:26.6.0";

    public static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer(KEYCLOAK_IMAGE_NAME)
            .withRealmImportFile("realm-config.json")
            .withExposedPorts(8080)
            .withNetwork(TEST_NETWORK)
            .withNetworkAliases("keycloak")
            .waitingFor(Wait.forHttp("/").forPort(8080).withStartupTimeout(Duration.ofSeconds(120)));
}
