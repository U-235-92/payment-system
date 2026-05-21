package aq.project.services;

import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.proxies.WalletClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletClient walletClient;

    public Mono<String> createWallet(CreateWalletRequestDTO createWalletRequestDTO) {
        return walletClient.createWallet(createWalletRequestDTO);

    }

    public Mono<WalletInfoResponseDTO> getWalletInfo(String walletId) {
        return walletClient.getWalletInfo(walletId);
    }
}
