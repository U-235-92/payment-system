package aq.project.utils.schedulers;

import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.utils.handlers.payment_provider_service.request.PaymentProviderServiceTransactionRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PaymentProviderServiceCreateTransactionScheduler {

    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;

    private final PaymentProviderServiceTransactionRequestHandler paymentProviderServiceTransactionRequestHandler;

    private final PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Scheduled(
            fixedRateString = "${service.transaction-service.schedule.handle-create-transaction-on-payment-provider-service}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void scheduleHandleTransactionRequestOnPaymentProviderService() {
        List<PaymentProviderServiceTransactionRequest> transactionRequests = paymentProviderServiceTransactionRequestRepository
                .findUnprocessedRequests();

        for(PaymentProviderServiceTransactionRequest transactionRequest : transactionRequests) {
            Long requestId = transactionRequest.getId();
            TransactionStatus transactionStatus = transactionRequest.getStatus();

            switch (transactionStatus) {
                case PENDING -> paymentProviderServiceTransactionRequestHandler
                        .handlePaymentProviderServiceCreateTransactionRequest(transactionRequest);

                case FAILED -> paymentProviderServiceTransactionRequestHandler
                        .handlePaymentProviderServiceFailTransactionRequest(transactionRequest);

                default -> throw new ProcessOperationException(String.format(
                                "Attempt to schedule transaction request with id: [%s] in status: [%s] " +
                                "for service: [%s] which is prohibited",
                                requestId, transactionStatus, paymentProviderServiceName));
            }
        }
    }
}
