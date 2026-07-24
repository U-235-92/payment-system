package aq.project.util;

import aq.project.dto.LoginUserDto;
import aq.project.dto.ResponseTokenDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public abstract class TestUtils {

    public static Mono<ResponseEntity<ResponseTokenDto>> loginUserMono(LoginUserDto loginUserDTO, WebClient webClient) {
        return webClient.post()
                .uri("/api/v1/user/login-user")
                .bodyValue(loginUserDTO)
                .exchangeToMono(response -> response.bodyToMono(ResponseTokenDto.class))
                .map(response -> ResponseEntity.ok().body(response));
    }

    public static WebClient getWebClient(int port) {
        return WebClient.builder().baseUrl("http://localhost:" + port).build();
    }
}
