package aq.project.controllers;

import aq.project.dto.*;
import aq.project.services.UserService;
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
    public Mono<ResponseEntity<TokenResponse>> createUser(@RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest)
                .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token));
    }

    @PostMapping("/login-user")
    public Mono<ResponseEntity<TokenResponse>> loginUser(@RequestBody LoginUserRequest loginUserRequest) {
        return userService.loginUser(loginUserRequest)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }

    @PatchMapping("/update-user")
    public Mono<ResponseEntity<Void>> updateUser(@RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUser(updateUserRequest)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @DeleteMapping("/delete-user-by-keycloak-id/{keycloakId}")
    public Mono<ResponseEntity<Void>> deleteUserByKeycloakId(@PathVariable String keycloakId) {
        return userService.deleteUserByKeycloakId(keycloakId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @GetMapping("/get-user-info")
    public Mono<ResponseEntity<UserInfoResponse>> getUserInfo() {
        return userService.getUserInfo()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return userService.refreshToken(refreshTokenRequest)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }
}


