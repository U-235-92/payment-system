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
@RequestMapping("/v1/auth")
public class AuthRestControllerV1 {

    private final UserService userService;

    @PostMapping("/registration")
    public Mono<ResponseEntity<TokenResponse>> createUser(@RequestBody UserRegistrationRequest request) {
        return userService.createUser(request)
                .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> requestToken(@RequestBody UserLoginRequest request) {
        return userService.requestToken(request)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> updateToken(@RequestBody TokenRefreshRequest request) {
        return userService.refreshToken(request)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> getUserInfo() {
        return userService.getUserInfo()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
}


