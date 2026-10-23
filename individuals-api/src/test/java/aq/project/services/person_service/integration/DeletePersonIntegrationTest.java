package aq.project.services.person_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
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

import static aq.project.controller.PersonRestControllerApi.PATH_DELETE_PERSON;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "person-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeletePersonIntegrationTest {

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
    public void successDeletePerson() {
//        Arrange
        UUID requestPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);
        UUID jwtPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        String undoDeletePersonUri = getUndoDeletePersonUri();
        String deletePersonUri = PATH_DELETE_PERSON.replace("{personKeycloakId}", requestPersonKeycloakId.toString());

        personServiceMock.stubFor(WireMock.delete(deletePersonUri).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(undoDeletePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService
                .deletePerson(jwtPersonKeycloakId, requestPersonKeycloakId).block());
    }

    @Test
    public void failDeletePersonOnRequestPersonKeycloakIsNotMatchAuthorizedPersonKeycloak() {
//        Arrange
        UUID requestPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);
        UUID jwtPersonKeycloakId = UUID.randomUUID();

        String undoDeletePersonUri = getUndoDeletePersonUri();
        String deletePersonUri = PATH_DELETE_PERSON.replace("{personKeycloakId}", requestPersonKeycloakId.toString());

        personServiceMock.stubFor(WireMock.delete(deletePersonUri).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(undoDeletePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.deletePerson(jwtPersonKeycloakId, requestPersonKeycloakId).block());
    }

    @Test
    public void failDeletePersonOn4xxErrorWhileDeletePersonOnPersonService() {
//        Arrange
        UUID requestPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);
        UUID jwtPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        String undoDeletePersonUri = getUndoDeletePersonUri();
        String deletePersonUri = PATH_DELETE_PERSON.replace("{personKeycloakId}", requestPersonKeycloakId.toString());

        personServiceMock.stubFor(WireMock.delete(deletePersonUri).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(undoDeletePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.deletePerson(jwtPersonKeycloakId, requestPersonKeycloakId).block());
    }

    @Test
    public void failDeletePersonOn5xxErrorWhileDeletePersonOnPersonService() {
//        Arrange
        UUID requestPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);
        UUID jwtPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        String undoDeletePersonUri = getUndoDeletePersonUri();
        String deletePersonUri = PATH_DELETE_PERSON.replace("{personKeycloakId}", requestPersonKeycloakId.toString());

        personServiceMock.stubFor(WireMock.delete(deletePersonUri).willReturn(WireMock.serverError()));
        personServiceMock.stubFor(WireMock.post(undoDeletePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> personService.deletePerson(jwtPersonKeycloakId, requestPersonKeycloakId).block());
    }

    @Test
    public void failDeletePersonOnErrorWhileDeletePersonOnPersonServiceAnd4xxErrorOccurredWhileUndoDeletePersonOnPersonService() {
//        Arrange
        UUID requestPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);
        UUID jwtPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        String undoDeletePersonUri = getUndoDeletePersonUri();
        String deletePersonUri = PATH_DELETE_PERSON.replace("{personKeycloakId}", requestPersonKeycloakId.toString());

        personServiceMock.stubFor(WireMock.delete(deletePersonUri).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(undoDeletePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.deletePerson(jwtPersonKeycloakId, requestPersonKeycloakId).block());
    }

    @Test
    public void failDeletePersonOnErrorWhileDeletePersonOnPersonServiceAnd5xxErrorOccurredWhileUndoDeletePersonOnPersonService() {
//        Arrange
        UUID requestPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);
        UUID jwtPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        String undoDeletePersonUri = getUndoDeletePersonUri();
        String deletePersonUri = PATH_DELETE_PERSON.replace("{personKeycloakId}", requestPersonKeycloakId.toString());

        personServiceMock.stubFor(WireMock.delete(deletePersonUri).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(undoDeletePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> personService.deletePerson(jwtPersonKeycloakId, requestPersonKeycloakId).block());
    }

    @Test
    public void failDeletePersonOnNullDeleteRequest() {
//        Arrange
        UUID requestPersonKeycloakId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.deletePerson(requestPersonKeycloakId, requestPersonKeycloakId).block());
    }

    @Test
    public void failDeletePersonOnFallbackCall() {
//        Arrange
        final int RATE_LIMIT = 50;

        UUID requestPersonKeycloakId = UUID.fromString(validTestPersonKeycloakId);

        String undoDeletePersonUri = getUndoDeletePersonUri();
        String deletePersonUri = PATH_DELETE_PERSON.replace("{personKeycloakId}", requestPersonKeycloakId.toString());

        personServiceMock.stubFor(WireMock.delete(deletePersonUri).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(undoDeletePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {

            if(i < RATE_LIMIT - 1)
                new Thread(() -> personService.deletePerson(requestPersonKeycloakId, requestPersonKeycloakId).block()).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> personService.deletePerson(requestPersonKeycloakId, requestPersonKeycloakId).block());
        }
    }

    private String getUndoDeletePersonUri() {
        return "/api/v1/person/undo-delete-person";
    }
}
