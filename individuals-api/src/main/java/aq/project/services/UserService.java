package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.*;
import aq.project.exceptions.ExternalServiceException;
import aq.project.exceptions.InvalidAccessTokenException;
import aq.project.exceptions.ServiceException;
import aq.project.person_service.PersonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static aq.project.dto.UndoOperationDto.OperationEnum.UNDO_DELETE_PERSON;
import static aq.project.dto.UndoOperationDto.OperationEnum.UNDO_UPDATE_PERSON;
import static aq.project.utils.constants.CustomConstants.*;
import static aq.project.utils.telemetry.TracePropagator.fetchTraceId;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PersonApiClient personApiClient;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    public Mono<ResponseTokenDto> createUser(
            CreateUserDto dto
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> keycloakServiceClientFacade.createUser(jwtHeader, dto)
                                .flatMap(keycloakUserId -> {
                                    dto.getIndividualData().setKeycloakUserId(keycloakUserId);
                                    return personApiClient.createPerson(xTraceId, Mono.just(dto.getIndividualData()), jwtHeader)
                                            .flatMap(personServiceResponse -> {
                                                if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                    return keycloakServiceClientFacade.undoCreateUser(jwtHeader, keycloakUserId)
                                                            .flatMap(keyclaokClientHttpStatusCode -> {
                                                                if(isErrorStatusCode(keyclaokClientHttpStatusCode))
                                                                    return Mono.error(new ExternalServiceException("Error occurred during [undo-create] user on keycloak service side."));
                                                                return Mono.empty();
                                                            })
                                                            .then(Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage("create", personServiceResponse.getBody()))));
                                                return Mono.empty();
                                            });
                                })
                                .then(keycloakServiceClientFacade.loginUser(dto.getIndividualData().getEmail(), dto.getPassword()))));
    }

    public Mono<ResponseTokenDto> loginUser(
            LoginUserDto dto
    ) {
        return keycloakServiceClientFacade.loginUser(dto.getEmail(), dto.getPassword());
    }

    public Mono<Void> updateUser(
            UpdateUserDto dto
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> personApiClient.updatePerson(xTraceId, Mono.just(dto.getIndividualData()), jwtHeader))
                                .flatMap(personServiceResponse -> {
                                    if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                        return Mono.error(new ExternalServiceException(
                                                getPersonServiceCallExceptionMessage(dto.getKeycloakUserId(), "update", null)));
                                    return Mono.empty();
                                })
                                .then(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                                        .flatMap(jwtHeader -> keycloakServiceClientFacade.updateUser(jwtHeader, dto)
                                                .flatMap(keycloakHttpResponseStatus -> {
                                                    if(isErrorStatusCode(keycloakHttpResponseStatus))
                                                        return personApiClient.undoUpdatePerson(xTraceId, Mono.just(getUndoOperationDto(dto.getKeycloakUserId(), UNDO_UPDATE_PERSON)), jwtHeader)
                                                                .flatMap(personServiceResponse -> {
                                                                    if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                                        return Mono.error(new ExternalServiceException(
                                                                                getPersonServiceCallExceptionMessage(dto.getKeycloakUserId(), "undo-update", null)));
                                                                    return Mono.empty();
                                                                })
                                                                .then(Mono.error(new ServiceException(
                                                                        getIndividualsApiServiceCallExceptionMessage(dto.getKeycloakUserId(), "update"))));
                                                    return Mono.empty();
                                                })
                                        )
                                )
                );
    }

    public Mono<Void> deleteUserByKeycloakId(
            String keycloakId
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> personApiClient.deletePersonByKeycloakId(keycloakId, xTraceId, jwtHeader))
                        .flatMap(personServiceResponse -> {
                            if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                return Mono.error(new ExternalServiceException(
                                        getPersonServiceCallExceptionMessage(keycloakId, "delete", null)));
                            return Mono.empty();
                        })
                        .then(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                                .flatMap(jwtHeader -> keycloakServiceClientFacade.deleteUserByKeycloakId(jwtHeader, keycloakId)
                                        .flatMap(keycloakHttpResponseStatus -> {
                                            if(isErrorStatusCode(keycloakHttpResponseStatus))
                                                return personApiClient.undoDeletePerson(xTraceId, Mono.just(getUndoOperationDto(keycloakId, UNDO_DELETE_PERSON)), jwtHeader)
                                                        .flatMap(personServiceResponse -> {
                                                            if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                                                return Mono.error(new ExternalServiceException(getPersonServiceCallExceptionMessage(keycloakId, "undo-delete", null)));
                                                            return Mono.empty();
                                                        })
                                                        .then(Mono.error(new ServiceException(getIndividualsApiServiceCallExceptionMessage(keycloakId, "delete"))));
                                            return Mono.empty();
                                        })
                                )
                        )
                );
    }

    public Mono<UserInfoResponseDto> getUserInfoResponseDTO(
            Authentication authentication
    ) {
        return getUserInfoResponseFromIncomingJwt(authentication)
                    .flatMap(this::complementUserInfoResponseDtoByIndividualDataResponseDto)
                    .switchIfEmpty(Mono.error(new InvalidAccessTokenException(getInvalidAccessTokenExceptionMessage())));
    }

    private Mono<UserInfoResponseDto> getUserInfoResponseFromIncomingJwt(
            Authentication authentication
    ) {
        if(authentication.getPrincipal() instanceof Jwt jwt) {
            UserInfoResponseDto response = new UserInfoResponseDto();
            response.keycloakUserId(jwt.getSubject())
                    .email(jwt.getClaim("email"))
                    .roles(getUserRoles(jwt))
                    .created(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            return Mono.just(response);
        }
        return Mono.error(() -> new InvalidAccessTokenException(getInvalidAccessTokenExceptionMessage()));
    }

    private List<String> getUserRoles(
            Jwt jwt
    ) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        return ((Map<String, List<Object>>) resourceAccess.get("account")).get("roles")
                .stream()
                .map(obj -> "ROLE_" + obj.toString().toUpperCase())
                .toList();
    }

    private Mono<UserInfoResponseDto> complementUserInfoResponseDtoByIndividualDataResponseDto(
            UserInfoResponseDto dto
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> personApiClient.getPersonByKeycloakId(dto.getKeycloakUserId(), xTraceId, jwtHeader)
                                .flatMap(personServiceResponse -> {
                                    if(isErrorStatusCode(personServiceResponse.getStatusCode()))
                                        return Mono.error(new ExternalServiceException(
                                                getPersonServiceCallExceptionMessage(dto.getKeycloakUserId(), "get-info", personServiceResponse.getBody().toString())));
                                    dto.setIndividualData(personServiceResponse.getBody());
                                    return Mono.just(dto);
                                })
                        )
                );
    }

    private UndoOperationDto getUndoOperationDto(
            String personKeycloakId,
            UndoOperationDto.OperationEnum operation
    ) {
        String timestamp = Long.toString(System.currentTimeMillis());
        String description = String.format("Undo [%s] with id: [%s]", operation.getValue().toLowerCase(), personKeycloakId);
        return new UndoOperationDto()
                .id(UUID.randomUUID().toString())
                .operation(operation)
                .putPayloadItem(UNDO_OPERATION_PERSON_ID, personKeycloakId)
                .putPayloadItem(UNDO_OPERATION_TIMESTAMP, timestamp)
                .putPayloadItem(UNDO_OPERATION_DESCRIPTION, description);
    }

    private String getPersonServiceCallExceptionMessage(
            String operation,
            String message
    ) {
        return (message == null || message.isBlank())
                ? String.format("Error occurred during [%s user] on person-service side",
                    operation)
                : String.format("Error occurred during [%s user] on person-service side. Description: %s",
                    operation, message);
    }

    private String getPersonServiceCallExceptionMessage(
            String personId,
            String operation,
            String message
    ) {
        return (message == null || message.isBlank())
                ? String.format("Error occurred during [%s user] with id: [%s] on person-service side",
                    operation, personId)
                : String.format("Error occurred during [%s user] with id: [%s] on person-service side. Description: %s",
                    operation, personId, message);
    }

    private String getIndividualsApiServiceCallExceptionMessage(
            String personId,
            String operation
    ) {
        return String.format("Error occurred during [%s user] with id: [%s] on individuals-api-service side",
                operation, personId);
    }

    private String getInvalidAccessTokenExceptionMessage() {
        return "Access denied. Valid access token required. " +
                "The request must include [Authorization] header with [Bearer [access_token]] value";
    }

    private boolean isErrorStatusCode(
            HttpStatusCode statusCode
    ) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }
}