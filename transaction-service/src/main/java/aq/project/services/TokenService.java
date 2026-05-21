package aq.project.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String ADMIN_ACCESS_TOKEN = "admin-access-token";

    private final Map<String, String> tokenMap = new HashMap<>();

    @Value("${spring.security.oauth2.client.registration.keycloak.admin-id}")
    private String adminClientID;

    @Value("${spring.security.oauth2.client.registration.keycloak.admin-secret}")
    private String adminClientSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenURI;

    private final RestClient restClient;

    public String getAdminAccessToken() {
        if(tokenMap.isEmpty()) {
            String accessToken = requestAdminAccessToken();
            tokenMap.put(ADMIN_ACCESS_TOKEN, accessToken);
            return accessToken;
        } else {
            String accessToken = tokenMap.get(ADMIN_ACCESS_TOKEN);
            if(isTokenExpired(accessToken)) {
                accessToken = requestAdminAccessToken();
                tokenMap.put(ADMIN_ACCESS_TOKEN, accessToken);
                return accessToken;
            }
            return accessToken;
        }
    }

    private String requestAdminAccessToken() {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", adminClientID);
        form.add("client_secret", adminClientSecret);
        form.add("grant_type", "client_credentials");
        String keycloakResponse = restClient.post()
                .uri(tokenURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);
        return new ObjectMapper()
                .readTree(keycloakResponse)
                .get("access_token")
                .asString();
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
}
