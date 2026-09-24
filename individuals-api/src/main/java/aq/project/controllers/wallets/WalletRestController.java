package aq.project.controllers.wallets;

import aq.project.controller.WalletRestControllerApi;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.services.wallets.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletRestController implements WalletRestControllerApi {

    private final WalletService walletService;

    @Override
    public Mono<ResponseEntity<UUID>> createWallet(
            Mono<CreateWalletRequestDto> createWalletRequestDto,
            ServerWebExchange exchange
    ) {
        return createWalletRequestDto.flatMap(dto -> walletService.createWallet(dto)
                .flatMap(walletId -> Mono.just(ResponseEntity.ok(walletId))));
    }

    @Override
    public Mono<ResponseEntity<WalletInfoResponseDto>> getWalletInfo(
            UUID walletId,
            ServerWebExchange exchange
    ) {
        return walletService.getWalletInfo(walletId)
                .flatMap(dto -> Mono.just(ResponseEntity.ok(dto)));
    }
}
