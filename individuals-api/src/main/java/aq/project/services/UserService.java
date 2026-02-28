package aq.project.services;

import aq.project.dto.*;
import aq.project.exceptions.LackAccessTokenException;
import aq.project.proxies.KeycloakClient;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Service
public class UserService {

    private final TokenService tokenService;
    private final KeycloakClient keycloakClient;

    public Mono<TokenResponse> createUser(UserRegistrationRequest request) {
        return keycloakClient.createUser(request)
                .then(tokenService.login(request.getEmail(), request.getPassword()));
    }

    public Mono<TokenResponse> login(UserLoginRequest request) {
        return keycloakClient.login(request.getEmail(), request.getPassword());
    }

    public Mono<TokenResponse> refreshToken(TokenRefreshRequest request) {
        return tokenService.refreshToken(request);
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
                    .roles(getRoles(jwt))
                    .createdAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            return Mono.just(response);
        }
        return Mono.error(() -> new LackAccessTokenException("Access denied. Valid access token required."));
    }

    private List<String> getRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        return ((Map<String, List<Object>>) resourceAccess.get("account")).get("roles")
                .stream()
                .map(obj -> "ROLE_" + obj.toString().toUpperCase())
                .toList();
    }
}
