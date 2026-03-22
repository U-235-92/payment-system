package aq.project.integration;

import aq.project.dto.AddressDTO;
import aq.project.dto.CountryDTO;
import aq.project.dto.CreateIndividualDataEvent;
import aq.project.dto.CreateUserEvent;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext
@ActiveProfiles("dev")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRegistrationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry, KEYCLOAK);
    }

    @Test
    public void testSuccessfulCreateUser() {
        CreateUserEvent Event = getValidCreateUserEvent();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    @Disabled("Test case connected with no valid admin client credentials")
    public void testFailCreateUserWithNoValidAdminClientCredentials() {
        CreateUserEvent Event = getValidCreateUserEvent();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    private CreateUserEvent getValidCreateUserEvent() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataEvent individualDataEvent = new CreateIndividualDataEvent();
        individualDataEvent.setFirstName("firstName");
        individualDataEvent.setLastName("lastName");
        individualDataEvent.setEmail("email@post.aq");
        individualDataEvent.phoneNumber("1234567890");
        individualDataEvent.setPassportNumber("1234567890");
        individualDataEvent.setAddress(addressDTO);

        return new CreateUserEvent()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad458")
                .username("username")
                .password("password")
                .confirmPassword("password")
                .individualData(individualDataEvent);
    }

    @Test
    public void testDuplicateCreateUserFail() {
        CreateUserEvent Event = getDuplicateCreateUserEvent();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private CreateUserEvent getDuplicateCreateUserEvent() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataEvent individualDataEvent = new CreateIndividualDataEvent();
        individualDataEvent.setFirstName("Alice");
        individualDataEvent.setLastName("K");
        individualDataEvent.setEmail("alice@post.aq");
        individualDataEvent.phoneNumber("1234567890");
        individualDataEvent.setPassportNumber("1234567890");
        individualDataEvent.setAddress(addressDTO);

        return new CreateUserEvent()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad407")
                .username("alice")
                .password("password")
                .confirmPassword("password")
                .individualData(individualDataEvent);
    }

    @Test
    public void testNoMatchPasswordsCreateUserEvent() {
        CreateUserEvent Event = getIncorrectCreateUserEventWithDoNotMatchPasswords();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserEvent getIncorrectCreateUserEventWithDoNotMatchPasswords() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataEvent individualDataEvent = new CreateIndividualDataEvent();
        individualDataEvent.setFirstName("Bob");
        individualDataEvent.setLastName("K");
        individualDataEvent.setEmail("bob@post.aq");
        individualDataEvent.phoneNumber("1234567890");
        individualDataEvent.setPassportNumber("1234567890");
        individualDataEvent.setAddress(addressDTO);

        return new CreateUserEvent()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad477")
                .username("bob")
                .password("password")
                .confirmPassword("123")
                .individualData(individualDataEvent);
    }

    @Test
    public void testNullFieldsCreateUserEvent() {
        CreateUserEvent Event = getIncorrectCreateUserEventWithNullFields();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserEvent getIncorrectCreateUserEventWithNullFields() {
        return new CreateUserEvent()
                .username(null)
                .password("password")
                .confirmPassword("123");
    }

    @Test
    public void testCreateUserWithNullIndividualData() {
        CreateUserEvent Event = getIncorrectCreateUserEventWithNullIndividualData();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserEvent getIncorrectCreateUserEventWithNullIndividualData() {
        return new CreateUserEvent()
                .username("test")
                .password("password")
                .confirmPassword("123")
                .individualData(null);
    }
}
