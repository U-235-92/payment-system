package aq.project.services;

import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.proxies.KeycloakClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final KeycloakClient keycloakClient;

    public Mono<ResponseTokenDTO> refreshToken(RefreshTokenDTO refreshTokenDTO) {
        return keycloakClient.refreshToken(refreshTokenDTO);
    }
}
