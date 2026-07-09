package aq.project.services;

import aq.project.clients.KeycloakServiceWebClientFacade;
import aq.project.clients.WalletServiceWebClient;
import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletServiceWebClient walletServiceWebClient;

    private final KeycloakServiceWebClientFacade keycloakServiceWebClientFacade;

    public Mono<String> createWallet(CreateWalletRequestDTO createWalletRequestDTO) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> walletServiceWebClient.createWallet(jwt, createWalletRequestDTO)
                        .map(HttpEntity::getBody));
    }

    public Mono<WalletInfoResponseDTO> getWalletInfo(String walletId) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> walletServiceWebClient.getWalletInfo(jwt, walletId)
                        .map(HttpEntity::getBody));
    }

    public Mono<String> getWalletCurrencyCode(String walletId) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> walletServiceWebClient.getWalletCurrency(jwt, walletId)
                        .map(HttpEntity::getBody));
    }
}
