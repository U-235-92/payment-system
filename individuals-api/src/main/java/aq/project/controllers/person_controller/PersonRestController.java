package aq.project.controllers.person_controller;

import aq.project.controller.PersonRestControllerApi;
import aq.project.dto.*;
import aq.project.services.person_service.PersonService;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PersonRestController implements PersonRestControllerApi {

    private final PersonService personService;

    @Override
    public Mono<ResponseEntity<IndividualsApiServiceTokenResponseDto>> createPerson(
            Mono<IndividualsApiServiceCreatePersonRequestDto> individualsApiServiceCreatePersonRequestDtoMono,
            ServerWebExchange exchange
    ) {
        return individualsApiServiceCreatePersonRequestDtoMono
                .flatMap(requestDto -> personService.createPerson(requestDto)
                        .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> deletePerson(
            UUID requestPersonKeycloakId,
            ServerWebExchange exchange
    ) {
        try {
            UUID jwtPersonKeycloakId = getJwtPersonKeycloakId(exchange);

            return personService.deletePerson(jwtPersonKeycloakId, requestPersonKeycloakId)
                    .flatMap(responseDto -> Mono.just(ResponseEntity.ok(responseDto)));

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    public Mono<ResponseEntity<IndividualsApiServiceTokenResponseDto>> loginPerson(
            Mono<IndividualsApiServiceLoginPersonRequestDto> individualsApiServiceLoginPersonRequestDtoMono,
            ServerWebExchange exchange
    ) {
        return individualsApiServiceLoginPersonRequestDtoMono
                .flatMap(requestDto -> personService.loginPerson(requestDto)
                        .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> updatePerson(
            Mono<IndividualsApiServiceUpdatePersonRequestDto> updatePersonRequestDtoMono,
            ServerWebExchange exchange
    ) {
        try {
            UUID jwtPersonKeycloakId = getJwtPersonKeycloakId(exchange);

            return updatePersonRequestDtoMono
                    .flatMap(requestDto -> personService
                            .updatePerson(jwtPersonKeycloakId, requestDto)
                            .map(ResponseEntity::ok));

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @GetMapping("/api/v1/person/get-person-info")
    public Mono<ResponseEntity<IndividualsApiServicePersonInfoResponseDto>> getPersonInfo(
            Authentication authentication
    ) {
        return personService.getPersonInfo(authentication)
                .map(ResponseEntity::ok);
    }

    private UUID getJwtPersonKeycloakId(
            ServerWebExchange exchange
    ) throws Exception {
        String jwt = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if(jwt != null && jwt.startsWith("Bearer "))
            return UUID.fromString(JWTParser.parse(jwt.substring(7))
                    .getJWTClaimsSet()
                    .getSubject());

        else
            throw new HttpClientErrorException(
                    HttpStatus.UNAUTHORIZED,
                    "Received request to update person with invalid Authorization header value");
    }
}
