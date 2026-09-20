package aq.project.utils.schedulers;

import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.utils.handlers.payment_provider_service.request.PaymentProviderServiceTransactionRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PaymentProviderServiceCreateTransactionScheduler {

    private final PaymentProviderServiceTransactionRequestHandler paymentProviderServiceTransactionRequestHandler;

    private final PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Scheduled(
            fixedRateString = "${service.transaction-service.schedule.handle-create-transaction-on-payment-provider-service}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void scheduleHandleCreateTransactionOnPaymentProviderService() {
        List<PaymentProviderServiceTransactionRequest> transactionRequests = paymentProviderServiceTransactionRequestRepository.findUnprocessedRequests();
        for(PaymentProviderServiceTransactionRequest transactionRequest : transactionRequests) {
            paymentProviderServiceTransactionRequestHandler.handlePaymentProviderServiceCreateTransactionRequest(transactionRequest);
        }
    }
}
