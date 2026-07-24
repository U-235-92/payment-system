package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.RefreshTokenDto;
import aq.project.dto.ResponseTokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    public Mono<ResponseTokenDto> refreshUserJwt(RefreshTokenDto dto) {
        return keycloakServiceClientFacade.refreshUserJwt(dto);
    }
}
