package aq.project.clients;

import aq.project.dto.CreateUserDto;
import aq.project.dto.RefreshTokenDto;
import aq.project.dto.ResponseTokenDto;
import aq.project.dto.UpdateUserDto;
import aq.project.repositories.JwtRepository;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.keycloak.OAuth2Constants.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.*;

public interface KeycloakServiceClient {

    default Mono<ResponseTokenDto> refreshUserJwt(
            String adminId,
            String adminSecret,
            String tokenEndpoint,
            RefreshTokenDto refreshTokenDto,
            WebClient webClient
    ) {
        LinkedMultiValueMap<String, String> formUrlEncoded = new LinkedMultiValueMap<>();
        formUrlEncoded.add(CLIENT_ID, adminId);
        formUrlEncoded.add(CLIENT_SECRET, adminSecret);
        formUrlEncoded.add(GRANT_TYPE, REFRESH_TOKEN);
        formUrlEncoded.add(REFRESH_TOKEN, refreshTokenDto.getRefreshToken());
        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formUrlEncoded)
                .exchangeToMono(response -> {
                    HttpStatusCode statusCode = response.statusCode();
                    if(statusCode.is5xxServerError()) {
                        String msg = "Error occurred during refresh JWT process";
                        return Mono.error(new HttpServerErrorException(statusCode, msg));
                    }
                    if(statusCode.is4xxClientError()) {
                        String msg = "Access token has expired or is invalid";
                        return Mono.error(new HttpClientErrorException(statusCode, msg));
                    }
                    return response.bodyToMono(ResponseTokenDto.class);
                });
    }

    default Mono<String> getAdminJwt(
            JwtRepository jwtRepository,
            String adminId,
            String adminSecret,
            String tokenEndpoint,
            WebClient webClient
    ) {
        if(jwtRepository.isAdminJwtExist()) {
            String existAdminJwt = jwtRepository.getAdminJwt();
            if(isJwtExpired(existAdminJwt)) {
                return getAdminJwt(adminId, adminSecret, tokenEndpoint, jwtRepository, webClient);
            }
            return Mono.just(existAdminJwt);
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
        return response -> {
            String msg = "Error occurred during getting admin access token: check admin credentials";
            return Mono.error(new HttpClientErrorException(response.statusCode(), msg));
        };
    }

    private Mono<String> extractAdminJwt(Map<String, Object> map) {
        if(map.get(ACCESS_TOKEN) != null)
            return Mono.just((String) map.get(ACCESS_TOKEN));
        HttpStatusCode statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        String msg = "Error occurred during getting admin access token";
        return Mono.error(new HttpServerErrorException(statusCode, msg));
    }

    private boolean isJwtExpired(String accessToken) {
        String payload = accessToken.split("\\.")[1];
        Base64.Decoder decoder = Base64.getDecoder();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(decoder.decode(payload));
        Instant now = Instant.now();
        Instant exp = Instant.ofEpochSecond(node.get("exp").asLong());
        return now.isAfter(exp);
    }

    default Mono<String> createUser(
            String adminJwtAuthorizationValue,
            String adminEndpoint,
            CreateUserDto createUserDto,
            WebClient webClient
    ) {
        return webClient.post()
                .uri(adminEndpoint)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, adminJwtAuthorizationValue)
                .bodyValue(getKeycloakUserRepresentation(createUserDto))
                .exchangeToMono(this::extractUserId);
    }

    private UserRepresentation getKeycloakUserRepresentation(CreateUserDto createUserDto) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(createUserDto.getPassword());

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(createUserDto.getIndividualData().getFirstName());
        userRepresentation.setLastName(createUserDto.getIndividualData().getLastName());
        userRepresentation.setUsername(createUserDto.getUsername());
        userRepresentation.setEmail(createUserDto.getIndividualData().getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        return userRepresentation;
    }

    private Mono<String> extractUserId(ClientResponse response) {
        HttpStatusCode statusCode = response.statusCode();
        if(statusCode.isSameCodeAs(HttpStatus.CONFLICT)) {
            String msg = "Only unique pair of username and email is valid";
            return Mono.error(new HttpClientErrorException(statusCode, msg));
        }
        URI locationURI = response.headers().asHttpHeaders().getLocation();
        if(locationURI == null || locationURI.getPath() == null || statusCode.is5xxServerError()) {
            String msg = "Error occurred during creating user";
            return Mono.error(new HttpServerErrorException(statusCode, msg));
        }
        String location = locationURI.getPath();
        String userId = location.substring(location.lastIndexOf('/') + 1);
        return Mono.just(userId);
    }

    default Mono<HttpStatusCode> undoCreateUser(
            String adminJwtAuthorizationValue,
            String keycloakUserId
    ) {
        return undoCreateUserInternal(adminJwtAuthorizationValue, keycloakUserId);
    }

    @DeleteExchange(
            value = "${application.keycloak-service.endpoints.admin}/{keycloakUserId}"
    )
    Mono<HttpStatusCode> undoCreateUserInternal(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable String keycloakUserId
    );

    default Mono<ResponseTokenDto> loginUser(
            String email,
            String password,
            String clientId,
            String clientSecret,
            String tokenEndpoint,
            WebClient webClient
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
                    if(statusCode.is4xxClientError()) {
                        String msg = String.format("Error occurred during login user: received email %s or password are incorrect",
                                email);
                        return Mono.error(new HttpClientErrorException(statusCode, msg));
                    }
                    if(statusCode.is5xxServerError()) {
                        String msg = "Error occurred during login user";
                        return Mono.error(new HttpServerErrorException(statusCode, msg));
                    }
                    return response.bodyToMono(ResponseTokenDto.class);
                })
                .map(tokenResponse -> setTokenResponseUserId(tokenResponse, tokenResponse.getAccessToken()));
    }

    private ResponseTokenDto setTokenResponseUserId(
            ResponseTokenDto responseTokenDTO,
            String accessToken
    ) {
        String payload = accessToken.split("\\.")[1];
        ObjectMapper mapper = new ObjectMapper();
        String userId = mapper.readTree(Base64.getDecoder().decode(payload)).get("sub").asString();
        responseTokenDTO.setKeycloakUserId(userId);
        return responseTokenDTO;
    }

    default Mono<HttpStatusCode> updateUser(
            String adminJwtAuthorizationValue,
            UpdateUserDto updateUserDto
    ) {
        UserRepresentation userRepresentation = getUserRepresentation(updateUserDto);
        return updateUserInternal(adminJwtAuthorizationValue, updateUserDto.getKeycloakUserId(), userRepresentation);
    }

    private UserRepresentation getUserRepresentation(UpdateUserDto updateUserDto) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(updateUserDto.getPassword());

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(updateUserDto.getIndividualData().getFirstName());
        userRepresentation.setLastName(updateUserDto.getIndividualData().getLastName());
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        return userRepresentation;
    }

    @PutExchange(
            value = "${application.keycloak-service.endpoints.admin}/{keycloakUserId}",
            contentType = APPLICATION_JSON_VALUE
    )
    Mono<HttpStatusCode> updateUserInternal(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("keycloakUserId") String keycloakUserId,
            @RequestBody UserRepresentation userRepresentation
    );

    default Mono<HttpStatusCode> deleteUserByKeycloakId(
            String adminJwtAuthorizationValue,
            String keycloakUserId
    ) {
        return deleteUserByKeycloakIdInternal(adminJwtAuthorizationValue, keycloakUserId);
    }

    @DeleteExchange(
            value = "${application.keycloak-service.endpoints.admin}/{keycloakUserId}"
    )
    Mono<HttpStatusCode> deleteUserByKeycloakIdInternal(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("keycloakUserId") String keycloakUserId
    );
}