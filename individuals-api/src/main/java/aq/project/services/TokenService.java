package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.TokenRequestDto;
import aq.project.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    public Mono<TokenResponseDto> refreshUserJwt(
            TokenRequestDto tokenRequestDto
    ) {
        return keycloakServiceClientFacade.refreshJwt(tokenRequestDto);
    }
}