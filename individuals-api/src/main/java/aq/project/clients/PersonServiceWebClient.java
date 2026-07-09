package aq.project.clients;

import aq.project.dto.CreateIndividualDataDTO;
import aq.project.dto.IndividualDataResponseDTO;
import aq.project.dto.UpdateIndividualDataDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface PersonServiceWebClient {

    @PostExchange(
            value = "${application.person-service.endpoints.create-person}",
            contentType = APPLICATION_JSON_VALUE
    )
    Mono<ResponseEntity<String>> createUser(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @RequestBody CreateIndividualDataDTO createIndividualDataDto
    );

    @PatchExchange(
            value = "${application.person-service.endpoints.update-person}",
            contentType = APPLICATION_JSON_VALUE
    )
    Mono<ResponseEntity<String>> updateUser(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @RequestBody UpdateIndividualDataDTO updateIndividualDataDto
    );

    @PostExchange(
            value = "${application.person-service.endpoints.undo-update-person}",
            contentType = APPLICATION_JSON_VALUE
    )
    Mono<ResponseEntity<String>> undoUpdateUser(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue
    );

    @DeleteExchange(
            value = "${application.person-service.endpoints.delete-person-by-keycloak-id}/{keycloakUserId}"
    )
    Mono<ResponseEntity<String>> deleteUserByKeycloakId(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("keycloakUserId") String keycloakUserId
    );

    @PostExchange(
            value = "${application.person-service.endpoints.undo-delete-person-by-keycloak-id}/{keycloakUserId}"
    )
    Mono<ResponseEntity<String>> undoDeleteUserByKeycloakId(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("keycloakUserId") String keycloakUserId
    );

    @GetExchange(
            value = "${application.person-service.endpoints.get-person-info-by-keycloak-id}/{keycloakUserId}"
    )
    Mono<ResponseEntity<IndividualDataResponseDTO>> getUserInfoByKeycloakId(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("keycloakUserId") String keycloakUserId
    );
}
