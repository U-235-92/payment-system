package aq.project.controllers;

import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallets")
public class WalletRestController {

    private final WalletService walletService;

    @PostMapping("/create")
    public Mono<ResponseEntity<String>> createWallet(@RequestBody CreateWalletRequestDTO createWalletRequestDTO) {
        return walletService.createWallet(createWalletRequestDTO)
                .flatMap(walletId -> Mono.just(ResponseEntity.ok(walletId)));
    }

    @GetMapping("/info/{walletId}")
    public Mono<ResponseEntity<WalletInfoResponseDTO>> getWalletInfo(@PathVariable("walletId") String walletId) {
        return walletService.getWalletInfo(walletId)
                .flatMap(dto -> Mono.just(ResponseEntity.ok(dto)));
    }
}
