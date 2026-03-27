package aq.project.integration;

import aq.project.dto.*;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateUserIntegrationTest {

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
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO);
        UpdateUserDTO updateUserDTO = getValidUpdateUserDTO(updateIndividualDataDTO);

        WebClient webClient = getWebClient();

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO)
                .flatMap(responseEntity -> webClient.patch()
                        .uri("/gateway/api/user/update-user")
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
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDtoWithUnknownKeycloakUserId(addressDTO);
        UpdateUserDTO updateUserDTO = getValidUpdateUserDtoWithUnknownKeycloakUserId(updateIndividualDataDTO);

        WebClient webClient = getWebClient();

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO)
                .flatMap(responseEntity -> webClient.patch()
                        .uri("/gateway/api/user/update-user")
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
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO);
        UpdateUserDTO updateUserDTO = getInvalidUpdateUserDtoWithNoMatchPassword(updateIndividualDataDTO);

        WebClient webClient = getWebClient();

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO)
                .flatMap(responseEntity -> webClient.patch()
                        .uri("/gateway/api/user/update-user")
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
        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO);
        UpdateUserDTO updateUserDTO = getInvalidUpdateUserDtoWithNullFields(updateIndividualDataDTO);
                webTestClient.patch()
                .uri("/gateway/api/user/update-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateUserDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failUpdateUserWithNullUpdateIndividualDataDtoTest() {
        UpdateUserDTO updateUserDTO = getValidUpdateUserDTO(null);
        webTestClient.patch()
                .uri("/gateway/api/user/update-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateUserDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private Mono<ResponseEntity<ResponseTokenDTO>> loginUserMono(LoginUserDTO loginUserDTO) {
        WebClient webClient = getWebClient();
        return webClient.post()
                .uri("/gateway/api/user/login-user")
                .bodyValue(loginUserDTO)
                .exchangeToMono(response -> response.bodyToMono(ResponseTokenDTO.class))
                .map(response -> ResponseEntity.ok().body(response));
    }

    private WebClient getWebClient() {
        return WebClient.builder().baseUrl("http://localhost:" + port).build();
    }

    private CountryDTO getValidCountryDTO() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("Russia");
        countryDTO.setCode("RU");
        return countryDTO;
    }

    private AddressDTO getValidAddressDTO(CountryDTO countryDTO) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("updated_state");
        addressDTO.setCity("updated_city");
        addressDTO.setAddress("updated_address");
        addressDTO.setZipCode("updated_zipcode");
        return addressDTO;
    }

    private UpdateIndividualDataDTO getValidUpdateIndividualDataDTO(AddressDTO addressDTO) {
        UpdateIndividualDataDTO updateIndividualDataDTO = new UpdateIndividualDataDTO();
        updateIndividualDataDTO.setKeycloakUserId(actualUserKeycloakId);
        updateIndividualDataDTO.setFirstName("updatedFirstName");
        updateIndividualDataDTO.setLastName("updatedLastName");
        updateIndividualDataDTO.phoneNumber("9876543210");
        updateIndividualDataDTO.setPassportNumber("9876543210");
        updateIndividualDataDTO.setAddress(addressDTO);
        return updateIndividualDataDTO;
    }

    private UpdateIndividualDataDTO getValidUpdateIndividualDataDtoWithUnknownKeycloakUserId(AddressDTO addressDTO) {
        UpdateIndividualDataDTO updateIndividualDataDTO = new UpdateIndividualDataDTO();
        updateIndividualDataDTO.setKeycloakUserId(unknownUserKeycloakId);
        updateIndividualDataDTO.setFirstName("updatedFirstName");
        updateIndividualDataDTO.setLastName("updatedLastName");
        updateIndividualDataDTO.phoneNumber("9876543210");
        updateIndividualDataDTO.setPassportNumber("9876543210");
        updateIndividualDataDTO.setAddress(addressDTO);
        return updateIndividualDataDTO;
    }

    private UpdateUserDTO getValidUpdateUserDTO(UpdateIndividualDataDTO updateIndividualDataDTO) {
        return new UpdateUserDTO()
                .keycloakUserId(actualUserKeycloakId)
                .password("password")
                .confirmPassword("password")
                .individualData(updateIndividualDataDTO);
    }

    private UpdateUserDTO getValidUpdateUserDtoWithUnknownKeycloakUserId(UpdateIndividualDataDTO updateIndividualDataDTO) {
        return new UpdateUserDTO()
                .keycloakUserId(unknownUserKeycloakId)
                .password("password")
                .confirmPassword("password")
                .individualData(updateIndividualDataDTO);
    }

    private UpdateUserDTO getInvalidUpdateUserDtoWithNoMatchPassword(UpdateIndividualDataDTO updateIndividualDataDTO) {
        return new UpdateUserDTO()
                .keycloakUserId(actualUserKeycloakId)
                .password("foo")
                .confirmPassword("bar")
                .individualData(updateIndividualDataDTO);
    }

    private UpdateUserDTO getInvalidUpdateUserDtoWithNullFields(UpdateIndividualDataDTO updateIndividualDataDTO) {
        return new UpdateUserDTO()
                .keycloakUserId(actualUserKeycloakId)
                .password(null)
                .confirmPassword("bar")
                .individualData(updateIndividualDataDTO);
    }
}
