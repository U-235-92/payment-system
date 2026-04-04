package aq.project.services;

import aq.project.dto.*;
import aq.project.exceptions.ExternalServiceException;
import aq.project.exceptions.InvalidAccessTokenException;
import aq.project.exceptions.ServiceException;
import aq.project.proxies.KeycloakClient;
import aq.project.proxies.PersonClient;
import aq.project.util.http.HttpUtils;
import lombok.RequiredArgsConstructor;
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

    private final PersonClient personClient;
    private final KeycloakClient keycloakClient;

    public Mono<ResponseTokenDTO> createUser(CreateUserDTO createUserDTO) {
        return keycloakClient.createUser(createUserDTO)
                .flatMap(keycloakUserId -> personClient.createUser(createUserDTO.getIndividualData(), keycloakUserId)
                        .flatMap(personServiceResponse -> {
                            if(HttpUtils.isErrorStatusCode(personServiceResponse.getStatusCode()))
                                return keycloakClient.undoCreateUser(keycloakUserId)
                                        .flatMap(keyclaokClientHttpStatusCode -> {
                                            if(HttpUtils.isErrorStatusCode(keyclaokClientHttpStatusCode))
                                                return Mono.error(new ExternalServiceException("Error occurred during [undo-create] user on keycloak service side."));
                                            return Mono.empty();
                                        })
                                        .then(Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("create", personServiceResponse.getBody()))));
                            return Mono.empty();
                        }))
                .then(keycloakClient.loginUser(createUserDTO.getIndividualData().getEmail(), createUserDTO.getPassword()));
    }

    public Mono<ResponseTokenDTO> loginUser(LoginUserDTO loginUserDTO) {
        return keycloakClient.loginUser(loginUserDTO.getEmail(), loginUserDTO.getPassword());
    }

    public Mono<Void> updateUser(UpdateUserDTO updateUserDTO) {
        return personClient.updateUser(updateUserDTO.getIndividualData())
                .flatMap(personServiceResponse -> {
                    if(HttpUtils.isErrorStatusCode(personServiceResponse.getStatusCode()))
                        return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("update", personServiceResponse.getBody())));
                    return Mono.empty();
                })
                .then(keycloakClient.updateUser(updateUserDTO)
                        .flatMap(keycloakHttpResponseStatus -> {
                            if(HttpUtils.isErrorStatusCode(keycloakHttpResponseStatus))
                                return personClient.undoUpdateUser(updateUserDTO.getKeycloakUserId())
                                        .flatMap(personServiceResponse -> {
                                            if(HttpUtils.isErrorStatusCode(personServiceResponse.getStatusCode()))
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
                    if(HttpUtils.isErrorStatusCode(personServiceResponse.getStatusCode()))
                        return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("delete", personServiceResponse.getBody())));
                    return Mono.empty();
                })
                .then(keycloakClient.deleteUserByKeycloakId(keycloakId)
                        .flatMap(keycloakHttpResponseStatus -> {
                            if(HttpUtils.isErrorStatusCode(keycloakHttpResponseStatus))
                                return personClient.undoDeleteUserByKeycloakId(keycloakId)
                                        .flatMap(personServiceResponse -> {
                                            if(HttpUtils.isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("undo-delete", personServiceResponse.getBody())));
                                            return Mono.empty();
                                        })
                                        .then(Mono.error(new ServiceException(getIndividualsApiServiceCallExceptionMessage("delete"))));
                            return Mono.empty();
                        }));
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
        return personClient.getUserInfoByKeycloakId(userInfoResponseDTO.getKeycloakUserId())
                .flatMap(personServiceResponse -> {
                    if(HttpUtils.isErrorStatusCode(personServiceResponse.getStatusCode()))
                        return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("get-info", personServiceResponse.getBody().toString())));
                    userInfoResponseDTO.setIndividualData((IndividualDataResponseDTO) personServiceResponse.getBody());
                    return Mono.just(userInfoResponseDTO);
                });
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
}
