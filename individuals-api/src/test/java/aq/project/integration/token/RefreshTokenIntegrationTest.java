package aq.project.integration.token;

import aq.project.controllers.UserRestController;
import aq.project.dto.LoginUserDTO;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.services.TokenService;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import static aq.project.util.TestDtoRepository.getLoginUserDTO;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RefreshTokenIntegrationTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRestController authController;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry);
    }

    @Test
    public void successRefreshTokenTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alice@post.aq", "123");
        ResponseTokenDTO responseTokenDTO = authController.loginUser(Mono.just(loginUserDTO), null).block().getBody();
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO().refreshToken(responseTokenDTO.getRefreshToken());
        Assertions.assertDoesNotThrow(() -> tokenService.refreshUserJwt(refreshTokenDTO));
    }

    @Test
    public void failRefreshNullTokenTest() {
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO().refreshToken(null);
        Assertions.assertThrows(ConstraintViolationException.class, () -> tokenService.refreshUserJwt(refreshTokenDTO));
    }

    @Test
    public void failRefreshWrongTokenTest() {
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO().refreshToken("wrong-token");
        Assertions.assertThrows(IllegalArgumentException.class, () -> tokenService.refreshUserJwt(refreshTokenDTO));
    }
}
