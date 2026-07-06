package aq.project.services;

import aq.project.clients.TransactionClient;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static aq.project.util.constants.RequestPropertyKeys.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionClient transactionClient;

    private final WalletService walletService;

    private final RateService rateService;

    public Mono<TransactionStatus> getTransactionStatus(String transactionId) {
        return transactionClient.getTransactionStatus(transactionId);
    }

    public Mono<String> doTransaction(TransactionRequestDTO transactionRequestDTO) {
        return Mono.just(transactionRequestDTO).flatMap(dto -> {
            dto.setTimestamp(System.currentTimeMillis());
            return switch (dto.getOperationType()) {
                case OperationType.WITHDRAW, OperationType.DEPOSIT -> walletService
                        .getWalletCurrencyCode(dto.getProperties().get(RECIPIENT_WALLET_ID))
                        .flatMap(walletCurrencyCode ->
                                getRate(
                                        dto.getCurrency(),
                                        walletCurrencyCode,
                                        dto.getRateProvider(),
                                        dto.getCurrencyRateDate()
                                ))
                        .flatMap(rate -> {
                            dto.getProperties().put(RECIPIENT_CURRENCY_RATE, rate);
                            return transactionClient.doTransaction(dto);
                        });
                case OperationType.TRANSFER -> walletService
                        .getWalletCurrencyCode(dto.getProperties().get(RECIPIENT_WALLET_ID))
                        .flatMap(recipientCurrencyCode -> walletService
                                .getWalletCurrencyCode(dto.getProperties().get(SENDER_WALLET_ID))
                                .flatMap(senderCurrencyCode -> getRate(dto.getCurrency(), recipientCurrencyCode, dto.getRateProvider(), dto.getCurrencyRateDate())
                                        .flatMap(recipientCurrencyRate -> getRate(dto.getCurrency(), senderCurrencyCode, dto.getRateProvider(), dto.getCurrencyRateDate())
                                                .flatMap(senderCurrencyRate -> {
                                                    dto.getProperties().put(RECIPIENT_CURRENCY_RATE, recipientCurrencyRate);
                                                    dto.getProperties().put(SENDER_CURRENCY_RATE, senderCurrencyRate);
                                                    return transactionClient.doTransaction(dto);
                                                })
                                        )
                                )
                        );
            };
        });
    }

    private Mono<String> getRate(String from, String to, String provider, LocalDate date) {
        return rateService.getRate(from, to, provider, date)
                .map(rateResponse -> {
                    if(rateResponse.getRate() == null)
                        throw new IllegalStateException(String.format("Error occurred during getting rate of [%s -> %s]",
                                from, to));
                    return String.valueOf(rateResponse.getRate());
                });
    }
}
