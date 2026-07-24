package aq.project.controllers;

import aq.project.controller.UserRestControllerApi;
import aq.project.dto.*;
import aq.project.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class UserRestController implements UserRestControllerApi {

    private final UserService userService;

    @Override
    public Mono<ResponseEntity<ResponseTokenDto>> createUser(
            Mono<CreateUserDto> createUserDTO,
            ServerWebExchange exchange
    ) {
        return createUserDTO.flatMap(dto -> userService.createUser(dto)
                .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token)));
    }

    @Override
    public Mono<ResponseEntity<ResponseTokenDto>> loginUser(
            Mono<LoginUserDto> loginUserDTO,
            ServerWebExchange exchange
    ) {
        return loginUserDTO.flatMap(dto -> userService.loginUser(dto)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token)));
    }

    @Override
    public Mono<ResponseEntity<Void>> updateUser(
            Mono<UpdateUserDto> updateUserDTO,
            ServerWebExchange exchange
    ) {
        return updateUserDTO.flatMap(dto -> userService.updateUser(dto)
                .then(Mono.just(ResponseEntity.ok().build())));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteUserByKeycloakId(
            String keycloakId,
            ServerWebExchange exchange
    ) {
        return userService.deleteUserByKeycloakId(keycloakId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @GetMapping("/api/v1/user/get-user-info")
    public Mono<ResponseEntity<UserInfoResponseDto>> getUserInfo(
            Authentication authentication,
            ServerWebExchange exchange
    ) {
        return userService.getUserInfoResponseDTO(authentication)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
}


