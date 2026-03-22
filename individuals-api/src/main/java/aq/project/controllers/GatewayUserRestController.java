package aq.project.controllers;

import aq.project.dto.*;
import aq.project.services.UserService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gateway/api/user")
public class GatewayUserRestController {

    private final UserService userService;

    @PostMapping("/create-user")
    @Timed(value = "individuals_api.create_user_time")
    public Mono<ResponseEntity<TokenResponse>> createUser(@RequestBody CreateUserEvent createUserEvent) {
        return userService.createUser(createUserEvent)
                .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token));
    }

    @PostMapping("/login-user")
    @Timed(value = "individuals_api.login_user_time")
    public Mono<ResponseEntity<TokenResponse>> loginUser(@RequestBody LoginUserEvent loginUserEvent) {
        return userService.loginUser(loginUserEvent)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }

    @PatchMapping("/update-user")
    @Timed(value = "individuals_api.update_user_time")
    public Mono<ResponseEntity<Void>> updateUser(@RequestBody UpdateUserEvent updateUserEvent) {
        return userService.updateUser(updateUserEvent)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @DeleteMapping("/delete-user-by-keycloak-id/{keycloakId}")
    @Timed(value = "individuals_api.delete_user_time")
    public Mono<ResponseEntity<Void>> deleteUserByKeycloakId(@PathVariable String keycloakId) {
        return userService.deleteUserByKeycloakId(keycloakId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @GetMapping("/get-user-info")
    @Timed(value = "individuals_api.get_user_info_time")
    public Mono<ResponseEntity<UserInfoResponse>> getUserInfo() {
        return userService.getIndividualDataResponseAndCombineWithUserInfoResponse()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @PostMapping("/refresh-token")
    @Timed(value = "individuals_api.refresh_token_time")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        return userService.refreshToken(refreshTokenDTO)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }
}


