package aq.project.services;

import aq.project.entities.PaymentProviderCallback;
import aq.project.repositories.PaymentProviderCallbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private final PaymentProviderCallbackRepository paymentProviderCallbackRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessed(PaymentProviderCallback paymentProviderCallback) {
        log.debug(">>>>> Attempt to mark as [PROCESSED] callback for transaction with id: [{}]",
                paymentProviderCallback.getTransactionId());

        paymentProviderCallback.markProcessed();

        paymentProviderCallbackRepository.save(paymentProviderCallback);

        log.debug(">>>>> Successful mark as [PROCESSED] callback for transaction with id: [{}]",
                paymentProviderCallback.getTransactionId());
    }
}