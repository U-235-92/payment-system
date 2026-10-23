package aq.project.services.person_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceCreatePersonRequestDto;
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
import org.springframework.web.client.HttpServerErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static aq.project._utils.entities.PersonServiceEntities.getInvalidIndividualsApiServiceCreatePersonRequestDto;
import static aq.project._utils.entities.PersonServiceEntities.getValidIndividualsApiServiceCreatePersonRequestDto;
import static aq.project.controller.PersonRestControllerApi.PATH_CREATE_PERSON;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "person-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePersonIntegrationTest {

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
    public void successCreatePerson() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.createPerson(createPersonRequestDto).block());
    }

    @Test
    public void failCreatePersonOnPersonService4xxPersonServiceError() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.createPerson(createPersonRequestDto).block());
    }

    @Test
    public void failCreatePersonOnPersonService5xxPersonServiceError() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> personService.createPerson(createPersonRequestDto).block());
    }

    @Test
    public void failCreatePersonOnInvalidRequest() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getInvalidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.createPerson(createPersonRequestDto).block());
    }

    @Test
    public void failCreatePersonOnNullRequest() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.createPerson(createPersonRequestDto).block());
    }

    @Test
    public void failCreatePersonOnFallbackCall() {
//        Arrange
        final int RATE_LIMIT = 50;

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.ok()));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
            createPersonRequestDto.setEmail(String.format("person%d@mail.aq", i));
            createPersonRequestDto.setFirstName("Firstname");
            createPersonRequestDto.setLastName("Lastname");

            if(i < RATE_LIMIT - 1)
                new Thread(() -> personService.createPerson(createPersonRequestDto).block()).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> personService.createPerson(createPersonRequestDto).block());
        }
    }
}
