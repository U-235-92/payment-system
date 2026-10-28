package aq.project._utils;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;

public abstract class Containers {

    public static final PostgreSQLContainer POSTGRESQL_CONTAINER = new PostgreSQLContainer("postgres:18-alpine");

    public static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer("quay.io/keycloak/keycloak:26.6.0")
            .withRealmImportFile("realm-config.json")
            .withExposedPorts(8080)
            .withNetworkAliases("keycloak")
            .waitingFor(Wait.forHttp("/").forPort(8080).withStartupTimeout(Duration.ofSeconds(120)));
}
