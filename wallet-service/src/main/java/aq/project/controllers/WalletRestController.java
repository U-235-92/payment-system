package aq.project.controllers;

import aq.project.controller.WalletRestControllerApi;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.entities.Wallet;
import aq.project.services.WalletService;
import aq.project.utils.mappers.WalletMapper;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WalletRestController implements WalletRestControllerApi {

    private final WalletMapper walletMapper;

    private final WalletService walletService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<String> createWallet(
            String xTraceId,
            CreateWalletRequestDto createWalletRequestDTO,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        String createdWalletId = walletService.createWallet(walletMapper.toWallet(createWalletRequestDTO));
        return ResponseEntity.ok(createdWalletId);
    }

    @Override
    public ResponseEntity<WalletInfoResponseDto> getWalletInfo(
            String id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        Wallet wallet = walletService.getWalletInfo(id);
        return ResponseEntity.ok(walletMapper.toWalletInfoResponseDto(wallet));
    }

    @Override
    public ResponseEntity<String> getWalletCurrency(
            String id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(walletService.getWalletCurrency(id));
    }
}
