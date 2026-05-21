package aq.project.controllers;

import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenRestController {

    private final TokenService tokenService;

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<ResponseTokenDTO>> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        return tokenService.refreshToken(refreshTokenDTO)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }
}
