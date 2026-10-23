package aq.project.services.person_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceUpdatePersonRequestDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.person_service.PersonService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.getInvalidIndividualsApiServiceUpdatePersonRequestDto;
import static aq.project._utils.entities.PersonServiceEntities.getValidIndividualsApiServiceUpdatePersonRequestDto;
import static aq.project.controller.PersonRestControllerApi.PATH_UPDATE_PERSON;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "person-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdatePersonIntegrationTest {

    @Value("${individuals-api.test.valid-test-user-keycloak-id}")
    private String validTestPersonKeycloakId;

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
    public void successUpdatePerson() {
//        Arrange
        UUID personKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = getValidIndividualsApiServiceUpdatePersonRequestDto();
        updatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        String undoUpdatePersonUri = getUndoUpdatePersonUri();

        personServiceMock.stubFor(WireMock.patch(PATH_UPDATE_PERSON).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(undoUpdatePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.updatePerson(personKeycloakId, updatePersonRequestDto).block());
    }

    @Test
    public void failUpdatePersonOnRequestPersonKeycloakIsNotMatchAuthorizedPersonKeycloak() {
//        Arrange
        UUID requestPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);
        UUID authorizedPersonKeycloakId = UUID.randomUUID();

        IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = getValidIndividualsApiServiceUpdatePersonRequestDto();
        updatePersonRequestDto.setPersonKeycloakId(requestPersonKeycloakId);

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.updatePerson(authorizedPersonKeycloakId, updatePersonRequestDto).block());
    }

    @Test
    public void failUpdatePersonOn4xxErrorWhileUpdatePersonOnPersonService() {
//        Arrange
        UUID personKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = getValidIndividualsApiServiceUpdatePersonRequestDto();
        updatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        String undoUpdatePersonUri = getUndoUpdatePersonUri();

        personServiceMock.stubFor(WireMock.patch(PATH_UPDATE_PERSON).willReturn(WireMock.badRequest()));
        personServiceMock.stubFor(WireMock.post(undoUpdatePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.updatePerson(personKeycloakId, updatePersonRequestDto).block());
    }

    @Test
    public void failUpdatePersonOn5xxErrorWhileUpdatePersonOnPersonService() {
//        Arrange
        UUID personKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = getValidIndividualsApiServiceUpdatePersonRequestDto();
        updatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        String undoUpdatePersonUri = getUndoUpdatePersonUri();

        personServiceMock.stubFor(WireMock.patch(PATH_UPDATE_PERSON).willReturn(WireMock.serverError()));
        personServiceMock.stubFor(WireMock.post(undoUpdatePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> personService.updatePerson(personKeycloakId, updatePersonRequestDto).block());
    }

    @Test
    public void failUpdatePersonOnErrorWhileUpdatePersonOnPersonServiceAnd4xxErrorOccurredWhileUndoUpdatePersonOnPersonService() {
//        Arrange
        UUID personKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = getValidIndividualsApiServiceUpdatePersonRequestDto();
        updatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        String undoUpdatePersonUri = getUndoUpdatePersonUri();

        personServiceMock.stubFor(WireMock.patch(PATH_UPDATE_PERSON).willReturn(WireMock.badRequest()));
        personServiceMock.stubFor(WireMock.post(undoUpdatePersonUri).willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.updatePerson(personKeycloakId, updatePersonRequestDto).block());
    }

    @Test
    public void failUpdatePersonOnErrorWhileUpdatePersonOnPersonServiceAnd5xxErrorOccurredWhileUndoUpdatePersonOnPersonService() {
//        Arrange
        UUID personKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = getValidIndividualsApiServiceUpdatePersonRequestDto();
        updatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        String undoUpdatePersonUri = getUndoUpdatePersonUri();

        personServiceMock.stubFor(WireMock.patch(PATH_UPDATE_PERSON).willReturn(WireMock.badRequest()));
        personServiceMock.stubFor(WireMock.post(undoUpdatePersonUri).willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.updatePerson(personKeycloakId, updatePersonRequestDto).block());
    }

    @Test
    public void failUpdatePersonOnInvalidUpdateRequest() {
//        Arrange
        IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = getInvalidIndividualsApiServiceUpdatePersonRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.updatePerson(UUID.randomUUID(), updatePersonRequestDto).block());
    }

    @Test
    public void failUpdatePersonOnNullUpdateRequest() {
//        Arrange
        IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.updatePerson(UUID.randomUUID(), updatePersonRequestDto).block());
    }

    @Test
    public void failUpdatePersonOnFallbackCall() {
//        Arrange
        final int RATE_LIMIT = 50;

        UUID personKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        String undoUpdatePersonUri = getUndoUpdatePersonUri();

        personServiceMock.stubFor(WireMock.patch(PATH_UPDATE_PERSON).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(undoUpdatePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto = getValidIndividualsApiServiceUpdatePersonRequestDto();
            updatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
            updatePersonRequestDto.setZipCode(String.format("Zip%d", i));

            if(i < RATE_LIMIT - 1)
                new Thread(() -> personService.updatePerson(personKeycloakId, updatePersonRequestDto).block()).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> personService.updatePerson(personKeycloakId, updatePersonRequestDto).block());
        }
    }

    private String getUndoUpdatePersonUri() {
        return "/api/v1/person/undo-update-person";
    }
}
