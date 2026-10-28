package aq.project.controllers.person_service;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceCreatePersonRequestDto;
import aq.project.dto.IndividualsApiServiceTokenResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jwt.JWTParser;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.getValidIndividualsApiServiceCreatePersonRequestDto;
import static aq.project.controller.PersonRestControllerApi.PATH_CREATE_PERSON;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "person-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeletePersonWebTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${application.person-service.endpoints.create-person}")
    private String personServiceCreatePersonUri;
    @Value("${application.person-service.endpoints.delete-person}")
    private String personServiceDeletePersonUri;
    @Value("${application.person-service.endpoints.undo-delete-person}")
    private String personServiceUndoDeletePersonUri;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("person-service")
    private WireMockServer personServiceMock;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.person-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successDeletePerson() throws IOException, ParseException {
//        Arrange
        String email = "person@mail.aq";
        String username = "person";

        IndividualsApiServiceTokenResponseDto tokenResponseDto = createPerson(email, username);

        String accessJwt = getCreatedPersonAccessJwt(tokenResponseDto);

        UUID personKeycloakId = getCreatedPersonKeycloakId(tokenResponseDto);

        String individualsApiServiceDeletePersonUri = String.format("%s/%s", this.personServiceDeletePersonUri, personKeycloakId);
        String personServiceDeletePersonUri = String.format("%s/%s", this.personServiceDeletePersonUri, personKeycloakId);

        personServiceMock.stubFor(WireMock.delete(personServiceDeletePersonUri).willReturn(WireMock.ok()));
        personServiceMock.stubFor(WireMock.post(personServiceUndoDeletePersonUri).willReturn(WireMock.ok()));

//        Act & Assert
        webTestClient.delete()
                .uri(individualsApiServiceDeletePersonUri)
                .header("Authorization", "Bearer " + accessJwt)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failDeletePersonOnInvalidDeleteRequest() throws IOException {
//        Arrange
        String email = "wrong@mail.aq";
        String username = "wrong";

        IndividualsApiServiceTokenResponseDto tokenResponseDto = createPerson(email, username);

        String accessJwt = getCreatedPersonAccessJwt(tokenResponseDto);

        String individualsApiServiceDeletePersonUri = String.format("%s/%s", this.personServiceDeletePersonUri, UUID.randomUUID());

//        Act & Assert
        webTestClient.delete()
                .uri(individualsApiServiceDeletePersonUri)
                .header("Authorization", "Bearer " + accessJwt)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failDeletePersonOnUnauthorizedRequest() {
//        Arrange
        String individualsApiServiceDeletePersonUri = String.format("%s/%s", this.personServiceDeletePersonUri, UUID.randomUUID());

//        Act & Assert
        webTestClient.delete()
                .uri(individualsApiServiceDeletePersonUri)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    private IndividualsApiServiceTokenResponseDto createPerson(
            String email,
            String username
    ) throws IOException {
        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.ok()));

        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setPersonKeycloakId(null);
        createPersonRequestDto.setEmail(email);
        createPersonRequestDto.setUsername(username);
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

        byte[] responseBytes = webTestClient.post()
                .uri(personServiceCreatePersonUri)
                .header("Content-Type", "application/json")
                .bodyValue(createPersonRequestDto)
                .exchange()
                .returnResult()
                .getResponseBodyContent();

        return OBJECT_MAPPER.readValue(responseBytes, IndividualsApiServiceTokenResponseDto.class);
    }

    private String getCreatedPersonAccessJwt(
            IndividualsApiServiceTokenResponseDto tokenResponseDto
    ) {
        return tokenResponseDto.getAccessToken();
    }

    private UUID getCreatedPersonKeycloakId(
            IndividualsApiServiceTokenResponseDto tokenResponseDto
    ) throws ParseException {
        String accessJwt = tokenResponseDto.getAccessToken();

        return UUID.fromString(JWTParser.parse(accessJwt)
                .getJWTClaimsSet()
                .getSubject());
    }
}
