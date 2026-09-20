package aq.project.test.schedulers.unit;

import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.utils.handlers.payment_provider_service.request.PaymentProviderServiceTransactionRequestHandler;
import aq.project.utils.schedulers.PaymentProviderServiceCreateTransactionScheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getValidPaymentProviderServiceTransactionRequest;

@ExtendWith(MockitoExtension.class)
public class ScheduleHandleCreateTransactionOnPaymentProviderServiceUnitTest {

    @Mock
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Mock
    private PaymentProviderServiceTransactionRequestHandler paymentProviderServiceTransactionRequestHandler;

    @InjectMocks
    private PaymentProviderServiceCreateTransactionScheduler paymentProviderServiceCreateTransactionScheduler;

    @Test
    public void successScheduleHandleCreateTransactionOnPaymentProviderService() {
//        Arrange
        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = getValidPaymentProviderServiceTransactionRequest();
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        Mockito.when(paymentProviderServiceTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(paymentProviderServiceTransactionRequest));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceCreateTransactionScheduler.scheduleHandleCreateTransactionOnPaymentProviderService());

        Mockito.verify(paymentProviderServiceTransactionRequestHandler, Mockito.times(1))
                .handlePaymentProviderServiceCreateTransactionRequest(Mockito.any());
    }

    @Test
    public void failedScheduleHandleCreateTransactionOnPaymentProviderServiceWhenEmptyList() {
//        Arrange
        Mockito.when(paymentProviderServiceTransactionRequestRepository.findUnprocessedRequests()).thenReturn(List.of());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceCreateTransactionScheduler.scheduleHandleCreateTransactionOnPaymentProviderService());

        Mockito.verify(paymentProviderServiceTransactionRequestHandler, Mockito.never())
                .handlePaymentProviderServiceCreateTransactionRequest(Mockito.any());
    }
}
