package aq.project.integration.mock;

import aq.project.dto.*;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static aq.project.util.TestDtoRepository.*;
import static aq.project.util.TestUtils.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "person-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateUserWithMockPersonServiceIntegrationTest {

    @Value("${application.person-service.endpoints.update-person}")
    private String personServiceUpdatePersonEndpoint;

    @Value("${application.individuals-api.endpoints.update-user}")
    private String individualsApiUpdateUserEndpoint;

    @Value("${application.individuals-api.test.actual-user-keycloak-id}")
    private String actualUserKeycloakId;

    @Value("${application.individuals-api.test.unknown-user-keycloak-id}")
    private String unknownUserKeycloakId;

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @InjectWireMock("person-service-mock")
    private WireMockServer personServiceMockServer;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("application.person-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successUpdateUserTest() {
        personServiceMockServer.stubFor(WireMock.patch(personServiceUpdatePersonEndpoint)
                .willReturn(WireMock.ok()));

        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO, actualUserKeycloakId);
        UpdateUserDTO updateUserDTO = getValidUpdateUserDTO(updateIndividualDataDTO, actualUserKeycloakId);

        WebClient webClient = getWebClient(port);

        Mono<ResponseEntity<Void>> responseEntityMono = loginUserMono(loginUserDTO, webClient)
                .flatMap(responseEntity -> webClient.patch()
                        .uri(individualsApiUpdateUserEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                        .bodyValue(updateUserDTO)
                        .exchangeToMono(response -> {
                            if(response.statusCode().is2xxSuccessful())
                                return Mono.just(ResponseEntity.ok().build());
                            return Mono.just(ResponseEntity.status(response.statusCode()).build());
                        }));

        StepVerifier.create(responseEntityMono)
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    public void failUpdateUserWithUnknownUserKeycloakIdTest() {
        personServiceMockServer.stubFor(WireMock.patch(personServiceUpdatePersonEndpoint)
                .willReturn(WireMock.badRequest()));

        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

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
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

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
        LoginUserDTO loginUserDTO = getLoginUserDTO("alexander@post.aq", "123");

        WebClient webClient = getWebClient(port);

        String accessToken = loginUserMono(loginUserDTO, webClient).block().getBody().getAccessToken();

        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO, actualUserKeycloakId);
        UpdateUserDTO updateUserDTO = getInvalidUpdateUserDtoWithNullFields(updateIndividualDataDTO, actualUserKeycloakId);

        webTestClient.patch()
                .uri(individualsApiUpdateUserEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateUserDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failUpdateUserWithNullUpdateIndividualDataDtoTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alexander@post.aq", "123");

        WebClient webClient = getWebClient(port);

        String accessToken = loginUserMono(loginUserDTO, webClient).block().getBody().getAccessToken();

        UpdateUserDTO updateUserDTO = getValidUpdateUserDTO(null, actualUserKeycloakId);

        webTestClient.patch()
                .uri(individualsApiUpdateUserEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateUserDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
