package aq.project.util;

import org.springframework.test.context.DynamicPropertyRegistry;

public class TestApplicationProperties {

    public static class KeycloakProperties {

        private static final String ISSUER_URI = "/realms/payment-system";

        private static final String JWK_SET_URI = "/realms/payment-system/protocol/openid-connect/certs";

        private static final String TOKEN_URI = "/realms/payment-system/protocol/openid-connect/token";

        private static final String ADMIN_URI = "/admin/realms/payment-system/users";

        private static final String CLIENT_ID = "individuals-api";

        private static final String ADMIN_CLIENT_ID = "payment-system-admin-cli";

        private static final String CLIENT_SECRET = "TEST-SECRET";

        public static void registerApplicationContextContainerProperties(DynamicPropertyRegistry registry) {
            String keycloakContainerBaseExposedUrl = TestContainers.Keycloak.KEYCLOAK_CONTAINER.getAuthServerUrl();

            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloakContainerBaseExposedUrl + ISSUER_URI);
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> keycloakContainerBaseExposedUrl + JWK_SET_URI);
            registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri", () -> keycloakContainerBaseExposedUrl + ISSUER_URI);
            registry.add("spring.security.oauth2.client.provider.keycloak.token-uri", () -> keycloakContainerBaseExposedUrl + TOKEN_URI);
            registry.add("spring.security.oauth2.client.registration.keycloak.client-id", () -> CLIENT_ID);
            registry.add("spring.security.oauth2.client.registration.keycloak.client-secret", () -> CLIENT_SECRET);
            registry.add("spring.security.oauth2.client.registration.keycloak.admin-id", () -> ADMIN_CLIENT_ID);
            registry.add("spring.security.oauth2.client.registration.keycloak.admin-secret", () -> CLIENT_SECRET);
            registry.add("keycloak.admin-uri", () -> keycloakContainerBaseExposedUrl + ADMIN_URI);
        }
    }

    public static class PersonServiceProperties {
        public static void registerApplicationContextContainerProperties(DynamicPropertyRegistry registry) {
//            Set up in Individuals-API test area (works on local machine) actual Person-service URI (fetched from container, person-service works on container)
            String personServiceContainerExposedHost = TestContainers.PersonService.PERSON_SERVICE_CONTAINER.getHost();
            int personServiceContainerExposedPort = TestContainers.PersonService.PERSON_SERVICE_CONTAINER.getMappedPort(8082);
            String personServiceContainerExposedUri = String.format("http://%s:%d", personServiceContainerExposedHost, personServiceContainerExposedPort);

            registry.add("application.person-service.uri", () -> personServiceContainerExposedUri);
        }
    }
}