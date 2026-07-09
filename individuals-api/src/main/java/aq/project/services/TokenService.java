package aq.project.services;

import aq.project.clients.KeycloakServiceWebClientFacade;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final KeycloakServiceWebClientFacade keycloakServiceWebClientFacade;

    public Mono<ResponseTokenDTO> refreshUserJwt(RefreshTokenDTO refreshTokenDTO) {
        return keycloakServiceWebClientFacade.refreshUserJwt(refreshTokenDTO);
    }
}
