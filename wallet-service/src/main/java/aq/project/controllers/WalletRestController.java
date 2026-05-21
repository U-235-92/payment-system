package aq.project.controllers;

import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.entities.Wallet;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.mappers.WalletMapper;
import aq.project.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallets")
public class WalletRestController {

    private final WalletMapper walletMapper;
    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<String> createWallet(@RequestBody CreateWalletRequestDTO createWalletRequestDTO) {
        String createdWalletId = walletService.createWallet(walletMapper.toWallet(createWalletRequestDTO));
        return ResponseEntity.ok(createdWalletId);
    }

    @GetMapping("/info/{walletId}")
    public ResponseEntity<WalletInfoResponseDTO> getWalletInfo(@PathVariable("walletId") String walletId) throws NoSuchWalletException {
        Wallet wallet = walletService.getWalletInfo(walletId);
        return ResponseEntity.ok(walletMapper.toWalletInfoResponseDto(wallet));
    }
}
