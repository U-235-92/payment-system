package aq.project.services;

import aq.project.clients.KeycloakServiceRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static aq.project.utils.constants.CustomHttpHeaders.BEARER;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String ADMIN_ACCESS_TOKEN = "admin-access-token";

    private final Map<String, String> tokenMap = new HashMap<>();

    @Value("${spring.security.oauth2.client.registration.keycloak.admin-id}")
    private String adminId;
    @Value("${spring.security.oauth2.client.registration.keycloak.admin-secret}")
    private String adminSecret;

    private final KeycloakServiceRestClient keycloakServiceRestClient;

    private final ObjectMapper objectMapper;

    public String getAdminJwt() {
        if(tokenMap.isEmpty()) {
            String accessToken = keycloakServiceRestClient.getAdminJwt(adminId, adminSecret, objectMapper);
            tokenMap.put(ADMIN_ACCESS_TOKEN, accessToken);
            return accessToken;
        } else {
            String accessToken = tokenMap.get(ADMIN_ACCESS_TOKEN);
            if(isJwtExpired(accessToken)) {
                accessToken = keycloakServiceRestClient.getAdminJwt(adminId, adminSecret, objectMapper);
                tokenMap.put(ADMIN_ACCESS_TOKEN, accessToken);
                return accessToken;
            }
            return accessToken;
        }
    }

    public String getAdminJwtAsAuthorizationHeaderValue() {
        return BEARER + getAdminJwt();
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
}
