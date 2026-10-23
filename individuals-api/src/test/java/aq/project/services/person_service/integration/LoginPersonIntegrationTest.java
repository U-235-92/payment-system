package aq.project.services.person_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceLoginPersonRequestDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.person_service.PersonService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static aq.project._utils.entities.PersonServiceEntities.getInvalidIndividualsApiServiceLoginPersonRequestDto;
import static aq.project._utils.entities.PersonServiceEntities.getValidIndividualsApiServiceLoginPersonRequestDto;
import static aq.project.controller.PersonRestControllerApi.PATH_CREATE_PERSON;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "person-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginPersonIntegrationTest {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("person-service")
    private WireMockServer personServiceMock;

    @Autowired
    private PersonService personService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.person-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successLoginPerson() {
//        Arrange
        IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto = getValidIndividualsApiServiceLoginPersonRequestDto();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.loginPerson(loginPersonRequestDto).block());
    }

    @Test
    public void failLoginPersonOnInvalidCredentials() {
//        Arrange
        IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto = getValidIndividualsApiServiceLoginPersonRequestDto();
        loginPersonRequestDto.setEmail("invalid@mail.aq");
        loginPersonRequestDto.setPassword("invalid");

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.loginPerson(loginPersonRequestDto).block());
    }

    @Test
    public void failLoginPersonOnInvalidRequest() {
//        Arrange
        IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto = getInvalidIndividualsApiServiceLoginPersonRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.loginPerson(loginPersonRequestDto).block());
    }

    @Test
    public void failLoginPersonOnNullRequest() {
//        Arrange
        IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.loginPerson(loginPersonRequestDto).block());
    }

    @Test
    public void failLoginPersonOnFallbackCall() {
//        Arrange
        final int RATE_LIMIT = 50;

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.ok()));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto = getValidIndividualsApiServiceLoginPersonRequestDto();

            if(i < RATE_LIMIT - 1)
                new Thread(() -> personService.loginPerson(loginPersonRequestDto).block()).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> personService.loginPerson(loginPersonRequestDto).block());
        }
    }
}
