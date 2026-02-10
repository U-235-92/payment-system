package aq.project.services;

import aq.project.dto.TokenRefreshRequest;
import aq.project.dto.TokenResponse;
import aq.project.proxies.KeycloakClient;
import lombok.Data;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Data
@Service
public class TokenService {

    private final KeycloakClient keycloakClient;

    public Mono<TokenResponse> requestUserToken(String email, String password) {
        return keycloakClient.requestToken(email, password);
    }

    public Mono<TokenResponse> refreshToken(TokenRefreshRequest request) {
        return keycloakClient.refreshToken(request);
    }
}
