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
        log.info("Received request to create a new wallet for user with id: [{}]",
                createWalletRequestDto.getUserId());

        try {

            Wallet newWallet = walletMapper.toWallet(createWalletRequestDto);

            Wallet createdWallet = walletRepository.save(newWallet);

            UUID walletId = createdWallet.getId();

            log.info("Success create wallet with id: [{}] for person with id: [{}]",
                    walletId, createWalletRequestDto.getUserId());

            return walletId;

        } catch (Exception e) {
            log.error("Error occurred while create a new wallet for user with id: [{}]. Error message: {}",
                    createWalletRequestDto.getUserId(), e.getMessage());
            throw e;
        }
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
        log.info("Received request to get wallet info for wallet with id: [{}]", walletId);

        Wallet wallet = getWallet(walletId);

        WalletInfoResponseDto responseDto = walletMapper.toWalletInfoResponseDto(wallet);

        log.info("Success get wallet info for wallet with id: [{}]", walletId);

        return responseDto;
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
        log.info("Received request to get wallet currency code for wallet with id: [{}]", walletId);

        Wallet wallet = getWallet(walletId);

        String currencyCode = wallet.getWalletType().getCurrencyCode();

        log.info("Success get wallet currency code for wallet with id: [{}]", walletId);

        return currencyCode;
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

        log.info("Received request to deposit sufficient on wallet with id: [{}]", walletId);

        Wallet wallet = getWalletWithLock(walletId);

        if(wallet.isBlocked())
            onBlockedWallet(walletId, "deposit");

        BigDecimal amount = depositWalletRequestDto.getAmount();

        wallet.credit(amount);

        log.info("Success deposit sufficient on wallet with id: [{}]", walletId);

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
                "Exception occurred during deposit sufficient on wallet with id: [%s]. Exception message: %s",
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

        log.info("Received request to withdrawal sufficient from wallet with id: [{}]", walletId);

        Wallet wallet = getWalletWithLock(walletId);

        if(wallet.isBlocked())
            onBlockedWallet(walletId, "withdrawal");

        BigDecimal amount = withdrawalWalletRequestDto.getAmount();

        if(wallet.isBalanceExceedAmount(amount)) {
            wallet.debit(amount);

            log.info("Success withdrawal sufficient from wallet with id: [{}]", walletId);

            return wallet.getId();

        } else {
            throw new IllegalStateException(
                    String.format("Amount of withdrawal operation: [%s %s] is exceeded current balance of wallet",
                            amount, withdrawalWalletRequestDto.getCurrencyCode()));
        }
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
                "Exception occurred during withdrawal sufficient from wallet with id: [%s]. Exception message: %s",
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

        log.info("Received request to transfer sufficient from wallet with id: [{}] to wallet with id: [{}]",
                sourceWalletId, targetWalletId);

        Wallet sourceWallet = getWalletWithLock(sourceWalletId);
        Wallet targetWallet = getWalletWithLock(targetWalletId);

        if(sourceWallet.isBlocked())
            onBlockedWallet(sourceWalletId, "transfer");

        if(targetWallet.isBlocked())
            onBlockedWallet(targetWalletId, "transfer");

        BigDecimal amount = transferWalletRequestDto.getAmount();

        if(sourceWallet.isBalanceExceedAmount(amount)) {
            sourceWallet.debit(amount);
            targetWallet.credit(amount);

            log.info("Success transfer sufficient from wallet with id: [{}] to wallet with id: [{}]",
                    sourceWalletId, targetWalletId);

            return List.of(sourceWalletId, targetWalletId);

        } else {
            throw new IllegalStateException(
                    String.format("Amount of transfer operation: [%s %s] is exceeded current balance of source wallet",
                            amount, transferWalletRequestDto.getCurrencyCode()));
        }
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
                "Exception occurred during transfer sufficient from wallet with id: [%s] to wallet with id: [%s]. " +
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