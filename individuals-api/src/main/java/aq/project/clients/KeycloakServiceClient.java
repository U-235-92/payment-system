package aq.project.clients;

import aq.project.dto.IndividualsApiServiceCreatePersonRequestDto;
import aq.project.dto.IndividualsApiServiceTokenRequestDto;
import aq.project.dto.IndividualsApiServiceTokenResponseDto;
import aq.project.dto.IndividualsApiServiceUpdatePersonRequestDto;
import aq.project.repositories.JwtRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PutExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.keycloak.OAuth2Constants.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.*;

public interface KeycloakServiceClient {

    default Mono<IndividualsApiServiceTokenResponseDto> refreshUserJwt(
            String adminId,
            String adminSecret,
            String tokenEndpoint,
            IndividualsApiServiceTokenRequestDto individualsApiServiceTokenRequestDto,
            WebClient webClient
    ) {
        LinkedMultiValueMap<String, String> formUrlEncoded = new LinkedMultiValueMap<>();

        formUrlEncoded.add(CLIENT_ID, adminId);
        formUrlEncoded.add(CLIENT_SECRET, adminSecret);
        formUrlEncoded.add(GRANT_TYPE, REFRESH_TOKEN);
        formUrlEncoded.add(REFRESH_TOKEN, individualsApiServiceTokenRequestDto.getRefreshToken());

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formUrlEncoded)
                .exchangeToMono(response -> {
                    HttpStatusCode statusCode = response.statusCode();

                    if(statusCode.is5xxServerError())
                        return Mono.error(new HttpServerErrorException(
                                statusCode, "Error occurred during refresh JWT process"));

                    if(statusCode.is4xxClientError())
                        return Mono.error(new HttpClientErrorException(
                                statusCode, "Access token has expired or is invalid"));

                    return response.bodyToMono(IndividualsApiServiceTokenResponseDto.class);
                });
    }

    default Mono<String> getAdminJwt(
            JwtRepository jwtRepository,
            String adminId,
            String adminSecret,
            String tokenEndpoint,
            WebClient webClient,
            ObjectMapper objectMapper
    ) {
        if(jwtRepository.isAdminJwtExist()) {
            String existAdminJwt = jwtRepository.getAdminJwt();
            try {
                if(isJwtExpired(existAdminJwt, objectMapper))
                    return getAdminJwt(adminId, adminSecret, tokenEndpoint, jwtRepository, webClient);

                return Mono.just(existAdminJwt);

            } catch (IOException e) {
                return Mono.error(e);
            }
        } else {
            return getAdminJwt(adminId, adminSecret, tokenEndpoint, jwtRepository, webClient);
        }
    }

    private Mono<String> getAdminJwt(
            String adminId,
            String adminSecret,
            String tokenEndpoint,
            JwtRepository jwtRepository,
            WebClient webClient
    ) {
        LinkedMultiValueMap<String, String> formUrlEncoded = new LinkedMultiValueMap<>();

        formUrlEncoded.add(CLIENT_ID, adminId);
        formUrlEncoded.add(CLIENT_SECRET, adminSecret);
        formUrlEncoded.add(GRANT_TYPE, CLIENT_CREDENTIALS);

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(APPLICATION_FORM_URLENCODED)
                .bodyValue(formUrlEncoded)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, on4xxClientError())
                .bodyToMono(Map.class)
                .flatMap(this::extractAdminJwt)
                .doOnSuccess(jwt -> jwtRepository.putAdminJwt( (String) jwt ));
    }

    private Function<ClientResponse, Mono<? extends Throwable>> on4xxClientError() {
        return response -> Mono.error(new HttpClientErrorException(
                response.statusCode(), "Error occurred during getting admin access token: check admin credentials"));
    }

    private Mono<String> extractAdminJwt(Map<String, Object> map) {
        if(map.get(ACCESS_TOKEN) != null)
            return Mono.just((String) map.get(ACCESS_TOKEN));

        return Mono.error(new HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred during getting admin access token"));
    }

    private boolean isJwtExpired(
            String accessToken,
            ObjectMapper objectMapper
    ) throws IOException {
        String payload = accessToken.split("\\.")[1];

        Base64.Decoder decoder = Base64.getDecoder();

        JsonNode node = objectMapper.readTree(decoder.decode(payload));

        Instant now = Instant.now();
        Instant exp = Instant.ofEpochSecond(node.get("exp").asLong());

        return now.isAfter(exp);
    }

    default Mono<UUID> createUser(
            String adminJwtAuthorizationValue,
            String adminEndpoint,
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto,
            WebClient webClient
    ) {
        return webClient.post()
                .uri(adminEndpoint)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, adminJwtAuthorizationValue)
                .bodyValue(getKeycloakUserRepresentation(individualsApiServiceCreatePersonRequestDto))
                .exchangeToMono(this::extractUserId);
    }

    private UserRepresentation getKeycloakUserRepresentation(
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto
    ) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(individualsApiServiceCreatePersonRequestDto.getPassword());

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(individualsApiServiceCreatePersonRequestDto.getFirstName());
        userRepresentation.setLastName(individualsApiServiceCreatePersonRequestDto.getLastName());
        userRepresentation.setUsername(individualsApiServiceCreatePersonRequestDto.getUsername());
        userRepresentation.setEmail(individualsApiServiceCreatePersonRequestDto.getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(List.of(credentialRepresentation));

        return userRepresentation;
    }

    private Mono<UUID> extractUserId(
            ClientResponse response
    ) {
        HttpStatusCode statusCode = response.statusCode();

        if(statusCode.isSameCodeAs(HttpStatus.CONFLICT))
            return Mono.error(new HttpClientErrorException(
                    statusCode, "Only unique pair of username and email is valid"));

        URI locationURI = response.headers().asHttpHeaders().getLocation();

        if(locationURI == null || locationURI.getPath() == null || statusCode.is5xxServerError())
            return Mono.error(new HttpServerErrorException(
                    statusCode, "Error occurred during creating user"));

        String location = locationURI.getPath();
        String userId = location.substring(location.lastIndexOf('/') + 1);

        return Mono.just(UUID.fromString(userId));
    }

    default Mono<HttpStatusCode> undoCreateUser(
            String adminJwtAuthorizationValue,
            UUID personKeycloakId
    ) {
        return undoCreateUserInternal(adminJwtAuthorizationValue, personKeycloakId.toString());
    }

    @DeleteExchange(
            value = "${application.keycloak-service.endpoints.admin}/{personKeycloakId}"
    )
    Mono<HttpStatusCode> undoCreateUserInternal(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("personKeycloakId") String personKeycloakId
    );

    default Mono<IndividualsApiServiceTokenResponseDto> loginUser(
            String email,
            String password,
            String clientId,
            String clientSecret,
            String tokenEndpoint,
            WebClient webClient,
            ObjectMapper objectMapper
    ) {
        LinkedMultiValueMap<String, String> formUrlEncoded = new LinkedMultiValueMap<>();

        formUrlEncoded.add(USERNAME, email);
        formUrlEncoded.add(PASSWORD, password);
        formUrlEncoded.add(CLIENT_ID, clientId);
        formUrlEncoded.add(CLIENT_SECRET, clientSecret);
        formUrlEncoded.add(GRANT_TYPE, "password");

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(APPLICATION_FORM_URLENCODED)
                .bodyValue(formUrlEncoded)
                .exchangeToMono(response -> {
                    HttpStatusCode statusCode = response.statusCode();

                    if(statusCode.is4xxClientError())
                        return Mono.error(new HttpClientErrorException(
                                statusCode, String.format(
                                        "Login error: received email: [%s] or password are incorrect", email)));

                    if(statusCode.is5xxServerError())
                        return Mono.error(new HttpServerErrorException(
                                statusCode, "Error occurred during login user"));

                    return response.bodyToMono(IndividualsApiServiceTokenResponseDto.class);
                })
                .flatMap(tokenResponse -> {
                    try {
                        return Mono.just(
                                setTokenResponseUserId(
                                        tokenResponse, tokenResponse.getAccessToken(), objectMapper));

                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                });
    }

    private IndividualsApiServiceTokenResponseDto setTokenResponseUserId(
            IndividualsApiServiceTokenResponseDto individualsApiServiceTokenResponseDto,
            String accessToken,
            ObjectMapper objectMapper
    ) throws IOException {
        String payload = accessToken.split("\\.")[1];

        UUID userId = UUID.fromString(objectMapper.readTree(Base64.getDecoder().decode(payload))
                .get("sub")
                .asText());

        individualsApiServiceTokenResponseDto.setPersonKeycloakId(userId);

        return individualsApiServiceTokenResponseDto;
    }

    default Mono<HttpStatusCode> updatePerson(
            String adminJwtAuthorizationValue,
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto
    ) {
        UserRepresentation userRepresentation = getUserRepresentation(individualsApiServiceUpdatePersonRequestDto);

        UUID personKeycloakId = individualsApiServiceUpdatePersonRequestDto.getPersonKeycloakId();

        return updateUserInternal(adminJwtAuthorizationValue, personKeycloakId, userRepresentation);
    }

    private UserRepresentation getUserRepresentation(
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto
    ) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(individualsApiServiceUpdatePersonRequestDto.getPassword());

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(individualsApiServiceUpdatePersonRequestDto.getFirstName());
        userRepresentation.setLastName(individualsApiServiceUpdatePersonRequestDto.getLastName());
        userRepresentation.setCredentials(List.of(credentialRepresentation));

        return userRepresentation;
    }

    @PutExchange(
            value = "${application.keycloak-service.endpoints.admin}/{keycloakUserId}",
            contentType = APPLICATION_JSON_VALUE
    )
    Mono<HttpStatusCode> updateUserInternal(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("keycloakUserId") UUID keycloakUserId,
            @RequestBody UserRepresentation userRepresentation
    );

    default Mono<HttpStatusCode> deleteUserByKeycloakId(
            String adminJwtAuthorizationValue,
            UUID keycloakUserId
    ) {
        return deleteUserByKeycloakIdInternal(adminJwtAuthorizationValue, keycloakUserId.toString());
    }

    @DeleteExchange(
            value = "${application.keycloak-service.endpoints.admin}/{personKeycloakId}"
    )
    Mono<HttpStatusCode> deleteUserByKeycloakIdInternal(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("personKeycloakId") String personKeycloakId
    );
}