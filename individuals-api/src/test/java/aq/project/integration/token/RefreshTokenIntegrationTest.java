package aq.project.integration.token;

import aq.project.controllers.UserRestController;
import aq.project.dto.LoginUserDto;
import aq.project.dto.RefreshTokenDto;
import aq.project.dto.ResponseTokenDto;
import aq.project.services.TokenService;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        LoginUserDto loginUserDTO = getLoginUserDTO("alice@post.aq", "123");
        ResponseTokenDto responseTokenDTO = authController.loginUser(Mono.just(loginUserDTO), null).block().getBody();
        RefreshTokenDto refreshTokenDTO = new RefreshTokenDto().refreshToken(responseTokenDTO.getRefreshToken());
        Assertions.assertDoesNotThrow(() -> tokenService.refreshUserJwt(refreshTokenDTO));
    }

    @Test
    public void failRefreshNullTokenTest() {
        RefreshTokenDto refreshTokenDTO = new RefreshTokenDto().refreshToken(null);
        Assertions.assertThrows(ConstraintViolationException.class, () -> tokenService.refreshUserJwt(refreshTokenDTO));
    }

    @Test
    public void failRefreshWrongTokenTest() {
        RefreshTokenDto refreshTokenDTO = new RefreshTokenDto().refreshToken("wrong-token");
        Assertions.assertThrows(IllegalArgumentException.class, () -> tokenService.refreshUserJwt(refreshTokenDTO));
    }
}
