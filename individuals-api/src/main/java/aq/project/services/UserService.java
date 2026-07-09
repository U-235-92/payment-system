package aq.project.services;

import aq.project.clients.KeycloakServiceWebClientFacade;
import aq.project.clients.PersonServiceWebClient;
import aq.project.dto.*;
import aq.project.exceptions.ExternalServiceException;
import aq.project.exceptions.InvalidAccessTokenException;
import aq.project.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PersonServiceWebClient personServiceWebClient;

    private final KeycloakServiceWebClientFacade keycloakServiceWebClientFacade;

    public Mono<ResponseTokenDTO> createUser(CreateUserDTO createUserDTO) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> keycloakServiceWebClientFacade.createUser(jwt, createUserDTO)
                                .flatMap(keycloakUserId -> {
                                    createUserDTO.getIndividualData().setKeycloakUserId(keycloakUserId);
                                    return personServiceWebClient.createUser(jwt, createUserDTO.getIndividualData())
                                            .flatMap(personServiceResponse -> {
                                                if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                    return keycloakServiceWebClientFacade.undoCreateUser(jwt, keycloakUserId)
                                                            .flatMap(keyclaokClientHttpStatusCode -> {
                                                                if(isErrorStatusCode(keyclaokClientHttpStatusCode))
                                                                    return Mono.error(new ExternalServiceException("Error occurred during [undo-create] user on keycloak service side."));
                                                                return Mono.empty();
                                                            })
                                                            .then(Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("create", personServiceResponse.getBody()))));
                                                return Mono.empty();
                                            });
                                })
                                .then(keycloakServiceWebClientFacade.loginUser(createUserDTO.getIndividualData().getEmail(), createUserDTO.getPassword())));
    }

    public Mono<ResponseTokenDTO> loginUser(LoginUserDTO loginUserDTO) {
        return keycloakServiceWebClientFacade.loginUser(loginUserDTO.getEmail(), loginUserDTO.getPassword());
    }

    public Mono<Void> updateUser(UpdateUserDTO updateUserDTO) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> personServiceWebClient.updateUser(jwt, updateUserDTO.getIndividualData())
                    .flatMap(personServiceResponse -> {
                        if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                            return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("update", personServiceResponse.getBody())));
                        return Mono.empty();
                    })
                    .then(keycloakServiceWebClientFacade.updateUser(jwt, updateUserDTO)
                            .flatMap(keycloakHttpResponseStatus -> {
                                if(isErrorStatusCode(keycloakHttpResponseStatus))
                                    return personServiceWebClient.undoUpdateUser(updateUserDTO.getKeycloakUserId())
                                            .flatMap(personServiceResponse -> {
                                                if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                    return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("undo-update", personServiceResponse.getBody())));
                                                return Mono.empty();
                                            })
                                            .then(Mono.error(new ServiceException(getIndividualsApiServiceCallExceptionMessage("update"))));
                                return Mono.empty();
                            })));
    }

    public Mono<Void> deleteUserByKeycloakId(String keycloakId) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> personServiceWebClient.deleteUserByKeycloakId(jwt, keycloakId)
                        .flatMap(personServiceResponse -> {
                            if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("delete", personServiceResponse.getBody())));
                            return Mono.empty();
                        })
                        .then(keycloakServiceWebClientFacade.deleteUserByKeycloakId(jwt, keycloakId)
                                .flatMap(keycloakHttpResponseStatus -> {
                                    if(isErrorStatusCode(keycloakHttpResponseStatus))
                                        return personServiceWebClient.undoDeleteUserByKeycloakId(jwt, keycloakId)
                                                .flatMap(personServiceResponse -> {
                                                    if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                        return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("undo-delete", personServiceResponse.getBody())));
                                                    return Mono.empty();
                                                })
                                                .then(Mono.error(new ServiceException(getIndividualsApiServiceCallExceptionMessage("delete"))));
                                    return Mono.empty();
                                })));
    }

    public Mono<UserInfoResponseDTO> getUserInfoResponseDTO(Authentication authentication) {
        return getUserInfoResponseFromIncomingJwt(authentication)
                    .flatMap(this::complementUserInfoResponseDtoByIndividualDataResponseDto)
                    .switchIfEmpty(Mono.error(new InvalidAccessTokenException(getInvalidAccessTokenExceptionMessage())));
    }

    private Mono<UserInfoResponseDTO> getUserInfoResponseFromIncomingJwt(Authentication authentication) {
        if(authentication.getPrincipal() instanceof Jwt jwt) {
            UserInfoResponseDTO response = new UserInfoResponseDTO();
            response.keycloakUserId(jwt.getSubject())
                    .email(jwt.getClaim("email"))
                    .roles(getUserRoles(jwt))
                    .created(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            return Mono.just(response);
        }
        return Mono.error(() -> new InvalidAccessTokenException(getInvalidAccessTokenExceptionMessage()));
    }

    private List<String> getUserRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        return ((Map<String, List<Object>>) resourceAccess.get("account")).get("roles")
                .stream()
                .map(obj -> "ROLE_" + obj.toString().toUpperCase())
                .toList();
    }

    private Mono<UserInfoResponseDTO> complementUserInfoResponseDtoByIndividualDataResponseDto(UserInfoResponseDTO userInfoResponseDTO) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> personServiceWebClient.getUserInfoByKeycloakId(jwt, userInfoResponseDTO.getKeycloakUserId())
                        .flatMap(personServiceResponse -> {
                            if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("get-info", personServiceResponse.getBody().toString())));
                            userInfoResponseDTO.setIndividualData((IndividualDataResponseDTO) personServiceResponse.getBody());
                            return Mono.just(userInfoResponseDTO);
                        }));
    }

    private String getPersonServiceCallExceptionMessage(String operation, String message) {
        return String.format("Error occurred during [%s user] on person-service side: %s", operation, message);
    }

    private String getIndividualsApiServiceCallExceptionMessage(String operation) {
        return String.format("Error occurred during [%s user] on individuals-api-service side. Check individuals-api logs and try again later.", operation);
    }

    private String getInvalidAccessTokenExceptionMessage() {
        return "Access denied. Valid access token required. " +
                "The request must include [Authorization] header with [Bearer [access_token]] value";
    }

    private boolean isErrorStatusCode(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }
}
