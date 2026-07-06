package aq.project.clients;

import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.exceptions.WalletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static aq.project.util.constants.CustomHttpHeaders.BEARER;

@Component
public class WalletClient {

    @Value("${application.wallet-service.endpoints.create-wallet}")
    private String createWalletEndpointUri;
    @Value("${application.wallet-service.endpoints.get-wallet-info}")
    private String getWalletInfoEndpointUri;
    @Value("${application.wallet-service.endpoints.get-wallet-currency}")
    private String getWalletCurrencyUri;

    @Autowired
    @Qualifier(value = "walletServiceWebClient")
    private WebClient webClient;

    @Autowired
    private JwtClient jwtClient;

    public Mono<String> createWallet(CreateWalletRequestDTO createWalletRequestDTO) {
        return jwtClient.requestAdminToken()
                .flatMap(adminToken -> webClient.post()
                        .uri(createWalletEndpointUri)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                        .bodyValue(createWalletRequestDTO)
                        .exchangeToMono(response -> {
                            if(isErrorResponse(response)) {
                                String msg = "Error occurred during creating wallet";
                                return Mono.error(new WalletException(msg, response.statusCode().value()));
                            }
                            return response.bodyToMono(String.class);
                        })
                );
    }

    public Mono<WalletInfoResponseDTO> getWalletInfo(String walletId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminToken -> webClient.get()
                        .uri(getWalletInfoEndpointUri + walletId)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                        .exchangeToMono(response -> {
                            if(isErrorResponse(response)) {
                                String msg = String.format("Error occurred during getting wallet info for wallet with id [%s]",
                                        walletId);
                                return Mono.error(new WalletException(msg, response.statusCode().value()));
                            }
                            return response.bodyToMono(WalletInfoResponseDTO.class);
                        })
                );
    }

    public Mono<String> getWalletCurrency(String walletId) {
        final String requestUrl = String.format("%s/%s", getWalletCurrencyUri, walletId);
        return jwtClient.requestAdminToken()
                .flatMap(adminToken -> webClient.get()
                        .uri(requestUrl)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                        .retrieve()
                        .bodyToMono(String.class));
    }

    private boolean isErrorResponse(ClientResponse response) {
        return response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError();
    }
}
