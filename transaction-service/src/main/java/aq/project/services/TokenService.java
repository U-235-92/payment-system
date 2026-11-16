package aq.project.services;

import aq.project.clients.KeycloakServiceRestClient;
import aq.project.utils.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${spring.security.oauth2.client.registration.keycloak.admin-id}")
    private String adminId;
    @Value("${spring.security.oauth2.client.registration.keycloak.admin-secret}")
    private String adminSecret;

    private final KeycloakServiceRestClient keycloakServiceRestClient;

    private final ObjectMapper objectMapper;

    private final Map<String, String> jwtRepository = new HashMap<>();

    public synchronized String getAdminJwt() {
        Supplier<String> jwtSupplier = () -> keycloakServiceRestClient.getAdminJwt(adminId, adminSecret, objectMapper);

        return JwtUtil.getAdminJwt(jwtSupplier, jwtRepository);
    }
}