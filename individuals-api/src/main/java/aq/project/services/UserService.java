package aq.project.services;

import aq.project.dto.*;
import aq.project.exceptions.LackAccessTokenException;
import aq.project.proxies.KeycloakClient;
import aq.project.proxies.PersonClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PersonClient personClient;
    private final TokenService tokenService;
    private final KeycloakClient keycloakClient;

    public Mono<TokenResponse> createUser(CreateUserRequest createUserRequest) {
//        TODO: добавить операцию отката в сервисе individuals-api в случае ошибки [create] в person-service!
//        TODO: в случае ошибки - удалить созданного в Keycloak пользователя в сервисе individuals-api!
//        TODO: следует использовать паттерн transaction-outbox!
        return keycloakClient.createUser(createUserRequest)
                .flatMap(keycloakUserId -> personClient.createUser(createUserRequest, keycloakUserId))
                .then(tokenService.login(createUserRequest.getIndividualData().getEmail(), createUserRequest.getPassword()));
    }

    public Mono<TokenResponse> loginUser(LoginUserRequest loginUserRequest) {
        return keycloakClient.loginUser(loginUserRequest.getEmail(), loginUserRequest.getPassword());
    }

    public Mono<Void> updateUser(UpdateUserRequest updateUserRequest) {
//        TODO: добавить операцию отката в сервисе person-service в случае ошибки [update] в individuals-api!
//        TODO: в случае ошибки - откатить внесенные изменения в person-service на первом шаге!
//        TODO: следует использовать паттерн transaction-outbox!
        return personClient.updateUser(updateUserRequest)
                .then(keycloakClient.updateUser(updateUserRequest));
    }

    public Mono<Void> deleteUserByKeycloakId(String keycloakId) {
//        TODO: добавить операцию отката в сервисе person-service в случае ошибки [delete] в individuals-api!
//        TODO: в случае ошибки - откатить внесенные изменения в person-service на первом шаге!
//        TODO: следует использовать паттерн transaction-outbox!
        return personClient.deleteUserByKeycloakId(keycloakId)
                .then(keycloakClient.deleteUserByKeycloakId(keycloakId));
    }

    public Mono<UserInfoResponse> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> getUserInfo(Objects.requireNonNull(context.getAuthentication())))
                .switchIfEmpty(Mono.error(new LackAccessTokenException("Access denied. Valid access token required.")));
    }

    private Mono<UserInfoResponse> getUserInfo(Authentication authentication) {
        if(authentication.getPrincipal() instanceof Jwt jwt) {
            UserInfoResponse response = new UserInfoResponse();
            response.id(jwt.getSubject())
                    .email(jwt.getClaim("email"))
                    .roles(getUserRoles(jwt))
                    .created(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            return Mono.just(response);
        }
        return Mono.error(() -> new LackAccessTokenException("Access denied. Valid access token required."));
    }

    private List<String> getUserRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        return ((Map<String, List<Object>>) resourceAccess.get("account")).get("roles")
                .stream()
                .map(obj -> "ROLE_" + obj.toString().toUpperCase())
                .toList();
    }

    public Mono<TokenResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return tokenService.refreshToken(refreshTokenRequest);
    }
}
