package aq.project.clients;

import aq.project.dto.CreateUserDto;
import aq.project.dto.RefreshTokenDto;
import aq.project.dto.ResponseTokenDto;
import aq.project.dto.UpdateUserDto;
import aq.project.repositories.JwtRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@Component
public class KeycloakServiceClientFacade {

    @Value("${spring.security.oauth2.client.registration.keycloak.admin-id}")
    private String adminId;
    @Value("${spring.security.oauth2.client.registration.keycloak.admin-secret}")
    private String adminSecret;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    @Value("${application.keycloak-service.endpoints.token}")
    private String tokenEndpoint;
    @Value("${application.keycloak-service.endpoints.admin}")
    private String adminEndpoint;

    private final KeycloakServiceClient keycloakServiceClient;

    private final JwtRepository jwtRepository;

    private final WebClient webClient;

    public KeycloakServiceClientFacade(
            KeycloakServiceClient keycloakServiceClient,
            JwtRepository jwtRepository,
            @Qualifier("keycloakWebClient") WebClient webClient
    ) {
        this.keycloakServiceClient = keycloakServiceClient;
        this.jwtRepository = jwtRepository;
        this.webClient = webClient;
    }

    public Mono<ResponseTokenDto> refreshUserJwt(RefreshTokenDto refreshTokenDto) {
        return keycloakServiceClient.refreshUserJwt(clientId, clientSecret, tokenEndpoint, refreshTokenDto, webClient);
    }

    public Mono<String> getAdminJwt() {
        return keycloakServiceClient.getAdminJwt(jwtRepository, adminId, adminSecret, tokenEndpoint, webClient);
    }

    public Mono<String> getAdminJwtAsAuthorizationHeaderValue() {
        return keycloakServiceClient.getAdminJwt(jwtRepository, adminId, adminSecret, tokenEndpoint, webClient)
                .map(jwt -> String.format("%s %s", BEARER.getValue(), jwt));
    }

    public Mono<String> createUser(String adminJwtAuthorizationValue, CreateUserDto createUserDto) {
        return keycloakServiceClient.createUser(adminJwtAuthorizationValue, adminEndpoint, createUserDto, webClient);
    }

    public Mono<HttpStatusCode> undoCreateUser(String adminJwtAuthorizationValue, String keycloakUserId) {
        return keycloakServiceClient.undoCreateUser(adminJwtAuthorizationValue, keycloakUserId);
    }

    public Mono<ResponseTokenDto> loginUser(String email, String password) {
        return keycloakServiceClient.loginUser(email, password, clientId, clientSecret, tokenEndpoint, webClient);
    }

    public Mono<HttpStatusCode> updateUser(String adminJwtAuthorizationValue, UpdateUserDto updateUserDTO) {
        return keycloakServiceClient.updateUser(adminJwtAuthorizationValue, updateUserDTO);
    }

    public Mono<HttpStatusCode> deleteUserByKeycloakId(String adminJwtAuthorizationValue, String keycloakUserId) {
        return keycloakServiceClient.deleteUserByKeycloakId(adminJwtAuthorizationValue, keycloakUserId);
    }
}
