package aq.project.integration;

import aq.project.controllers.GatewayUserRestController;
import aq.project.dto.LoginUserDTO;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
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
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RefreshTokenIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GatewayUserRestController authController;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry);
    }

    @Test
    public void successRefreshTokenTest() {
        LoginUserDTO loginUserDTO = new LoginUserDTO().email("alice@post.aq").password("123");
        ResponseTokenDTO responseTokenDTO = authController.loginUser(loginUserDTO).block().getBody();
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO().refreshToken(responseTokenDTO.getRefreshToken());
        webTestClient.post()
                .uri("/gateway/api/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshTokenDTO)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failRefreshNullTokenTest() {
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO().refreshToken(null);
        webTestClient.post()
                .uri("/gateway/api/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshTokenDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failRefreshWrongTokenTest() {
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO().refreshToken("wrong-token");
        webTestClient.post()
                .uri("/gateway/api/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshTokenDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
