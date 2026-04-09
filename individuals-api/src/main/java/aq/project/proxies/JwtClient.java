package aq.project.proxies;

import aq.project.exceptions.ServiceException;
import aq.project.repositories.JwtTokenRepository;
import aq.project.util.jwt.JwtUtils;
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

import java.util.Map;
import java.util.function.Function;

@Component
public class JwtClient {

    private static final String CLIENT_ID = "client_id";
    private static final String GRANT_TYPE = "grant_type";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String CLIENT_SECRET = "client_secret";

    @Value("spring.security.oauth2.client.registration.keycloak.admin-id")
    private String adminClientID;

    @Value("spring.security.oauth2.client.registration.keycloak.admin-secret")
    private String adminClientSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenURI;

    @Autowired
    @Qualifier("keycloakWebClient")
    private WebClient webClient;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    public Mono<String> requestAdminToken() {
        if(jwtTokenRepository.isAdminTokenExists()) {
            String adminAccessToken = jwtTokenRepository.getAdminAccessToken();
            if(JwtUtils.isTokenExpired(adminAccessToken))
                return requestNewAdminToken();
            return Mono.just(adminAccessToken);
        } else {
            return requestNewAdminToken();
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
