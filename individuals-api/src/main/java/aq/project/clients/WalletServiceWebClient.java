package aq.project.clients;

import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public interface WalletServiceWebClient {

    @PostExchange(
            value = "${application.wallet-service.endpoints.create-wallet}"
    )
    Mono<ResponseEntity<String>> createWallet(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @RequestBody CreateWalletRequestDTO createWalletRequestDTO
    );

    @GetExchange(
            value = "${application.wallet-service.endpoints.get-wallet-info}/{walletId}"
    )
    Mono<ResponseEntity<WalletInfoResponseDTO>> getWalletInfo(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("walletId") String walletId
    );

    @GetExchange(
            value = "${application.wallet-service.endpoints.get-wallet-currency}/{walletId}"
    )
    Mono<ResponseEntity<String>> getWalletCurrency(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("walletId") String walletId
    );
}
