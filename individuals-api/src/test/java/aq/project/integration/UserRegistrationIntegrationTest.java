package aq.project.integration;

import aq.project.dto.AddressDTO;
import aq.project.dto.CountryDTO;
import aq.project.dto.CreateIndividualDataRequest;
import aq.project.dto.CreateUserRequest;
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
        CreateUserRequest request = getValidCreateUserRequest();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    @Disabled("Test case connected with no valid admin client credentials")
    public void testFailCreateUserWithNoValidAdminClientCredentials() {
        CreateUserRequest request = getValidCreateUserRequest();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    private CreateUserRequest getValidCreateUserRequest() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataRequest individualDataRequest = new CreateIndividualDataRequest();
        individualDataRequest.setFirstName("firstName");
        individualDataRequest.setLastName("lastName");
        individualDataRequest.setEmail("email@post.aq");
        individualDataRequest.phoneNumber("1234567890");
        individualDataRequest.setPassportNumber("1234567890");
        individualDataRequest.setAddress(addressDTO);

        return new CreateUserRequest()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad458")
                .username("username")
                .password("password")
                .confirmPassword("password")
                .individualData(individualDataRequest);
    }

    @Test
    public void testDuplicateCreateUserFail() {
        CreateUserRequest request = getDuplicateCreateUserRequest();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private CreateUserRequest getDuplicateCreateUserRequest() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataRequest individualDataRequest = new CreateIndividualDataRequest();
        individualDataRequest.setFirstName("Alice");
        individualDataRequest.setLastName("K");
        individualDataRequest.setEmail("alice@post.aq");
        individualDataRequest.phoneNumber("1234567890");
        individualDataRequest.setPassportNumber("1234567890");
        individualDataRequest.setAddress(addressDTO);

        return new CreateUserRequest()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad407")
                .username("alice")
                .password("password")
                .confirmPassword("password")
                .individualData(individualDataRequest);
    }

    @Test
    public void testNoMatchPasswordsCreateUserRequest() {
        CreateUserRequest request = getIncorrectCreateUserRequestWithDoNotMatchPasswords();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserRequest getIncorrectCreateUserRequestWithDoNotMatchPasswords() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataRequest individualDataRequest = new CreateIndividualDataRequest();
        individualDataRequest.setFirstName("Bob");
        individualDataRequest.setLastName("K");
        individualDataRequest.setEmail("bob@post.aq");
        individualDataRequest.phoneNumber("1234567890");
        individualDataRequest.setPassportNumber("1234567890");
        individualDataRequest.setAddress(addressDTO);

        return new CreateUserRequest()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad477")
                .username("bob")
                .password("password")
                .confirmPassword("123")
                .individualData(individualDataRequest);
    }

    @Test
    public void testNullFieldsCreateUserRequest() {
        CreateUserRequest request = getIncorrectCreateUserRequestWithNullFields();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserRequest getIncorrectCreateUserRequestWithNullFields() {
        return new CreateUserRequest()
                .username(null)
                .password("password")
                .confirmPassword("123");
    }

    @Test
    public void testCreateUserWithNullIndividualData() {
        CreateUserRequest request = getIncorrectCreateUserRequestWithNullIndividualData();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CreateUserRequest getIncorrectCreateUserRequestWithNullIndividualData() {
        return new CreateUserRequest()
                .username("test")
                .password("password")
                .confirmPassword("123")
                .individualData(null);
    }
}
