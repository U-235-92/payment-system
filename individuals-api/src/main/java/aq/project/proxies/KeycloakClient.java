package aq.project.proxies;

import aq.project.dto.TokenRefreshRequest;
import aq.project.dto.TokenResponse;
import aq.project.dto.UserRegistrationRequest;
import aq.project.exceptions.IncorrectUserCredentialsException;
import aq.project.exceptions.InvalidTokenException;
import aq.project.exceptions.ServiceException;
import aq.project.exceptions.UserExistsException;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class KeycloakClient {

    private static final String BEARER = "Bearer ";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CLIENT_ID = "client_id";
    private static final String GRANT_TYPE = "grant_type";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String ADMIN_ACCESS_TOKEN = "admin_access_token";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final WebClient webClient;

    private final Map<String, String> keyCloakPropertiesCache = new HashMap<>();

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientID;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    @Value("${application.keycloak.admin.client-id}")
    private String adminClientID;
    @Value("${application.keycloak.admin.client-secret}")
    private String adminClientSecret;
    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenURI;
    @Value("${application.keycloak.admin-uri}")
    private String adminURI;

    public Mono<Void> createUser(UserRegistrationRequest request) {
        return requestAdminToken()
                .flatMap(adminToken -> createUser(adminToken, request));
    }

    private Mono<String> requestAdminToken() {
        if(keyCloakPropertiesCache.get(ADMIN_ACCESS_TOKEN) == null) {
            return requestNewAdminToken();
        } else {
            String adminAccessToken = keyCloakPropertiesCache.get(ADMIN_ACCESS_TOKEN);
            if(isTokenExpired(adminAccessToken)) {
                return requestNewAdminToken();
            }
            return Mono.just(adminAccessToken);
        }
    }

    private Mono<String> requestNewAdminToken() {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(CLIENT_ID, adminClientID);
        form.add(CLIENT_SECRET, adminClientSecret);
        form.add(GRANT_TYPE, OAuth2Constants.CLIENT_CREDENTIALS);
        return webClient.post()
                .uri(tokenURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(this::getAdminAccessToken)
                .doOnSuccess(this::putAdminAccessTokenIntoCache);
    }

    private Mono<String> getAdminAccessToken(Map<String, Object> map) {
        if(map.get(ACCESS_TOKEN) != null) {
            return Mono.just((String) map.get(ACCESS_TOKEN));
        }
        return Mono.error(new ServiceException("Service error. Error occurred during getting admin access token."));
    }

    private void putAdminAccessTokenIntoCache(Object accessToken) {
        keyCloakPropertiesCache.put(ADMIN_ACCESS_TOKEN, (String) accessToken);
    }

    private boolean isTokenExpired(String accessToken) {
        String payload = accessToken.split("\\.")[1];
        Base64.Decoder decoder = Base64.getDecoder();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(decoder.decode(payload));
        Instant now = Instant.now();
        Instant exp = Instant.ofEpochSecond(node.get("exp").asLong());
        return now.isAfter(exp);
    }

    private Mono<Void> createUser(String adminToken, UserRegistrationRequest request) {
        return webClient.post()
                .uri(adminURI)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                .bodyValue(getUserRepresentation(request))
                .retrieve()
                .onStatus(code -> code.isSameCodeAs(HttpStatus.CONFLICT), on409ErrorResponse())
                .bodyToMono(Void.class);
    }

    private UserRepresentation getUserRepresentation(UserRegistrationRequest request) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(request.getFirstName());
        userRepresentation.setLastName(request.getLastName());
        userRepresentation.setUsername(request.getUsername());
        userRepresentation.setEmail(request.getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(List.of(getCredentialRepresentation(request)));
        return userRepresentation;
    }

    private CredentialRepresentation getCredentialRepresentation(UserRegistrationRequest request) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType("password");
        credentialRepresentation.setValue(request.getPassword());
        return credentialRepresentation;
    }

    private Function<ClientResponse, Mono<? extends Throwable>> on409ErrorResponse() {
        return response -> Mono.error(new UserExistsException("Registration error. " +
                "Only unique pair of username and email is valid."));
    }

    public Mono<TokenResponse> requestToken(String email, String password) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(USERNAME, email);
        form.add(PASSWORD, password);
        form.add(CLIENT_ID, clientID);
        form.add(CLIENT_SECRET, clientSecret);
        form.add(GRANT_TYPE, "password");
        return webClient.post()
            .uri(tokenURI)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(form)
            .retrieve()
            .onStatus(code -> code.isSameCodeAs(HttpStatus.UNAUTHORIZED), on401ErrorResponse())
            .bodyToMono(TokenResponse.class)
            .map(tokenResponse -> setUserId(tokenResponse, tokenResponse.getAccessToken()));
    }

    private Function<ClientResponse, Mono<? extends Throwable>> on401ErrorResponse() {
        return response -> Mono
                .error(new IncorrectUserCredentialsException("Authentication error. " +
                        "Input credentials are incorrect. " +
                        "Check email and password and try again."));
    }

    private TokenResponse setUserId(TokenResponse tokenResponse, String accessToken) {
        String payload = accessToken.split("\\.")[1];
        ObjectMapper mapper = new ObjectMapper();
        String userId = mapper.readTree(Base64.getDecoder().decode(payload)).get("sub").asString();
        tokenResponse.setUserId(userId);
        return tokenResponse;
    }

    public Mono<TokenResponse> refreshToken(TokenRefreshRequest request) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(CLIENT_ID, clientID);
        form.add(CLIENT_SECRET, clientSecret);
        form.add(GRANT_TYPE, OAuth2Constants.REFRESH_TOKEN);
        form.add(OAuth2Constants.REFRESH_TOKEN, request.getRefreshToken());
        return webClient.post()
                .uri(tokenURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(code -> code.isSameCodeAs(HttpStatus.BAD_REQUEST), on400ErrorResponse())
                .bodyToMono(TokenResponse.class);
    }

    private Function<ClientResponse, Mono<? extends Throwable>> on400ErrorResponse() {
        return response -> Mono
                .error(new InvalidTokenException("Access denied. The access token has expired or is invalid."));
    }
}
