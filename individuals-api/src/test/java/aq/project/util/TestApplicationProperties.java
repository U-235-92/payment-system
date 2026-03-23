package aq.project.util;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;

public class TestApplicationProperties {

    public static class KeycloakProperties {

        private static final String ISSUER_URI = "/realms/payment-system";

        private static final String JWK_SET_URI = "/realms/payment-system/protocol/openid-connect/certs";

        private static final String TOKEN_URI = "/realms/payment-system/protocol/openid-connect/token";

        private static final String ADMIN_URI = "/admin/realms/payment-system/users";

        private static final String CLIENT_ID = "individuals-api";

        private static final String ADMIN_CLIENT_ID = "payment-system-admin-cli";

        private static final String CLIENT_SECRET = "TEST-SECRET";

        public static void registerApplicationContextContainerProperties(DynamicPropertyRegistry registry, KeycloakContainer container) {
            String baseUrl = container.getAuthServerUrl();
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> baseUrl + ISSUER_URI);
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> baseUrl + JWK_SET_URI);
            registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri", () -> baseUrl + ISSUER_URI);
            registry.add("spring.security.oauth2.client.provider.keycloak.token-uri", () -> baseUrl + TOKEN_URI);
            registry.add("spring.security.oauth2.client.registration.keycloak.client-id", () -> CLIENT_ID);
            registry.add("spring.security.oauth2.client.registration.keycloak.client-secret", () -> CLIENT_SECRET);
            registry.add("keycloak.admin.client-id", () -> ADMIN_CLIENT_ID);
            registry.add("keycloak.admin.client-secret", () -> CLIENT_SECRET);
            registry.add("keycloak.admin-uri", () -> baseUrl + ADMIN_URI);
        }
    }

    public static class PersonServiceProperties {
        public static void registerApplicationContextContainerProperties(DynamicPropertyRegistry registry, GenericContainer<?> genericContainer) {
//            Set up Individuals-API actual Person-service URI
            String genericContainerHost = genericContainer.getHost();
            int genericContainerPort = genericContainer.getMappedPort(8082);
            String actualPersonServiceUri = String.format("http://%s:%d", genericContainerHost, genericContainerPort);
            registry.add("application.person-service.uri", () -> actualPersonServiceUri);

//            Set up Person-service actual Keycloak connection (as resource service) URIs
            String keycloakContainerHost = TestContainers.Keycloak.KEYCLOAK_CONTAINER.getHost();
            int keycloakContainerPort = TestContainers.Keycloak.KEYCLOAK_CONTAINER.getMappedPort(8080);
            genericContainer.withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI",
                    String.format("http://%s:%d/realms/payment-system",
                            keycloakContainerHost, keycloakContainerPort));
            genericContainer.withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI",
                    String.format("http://%s:%d/realms/payment-system/protocol/openid-connect/certs",
                            keycloakContainerHost, keycloakContainerPort));
        }
    }
}