package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Wallet;
import aq.project.entities.WalletOperation;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.mappers.OperationMapper;
import aq.project.mappers.WalletMapper;
import aq.project.repositories.WalletOperationRepository;
import aq.project.repositories.WalletRepository;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper = WalletMapper.INSTANCE;

    private final OperationMapper operationMapper = OperationMapper.INSTANCE;

    private final WalletRepository walletRepository;

    private final WalletOperationRepository walletOperationRepository;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-wallet-rate-limiter",
            fallbackMethod = "createWalletFallback"
    )
    @Transactional
    public UUID createWallet(
            CreateWalletRequestDto createWalletRequestDto
    ) {
        try {

            Wallet newWallet = walletMapper.toWallet(createWalletRequestDto);

            Wallet createdWallet = walletRepository.save(newWallet);

            return createdWallet.getId();

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

        String exceptionClass = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "[%s] occurred during create of wallet for person with id: [%s]. Exception message: %s",
                exceptionClass, userId, exceptionMessage);

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

        String exceptionClass = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "[%s] occurred during getting wallet info for wallet with id: [%s]. Exception message: %s",
                exceptionClass, walletId, exceptionMessage);

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

        String exceptionClass = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "[%s] occurred during getting wallet currency code for wallet with id: [%s]. Exception message: %s",
                exceptionClass, walletId, exceptionMessage);

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
        UUID walletOperationId = depositWalletRequestDto.getOperationId();

        Optional<WalletOperation> walletOperationOptional = walletOperationRepository.findById(walletOperationId);

//        Prevent handle of duplicate request (idempotent operation)
        if(walletOperationOptional.isPresent())
            return walletOperationOptional.get()
                    .getWallet()
                    .getId();

        UUID walletId = depositWalletRequestDto.getWalletId();

        String requestCurrencyCode = depositWalletRequestDto.getCurrencyCode();

        log.debug(">>>>> Received request to deposit wallet with id: [{}]", walletId);

        Wallet wallet = getWalletWithLock(walletId);

        String walletCurrencyCode = wallet.getWalletType().getCurrencyCode();

        if(!walletCurrencyCode.equals(requestCurrencyCode))
            throw new IllegalStateException(String.format(
                    "Received request with mismatch request currency code: [%s] and wallet currency code: [%s] " +
                            "for wallet with id: [%s]", requestCurrencyCode, walletCurrencyCode, wallet.getId()));

        WalletOperation walletOperation = operationMapper.toWalletOperation(depositWalletRequestDto);

        walletOperation.setWallet(wallet);

        walletOperationRepository.save(walletOperation);

        if(wallet.isBlocked())
            onBlockedWallet(walletId, "deposit");

        BigDecimal amount = depositWalletRequestDto.getAmount();

        log.debug(">>>>> Current balance of wallet: [{} {}] before deposit",
                wallet.getBalance(), wallet.getWalletType().getCurrencyCode());

        log.debug(">>>>> Current amount of transaction: [{} {}]",
                depositWalletRequestDto.getAmount(), wallet.getWalletType().getCurrencyCode());

        log.debug(">>>>> Attempt to deposit wallet with id: [{}]", walletId);

        wallet.credit(amount);

        log.debug(">>>>> Current balance of wallet: [{} {}] after deposit",
                wallet.getBalance(), wallet.getWalletType().getCurrencyCode());

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

        String exceptionClass = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "[%s] occurred during deposit sufficient on wallet with id: [%s]. Exception message: %s",
                exceptionClass, depositWalletRequestDto.getWalletId(), exceptionMessage);

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
        UUID walletOperationId = withdrawalWalletRequestDto.getOperationId();

        Optional<WalletOperation> walletOperationOptional = walletOperationRepository.findById(walletOperationId);

