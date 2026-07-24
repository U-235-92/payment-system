package aq.project.integration.user;

import aq.project.dto.*;
import aq.project.services.UserService;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Assertions;
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
import static aq.project.util.TestUtils.getWebClient;
import static aq.project.util.TestUtils.loginUserMono;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "person-service"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateUserIntegrationTest {

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

    @Autowired
    private UserService userService;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @InjectWireMock("person-service")
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

        CountryDto countryDTO = getValidCountryDTO();
        AddressDto addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDto updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO, actualUserKeycloakId);
        UpdateUserDto updateUserDTO = getValidUpdateUserDTO(updateIndividualDataDTO, actualUserKeycloakId);

        Assertions.assertDoesNotThrow(() -> userService.updateUser(updateUserDTO));
    }

    @Test
    public void failUpdateUserWithUnknownUserKeycloakIdTest() {
        personServiceMockServer.stubFor(WireMock.patch(personServiceUpdatePersonEndpoint)
                .willReturn(WireMock.badRequest()));

        LoginUserDto loginUserDTO = new LoginUserDto();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

        CountryDto countryDTO = getValidCountryDTO();
        AddressDto addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDto updateIndividualDataDTO = getValidUpdateIndividualDataDtoWithUnknownKeycloakUserId(addressDTO, unknownUserKeycloakId);
        UpdateUserDto updateUserDTO = getValidUpdateUserDtoWithUnknownKeycloakUserId(updateIndividualDataDTO, unknownUserKeycloakId);

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
        LoginUserDto loginUserDTO = new LoginUserDto();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

        CountryDto countryDTO = getValidCountryDTO();
        AddressDto addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDto updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO, actualUserKeycloakId);
        UpdateUserDto updateUserDTO = getInvalidUpdateUserDtoWithNoMatchPassword(updateIndividualDataDTO, actualUserKeycloakId);

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
        LoginUserDto loginUserDTO = getLoginUserDTO("alexander@post.aq", "123");

        WebClient webClient = getWebClient(port);

        String accessToken = loginUserMono(loginUserDTO, webClient).block().getBody().getAccessToken();

        CountryDto countryDTO = getValidCountryDTO();
        AddressDto addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDto updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO, actualUserKeycloakId);
        UpdateUserDto updateUserDTO = getInvalidUpdateUserDtoWithNullFields(updateIndividualDataDTO, actualUserKeycloakId);

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
        LoginUserDto loginUserDTO = getLoginUserDTO("alexander@post.aq", "123");

        WebClient webClient = getWebClient(port);

        String accessToken = loginUserMono(loginUserDTO, webClient).block().getBody().getAccessToken();

        UpdateUserDto updateUserDTO = getValidUpdateUserDTO(null, actualUserKeycloakId);

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
