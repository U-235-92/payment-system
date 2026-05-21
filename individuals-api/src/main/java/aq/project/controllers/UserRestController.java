package aq.project.controllers;

import aq.project.dto.*;
import aq.project.services.UserService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gateway/api/user")
public class UserRestController {

    private final UserService userService;

    @PostMapping("/create-user")
    public Mono<ResponseEntity<ResponseTokenDTO>> createUser(@RequestBody CreateUserDTO createUserDTO) {
        return userService.createUser(createUserDTO)
                .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token));
    }

    @PostMapping("/login-user")
    public Mono<ResponseEntity<ResponseTokenDTO>> loginUser(@RequestBody LoginUserDTO loginUserDTO) {
        return userService.loginUser(loginUserDTO)
                .map(token -> ResponseEntity.status(HttpStatus.OK).body(token));
    }

    @PatchMapping("/update-user")
    public Mono<ResponseEntity<Void>> updateUser(@RequestBody UpdateUserDTO updateUserDTO) {
        return userService.updateUser(updateUserDTO)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @DeleteMapping("/delete-user-by-keycloak-id/{keycloakId}")
    public Mono<ResponseEntity<Void>> deleteUserByKeycloakId(@PathVariable String keycloakId) {
        return userService.deleteUserByKeycloakId(keycloakId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @GetMapping("/get-user-info")
    public Mono<ResponseEntity<UserInfoResponseDTO>> getUserInfo(Authentication authentication) {
        return userService.getUserInfoResponseDTO(authentication)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
}


