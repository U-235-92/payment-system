package aq.project.controllers;

import aq.project.controller.TokenRestControllerApi;
import aq.project.dto.RefreshTokenDto;
import aq.project.dto.ResponseTokenDto;
import aq.project.services.TokenService;
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
    public Mono<ResponseEntity<ResponseTokenDto>> refreshToken(
            Mono<RefreshTokenDto> refreshTokenDTO,
            ServerWebExchange exchange
    ) {
        return refreshTokenDTO.flatMap(dto ->  tokenService.refreshUserJwt(dto)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token)));
    }
}
