package aq.project.controllers;

import aq.project.controller.TokenRestControllerApi;
import aq.project.dto.TokenRequestDto;
import aq.project.dto.TokenResponseDto;
import aq.project.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TokenRestController implements TokenRestControllerApi {

    private final TokenService tokenService;

    @Override
    public Mono<ResponseEntity<TokenResponseDto>> refreshToken(
            Mono<TokenRequestDto> tokenRequestDto,
            ServerWebExchange exchange
    ) {
        return tokenRequestDto
                .flatMap(dto ->  tokenService.refreshUserJwt(dto)
                .map(ResponseEntity::ok));
    }
}