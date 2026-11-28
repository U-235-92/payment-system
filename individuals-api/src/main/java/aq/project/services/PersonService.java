package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.*;
import aq.project.exceptions.FallbackOperationException;
import aq.project.person_service.PersonApiClient;
import aq.project.utils.telemetry.TraceFactory;
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
import java.util.Objects;
import java.util.UUID;

import static aq.project.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.telemetry.TracePropagator.fetchTraceId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    @Value("${spring.application.name}")
    private String serviceName;

    private final PersonApiClient personClient;

    private final KeycloakServiceClientFacade keycloakClient;

    @RateLimiter(
            name = "login-person-rate-limiter",
            fallbackMethod = "loginPersonFallback"
    )
    @CircuitBreaker(
            name = "login-person-circuitbreaker",
            fallbackMethod = "loginPersonFallback"
    )
    public Mono<TokenResponseDto> loginPerson(
            LoginPersonRequestDto loginPersonRequestDto
    ) {
        String email = loginPersonRequestDto.getEmail();
        String password = loginPersonRequestDto.getPassword();

        return keycloakClient.loginPerson(email, password);
    }

    private Mono<TokenResponseDto> loginPersonFallback(
            LoginPersonRequestDto loginPersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String email = loginPersonRequestDto.getEmail();
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
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
            fallbackMethod = "createPersonFallback"
    )
    @CircuitBreaker(
            name = "create-person-circuitbreaker",
            fallbackMethod = "createPersonFallback"
    )
    public Mono<TokenResponseDto> createPerson(
            CreatePersonRequestDto createPersonRequestDto
    ) {
        String email = createPersonRequestDto.getEmail();
        String password = createPersonRequestDto.getPassword();

        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(jwtHeader -> keycloakClient.createPerson(jwtHeader, createPersonRequestDto)
                                .doOnError(Mono::error)
                                .flatMap(personKeycloakId -> createPersonOnPersonService(traceId, jwtHeader, personKeycloakId, createPersonRequestDto)
                                        .onErrorResume(personServiceException -> keycloakClient.undoCreatePerson(jwtHeader, personKeycloakId)
                                                .doOnError(Mono::error)
                                                .then(Mono.error(personServiceException)))
                                        .then(keycloakClient.loginPerson(email, password)))));
    }

    private Mono<TokenResponseDto> createPersonFallback(
            CreatePersonRequestDto createPersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String email = createPersonRequestDto.getEmail();
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
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
            CreatePersonRequestDto createPersonRequestDto
    ) {
        createPersonRequestDto.setPersonKeycloakId(personKeycloakId);

        return personClient
                .createPerson(Mono.just(createPersonRequestDto), traceId, authorization)
                .flatMap(personServiceResponse -> Mono.empty())
                .then();
    }

    @RateLimiter(
            name = "update-person-rate-limiter",
            fallbackMethod = "updatePersonFallback"
    )
    @CircuitBreaker(
            name = "update-person-circuitbreaker",
            fallbackMethod = "updatePersonFallback"
    )
    public Mono<Void> updatePerson(
            UUID jwtPersonKeycloakId,
            UpdatePersonRequestDto updatePersonRequestDto
    ) {
        return fetchTraceId()
                .flatMap(traceId -> checkValidPersonKeycloakId(jwtPersonKeycloakId, updatePersonRequestDto.getPersonKeycloakId(), "update-person")
                        .then(keycloakClient.getAdminBearerToken()
                                .flatMap(jwtHeader -> updatePersonOnPersonService(traceId, jwtHeader, updatePersonRequestDto)
                                        .doOnError(Mono::error)
                                        .then(keycloakClient.updatePerson(jwtHeader, updatePersonRequestDto)
                                                .onErrorResume(keycloakServiceException -> undoUpdatePersonOnPersonService(traceId, jwtHeader, updatePersonRequestDto)
                                                        .doOnError(Mono::error)
                                                        .then(Mono.error(keycloakServiceException)))))))
                .then();
    }

    private Mono<Void> updatePersonFallback(
            UUID jwtPersonKeycloakId,
            UpdatePersonRequestDto updatePersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            UUID personKeycloakId = updatePersonRequestDto.getPersonKeycloakId();
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
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
            UpdatePersonRequestDto updatePersonRequestDto
    ) {
        return personClient
                .updatePerson(Mono.just(updatePersonRequestDto), traceId, authorization)
                .then();
    }

    private Mono<HttpStatusCode> undoUpdatePersonOnPersonService(
            String traceId,
            String authorization,
            UpdatePersonRequestDto updatePersonRequestDto
    ) {
        UUID personKeycloakId = updatePersonRequestDto.getPersonKeycloakId();

        String description = String.format("Undo update person with keycloak id: [%s]", personKeycloakId);

        UndoUpdatePersonRequestDto undoRequestDto = getUndoUpdateRequestDto(personKeycloakId, description);

        return personClient
                .undoUpdatePerson(Mono.just(undoRequestDto), traceId, authorization)
                .flatMap(personServiceResponse -> Mono.just(personServiceResponse.getStatusCode()));
    }

    private UndoUpdatePersonRequestDto getUndoUpdateRequestDto(
            UUID personKeycloakId,
            String description
    ) {
        UndoUpdatePersonRequestDto undoUpdatePersonRequestDto = new UndoUpdatePersonRequestDto();

        undoUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        undoUpdatePersonRequestDto.setDescription(description);

        return undoUpdatePersonRequestDto;
    }

    @RateLimiter(
            name = "delete-person-rate-limiter",
            fallbackMethod = "deletePersonFallback"
    )
    @CircuitBreaker(
            name = "delete-person-circuitbreaker",
            fallbackMethod = "deletePersonFallback"
    )
    public Mono<Void> deletePerson(
            UUID jwtPersonKeycloakId,
            UUID requestPersonKeycloakId
    ) {
        return fetchTraceId()
                .flatMap(traceId -> checkValidPersonKeycloakId(jwtPersonKeycloakId, requestPersonKeycloakId, "delete-person")
                        .then(keycloakClient.getAdminBearerToken()
                                .flatMap(jwtHeader -> personClient.deletePerson(requestPersonKeycloakId, traceId, jwtHeader)
                                        .doOnError(Mono::error)
                                        .then(keycloakClient.deletePerson(jwtHeader, requestPersonKeycloakId)
                                                .onErrorResume(keycloakServiceException -> undoDeletePersonOnPersonService(traceId, jwtHeader, requestPersonKeycloakId)
                                                        .doOnError(Mono::error)
                                                        .then(Mono.error(keycloakServiceException))))))
                .then());
    }

    private Mono<Void> deletePersonFallback(
            UUID jwtPersonKeycloakId,
            UUID requestPersonKeycloakId,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
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

        UndoDeletePersonRequestDto undoDeletePersonRequestDto = getUndoDeleteRequestDto(personKeycloakId, description);

        return personClient
                .undoDeletePerson(Mono.just(undoDeletePersonRequestDto), traceId, authorization)
                .flatMap(personServiceResponse -> Mono.just(personServiceResponse.getStatusCode()));
    }

    private UndoDeletePersonRequestDto getUndoDeleteRequestDto(
            UUID personKeycloakId,
            String description
    ) {
        UndoDeletePersonRequestDto undoDeletePersonRequestDto = new UndoDeletePersonRequestDto();

        undoDeletePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        undoDeletePersonRequestDto.setDescription(description);

        return undoDeletePersonRequestDto;
    }

    @RateLimiter(
            name = "get-person-info-rate-limiter",
            fallbackMethod = "getPersonInfoFallback"
    )
    @CircuitBreaker(
            name = "get-person-info-circuitbreaker",
            fallbackMethod = "getPersonInfoFallback"
    )
    public Mono<PersonInfoResponseDto> getPersonInfo(
            Authentication authentication
    ) {
        return getPersonInfoResponseDto(authentication)
                .flatMap(this::complementPersonInfoResponseDto);
    }

    private Mono<PersonInfoResponseDto> getPersonInfoFallback(
            Authentication authentication,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
            String action = "get-person-info-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while getting person info. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private Mono<PersonInfoResponseDto> getPersonInfoResponseDto(
            Authentication authentication
    ) {
        if(authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt))
            return Mono.error(new HttpClientErrorException(
                    HttpStatus.UNAUTHORIZED, "Access denied. Valid JWT required."));

        PersonInfoResponseDto personInfoResponseDto = new PersonInfoResponseDto();

        personInfoResponseDto.setPersonKeycloakId(UUID.fromString(jwt.getSubject()));
        personInfoResponseDto.setEmail(jwt.getClaim("email"));
        personInfoResponseDto.setRoles(getUserRoles(jwt));
        personInfoResponseDto.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));

        return Mono.just(personInfoResponseDto);
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

    private Mono<PersonInfoResponseDto> complementPersonInfoResponseDto(
            PersonInfoResponseDto personInfoResponseDto
    ) {
        UUID personKeycloakId = personInfoResponseDto.getPersonKeycloakId();

        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> getPersonInfoResponseDto(traceId, authorization, personKeycloakId)
                                .then(Mono.just(personInfoResponseDto))));
    }

    private Mono<PersonInfoResponseDto> getPersonInfoResponseDto(
            String traceId,
            String authorization,
            UUID personKeycloakId
    ) {
        return personClient.getPersonInfo(personKeycloakId, traceId, authorization)
                .map(response -> Objects.requireNonNull(response.getBody()));
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