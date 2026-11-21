package aq.project.services;

import aq.project.dto.OperationType;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.PaymentProviderCallback;
import aq.project.entities.UnknownCallback;
import aq.project.exceptions.UnknownOperationTypeException;
import aq.project.mappers.WebhookMapper;
import aq.project.repositories.PaymentProviderCallbackRepository;
import aq.project.repositories.UnknownCallbackRepository;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookMapper webhookMapper = WebhookMapper.INSTANCE;

    private final PaymentProviderCallbackRepository paymentProviderCallbackRepository;

    private final UnknownCallbackRepository unknownCallbackRepository;

    private final TraceContext traceContext;

    private final Fallback fallback;

    @RateLimiter(
            name = "confirm-operation-rate-limiter",
            fallbackMethod = "confirmOperationRateLimiterFallback"
    )
    @Transactional(noRollbackFor = UnknownOperationTypeException.class)
    public void confirmOperation(
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto
    ) {
        UUID transactionId = updateStatusOperationRequestDto.getTransactionId();

        OperationType operationType = updateStatusOperationRequestDto.getOperationType();

        switch (operationType) {
            case PAYOUT, TRANSACTION -> {
                boolean isIdempotentHandle = paymentProviderCallbackRepository
                        .findByTransactionId(transactionId)
                        .isEmpty();

                if(isIdempotentHandle) {
                    PaymentProviderCallback paymentProviderCallback = webhookMapper
                            .toPaymentProviderCallback(updateStatusOperationRequestDto);
                    paymentProviderCallback.setProcessed(false);

    //                The next step of handle callback will be on scheduler side.
    //                See WebhookScheduler.schedulePaymentProviderCallbacks(..) for details.
                    paymentProviderCallbackRepository.save(paymentProviderCallback);
                } else {
                    log.info("Received duplicate of update [{}] for transaction with id: [{}]",
                            operationType, transactionId);
                }
            }

            default -> {
                String traceId = traceContext.getTraceId() == null
                        ? TraceFactory.genTraceId()
                        : traceContext.getTraceId();

                log.warn("[{}]: received request with unknown operation type: [{}] for transaction with id: [{}]",
                        traceId, operationType, transactionId);

                UnknownCallback unknownCallback = webhookMapper
                        .toUnknownCallbackRepository(updateStatusOperationRequestDto);
                unknownCallback.setProcessed(false);

                unknownCallbackRepository.save(unknownCallback);

                throw new UnknownOperationTypeException(String.format(
                        "Received request with unknown operation type: [%s] for transaction with id: [%s]",
                        operationType, transactionId));
            }
        }
    }

    private void confirmOperationRateLimiterFallback(
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto,
            RequestNotPermitted exception
    ) {
        String action = "confirm-operation";

        UUID transactionId = updateStatusOperationRequestDto.getTransactionId();

        OperationType operationType = updateStatusOperationRequestDto.getOperationType();

        String message = String.format(
                "Exception occurred during handle of confirm [%s] for transaction with id: [%s]. Exception message: %s",
                operationType, transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);
    }
}