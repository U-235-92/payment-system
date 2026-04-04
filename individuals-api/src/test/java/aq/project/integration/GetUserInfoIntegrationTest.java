package aq.project.integration;

import aq.project.dto.LoginUserDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.dto.UserInfoResponseDTO;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetUserInfoIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private int localTestServerPort;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @Container
    private static final GenericContainer<?> PERSON_SERVICE_CONTAINER = TestContainers.PersonService.PERSON_SERVICE_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry);
        TestApplicationProperties.PersonServiceProperties
                .registerApplicationContextContainerProperties(registry);
    }

    @Test
    public void successGetUserInfoTest() {
//        In this test we are using regular WebClient instead of other tests.
//        This happens because when you use WebTestClient the SecurityContext lives through one request reactive chain.
//        When you start next independent request reactive chain - the previous SecurityContext will not go to the new reactive chain
//        It is usual situation for Test reactive. The example of that fact below.
//        When you uncomment this case and breakpoint UserService.getUserInfoResponseDTO() method
//        You will see that after webTestClient.get() call the SecurityContext is empty.
//        To prevent it you may use WebClient + StepVerifier to check that this test case works fine.

//        >>>>>>>>>> DANGER ZONE: THE CODE UNDER COMMENTS DOESN'T WORK (THROWS 401 ERROR IN ANY CASE) <<<<<<<<<<

//        LoginUserDTO userLoginRequest = new LoginUserDTO().email("alice@post.aq").password("123");
//        ResponseTokenDTO responseTokenDTO = webTestClient.post()
//                .uri("/gateway/api/user/login-user")
//                .bodyValue(userLoginRequest)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(ResponseTokenDTO.class)
//                .returnResult()
//                .getResponseBody();
//        webTestClient.get()
//                .uri("/gateway/api/user/get-user-info")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseTokenDTO.getAccessToken())
//                .exchange()
//                .expectStatus()
//                .isOk();

//        >>>>>>>>>> WORKING TEST CASE <<<<<<<<<<
        WebClient webClient = getWebClient();
        LoginUserDTO loginUserDTO = getTestLoginUserDTO();
        Mono<ResponseEntity<ResponseTokenDTO>> loginUserMono = loginUserAndGetLoginUserMono(webClient, loginUserDTO);
        Mono<ResponseEntity<UserInfoResponseDTO>> userInfoMono = getUserInfoAndGetUserInfoMono(webClient, loginUserMono);
        StepVerifier.create(userInfoMono)
                .expectNextMatches(this::is2xxStatusCode)
                .verifyComplete();
    }

    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:" + localTestServerPort)
                .build();
    }

    private LoginUserDTO getTestLoginUserDTO() {
        return new LoginUserDTO()
                .email("alice@post.aq")
                .password("123");
    }

    private Mono<ResponseEntity<ResponseTokenDTO>> loginUserAndGetLoginUserMono(WebClient webClient, LoginUserDTO loginUserDTO) {
        return webClient.post()
                .uri("/gateway/api/user/login-user")
                .bodyValue(loginUserDTO)
                .exchangeToMono(response -> response.bodyToMono(ResponseTokenDTO.class))
                .map(ResponseEntity::ok);
    }

    private Mono<ResponseEntity<UserInfoResponseDTO>> getUserInfoAndGetUserInfoMono(WebClient webClient, Mono<ResponseEntity<ResponseTokenDTO>> loginUserMono) {
        return loginUserMono.flatMap(loginUserResponseEntity -> webClient.get()
                        .uri("/gateway/api/user/get-user-info")
                        .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderValue(loginUserResponseEntity))
                        .exchangeToMono(response -> response.bodyToMono(UserInfoResponseDTO.class))
                        .map(ResponseEntity::ok));
    }

    private String getAuthorizationHeaderValue(ResponseEntity<ResponseTokenDTO> responseEntity) {
        return "Bearer " + responseEntity.getBody().getAccessToken();
    }

    private boolean is2xxStatusCode(ResponseEntity<?> responseEntity) {
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Test
    public void failGetUserInfoWithNoAccessTokenTest() {
        webTestClient.get()
                .uri("/gateway/api/user/get-user-info")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void failGetUserInfoWithInvalidAccessTokenTest() {
        String wrongAccessToken = "wrong-access-token";
        webTestClient.get()
                .uri("/gateway/api/user/get-user-info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + wrongAccessToken)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
