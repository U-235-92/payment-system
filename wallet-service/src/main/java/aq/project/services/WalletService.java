package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Wallet;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.mappers.WalletMapper;
import aq.project.repositories.WalletRepository;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper = WalletMapper.INSTANCE;

    private final WalletRepository walletRepository;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-wallet-rate-limiter",
            fallbackMethod = "createWalletFallback"
    )
    @Transactional
    public UUID createWallet(
            CreateWalletRequestDto createWalletRequestDto
    ) {
        Wallet newWallet = walletMapper.toWallet(createWalletRequestDto);

        Wallet createdWallet = walletRepository.save(newWallet);
        return createdWallet.getId();
    }

    private UUID createWalletFallback(
            CreateWalletRequestDto createWalletRequestDto,
            Exception exception
    ) {
        String action = "create-wallet";
        UUID userId = createWalletRequestDto.getUserId();
        String message = String.format(
                "Exception occurred during create of wallet for person with id: [%s]. Exception message: %s",
                userId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-wallet-info-rate-limiter",
            fallbackMethod = "getWalletInfoFallback"
    )
    public WalletInfoResponseDto getWalletInfo(
            UUID walletId
    ) {
        Wallet wallet = getWallet(walletId);

        return walletMapper.toWalletInfoResponseDto(wallet);
    }

    private WalletInfoResponseDto getWalletInfoFallback(
            UUID walletId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-wallet-info";
        String message = String.format(
                "Exception occurred during getting wallet info for wallet with id: [%s]. Exception message: %s",
                walletId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-wallet-currency-code-rate-limiter",
            fallbackMethod = "getWalletCurrencyCodeFallback"
    )
    public String getWalletCurrencyCode(
            UUID walletId
    ) {
        Wallet wallet = getWallet(walletId);

        return wallet.getWalletType().getCurrencyCode();
    }

    private String getWalletCurrencyCodeFallback(
            UUID walletId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-wallet-currency-code";
        String message = String.format(
                "Exception occurred during getting wallet currency code for wallet with id: [%s]. Exception message: %s",
                walletId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "deposit-wallet-rate-limiter",
            fallbackMethod = "depositWalletFallback"
    )
    @Transactional
    public UUID depositWallet(
            DepositWalletRequestDto depositWalletRequestDto
    ) {
        UUID walletId = depositWalletRequestDto.getWalletId();

        Wallet wallet = getWalletWithLock(walletId);

        if(wallet.isBlocked())
            onBlockedWallet(walletId, "deposit");

        BigDecimal amount = depositWalletRequestDto.getAmount();

        wallet.credit(amount);

        return wallet.getId();
    }

    private UUID depositWalletFallback(
            DepositWalletRequestDto depositWalletRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof IllegalStateException
                || exception instanceof EntityNotFoundException)
            throw exception;

        String action = "deposit-wallet";
        String message = String.format(
                "Exception occurred during deposit wallet with id: [%s]. Exception message: %s",
                depositWalletRequestDto.getWalletId(), exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "withdrawal-wallet-rate-limiter",
            fallbackMethod = "withdrawalWalletFallback"
    )
    @Transactional
    public UUID withdrawalWallet(
            WithdrawalWalletRequestDto withdrawalWalletRequestDto
    ) {
        UUID walletId = withdrawalWalletRequestDto.getWalletId();

        Wallet wallet = getWalletWithLock(walletId);

        if(wallet.isBlocked())
            onBlockedWallet(walletId, "withdrawal");

        BigDecimal amount = withdrawalWalletRequestDto.getAmount();

        wallet.debit(amount);

        return wallet.getId();
    }

    private UUID withdrawalWalletFallback(
            WithdrawalWalletRequestDto withdrawalWalletRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof IllegalStateException
                || exception instanceof EntityNotFoundException)
            throw exception;

        String action = "withdrawal-wallet";
        String message = String.format(
                "Exception occurred during withdrawal wallet with id: [%s]. Exception message: %s",
                withdrawalWalletRequestDto.getWalletId(), exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "transfer-wallet-rate-limiter",
            fallbackMethod = "transferWalletFallback"
    )
    @Transactional
    public List<UUID> transferWallet(
            TransferWalletRequestDto transferWalletRequestDto
    ) {
        UUID sourceWalletId = transferWalletRequestDto.getSourceWalletId();
        UUID targetWalletId = transferWalletRequestDto.getTargetWalletId();

        Wallet sourceWallet = getWalletWithLock(sourceWalletId);
        Wallet targetWallet = getWalletWithLock(targetWalletId);

        if(sourceWallet.isBlocked())
            onBlockedWallet(sourceWalletId, "transfer");

        if(targetWallet.isBlocked())
            onBlockedWallet(targetWalletId, "transfer");

        BigDecimal amount = transferWalletRequestDto.getAmount();

        sourceWallet.debit(amount);
        targetWallet.credit(amount);

        return List.of(sourceWalletId, targetWalletId);
    }

    private List<UUID> transferWalletFallback(
            TransferWalletRequestDto transferWalletRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof IllegalStateException
                || exception instanceof EntityNotFoundException)
            throw exception;

        String action = "transfer-wallet";
        String message = String.format(
                "Exception occurred during transfer from wallet with id: [%s] to wallet with id: [%s]. " +
                "Exception message: %s",
                transferWalletRequestDto.getSourceWalletId(),
                transferWalletRequestDto.getTargetWalletId(),
                exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    private Wallet getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Wallet with id: [%s] not found", walletId)));
    }

    private Wallet getWalletWithLock(UUID walletId) {
        return walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Wallet with id: [%s] not found", walletId)));
    }

    private void onBlockedWallet(UUID walletId, String operation) {
        throw new IllegalStateException(String.format("Process [%s] is not allowed for wallet with id: [%s]. " +
                "Wallet is blocked", operation, walletId));
    }
}