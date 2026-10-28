package aq.project.clients;

import aq.project.dto.IndividualsApiServiceCreatePersonRequestDto;
import aq.project.dto.IndividualsApiServiceTokenRequestDto;
import aq.project.dto.IndividualsApiServiceTokenResponseDto;
import aq.project.dto.IndividualsApiServiceUpdatePersonRequestDto;
import aq.project.repositories.JwtRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

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

    private final WebClient keycloakWebClient;

    private final ObjectMapper objectMapper;

    public KeycloakServiceClientFacade(
            KeycloakServiceClient keycloakServiceClient,
            JwtRepository jwtRepository,
            @Qualifier("keycloakWebClient") WebClient keycloakWebClient,
            ObjectMapper objectMapper
    ) {
        this.keycloakServiceClient = keycloakServiceClient;
        this.jwtRepository = jwtRepository;
        this.keycloakWebClient = keycloakWebClient;
        this.objectMapper = objectMapper;
    }

    public Mono<IndividualsApiServiceTokenResponseDto> refreshJwt(
            IndividualsApiServiceTokenRequestDto individualsApiServiceTokenRequestDto
    ) {
        return keycloakServiceClient
                .refreshUserJwt(
                        clientId, clientSecret, tokenEndpoint, individualsApiServiceTokenRequestDto, keycloakWebClient);
    }

    public Mono<String> getAdminJwt() {
        return keycloakServiceClient
                .getAdminJwt(
                        jwtRepository, adminId, adminSecret, tokenEndpoint, keycloakWebClient, objectMapper);
    }

    public Mono<String> getAdminJwtAsAuthorizationHeaderValue() {
        return keycloakServiceClient
                .getAdminJwt(jwtRepository, adminId, adminSecret, tokenEndpoint, keycloakWebClient, objectMapper)
                .map(jwt -> String.format("%s %s", BEARER.getValue(), jwt));
    }

    public Mono<UUID> createPerson(
            String adminJwtAuthorizationValue,
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto
    ) {
        return keycloakServiceClient
                .createUser(
                        adminJwtAuthorizationValue, adminEndpoint, individualsApiServiceCreatePersonRequestDto, keycloakWebClient);
    }

    public Mono<HttpStatusCode> undoCreatePerson(
            String adminJwtAuthorizationValue,
            UUID personKeycloakId
    ) {
        return keycloakServiceClient
                .undoCreateUser(adminJwtAuthorizationValue, personKeycloakId);
    }

    public Mono<IndividualsApiServiceTokenResponseDto> loginPerson(
            String email,
            String password
    ) {
        return keycloakServiceClient
                .loginUser(
                        email, password, clientId, clientSecret, tokenEndpoint, keycloakWebClient, objectMapper);
    }

    public Mono<HttpStatusCode> updatePerson(
            String adminJwtAuthorizationValue,
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto
    ) {
        return keycloakServiceClient
                .updatePerson(adminJwtAuthorizationValue, individualsApiServiceUpdatePersonRequestDto);
    }

    public Mono<HttpStatusCode> deletePerson(
            String adminJwtAuthorizationValue,
            UUID personKeycloakId
    ) {
        return keycloakServiceClient
                .deleteUserByKeycloakId(adminJwtAuthorizationValue, personKeycloakId);
    }
}