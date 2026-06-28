package aq.project.util.handlers;

import aq.project.clients.CurrencyRateServiceClient;
import aq.project.dto.RateResponse;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.util.mappers.TransactionMapper;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WalletRepository;
import aq.project.util.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class CommonRequestHandler {

    protected final WalletRepository walletRepository;

    private final CurrencyRateServiceClient currencyRateServiceClient;

    private final TransactionWriter transactionWriter;

    private final TraceContext traceContext;

    @Transactional
    public void handleRequestMessage(TransactionRequest transactionRequest,
                                           TransactionMapper transactionMapper,
                                           TransactionRepository transactionRepository
    ) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        try {
            checkTransactionRequestPropertyConstrains(transactionRequest);
            handleTransactionRequestOperation(transactionRequest);
            transactionWriter.commitCompleteTransaction(transactionRepository, transactionMapper.toTransaction(transactionRequest));
        } catch (WalletConstrainsException | CreditCardConstrainsException | NoSuchWalletException e) {
            transactionWriter.commitFailedTransaction(transactionRepository, transactionMapper.toTransaction(transactionRequest));
            throw e;
        }
    }

    protected void checkTransactionRequestPropertyConstrains(TransactionRequest transactionRequest) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        throw new UnsupportedOperationException();
    }

    protected void handleTransactionRequestOperation(TransactionRequest transactionRequest) {
        throw new UnsupportedOperationException();
    }

    protected final BigDecimal getConversionRate(String sourceCurrency, String destinationCurrency) {
        String traceId = traceContext.getTraceId();
        RateResponse rateResponse = currencyRateServiceClient.getRates(sourceCurrency, destinationCurrency, traceId);
        return rateResponse.getRate();
    }
}
