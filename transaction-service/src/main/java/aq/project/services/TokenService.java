package aq.project.services;

import aq.project.clients.KeycloakServiceRestClient;
import aq.project.utils.jwt.JwtUtil;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
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

    private final TraceContext traceContext;

    private volatile boolean isUpdateToken = false;

    private final Object synchronizer = new Object();

    public String getAdminJwtAsAuthorizationHeaderValue() {
        return "Bearer " + getAdminJwt();
    }

    public String getAdminJwt() {
        if(tokenMap.isEmpty()) {
            updateJwt();
            return tokenMap.get(ADMIN_ACCESS_TOKEN);

        } else {
            String accessToken = tokenMap.get(ADMIN_ACCESS_TOKEN);

            if(JwtUtil.isJwtExpired(accessToken)) {
                updateJwt();
                return tokenMap.get(ADMIN_ACCESS_TOKEN);
            }
            return accessToken;
        }
    }

    private void updateJwt() {
        synchronized (synchronizer) {
            try {
                while (isUpdateToken) {
                    wait(TimeUnit.SECONDS.toMillis(10));
                }
            } catch (InterruptedException e) {
                String traceId = traceContext.getTraceId() == null
                        ? TraceFactory.genTraceId()
                        : traceContext.getTraceId();

                log.error("[{}]: {} occurred while update an access JWT. Exception message: {}",
                        traceId, e.getClass().getSimpleName(), e.getMessage());
            }
            isUpdateToken = true;

            String accessToken = keycloakServiceRestClient.getAdminJwt(adminId, adminSecret, objectMapper);

            tokenMap.put(ADMIN_ACCESS_TOKEN, accessToken);

            isUpdateToken = false;

            notifyAll();
        }
    }
}