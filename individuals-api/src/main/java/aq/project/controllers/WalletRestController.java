package aq.project.controllers;

import aq.project.controller.WalletRestControllerApi;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class WalletRestController implements WalletRestControllerApi {

    private final WalletService walletService;

    @Override
    public Mono<ResponseEntity<String>> createWallet(
            Mono<CreateWalletRequestDto> createWalletRequestDTO,
            ServerWebExchange exchange
    ) {
        return createWalletRequestDTO.flatMap(dto -> walletService.createWallet(dto)
                .flatMap(walletId -> Mono.just(ResponseEntity.ok(walletId))));
    }

    @Override
    public Mono<ResponseEntity<WalletInfoResponseDto>> getWalletInfo(
            String walletId,
            ServerWebExchange exchange
    ) {
        return walletService.getWalletInfo(walletId)
                .flatMap(dto -> Mono.just(ResponseEntity.ok(dto)));
    }
}
