package aq.project.controllers;

import aq.project.controller.WalletRestControllerApi;
import aq.project.dto.*;
import aq.project.services.WalletService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletServiceRestController implements WalletRestControllerApi {

    private final WalletService walletService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<UUID> createWallet(
            String xTraceId,
            CreateWalletRequestDto createWalletRequestDto,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(createWalletRequestDto));
    }

    @Override
    public ResponseEntity<WalletInfoResponseDto> getWalletInfo(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(walletService.getWalletInfo(id));
    }

    @Override
    public ResponseEntity<String> getWalletCurrency(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(walletService.getWalletCurrencyCode(id));
    }

    @Override
    public ResponseEntity<UUID> depositWallet(
            String xTraceId,
            DepositWalletRequestDto depositWalletRequestDto,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(walletService.depositWallet(depositWalletRequestDto));
    }

    @Override
    public ResponseEntity<UUID> withdrawalWallet(
            String xTraceId,
            WithdrawalWalletRequestDto withdrawalWalletRequestDto,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(walletService.withdrawalWallet(withdrawalWalletRequestDto));
    }

    @Override
    public ResponseEntity<List<UUID>> transferWallet(
            String xTraceId,
            TransferWalletRequestDto transferWalletRequestDto,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(walletService.transferWallet(transferWalletRequestDto));
    }
}
