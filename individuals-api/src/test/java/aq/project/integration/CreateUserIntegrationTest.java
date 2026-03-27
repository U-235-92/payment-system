package aq.project.integration;

import aq.project.dto.AddressDTO;
import aq.project.dto.CountryDTO;
import aq.project.dto.CreateIndividualDataDTO;
import aq.project.dto.CreateUserDTO;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateUserIntegrationTest {

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
    public void successCreateUserTest() {
        CreateUserDTO validCreateUserDTO = getValidCreateUserDTO();
        webTestClient.post()
                .uri("/gateway/api/user/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validCreateUserDTO)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    @Disabled("Test case connected with no valid admin client credentials. " +
            "To do this you have to change both [keycloak.admin.client-id] " +
            "and [keycloak.admin.client-secret] property values on any random value in " +
            "TestApplicationProperties.KeycloakProperties class in" +
            "registerApplicationContextContainerProperties() method")
    public void failCreateUserWithNoValidAdminClientCredentialsTest() {
        CreateUserDTO validCreateUserDTO = getValidCreateUserDTO();
        webTestClient.post()
                .uri("/gateway/api/user/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validCreateUserDTO)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    private CreateUserDTO getValidCreateUserDTO() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDTO createIndividualDataDTO = new CreateIndividualDataDTO();
        createIndividualDataDTO.setFirstName("firstName");
        createIndividualDataDTO.setLastName("lastName");
        createIndividualDataDTO.setEmail("email@post.aq");
        createIndividualDataDTO.phoneNumber("1234567890");
        createIndividualDataDTO.setPassportNumber("1234567890");
        createIndividualDataDTO.setAddress(addressDTO);

        return new CreateUserDTO()
                .username("username")
                .password("password")
                .confirmPassword("password")
                .individualData(createIndividualDataDTO);
    }

    @Test
    public void failCreateDuplicateUserTest() {
        CreateUserDTO Event = getDuplicateCreateUserDTO();
        webTestClient.post()
                .uri("/gateway/api/user/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private CreateUserDTO getDuplicateCreateUserDTO() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDTO createIndividualDataDTO = new CreateIndividualDataDTO();
        createIndividualDataDTO.setFirstName("Alice");
        createIndividualDataDTO.setLastName("K");
        createIndividualDataDTO.setEmail("alice@post.aq");
        createIndividualDataDTO.phoneNumber("1234567890");
        createIndividualDataDTO.setPassportNumber("1234567890");
        createIndividualDataDTO.setAddress(addressDTO);

        return new CreateUserDTO()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad407")
                .username("alice")
                .password("password")
                .confirmPassword("password")
                .individualData(createIndividualDataDTO);
    }

    @Test
    public void failCreateUserWithNoMatchPasswordsTest() {
        CreateUserDTO Event = getIncorrectCreateUserDTOWithDoNotMatchPasswords();
        webTestClient.post()
                .uri("/gateway/api/user/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserDTO getIncorrectCreateUserDTOWithDoNotMatchPasswords() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDTO individualDataEvent = new CreateIndividualDataDTO();
        individualDataEvent.setFirstName("Bob");
        individualDataEvent.setLastName("K");
        individualDataEvent.setEmail("bob@post.aq");
        individualDataEvent.phoneNumber("1234567890");
        individualDataEvent.setPassportNumber("1234567890");
        individualDataEvent.setAddress(addressDTO);

        return new CreateUserDTO()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad477")
                .username("bob")
                .password("password")
                .confirmPassword("123")
                .individualData(individualDataEvent);
    }

    @Test
    public void failCreateUserWithNullFieldsTest() {
        CreateUserDTO Event = getIncorrectCreateUserDTOWithNullFields();
        webTestClient.post()
                .uri("/gateway/api/user/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserDTO getIncorrectCreateUserDTOWithNullFields() {
        return new CreateUserDTO()
                .username(null)
                .password("password")
                .confirmPassword("123");
    }

    @Test
    public void failCreateUserWithNullIndividualDataTest() {
        CreateUserDTO Event = getIncorrectCreateUserDTOWithNullIndividualData();
        webTestClient.post()
                .uri("/gateway/api/user/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserDTO getIncorrectCreateUserDTOWithNullIndividualData() {
        return new CreateUserDTO()
                .username("test")
                .password("password")
                .confirmPassword("123")
                .individualData(null);
    }
}
