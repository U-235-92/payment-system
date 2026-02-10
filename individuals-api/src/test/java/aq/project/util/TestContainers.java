package aq.project.util;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public class TestContainers {

    public static class Keycloak {

        private static final String DOCKER_IMAGE_NAME = "quay.io/keycloak/keycloak:latest";

        public static final KeycloakContainer CONTAINER = new KeycloakContainer(DOCKER_IMAGE_NAME)
                .withRealmImportFile("realm-config.json")
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/").forPort(8080).withStartupTimeout(Duration.ofSeconds(120)));


    }
}
