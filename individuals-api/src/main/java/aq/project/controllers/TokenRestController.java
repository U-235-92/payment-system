package aq.project.controllers;

import aq.project.controller.TokenRestControllerApi;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TokenRestController implements TokenRestControllerApi {

    private final TokenService tokenService;

    @Override
    public Mono<ResponseEntity<ResponseTokenDTO>> refreshToken(Mono<RefreshTokenDTO> refreshTokenDTO, ServerWebExchange exchange) {
        return refreshTokenDTO.flatMap(dto ->  tokenService.refreshToken(dto)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token)));
    }
}
