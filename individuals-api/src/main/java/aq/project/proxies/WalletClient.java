package aq.project.proxies;

import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.exceptions.WalletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class WalletClient {

    private static final String BEARER = "Bearer ";

    @Value("${application.wallet-service.endpoints.create-wallet}")
    private String createWalletEndpointUri;
    @Value("${application.wallet-service.endpoints.get-wallet-info}")
    private String getWalletInfoEndpointUri;

    @Autowired
    @Qualifier(value = "walletWebClient")
    private WebClient webClient;

    @Autowired
    private JwtClient jwtClient;

    public Mono<String> createWallet(CreateWalletRequestDTO createWalletRequestDTO) {
        return jwtClient.requestAdminToken()
                .flatMap(adminToken -> createWallet(adminToken, createWalletRequestDTO));
    }

    private Mono<String> createWallet(String accessToken, CreateWalletRequestDTO createWalletRequestDTO) {
        return webClient.post()
                .uri(createWalletEndpointUri)
                .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
                .bodyValue(createWalletRequestDTO)
                .exchangeToMono(response -> {
                    if(response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        String msg = "Error occurred during creating wallet";
                        return Mono.error(new WalletException(msg, response.statusCode().value()));
                    }
                    return response.bodyToMono(String.class);
                });
    }

    public Mono<WalletInfoResponseDTO> getWalletInfo(String walletId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminToken -> getWalletInfo(adminToken, walletId));
    }

    private Mono<WalletInfoResponseDTO> getWalletInfo(String accessToken, String walletId) {
        return webClient.get()
                .uri(getWalletInfoEndpointUri + walletId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
                .exchangeToMono(response -> {
                    if(response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        String msg = String.format("Error occurred during getting wallet info for wallet with id [%s]",
                                walletId);
                        return Mono.error(new WalletException(msg, response.statusCode().value()));
                    }
                    return response.bodyToMono(WalletInfoResponseDTO.class);
                });
    }
}
