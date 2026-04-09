package aq.project.util;

import aq.project.dto.LoginUserDTO;
import aq.project.dto.ResponseTokenDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public abstract class TestUtils {

    public static Mono<ResponseEntity<ResponseTokenDTO>> loginUserMono(LoginUserDTO loginUserDTO, WebClient webClient) {
        return webClient.post()
                .uri("/gateway/api/user/login-user")
                .bodyValue(loginUserDTO)
                .exchangeToMono(response -> response.bodyToMono(ResponseTokenDTO.class))
                .map(response -> ResponseEntity.ok().body(response));
    }

    public static WebClient getWebClient(int port) {
        return WebClient.builder().baseUrl("http://localhost:" + port).build();
    }
}
