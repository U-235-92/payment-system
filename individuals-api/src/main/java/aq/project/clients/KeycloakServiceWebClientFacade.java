package aq.project.clients;

import aq.project.dto.CreateUserDTO;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.dto.UpdateUserDTO;
import aq.project.repositories.JwtRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@Component
public class KeycloakServiceWebClientFacade {

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

    private final KeycloakServiceWebClient keycloakServiceWebClient;

    private final JwtRepository jwtRepository;

    private final WebClient webClient;

    public KeycloakServiceWebClientFacade(
            KeycloakServiceWebClient keycloakServiceWebClient,
            JwtRepository jwtRepository,
            @Qualifier("keycloakWebClient") WebClient webClient
    ) {
        this.keycloakServiceWebClient = keycloakServiceWebClient;
        this.jwtRepository = jwtRepository;
        this.webClient = webClient;
    }

    public Mono<ResponseTokenDTO> refreshUserJwt(RefreshTokenDTO refreshTokenDto) {
        return keycloakServiceWebClient.refreshUserJwt(clientId, clientSecret, tokenEndpoint, refreshTokenDto, webClient);
    }

    public Mono<String> getAdminJwt() {
        return keycloakServiceWebClient.getAdminJwt(jwtRepository, adminId, adminSecret, tokenEndpoint, webClient);
    }

    public Mono<String> getAdminJwtAsAuthorizationHeaderValue() {
        return keycloakServiceWebClient.getAdminJwt(jwtRepository, adminId, adminSecret, tokenEndpoint, webClient)
                .map(jwt -> String.format("%s %s", BEARER.getValue(), jwt));
    }

    public Mono<String> createUser(String adminJwtAuthorizationValue, CreateUserDTO createUserDto) {
        return keycloakServiceWebClient.createUser(adminJwtAuthorizationValue, adminEndpoint, createUserDto, webClient);
    }

    public Mono<HttpStatusCode> undoCreateUser(String adminJwtAuthorizationValue, String keycloakUserId) {
        return keycloakServiceWebClient.undoCreateUser(adminJwtAuthorizationValue, keycloakUserId);
    }

    public Mono<ResponseTokenDTO> loginUser(String email, String password) {
        return keycloakServiceWebClient.loginUser(email, password, clientId, clientSecret, tokenEndpoint, webClient);
    }

    public Mono<HttpStatusCode> updateUser(String adminJwtAuthorizationValue, UpdateUserDTO updateUserDTO) {
        return keycloakServiceWebClient.updateUser(adminJwtAuthorizationValue, updateUserDTO);
    }

    public Mono<HttpStatusCode> deleteUserByKeycloakId(String adminJwtAuthorizationValue, String keycloakUserId) {
        return keycloakServiceWebClient.deleteUserByKeycloakId(adminJwtAuthorizationValue, keycloakUserId);
    }
}
