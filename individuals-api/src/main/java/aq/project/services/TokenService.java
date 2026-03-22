package aq.project.services;

import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.TokenResponse;
import aq.project.proxies.KeycloakClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final KeycloakClient keycloakClient;

    public Mono<TokenResponse> login(String email, String password) {
        return keycloakClient.loginUser(email, password);
    }

    public Mono<TokenResponse> refreshToken(RefreshTokenDTO refreshTokenDTO) {
        return keycloakClient.refreshToken(refreshTokenDTO);
    }
}
