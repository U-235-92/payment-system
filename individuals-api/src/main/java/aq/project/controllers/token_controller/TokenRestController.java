package aq.project.controllers.token_controller;

import aq.project.controller.TokenRestControllerApi;
import aq.project.dto.IndividualsApiServiceTokenRequestDto;
import aq.project.dto.IndividualsApiServiceTokenResponseDto;
import aq.project.services.token_service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TokenRestController implements TokenRestControllerApi {

    private final TokenService tokenService;

    @Override
    public Mono<ResponseEntity<IndividualsApiServiceTokenResponseDto>> refreshToken(
            Mono<IndividualsApiServiceTokenRequestDto> individualsApiServiceTokenRequestDto,
            ServerWebExchange exchange
    ) {
        return individualsApiServiceTokenRequestDto
                .flatMap(dto ->  tokenService.refreshUserJwt(dto)
                .map(token -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(token)));
    }
}