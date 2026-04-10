package aq.project.integration.end_to_end;

import aq.project.dto.*;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static aq.project.util.TestDtoRepository.*;
import static aq.project.util.TestUtils.*;

@Disabled
@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateUserEndToEndIntegrationTest {

    @Value("${application.individuals-api.endpoints.update-user}")
    private String individualsApiUpdateUserEndpoint;

    @Value("${application.individuals-api.test.actual-user-keycloak-id}")
    private String actualUserKeycloakId;

    @Value("${application.individuals-api.test.unknown-user-keycloak-id}")
    private String unknownUserKeycloakId;

    @LocalServerPort
    private int port;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @Container
    private static final GenericContainer<?> PERSON_SERVICE_CONTAINER = TestContainers.PersonService.PERSON_SERVICE_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry);
        TestApplicationProperties.PersonServiceProperties
                .registerApplicationContextContainerProperties(registry);
    }

    @Test
    public void successUpdateUserTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alice@post.aq", "123");

        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO, actualUserKeycloakId);
        UpdateUserDTO updateUserDTO = getValidUpdateUserDTO(updateIndividualDataDTO, actualUserKeycloakId);

        WebClient webClient = getWebClient(port);

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO, webClient)
                .flatMap(responseEntity -> webClient.patch()
                        .uri(individualsApiUpdateUserEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                        .bodyValue(updateUserDTO)
                        .exchangeToMono(response -> {
                            if(response.statusCode().is2xxSuccessful())
                                return Mono.just(ResponseEntity.ok().build());
                            return Mono.just(ResponseEntity.status(response.statusCode()).build());
                        }));
        StepVerifier.create(updateUserMono)
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    public void failUpdateUserWithUnknownUserKeycloakIdTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alice@post.aq", "123");

        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDtoWithUnknownKeycloakUserId(addressDTO, unknownUserKeycloakId);
        UpdateUserDTO updateUserDTO = getValidUpdateUserDtoWithUnknownKeycloakUserId(updateIndividualDataDTO, unknownUserKeycloakId);

        WebClient webClient = getWebClient(port);

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO, webClient)
                .flatMap(responseEntity -> webClient.patch()
                        .uri(individualsApiUpdateUserEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                        .bodyValue(updateUserDTO)
                        .exchangeToMono(response -> {
                            if(response.statusCode().is2xxSuccessful())
                                return Mono.just(ResponseEntity.ok().build());
                            return Mono.just(ResponseEntity.status(response.statusCode()).build());
                        }));
        StepVerifier.create(updateUserMono)
                .expectNextMatches(response -> response.getStatusCode().is4xxClientError());
    }

    @Test
    public void failUpdateUserWithNoMatchPasswordsOfUpdateUserDtoTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alice@post.aq", "123");

        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO, actualUserKeycloakId);
        UpdateUserDTO updateUserDTO = getInvalidUpdateUserDtoWithNoMatchPassword(updateIndividualDataDTO, actualUserKeycloakId);

        WebClient webClient = getWebClient(port);

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO, webClient)
                .flatMap(responseEntity -> webClient.patch()
                        .uri(individualsApiUpdateUserEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                        .bodyValue(updateUserDTO)
                        .exchangeToMono(response -> {
                            if(response.statusCode().is2xxSuccessful())
                                return Mono.just(ResponseEntity.ok().build());
                            return Mono.just(ResponseEntity.status(response.statusCode()).build());
                        }));
        StepVerifier.create(updateUserMono)
                .expectNextMatches(response -> response.getStatusCode().is4xxClientError());
    }

    @Test
    public void failUpdateUserWithNullFieldsOfUpdateUserDtoTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alice@post.aq", "123");

        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO, actualUserKeycloakId);
        UpdateUserDTO updateUserDTO = getInvalidUpdateUserDtoWithNullFields(updateIndividualDataDTO, actualUserKeycloakId);

        WebClient webClient = getWebClient(port);

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO, webClient)
                .flatMap(responseEntity -> webClient.patch()
                        .uri(individualsApiUpdateUserEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                        .bodyValue(updateUserDTO)
                        .exchangeToMono(response -> {
                            if(response.statusCode().is2xxSuccessful())
                                return Mono.just(ResponseEntity.ok().build());
                            return Mono.just(ResponseEntity.status(response.statusCode()).build());
                        }));
        StepVerifier.create(updateUserMono)
                .expectNextMatches(response -> response.getStatusCode().is4xxClientError())
                .verifyComplete();
    }

    @Test
    public void failUpdateUserWithNullUpdateIndividualDataDtoTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alice@post.aq", "123");

        UpdateUserDTO updateUserDTO = getValidUpdateUserDTO(null, actualUserKeycloakId);

        WebClient webClient = getWebClient(port);

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO, webClient)
                .flatMap(responseEntity -> webClient.patch()
                        .uri(individualsApiUpdateUserEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                        .bodyValue(updateUserDTO)
                        .exchangeToMono(response -> {
                            if(response.statusCode().is2xxSuccessful())
                                return Mono.just(ResponseEntity.ok().build());
                            return Mono.just(ResponseEntity.status(response.statusCode()).build());
                        }));
        StepVerifier.create(updateUserMono)
                .expectNextMatches(response -> response.getStatusCode().is4xxClientError())
                .verifyComplete();
    }
}
