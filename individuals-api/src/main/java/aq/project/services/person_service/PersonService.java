package aq.project.services.person_service;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.*;
import aq.project.exceptions.FallbackOperationException;
import aq.project.person_service.PersonApiClient;
import aq.project.utils.mappers.person.PersonMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.utils.telemetry.TracePropagator.fetchTraceId;
import static aq.project.utils.telemetry.TracePropagator.genTraceId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    @Value("${spring.application.name}")
    private String serviceName;

    private final PersonMapper personMapper = PersonMapper.INSTANCE;

    private final PersonApiClient personApiClient;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    @RateLimiter(
            name = "login-person-rate-limiter",
            fallbackMethod = "loginFallback"
    )
    @CircuitBreaker(
            name = "login-person-circuitbreaker",
            fallbackMethod = "loginFallback"
    )
    public Mono<IndividualsApiServiceTokenResponseDto> loginPerson(
            IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto
    ) {
        String email = loginPersonRequestDto.getEmail();
        String password = loginPersonRequestDto.getPassword();

        return keycloakServiceClientFacade.loginPerson(email, password);
    }

    private Mono<IndividualsApiServiceTokenResponseDto> loginFallback(
            IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String email = loginPersonRequestDto.getEmail();
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, genTraceId());
            String action = "login-person-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while login person with email: [{}]. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, email, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    @RateLimiter(
            name = "create-person-rate-limiter",
            fallbackMethod = "createFallback"
    )
    @CircuitBreaker(
            name = "create-person-circuitbreaker",
            fallbackMethod = "createFallback"
    )
    public Mono<IndividualsApiServiceTokenResponseDto> createPerson(
            IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto
    ) {
        String email = createPersonRequestDto.getEmail();
        String password = createPersonRequestDto.getPassword();

        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> keycloakServiceClientFacade.createPerson(jwtHeader, createPersonRequestDto)
                                .doOnError(Mono::error)
                                .flatMap(personKeycloakId -> createPersonOnPersonService(traceId, jwtHeader, personKeycloakId, createPersonRequestDto)
                                        .onErrorResume(personServiceException -> keycloakServiceClientFacade.undoCreatePerson(jwtHeader, personKeycloakId)
                                                .doOnError(Mono::error)
                                                .then(Mono.error(personServiceException)))
                                        .then(keycloakServiceClientFacade.loginPerson(email, password)))));
    }

    private Mono<IndividualsApiServiceTokenResponseDto> createFallback(
            IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String email = createPersonRequestDto.getEmail();
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, genTraceId());
            String action = "create-person-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while create person with email: [{}]. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, email, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private Mono<Void> createPersonOnPersonService(
            String traceId,
            String authorization,
            UUID personKeycloakId,
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto
    ) {
        individualsApiServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = personMapper
                .toPersonServiceCreatePersonRequestDto(individualsApiServiceCreatePersonRequestDto);

        return personApiClient.createPerson(traceId, Mono.just(personServiceCreatePersonRequestDto), authorization)
                .flatMap(personServiceResponse -> Mono.empty())
                .then();
    }

    @RateLimiter(
            name = "update-person-rate-limiter",
            fallbackMethod = "updateFallback"
    )
    @CircuitBreaker(
            name = "update-person-circuitbreaker",
            fallbackMethod = "updateFallback"
    )
    public Mono<Void> updatePerson(
            UUID jwtPersonKeycloakId,
            IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto
    ) {
        return fetchTraceId()
                .flatMap(traceId -> checkValidPersonKeycloakId(jwtPersonKeycloakId, updatePersonRequestDto.getPersonKeycloakId(), "update-person")
                        .then(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                                .flatMap(jwtHeader -> updatePersonOnPersonService(traceId, jwtHeader, updatePersonRequestDto)
                                        .doOnError(Mono::error)
                                        .then(keycloakServiceClientFacade.updatePerson(jwtHeader, updatePersonRequestDto)
                                                .onErrorResume(keycloakServiceException -> undoUpdatePersonOnPersonService(traceId, jwtHeader, updatePersonRequestDto)
                                                        .doOnError(Mono::error)
                                                        .then(Mono.error(keycloakServiceException)))))))
                .then();
    }

    private Mono<Void> updateFallback(
            UUID jwtPersonKeycloakId,
            IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            UUID personKeycloakId = updatePersonRequestDto.getPersonKeycloakId();
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, genTraceId());
            String action = "update-person-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while update person with id: [{}]. " +
                    "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, personKeycloakId, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private Mono<Void> updatePersonOnPersonService(
            String traceId,
            String authorization,
            IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto
    ) {
        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = personMapper
                .toPersonServiceUpdatePersonRequestDto(updatePersonRequestDto);

        return personApiClient.updatePerson(traceId, Mono.just(personServiceUpdatePersonRequestDto), authorization)
                .then();
    }

    private Mono<HttpStatusCode> undoUpdatePersonOnPersonService(
            String traceId,
            String authorization,
            IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto
    ) {
        UUID personKeycloakId = updatePersonRequestDto.getPersonKeycloakId();

        String description = String.format("Undo update person with keycloak id: [%s]", personKeycloakId);

        PersonServiceUndoUpdatePersonRequestDto personServiceUndoRequestDto = getPersonServiceUndoUpdateRequestDto(
                personKeycloakId, description);

        return personApiClient.undoUpdatePerson(traceId, Mono.just(personServiceUndoRequestDto), authorization)
                .flatMap(personServiceResponse -> Mono.just(personServiceResponse.getStatusCode()));
    }

    private PersonServiceUndoUpdatePersonRequestDto getPersonServiceUndoUpdateRequestDto(
            UUID personKeycloakId,
            String description
    ) {
        PersonServiceUndoUpdatePersonRequestDto personServiceUndoRequestDto = new PersonServiceUndoUpdatePersonRequestDto();

        personServiceUndoRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceUndoRequestDto.setDescription(description);

        return personServiceUndoRequestDto;
    }

    @RateLimiter(
            name = "delete-person-rate-limiter",
            fallbackMethod = "deleteFallback"
    )
    @CircuitBreaker(
            name = "delete-person-circuitbreaker",
            fallbackMethod = "deleteFallback"
    )
    public Mono<Void> deletePerson(
            UUID jwtPersonKeycloakId,
            UUID requestPersonKeycloakId
    ) {
        return fetchTraceId()
                .flatMap(traceId -> checkValidPersonKeycloakId(jwtPersonKeycloakId, requestPersonKeycloakId, "delete-person")
                        .then(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                                .flatMap(jwtHeader -> personApiClient.deletePerson(requestPersonKeycloakId, traceId, jwtHeader)
                                        .doOnError(Mono::error)
                                        .then(keycloakServiceClientFacade.deletePerson(jwtHeader, requestPersonKeycloakId)
                                                .onErrorResume(keycloakServiceException -> undoDeletePersonOnPersonService(traceId, jwtHeader, requestPersonKeycloakId)
                                                        .doOnError(Mono::error)
                                                        .then(Mono.error(keycloakServiceException))))))
                .then());
    }

    private Mono<Void> deleteFallback(
            UUID jwtPersonKeycloakId,
            UUID requestPersonKeycloakId,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, genTraceId());
            String action = "delete-person-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while delete person with id: [{}]. " +
                    "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, requestPersonKeycloakId, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private Mono<HttpStatusCode> undoDeletePersonOnPersonService(
            String traceId,
            String authorization,
            UUID personKeycloakId
    ) {
        String description = String.format("Undo delete person with id: [%s]", personKeycloakId);

        PersonServiceUndoDeletePersonRequestDto personServiceUndoRequestDto = getPersonServiceUndoDeleteRequestDto(
                personKeycloakId, description);

        return personApiClient.undoDeletePerson(traceId, Mono.just(personServiceUndoRequestDto), authorization)
                .flatMap(personServiceResponse -> Mono.just(personServiceResponse.getStatusCode()));
    }

    private PersonServiceUndoDeletePersonRequestDto getPersonServiceUndoDeleteRequestDto(
            UUID personKeycloakId,
            String description
    ) {
        PersonServiceUndoDeletePersonRequestDto personServiceUndoRequestDto = new PersonServiceUndoDeletePersonRequestDto();

        personServiceUndoRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceUndoRequestDto.setDescription(description);

        return personServiceUndoRequestDto;
    }

    @RateLimiter(
            name = "get-person-info-rate-limiter",
            fallbackMethod = "getInfoFallback"
    )
    @CircuitBreaker(
            name = "get-person-info-circuitbreaker",
            fallbackMethod = "getInfoFallback"
    )
    public Mono<IndividualsApiServicePersonInfoResponseDto> getPersonInfo(
            Authentication authentication
    ) {
        return getIndividualsApiServicePersonInfoResponseDto(authentication)
                .flatMap(this::complementIndividualsApiServicePersonInfoResponseDto);
    }

    private Mono<IndividualsApiServicePersonInfoResponseDto> getInfoFallback(
            Authentication authentication,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, genTraceId());
            String action = "get-person-info-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while getting person info. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private Mono<IndividualsApiServicePersonInfoResponseDto> getIndividualsApiServicePersonInfoResponseDto(
            Authentication authentication
    ) {
        if(authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt))
            return Mono.error(new HttpClientErrorException(
                    HttpStatus.UNAUTHORIZED, "Access denied. Valid JWT required."));

        IndividualsApiServicePersonInfoResponseDto dto = new IndividualsApiServicePersonInfoResponseDto();

        dto.setPersonKeycloakId(UUID.fromString(jwt.getSubject()));
        dto.setEmail(jwt.getClaim("email"));
        dto.setRoles(getUserRoles(jwt));
        dto.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));

        return Mono.just(dto);
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserRoles(
            Jwt jwt
    ) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        return ((Map<String, List<Object>>) resourceAccess.get("account")).get("roles")
                .stream()
                .map(obj -> "ROLE_" + obj.toString().toUpperCase())
                .toList();
    }

    private Mono<IndividualsApiServicePersonInfoResponseDto> complementIndividualsApiServicePersonInfoResponseDto(
            IndividualsApiServicePersonInfoResponseDto individualsApiServicePersonInfoResponseDto
    ) {
        UUID personKeycloakId = individualsApiServicePersonInfoResponseDto.getPersonKeycloakId();

        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> getPersonServicePersonInfoResponseDto(traceId, jwtHeader, personKeycloakId)
                                .flatMap(personServicePersonInfoDto -> complementIndividualsApiServicePersonInfoResponseDto(individualsApiServicePersonInfoResponseDto, personServicePersonInfoDto)
                                        .then(Mono.just(individualsApiServicePersonInfoResponseDto)))));
    }

    private Mono<PersonServicePersonInfoResponseDto> getPersonServicePersonInfoResponseDto(
            String traceId,
            String authorization,
            UUID personKeycloakId
    ) {
        return personApiClient.getPersonInfo(personKeycloakId, traceId, authorization)
                .map(personServiceResponse -> personServiceResponse.getBody());
    }

    private Mono<Void> complementIndividualsApiServicePersonInfoResponseDto(
            IndividualsApiServicePersonInfoResponseDto individualsApiServicePersonInfoResponseDto,
            PersonServicePersonInfoResponseDto personServicePersonInfoResponseDto
    ) {
        personMapper.toIndividualsApiServicePersonInfoResponseDto(
                individualsApiServicePersonInfoResponseDto, personServicePersonInfoResponseDto);

        return Mono.empty();
    }

    private Mono<Void> checkValidPersonKeycloakId(
            UUID jwtPersonKeycloakId,
            UUID requestPersonKeycloakId,
            String operation
    ) {
        if(jwtPersonKeycloakId.equals(requestPersonKeycloakId))
            return Mono.empty();

        return Mono.error(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                String.format(
                        "Received request to [%s] that contains person keycloak id: [%s] " +
                        "which is not match with JWT sub claim " +
                        "that describes current authorized person with person keycloak id: [%s]",
                        requestPersonKeycloakId, operation, jwtPersonKeycloakId)
        ));
    }
}
