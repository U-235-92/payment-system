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
    public Mono<IndividualsApiServiceTokenResponseDto> login(
            IndividualsApiServiceLoginPersonRequestDto individualsApiServiceLoginPersonRequestDto
    ) {
        String email = individualsApiServiceLoginPersonRequestDto.getEmail();
        String password = individualsApiServiceLoginPersonRequestDto.getPassword();

        return keycloakServiceClientFacade.loginPerson(email, password);
    }

    private Mono<IndividualsApiServiceTokenResponseDto> loginFallback(
            IndividualsApiServiceLoginPersonRequestDto individualsApiServiceLoginPersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String email = individualsApiServiceLoginPersonRequestDto.getEmail();
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
    public Mono<IndividualsApiServiceTokenResponseDto> create(
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto
    ) {
        String email = individualsApiServiceCreatePersonRequestDto.getEmail();
        String password = individualsApiServiceCreatePersonRequestDto.getPassword();

        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> keycloakServiceClientFacade.createPerson(jwtHeader, individualsApiServiceCreatePersonRequestDto)
                                .doOnError(Mono::error)
                                .flatMap(personKeycloakId -> createPersonOnPersonService(traceId, jwtHeader, personKeycloakId, individualsApiServiceCreatePersonRequestDto)
                                        .onErrorResume(personServiceException -> keycloakServiceClientFacade.undoCreatePerson(jwtHeader, personKeycloakId)
                                                .doOnError(Mono::error)
                                                .then(Mono.error(personServiceException)))
                                        .then(keycloakServiceClientFacade.loginPerson(email, password)))));
    }

    private Mono<IndividualsApiServiceTokenResponseDto> createFallback(
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String email = individualsApiServiceCreatePersonRequestDto.getEmail();
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
    public Mono<Void> update(
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> updatePersonOnPersonService(traceId, jwtHeader, individualsApiServiceUpdatePersonRequestDto)
                                .doOnError(Mono::error)
                                .then(keycloakServiceClientFacade.updatePerson(jwtHeader, individualsApiServiceUpdatePersonRequestDto)
                                        .onErrorResume(keycloakServiceException -> undoUpdatePersonOnPersonService(traceId, jwtHeader, individualsApiServiceUpdatePersonRequestDto)
                                                .doOnError(Mono::error)
                                                .then(Mono.error(keycloakServiceException))))))
                .then();
    }

    private Mono<Void> updateFallback(
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            UUID personKeycloakId = individualsApiServiceUpdatePersonRequestDto.getPersonKeycloakId();
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
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto
    ) {
        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = personMapper
                .toPersonServiceUpdatePersonRequestDto(individualsApiServiceUpdatePersonRequestDto);

        return personApiClient.updatePerson(traceId, Mono.just(personServiceUpdatePersonRequestDto), authorization)
                .then();
    }

    private Mono<HttpStatusCode> undoUpdatePersonOnPersonService(
            String traceId,
            String authorization,
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto
    ) {
        UUID personKeycloakId = individualsApiServiceUpdatePersonRequestDto.getPersonKeycloakId();

        UndoOperation undoOperation = UndoOperation.UPDATE_PERSON;

        String description = String.format(
                "Undo [%s] with id: [%s]", undoOperation.getValue().toLowerCase(), personKeycloakId);

        PersonServiceUndoRequestDto personServiceUndoRequestDto = getPersonServiceUndoRequestDto(
                personKeycloakId, undoOperation, description);

        return personApiClient.undoUpdatePerson(traceId, Mono.just(personServiceUndoRequestDto), authorization)
                .flatMap(personServiceResponse -> Mono.just(personServiceResponse.getStatusCode()));
    }

    @RateLimiter(
            name = "delete-person-rate-limiter",
            fallbackMethod = "deleteFallback"
    )
    @CircuitBreaker(
            name = "delete-person-circuitbreaker",
            fallbackMethod = "deleteFallback"
    )
    public Mono<Void> delete(
            UUID personKeycloakId
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> personApiClient.deletePersonByKeycloakId(personKeycloakId, traceId, jwtHeader)
                                .doOnError(Mono::error)
                                .then(keycloakServiceClientFacade.deletePerson(jwtHeader, personKeycloakId)
                                        .onErrorResume(keycloakServiceException -> undoDeletePersonOnPersonService(traceId, jwtHeader, personKeycloakId)
                                                .doOnError(Mono::error)
                                                .then(Mono.error(keycloakServiceException)))))
                .then());
    }

    private Mono<Void> deleteFallback(
            UUID personKeycloakId,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, genTraceId());
            String action = "delete-person-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while delete person with id: [{}]. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, personKeycloakId, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private Mono<HttpStatusCode> undoDeletePersonOnPersonService(
            String traceId,
            String authorization,
            UUID personKeycloakId
    ) {
        UndoOperation undoOperation = UndoOperation.DELETE_PERSON;

        String description = String.format(
                "Undo [%s] with id: [%s]", undoOperation.getValue().toLowerCase(), personKeycloakId);

        PersonServiceUndoRequestDto personServiceUndoRequestDto = getPersonServiceUndoRequestDto(
                personKeycloakId, undoOperation, description);

        return personApiClient.undoDeletePerson(traceId, Mono.just(personServiceUndoRequestDto), authorization)
                .flatMap(personServiceResponse -> Mono.just(personServiceResponse.getStatusCode()));
    }

    private PersonServiceUndoRequestDto getPersonServiceUndoRequestDto(
            UUID personKeycloakId,
            UndoOperation undoOperation,
            String description
    ) {
        PersonServiceUndoRequestDto personServiceUndoRequestDto = new PersonServiceUndoRequestDto();

        personServiceUndoRequestDto.setId(UUID.randomUUID());
        personServiceUndoRequestDto.setPersonId(personKeycloakId);
        personServiceUndoRequestDto.setOperation(undoOperation);
        personServiceUndoRequestDto.setTimestamp(System.currentTimeMillis());
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
    public Mono<IndividualsApiServicePersonInfoResponseDto> getInfo(
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
        if(authentication.getPrincipal() instanceof Jwt jwt) {
            IndividualsApiServicePersonInfoResponseDto dto = new IndividualsApiServicePersonInfoResponseDto();

            dto.setPersonKeycloakId(UUID.fromString(jwt.getSubject()));
            dto.setEmail(jwt.getClaim("email"));
            dto.setRoles(getUserRoles(jwt));
            dto.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));

            return Mono.just(dto);
        }
        String msg = "Access denied. Valid access token required. " +
                "The request must include [Authorization] header with [Bearer [access_token]] value";

        return Mono.error(new HttpClientErrorException(HttpStatus.BAD_REQUEST, msg));
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
        PersonServiceAddressDto addressDto = personServicePersonInfoResponseDto.getAddress();

        PersonServiceCountryDto countryDto = addressDto.getCountry();

        String firstName = personServicePersonInfoResponseDto.getFirstName();
        String lastName = personServicePersonInfoResponseDto.getLastName();
        String email = personServicePersonInfoResponseDto.getEmail();
        String phoneNumber = personServicePersonInfoResponseDto.getPhoneNumber();
        String passportNumber = personServicePersonInfoResponseDto.getPassportNumber();
        String countryName = countryDto.getName();
        String countryCode = countryDto.getCode();
        String stateName = addressDto.getState();
        String city = addressDto.getCity();
        String address = addressDto.getAddress();
        String zipCode = addressDto.getZipCode();

        individualsApiServicePersonInfoResponseDto.setFirstName(firstName);
        individualsApiServicePersonInfoResponseDto.setLastName(lastName);
        individualsApiServicePersonInfoResponseDto.setEmail(email);
        individualsApiServicePersonInfoResponseDto.setPhoneNumber(phoneNumber);
        individualsApiServicePersonInfoResponseDto.setPassportNumber(passportNumber);
        individualsApiServicePersonInfoResponseDto.setCountryName(countryName);
        individualsApiServicePersonInfoResponseDto.setCountryCode(countryCode);
        individualsApiServicePersonInfoResponseDto.setState(stateName);
        individualsApiServicePersonInfoResponseDto.setCity(city);
        individualsApiServicePersonInfoResponseDto.setAddress(address);
        individualsApiServicePersonInfoResponseDto.setZipCode(zipCode);

        return Mono.empty();
    }
}
