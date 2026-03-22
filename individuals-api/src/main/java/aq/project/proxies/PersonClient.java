package aq.project.proxies;

import aq.project.dto.*;
import aq.project.exceptions.ExternalServiceException;
import aq.project.util.http.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class PersonClient {

    private static final String BEARER = "Bearer ";

    @Value("${application.person-service.endpoints.create-person}")
    private String createPersonEndpoint;

    @Value("${application.person-service.endpoints.update-person}")
    private String updatePersonEndpoint;

    @Value("${application.person-service.endpoints.undo-update-person}")
    private String undoUpdatePersonEndpoint;

    @Value("${application.person-service.endpoints.delete-person-by-keycloak-id}")
    private String deletePersonByKeycloakIdEndpoint;

    @Value("${application.person-service.endpoints.undo-delete-person-by-keycloak-id}")
    private String undoDeletePersonByKeycloakIdEndpoint;

    @Value("${application.person-service.endpoints.get-person-info-by-keycloak-id}")
    private String getPersonByKeycloakIdEndpoint;

    @Autowired
    @Qualifier("personWebClient")
    private WebClient personWebClient;
    @Autowired
    private JwtClient jwtClient;

    public Mono<ResponseEntity<String>> createUser(CreateIndividualDataEvent createIndividualDataEvent, String keycloakUserId) {
        createIndividualDataEvent.setKeycloakUserId(keycloakUserId);
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.post()
                        .uri(createPersonEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .bodyValue(createIndividualDataEvent)
                        .exchangeToMono(response ->  {
                            if(HttpUtil.isErrorStatusCode(response.statusCode()))
                                return getErrorResponseEntityMono(response);
                            return getOkResponseEntityMono();
                        }));
    }

    public Mono<ResponseEntity<String>> updateUser(UpdateIndividualDataEvent updateIndividualDataEvent) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.patch()
                        .uri(updatePersonEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .bodyValue(updateIndividualDataEvent)
                        .exchangeToMono(response ->  {
                            if(HttpUtil.isErrorStatusCode(response.statusCode()))
                                return getErrorResponseEntityMono(response);
                            return getOkResponseEntityMono();
                        }));
    }

    public Mono<ResponseEntity<String>> undoUpdateUser(String keycloakUserId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("person-keycloak-id", keycloakUserId);
        payload.put("timestamp", System.currentTimeMillis() + "");
        payload.put("description", "Individuals-API undo update person call");
        UndoOperationDTO undoOperationDTO = new UndoOperationDTO();
        undoOperationDTO.setOperation(UndoOperationDTO.OperationEnum.UNDO_UPDATE_PERSON);
        undoOperationDTO.setPayload(payload);
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.post()
                        .uri(undoUpdatePersonEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .bodyValue(undoOperationDTO)
                        .exchangeToMono(response ->  {
                            if(HttpUtil.isErrorStatusCode(response.statusCode()))
                                return getErrorResponseEntityMono(response);
                            return getOkResponseEntityMono();
                        }));
    }

    public Mono<ResponseEntity<String>> deleteUserByKeycloakId(String keycloakUserId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.delete()
                        .uri(deletePersonByKeycloakIdEndpoint + keycloakUserId)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .exchangeToMono(response ->  {
                            if(HttpUtil.isErrorStatusCode(response.statusCode()))
                                return getErrorResponseEntityMono(response);
                            return getOkResponseEntityMono();
                        }));
    }

    public Mono<ResponseEntity<String>> undoDeleteUserByKeycloakId(String keycloakUserId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.post()
                        .uri(undoDeletePersonByKeycloakIdEndpoint + keycloakUserId)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .exchangeToMono(response ->  {
                            if(HttpUtil.isErrorStatusCode(response.statusCode()))
                                return getErrorResponseEntityMono(response);
                            return getOkResponseEntityMono();
                        }));
    }

    public Mono<ResponseEntity<?>> getUserInfoByKeycloakId(String keycloakUserId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.get()
                        .uri(getPersonByKeycloakIdEndpoint + keycloakUserId)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .exchangeToMono(response ->  {
                            if(HttpUtil.isErrorStatusCode(response.statusCode()))
                                return response.bodyToMono(ErrorDTO.class)
                                        .map(errorDTO -> ResponseEntity.status(errorDTO.httpStatus()).body(errorDTO.message()));
                            return response.bodyToMono(IndividualDataResponse.class)
                                    .map(ResponseEntity::ok);
                        }));
    }

    private Mono<ResponseEntity<String>> getErrorResponseEntityMono(ClientResponse response) {
        return response.bodyToMono(ErrorDTO.class)
                .map(errorDTO -> ResponseEntity.status(errorDTO.httpStatus()).body(errorDTO.message()));
    }

    private Mono<ResponseEntity<String>> getOkResponseEntityMono() {
        return Mono.just(ResponseEntity.ok().build());
    }
}
