package aq.project.controllers.wallet;

import aq.project.controller.WalletRestControllerApi;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.entities.wallet.Wallet;
import aq.project.services.wallet.WalletService;
import aq.project.utils.mappers.wallet.WalletMapper;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletRestController implements WalletRestControllerApi {

    private final WalletMapper walletMapper = WalletMapper.INSTANCE;

    private final WalletService walletService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<UUID> createWallet(
            String xTraceId,
            CreateWalletRequestDto createWalletRequestDTO,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        UUID createdWalletId = walletService.createWallet(walletMapper.toWallet(createWalletRequestDTO));
        return ResponseEntity.ok(createdWalletId);
    }

    @Override
    public ResponseEntity<WalletInfoResponseDto> getWalletInfo(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        Wallet wallet = walletService.getWallet(id);
        return ResponseEntity.ok(walletMapper.toWalletInfoResponseDto(wallet));
    }

    @Override
    public ResponseEntity<String> getWalletCurrency(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(walletService.getWalletCurrencyCode(id));
    }
}
