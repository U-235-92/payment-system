package aq.project.controllers;

import aq.project.controller.WalletRestControllerApi;
import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.entities.Wallet;
import aq.project.mappers.WalletMapper;
import aq.project.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WalletRestController implements WalletRestControllerApi {

    private final WalletMapper walletMapper;
    private final WalletService walletService;

    public ResponseEntity<String> createWallet(CreateWalletRequestDTO createWalletRequestDTO) {
        String createdWalletId = walletService.createWallet(walletMapper.toWallet(createWalletRequestDTO));
        return ResponseEntity.ok(createdWalletId);
    }

    public ResponseEntity<WalletInfoResponseDTO> getWalletInfo(String id) {
        Wallet wallet = walletService.getWalletInfo(id);
        return ResponseEntity.ok(walletMapper.toWalletInfoResponseDto(wallet));
    }
}