//        Prevent handle of duplicate request (idempotent operation)
        if(walletOperationOptional.isPresent())
            return walletOperationOptional.get()
                    .getWallet()
                    .getId();

        UUID walletId = withdrawalWalletRequestDto.getWalletId();

        String requestCurrencyCode = withdrawalWalletRequestDto.getCurrencyCode();

        log.debug(">>>>> Received request to withdrawal wallet with id: [{}]", walletId);

        Wallet wallet = getWalletWithLock(walletId);

        String walletCurrencyCode = wallet.getWalletType().getCurrencyCode();

        if(!walletCurrencyCode.equals(requestCurrencyCode))
            throw new IllegalStateException(String.format(
                    "Received request with mismatch request currency code: [%s] and wallet currency code: [%s] " +
                            "for wallet with id: [%s]", requestCurrencyCode, walletCurrencyCode, wallet.getId()));

        WalletOperation walletOperation = operationMapper.toWalletOperation(withdrawalWalletRequestDto);

        walletOperation.setWallet(wallet);

        walletOperationRepository.save(walletOperation);

        if(wallet.isBlocked())
            onBlockedWallet(walletId, "withdrawal");

        BigDecimal amount = withdrawalWalletRequestDto.getAmount();

        log.debug(">>>>> Current balance of wallet: [{} {}] before withdrawal",
                wallet.getBalance(), wallet.getWalletType().getCurrencyCode());

        log.debug(">>>>> Current amount of transaction: [{} {}]",
                withdrawalWalletRequestDto.getAmount(), wallet.getWalletType().getCurrencyCode());

        log.debug(">>>>> Attempt to withdrawal wallet with id: [{}]", walletId);

        if(wallet.isBalanceExceedAmount(amount)) {
            wallet.debit(amount);

            log.debug(">>>>> Current balance of wallet: [{} {}] after withdrawal",
                    wallet.getBalance(), wallet.getWalletType().getCurrencyCode());

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

        String exceptionClass = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "[%s] occurred during withdrawal sufficient from wallet with id: [%s]. Exception message: %s",
                exceptionClass, withdrawalWalletRequestDto.getWalletId(), exceptionMessage);

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
        UUID walletOperationId = transferWalletRequestDto.getOperationId();

        Optional<WalletOperation> walletOperationOptional = walletOperationRepository.findById(walletOperationId);

//        Prevent handle of duplicate request (idempotent operation)
        if(walletOperationOptional.isPresent())
            return List.of(
                    walletOperationOptional.get().getWallet().getId(),
                    walletOperationOptional.get().getTargetWallet().getId()
            );

        String requestCurrencyCode = transferWalletRequestDto.getCurrencyCode();

        UUID sourceWalletId = transferWalletRequestDto.getSourceWalletId();
        UUID targetWalletId = transferWalletRequestDto.getTargetWalletId();

        log.info("Received request to transfer sufficient from wallet with id: [{}] to wallet with id: [{}]",
                sourceWalletId, targetWalletId);

        Wallet sourceWallet = getWalletWithLock(sourceWalletId);
        Wallet targetWallet = getWalletWithLock(targetWalletId);

        String sourceWalletCurrencyCode = sourceWallet.getWalletType().getCurrencyCode();
        String targetWalletCurrencyCode = targetWallet.getWalletType().getCurrencyCode();

        if(!sourceWalletCurrencyCode.equals(requestCurrencyCode))
            throw new IllegalStateException(String.format(
                    "Received request with mismatch request currency code: [%s] and wallet currency code: [%s] " +
                            "for wallet with id: [%s]", requestCurrencyCode, sourceWalletCurrencyCode, sourceWallet.getId()));

        if(!targetWalletCurrencyCode.equals(requestCurrencyCode))
            throw new IllegalStateException(String.format(
                    "Received request with mismatch request currency code: [%s] and wallet currency code: [%s] " +
                            "for wallet with id: [%s]", requestCurrencyCode, sourceWalletCurrencyCode, targetWallet.getId()));

        WalletOperation walletOperation = operationMapper.toWalletOperation(transferWalletRequestDto);

        walletOperation.setWallet(sourceWallet);
        walletOperation.setTargetWallet(targetWallet);

        walletOperationRepository.save(walletOperation);

        if(sourceWallet.isBlocked())
            onBlockedWallet(sourceWalletId, "transfer");

        if(targetWallet.isBlocked())
            onBlockedWallet(targetWalletId, "transfer");

        BigDecimal walletAmount = transferWalletRequestDto.getAmount();
        BigDecimal targetWalletAmount = transferWalletRequestDto.getTargetAmount();

        if(sourceWallet.isBalanceExceedAmount(walletAmount)) {
            sourceWallet.debit(walletAmount);
            targetWallet.credit(targetWalletAmount);

            log.info("Success transfer sufficient from wallet with id: [{}] to wallet with id: [{}]",
                    sourceWalletId, targetWalletId);

            return List.of(sourceWalletId, targetWalletId);

        } else {
            throw new IllegalStateException(
                    String.format("Amount of transfer operation: [%s %s] is exceeded current balance of source wallet",
                            walletAmount, transferWalletRequestDto.getCurrencyCode()));
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

        String exceptionClass = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "[%s] occurred during transfer sufficient from wallet with id: [%s] to wallet with id: [%s]. " +
                "Exception message: %s",
                exceptionClass,
                transferWalletRequestDto.getSourceWalletId(),
                transferWalletRequestDto.getTargetWalletId(),
                exceptionMessage
        );

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