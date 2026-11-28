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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletServiceRestController implements WalletRestControllerApi {

    private final WalletService walletService;

    @Override
    public Mono<ResponseEntity<UUID>> createWallet(
            Mono<CreateWalletRequestDto> createWalletRequestDto,
            ServerWebExchange exchange
    ) {
        return createWalletRequestDto
                .flatMap(walletService::createWallet)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<WalletInfoResponseDto>> getWalletInfo(
            UUID walletId,
            ServerWebExchange exchange
    ) {
        return walletService.getWalletInfo(walletId)
                .map(ResponseEntity::ok);
    }
}