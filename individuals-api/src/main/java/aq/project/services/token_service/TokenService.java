package aq.project.services.token_service;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceTokenRequestDto;
import aq.project.dto.IndividualsApiServiceTokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    public Mono<IndividualsApiServiceTokenResponseDto> refreshUserJwt(
            IndividualsApiServiceTokenRequestDto individualsApiServiceTokenRequestDto
    ) {
        return keycloakServiceClientFacade.refreshJwt(individualsApiServiceTokenRequestDto);
    }
}