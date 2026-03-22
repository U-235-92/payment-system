package aq.project.services;

import aq.project.dto.*;
import aq.project.exceptions.ExternalServiceException;
import aq.project.exceptions.LackAccessTokenException;
import aq.project.exceptions.ServiceException;
import aq.project.proxies.KeycloakClient;
import aq.project.proxies.PersonClient;
import aq.project.util.http.HttpUtil;
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

    public Mono<TokenResponse> createUser(CreateUserEvent createUserEvent) {
        return keycloakClient.createUser(createUserEvent)
                .flatMap(keycloakUserId -> personClient.createUser(createUserEvent.getIndividualData(), keycloakUserId)
                        .flatMap(personServiceResponse -> {
                            if(HttpUtil.isErrorStatusCode(personServiceResponse.getStatusCode()))
                                return keycloakClient.undoCreateUser(keycloakUserId)
                                        .flatMap(keyclaokClientHttpStatusCode -> {
                                            if(HttpUtil.isErrorStatusCode(keyclaokClientHttpStatusCode))
                                                return Mono.error(new ExternalServiceException("Error occurred during [undo-create] user on keycloak service side."));
                                            return Mono.empty();
                                        })
                                        .then(Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("create", personServiceResponse.getBody()))));
                            return Mono.empty();
                        }))
                .then(tokenService.login(createUserEvent.getIndividualData().getEmail(), createUserEvent.getPassword()));
    }

    public Mono<TokenResponse> loginUser(LoginUserEvent loginUserEvent) {
        return keycloakClient.loginUser(loginUserEvent.getEmail(), loginUserEvent.getPassword());
    }

    public Mono<Void> updateUser(UpdateUserEvent updateUserEvent) {
        return personClient.updateUser(updateUserEvent.getIndividualData())
                .flatMap(personServiceResponse -> {
                    if(HttpUtil.isErrorStatusCode(personServiceResponse.getStatusCode()))
                        return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("update", personServiceResponse.getBody())));
                    return Mono.empty();
                })
                .then(keycloakClient.updateUser(updateUserEvent)
                        .flatMap(keycloakHttpResponseStatus -> {
                            if(HttpUtil.isErrorStatusCode(keycloakHttpResponseStatus))
                                return personClient.undoUpdateUser(updateUserEvent.getKeycloakUserId())
                                        .flatMap(personServiceResponse -> {
                                            if(HttpUtil.isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("undo-update", personServiceResponse.getBody())));
                                            return Mono.empty();
                                        })
                                        .then(Mono.error(new ServiceException(getIndividualsApiServiceCallExceptionMessage("update"))));
                            return Mono.empty();
                        }));
    }

    public Mono<Void> deleteUserByKeycloakId(String keycloakId) {
        return personClient.deleteUserByKeycloakId(keycloakId)
                .flatMap(personServiceResponse -> {
                    if(HttpUtil.isErrorStatusCode(personServiceResponse.getStatusCode()))
                        return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("delete", personServiceResponse.getBody())));
                    return Mono.empty();
                })
                .then(keycloakClient.deleteUserByKeycloakId(keycloakId)
                        .flatMap(keycloakHttpResponseStatus -> {
                            if(HttpUtil.isErrorStatusCode(keycloakHttpResponseStatus))
                                return personClient.undoDeleteUserByKeycloakId(keycloakId)
                                        .flatMap(personServiceResponse -> {
                                            if(HttpUtil.isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("undo-delete", personServiceResponse.getBody())));
                                            return Mono.empty();
                                        })
                                        .then(Mono.error(new ServiceException(getIndividualsApiServiceCallExceptionMessage("delete"))));
                            return Mono.empty();
                        }));
    }

    public Mono<UserInfoResponse> getIndividualDataResponseAndCombineWithUserInfoResponse() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> getUserInfoResponseFromIncomingJwt(Objects.requireNonNull(context.getAuthentication()))
                        .flatMap(this::getIndividualDataResponseAndCombineWithUserInfoResponse))
                        .switchIfEmpty(Mono.error(new LackAccessTokenException(getLackAccessTokenExceptionMessage())));
    }

    private Mono<UserInfoResponse> getUserInfoResponseFromIncomingJwt(Authentication authentication) {
        if(authentication.getPrincipal() instanceof Jwt jwt) {
            UserInfoResponse response = new UserInfoResponse();
            response.keycloakUserId(jwt.getSubject())
                    .email(jwt.getClaim("email"))
                    .roles(getUserRoles(jwt))
                    .created(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            return Mono.just(response);
        }
        return Mono.error(() -> new LackAccessTokenException(getLackAccessTokenExceptionMessage()));
    }

    private List<String> getUserRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        return ((Map<String, List<Object>>) resourceAccess.get("account")).get("roles")
                .stream()
                .map(obj -> "ROLE_" + obj.toString().toUpperCase())
                .toList();
    }

    private Mono<UserInfoResponse> getIndividualDataResponseAndCombineWithUserInfoResponse(UserInfoResponse userInfoResponse) {
        return personClient.getUserInfoByKeycloakId(userInfoResponse.getKeycloakUserId())
                .flatMap(personServiceResponse -> {
                    if(HttpUtil.isErrorStatusCode(personServiceResponse.getStatusCode()))
                        return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("get-info", personServiceResponse.getBody().toString())));
                    userInfoResponse.setIndividualData((IndividualDataResponse) personServiceResponse.getBody());
                    return Mono.just(userInfoResponse);
                });
    }

    public Mono<TokenResponse> refreshToken(RefreshTokenDTO refreshTokenDTO) {
        return tokenService.refreshToken(refreshTokenDTO);
    }

    private String getPersonServiceCallExceptionMessage(String operation, String message) {
        return String.format("Error occurred during [%s user] on person-service side: %s", operation, message);
    }

    private String getIndividualsApiServiceCallExceptionMessage(String operation) {
        return String.format("Error occurred during [%s user] on individuals-api-service side. Check individuals-api logs and try again later.", operation);
    }

    private String getLackAccessTokenExceptionMessage() {
        return "Access denied. Valid access token required. " +
                "The request must include [Authorization] header with [Bearer [access_token]] value";
    }
}
