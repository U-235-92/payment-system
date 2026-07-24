package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.wallet_service.WalletApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static aq.project.utils.telemetry.TracePropagator.fetchTraceId;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletApiClient walletApiClient;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    public Mono<String> createWallet(
            CreateWalletRequestDto dto
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> walletApiClient.createWallet(xTraceId, Mono.just(dto), jwtHeader)))
                .flatMap(response -> Mono.just(response.getBody()));
    }

    public Mono<WalletInfoResponseDto> getWalletInfo(
            String walletId
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> walletApiClient.getWalletInfo(walletId, xTraceId, jwtHeader)))
                .flatMap(response -> Mono.just(response.getBody()));
    }

    public Mono<String> getWalletCurrencyCode(
            String walletId
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> walletApiClient.getWalletCurrency(walletId, xTraceId, jwtHeader)))
                .flatMap(response -> Mono.just(response.getBody()));
    }
}
