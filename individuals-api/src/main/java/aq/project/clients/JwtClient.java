package aq.project.clients;

import aq.project.exceptions.ServiceException;
import aq.project.repositories.JwtTokenRepository;
import org.keycloak.OAuth2Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtClient {

    private static final String CLIENT_ID = "client_id";
    private static final String GRANT_TYPE = "grant_type";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String CLIENT_SECRET = "client_secret";

    @Value("${spring.security.oauth2.client.registration.keycloak.admin-id}")
    private String adminClientID;

    @Value("${spring.security.oauth2.client.registration.keycloak.admin-secret}")
    private String adminClientSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenURI;

    @Autowired
    @Qualifier("keycloakServiceWebClient")
    private WebClient webClient;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    public Mono<String> requestAdminToken() {
        if(jwtTokenRepository.isAdminTokenExists()) {
            String adminAccessToken = jwtTokenRepository.getAdminAccessToken();
            if(isTokenExpired(adminAccessToken))
                return requestNewAdminToken();
            return Mono.just(adminAccessToken);
        } else {
            return requestNewAdminToken();
        }
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
                .onStatus(code -> code.isSameCodeAs(HttpStatus.UNAUTHORIZED), on401AdminLoginErrorResponse())
                .bodyToMono(Map.class)
                .flatMap(this::extractAdminAccessToken)
                .doOnSuccess(tokenObj -> jwtTokenRepository.putAdminAccessToken((String) tokenObj));
    }

    private Function<ClientResponse, Mono<? extends Throwable>> on401AdminLoginErrorResponse() {
        return response -> Mono.error(new ServiceException("Service error. " +
                "Exception occurred during getting admin access token. Check admin client credentials."));
    }

    private Mono<String> extractAdminAccessToken(Map<String, Object> map) {
        if(map.get(ACCESS_TOKEN) != null)
            return Mono.just((String) map.get(ACCESS_TOKEN));
        return Mono.error(new ServiceException("Service error. Error occurred during getting admin access token."));
    }
}
