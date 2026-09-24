package aq.project._utils;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;

public class ContainerPropertiesConfigurer {

    private static final String ISSUER_URI = "/realms/payment-system";

    private static final String JWK_SET_URI = "/realms/payment-system/protocol/openid-connect/certs";

    private static final String TOKEN_URI = "/realms/payment-system/protocol/openid-connect/token";

    private static final String ADMIN_URI = "/admin/realms/payment-system/users";

    private static final String CLIENT_ID = "individuals-api";

    private static final String ADMIN_CLIENT_ID = "payment-system-admin-cli";

    private static final String CLIENT_SECRET = "TEST-SECRET";

    public static void registerApplicationContextKeycloakContainerProperties(DynamicPropertyRegistry registry, KeycloakContainer container) {
        String keycloakContainerBaseExposedUrl = container.getAuthServerUrl();

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloakContainerBaseExposedUrl + ISSUER_URI);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> keycloakContainerBaseExposedUrl + JWK_SET_URI);
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri", () -> keycloakContainerBaseExposedUrl + ISSUER_URI);
        registry.add("spring.security.oauth2.client.provider.keycloak.token-uri", () -> keycloakContainerBaseExposedUrl + TOKEN_URI);
        registry.add("spring.security.oauth2.client.registration.keycloak.client-id", () -> CLIENT_ID);
        registry.add("spring.security.oauth2.client.registration.keycloak.client-secret", () -> CLIENT_SECRET);
        registry.add("spring.security.oauth2.client.registration.keycloak.admin-id", () -> ADMIN_CLIENT_ID);
        registry.add("spring.security.oauth2.client.registration.keycloak.admin-secret", () -> CLIENT_SECRET);
        registry.add("application.keycloak-service.admin-uri", () -> keycloakContainerBaseExposedUrl + ADMIN_URI);
        registry.add("application.keycloak-service.uri", () -> keycloakContainerBaseExposedUrl);
    }
}