package aq.project.controllers.person_controller;

import aq.project.controller.PersonRestControllerApi;
import aq.project.dto.*;
import aq.project.services.person_service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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
                .flatMap(requestDto -> personService.create(requestDto)
                        .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> deletePerson(
            UUID personKeycloakId,
            ServerWebExchange exchange
    ) {
        return personService.delete(personKeycloakId)
                .flatMap(responseDto -> Mono.just(ResponseEntity.ok(responseDto)));
    }

    @Override
    public Mono<ResponseEntity<IndividualsApiServiceTokenResponseDto>> loginPerson(
            Mono<IndividualsApiServiceLoginPersonRequestDto> individualsApiServiceLoginPersonRequestDtoMono,
            ServerWebExchange exchange
    ) {
        return individualsApiServiceLoginPersonRequestDtoMono
                .flatMap(requestDto -> personService.login(requestDto)
                        .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> updatePerson(
            Mono<IndividualsApiServiceUpdatePersonRequestDto> individualsApiServiceUpdatePersonRequestDtoMono,
            ServerWebExchange exchange
    ) {
        return individualsApiServiceUpdatePersonRequestDtoMono
                .flatMap(requestDto -> personService.update(requestDto)
                        .map(ResponseEntity::ok));
    }

    @GetMapping("/api/v1/person/get-person-info")
    public Mono<ResponseEntity<IndividualsApiServicePersonInfoResponseDto>> getPersonInfo(
            Authentication authentication,
            ServerWebExchange exchange
    ) {
        return personService.getInfo(authentication).map(ResponseEntity::ok);
    }
}
