package aq.project.controllers;

import aq.project.dto.*;
import aq.project.exceptions.IncorrectUserCredentialsException;
import aq.project.exceptions.InvalidTokenException;
import aq.project.exceptions.LackAccessTokenException;
import aq.project.exceptions.UserExistsException;
import aq.project.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/registration")
    public Mono<ResponseEntity<TokenResponse>> createUser(@RequestBody UserRegistrationRequest request) throws UserExistsException {
        return userService.createUser(request)
                .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> requestToken(@RequestBody UserLoginRequest request) throws IncorrectUserCredentialsException {
        return userService.requestToken(request)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> updateToken(@RequestBody TokenRefreshRequest request) throws InvalidTokenException {
        return userService.refreshToken(request)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> getUserInfo() throws LackAccessTokenException {
        return userService.getUserInfo()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
}


