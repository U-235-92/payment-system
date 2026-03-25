package aq.project.proxies;

import aq.project.dto.CreateUserDTO;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.dto.UpdateUserDTO;
import aq.project.exceptions.IncorrectUserCredentialsException;
import aq.project.exceptions.InvalidTokenException;
import aq.project.exceptions.ServiceException;
import aq.project.exceptions.UserExistsException;
import aq.project.util.http.HttpUtils;
import aq.project.util.keycloak.KeycloakUtils;
import org.keycloak.OAuth2Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Base64;

@Component
public class KeycloakClient {

    private static final String BEARER = "Bearer ";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CLIENT_ID = "client_id";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_SECRET = "client_secret";

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenURI;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientID;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-uri}")
    private String adminURI;

    @Autowired
    @Qualifier("keycloakWebClient")
    private WebClient webClient;
    @Autowired
    private JwtClient jwtClient;

    public Mono<String> createUser(CreateUserDTO createUserDTO) {
        return jwtClient.requestAdminToken()
                .flatMap(adminToken -> createUser(adminToken, createUserDTO));
    }

    private Mono<String> createUser(String adminToken, CreateUserDTO createUserDTO) {
        return webClient.post()
                .uri(adminURI)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                .bodyValue(KeycloakUtils.getUserRepresentation(createUserDTO))
                .exchangeToMono(this::extractUserId);
    }

    private Mono<String> extractUserId(ClientResponse clientResponse) {
        if(clientResponse.statusCode().isSameCodeAs(HttpStatus.CONFLICT))
            return Mono.error(new UserExistsException("Registration error. Only unique pair of username and email is valid."));
        URI locationURI = clientResponse.headers().asHttpHeaders().getLocation();
        if(locationURI == null || locationURI.getPath() == null || clientResponse.statusCode().is5xxServerError())
            return Mono.error(new ServiceException("Error occurred during creating user."));
        String location = locationURI.getPath();
        String userId = location.substring(location.lastIndexOf('/') + 1);
        return Mono.just(userId);
    }

    public Mono<HttpStatusCode> undoCreateUser(String keycloakUserId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminToken -> webClient.delete()
                        .uri(adminURI + "/" + keycloakUserId)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                        .exchangeToMono(response -> {
                            if(HttpUtils.isErrorStatusCode(response.statusCode()))
                                return Mono.just(response.statusCode());
                            return Mono.just(HttpStatus.OK);
                        }));
    }

    public Mono<ResponseTokenDTO> loginUser(String email, String password) {
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
            .exchangeToMono(response -> {
                if(response.statusCode().is5xxServerError())
                    return Mono.error(new ServiceException("Error occurred during login user."));
                if(response.statusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED))
                    return Mono.error(new IncorrectUserCredentialsException("Authentication error. Input credentials are incorrect. Check email and password and try again."));
                return response.bodyToMono(ResponseTokenDTO.class);
            })
            .map(tokenResponse -> setTokenResponseUserId(tokenResponse, tokenResponse.getAccessToken()));
    }

    private ResponseTokenDTO setTokenResponseUserId(ResponseTokenDTO responseTokenDTO, String accessToken) {
        String payload = accessToken.split("\\.")[1];
        ObjectMapper mapper = new ObjectMapper();
        String userId = mapper.readTree(Base64.getDecoder().decode(payload)).get("sub").asString();
        responseTokenDTO.setKeycloakUserId(userId);
        return responseTokenDTO;
    }

    public Mono<HttpStatusCode> updateUser(UpdateUserDTO updateUserDTO) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> webClient.put()
                        .uri(adminURI + "/" + updateUserDTO.getKeycloakUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .bodyValue(KeycloakUtils.getUserRepresentation(updateUserDTO))
                        .exchangeToMono(response -> {
                            if(HttpUtils.isErrorStatusCode(response.statusCode()))
                                return Mono.just(response.statusCode());
                            return Mono.just(HttpStatus.OK);
                        }));
    }

    public Mono<HttpStatusCode> deleteUserByKeycloakId(String keycloakUserId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminToken -> webClient.delete()
                        .uri(adminURI + "/" + keycloakUserId)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                        .exchangeToMono(response -> {
                            if(HttpUtils.isErrorStatusCode(response.statusCode()))
                                return Mono.just(response.statusCode());
                            return Mono.just(HttpStatus.OK);
                        }));
    }

    public Mono<ResponseTokenDTO> refreshToken(RefreshTokenDTO refreshTokenDTO) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(CLIENT_ID, clientID);
        form.add(CLIENT_SECRET, clientSecret);
        form.add(GRANT_TYPE, OAuth2Constants.REFRESH_TOKEN);
        form.add(OAuth2Constants.REFRESH_TOKEN, refreshTokenDTO.getRefreshToken());
        return webClient.post()
                .uri(tokenURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .exchangeToMono(response -> {
                    if(response.statusCode().is5xxServerError())
                        return Mono.error(new ServiceException("Error occurred during refresh token."));
                    if(response.statusCode().isSameCodeAs(HttpStatus.BAD_REQUEST))
                        return Mono.error(new InvalidTokenException("Access denied. The access token has expired or is invalid."));
                    return response.bodyToMono(ResponseTokenDTO.class);
                });
    }
}
