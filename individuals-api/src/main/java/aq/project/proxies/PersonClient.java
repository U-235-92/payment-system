package aq.project.proxies;

import aq.project.dto.CreateUserRequest;
import aq.project.dto.IndividualDataResponse;
import aq.project.dto.UpdateUserRequest;
import aq.project.exceptions.CreateUserException;
import aq.project.exceptions.DeleteUserException;
import aq.project.exceptions.GetUserInfoException;
import aq.project.exceptions.UpdateUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PersonClient {

    private static final String BEARER = "Bearer ";

    @Value("${application.person-service.endpoints.create-person}")
    private String createPersonEndpoint;

    @Value("${application.person-service.endpoints.update-person}")
    private String updatePersonEndpoint;

    @Value("${application.person-service.endpoints.delete-person-by-keycloak-id}")
    private String deletePersonEndpoint;

    @Value("${application.person-service.endpoints.get-person-info-by-keycloak-id}")
    private String getPersonByIdEndpoint;

    @Autowired
    @Qualifier("personWebClient")
    private WebClient personWebClient;

    @Autowired
    private JwtClient jwtClient;

    public Mono<Void> createUser(CreateUserRequest createUserRequest, String keycloakUserId) {
        createUserRequest.setKeycloakUserId(keycloakUserId);
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.post()
                        .uri(createPersonEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .bodyValue(createUserRequest.getIndividualData())
                        .exchangeToMono(response ->  {
                            if(isErrorStatusCode(response.statusCode()))
                                return Mono.error(new CreateUserException("Error occurred during create user on person-service side. Try again later."));
                            return Mono.empty();
                        }));
    }

    public Mono<Void> updateUser(UpdateUserRequest updateUserRequest) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.patch()
                        .uri(updatePersonEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .bodyValue(updateUserRequest.getIndividualData())
                        .exchangeToMono(response ->  {
                            if(isErrorStatusCode(response.statusCode()))
                                return Mono.error(new UpdateUserException("Error occurred during update user on person-service side. Try again later."));
                            return Mono.empty();
                        }));
    }

    public Mono<Void> deleteUserByKeycloakId(String keycloakUserId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.delete()
                        .uri(deletePersonEndpoint + keycloakUserId)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .exchangeToMono(response ->  {
                            if(isErrorStatusCode(response.statusCode()))
                                return Mono.error(new DeleteUserException("Error occurred during delete user on person-service side. Try again later."));
                            return Mono.empty();
                        }));
    }

    public Mono<IndividualDataResponse> getUserInfoByKeycloakId(String keycloakUserId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> personWebClient.get()
                        .uri(getPersonByIdEndpoint + keycloakUserId)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .exchangeToMono(response ->  {
                            if(isErrorStatusCode(response.statusCode()))
                                return Mono.error(new GetUserInfoException("Error occurred during get user info on person-service side. Try again later."));
                            return response.bodyToMono(IndividualDataResponse.class);
                        }));
    }

    private boolean isErrorStatusCode(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }
}
