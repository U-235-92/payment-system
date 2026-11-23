package aq.project.services;

import aq.project.dto.OperationType;
import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.PaymentProviderCallback;
import aq.project.entities.UnknownCallback;
import aq.project.exceptions.UnknownOperationStatusException;
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

import java.util.Optional;
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

        Status requestStatus = updateStatusOperationRequestDto.getStatus();

        String pendingOperationType = updateStatusOperationRequestDto.getOperationType();

        log.debug(">>>>> Received request for handle operation: [{}] in status: [{}] for transaction id: [{}]",
                pendingOperationType, requestStatus, transactionId);

        switch (requestStatus) {
            case SUCCESS, FAIL -> {
                try {
                    OperationType operationType = OperationType.valueOf(pendingOperationType);

                    switch (operationType) {
                        case PAYOUT, TRANSACTION -> handleKnownOperationRequest(updateStatusOperationRequestDto);

                        default -> handleUnknownOperationRequest(updateStatusOperationRequestDto);
                    }
                } catch (IllegalArgumentException e) {

                    handleUnknownOperationRequest(updateStatusOperationRequestDto);
                }
            }

            default -> {
                String msg = String.format(
                        "Received request for handle operation: [%s] in prohibited status: [%s] " +
                                "for transaction id: [%s]", pendingOperationType, requestStatus, transactionId);

                throw new UnknownOperationStatusException(msg);
            }
        }
    }

    private void confirmOperationRateLimiterFallback(
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto,
            RequestNotPermitted exception
    ) {
        String action = "confirm-operation";

        UUID transactionId = updateStatusOperationRequestDto.getTransactionId();

        String operationType = updateStatusOperationRequestDto.getOperationType();

        String exceptionType = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during handle of confirm [%s] for transaction with id: [%s]. " +
                        "Exception message: %s", exceptionType, operationType, transactionId, exceptionMessage);

        fallback.handleRateLimiterFallback(action, message, exception);
    }

    private void handleKnownOperationRequest(
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto
    ) {
        UUID transactionId = updateStatusOperationRequestDto.getTransactionId();
        UUID operationId = updateStatusOperationRequestDto.getId();

        OperationType operationType = OperationType.valueOf(updateStatusOperationRequestDto.getOperationType());

        Optional<PaymentProviderCallback> paymentProviderCallbackOptional = paymentProviderCallbackRepository
                .findById(operationId);

//        Check idempotent request
        if(paymentProviderCallbackOptional.isEmpty()) {

            paymentProviderCallbackOptional = paymentProviderCallbackRepository.findByTransactionId(transactionId);

//            Check if request for handle transaction by its id wasn't handled
            if(paymentProviderCallbackOptional.isEmpty()) {
                Status targetStatus = updateStatusOperationRequestDto.getStatus();

                PaymentProviderCallback paymentProviderCallback = webhookMapper
                        .toPaymentProviderCallback(updateStatusOperationRequestDto);

                paymentProviderCallback.setProcessed(false);

//            The next step of handle callback will be on scheduler side.
//            See WebhookScheduler.schedulePaymentProviderCallbacks(...) for details.
                paymentProviderCallbackRepository.save(paymentProviderCallback);

                log.debug(">>>>> Success process of request to handle operation: [{}] with id: [{}] " +
                                "in status: [{}] for transaction id: [{}]",
                        operationType, operationId, targetStatus, transactionId);
            } else {
//            Received duplicate of operation request.
//            There is nothing to do just log event.
                PaymentProviderCallback paymentProviderCallback = paymentProviderCallbackOptional.get();

                Status targetStatus = updateStatusOperationRequestDto.getStatus();
                Status currentStatus = paymentProviderCallback.getWebhookStatus();

                handleDuplicateKnownOperationRequest(targetStatus, currentStatus, operationType, operationId, transactionId);
            }
        } else {
//            Received duplicate of operation request.
//            There is nothing to do just log event.
            PaymentProviderCallback paymentProviderCallback = paymentProviderCallbackOptional.get();

            Status targetStatus = updateStatusOperationRequestDto.getStatus();
            Status currentStatus = paymentProviderCallback.getWebhookStatus();

            handleDuplicateKnownOperationRequest(targetStatus, currentStatus, operationType, operationId, transactionId);
        }
    }

    private void handleDuplicateKnownOperationRequest(
            Status targetStatus,
            Status currentStatus,
            OperationType operationType,
            UUID operationId,
            UUID transactionId
    ) {
        if(targetStatus.equals(currentStatus)) {
            log.info("Received duplicate request for update of [{}] operation with id: [{}] " +
                            "for transaction with id: [{}] in status: [{}]",
                    operationType, operationId, transactionId, targetStatus);
        } else {
            log.warn("Received duplicate request for update of [{}] operation with id: [{}] " +
                            "for transaction with id: [{}] in status: [{}] which doesn't match with " +
                            "current webhook status: [{}]",
                    operationType, operationId, transactionId, targetStatus, currentStatus);
        }
    }

    private void handleUnknownOperationRequest(
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto
    ) {
        UUID transactionId = updateStatusOperationRequestDto.getTransactionId();

        String operationType = updateStatusOperationRequestDto.getOperationType();

        String traceId = traceContext.getTraceId() == null
                ? TraceFactory.genTraceId()
                : traceContext.getTraceId();

        log.warn("[{}]: received request with unknown operation type: [{}] for transaction with id: [{}]",
                traceId, operationType, transactionId);

        UnknownCallback unknownCallback = webhookMapper
                .toUnknownCallbackRepository(updateStatusOperationRequestDto);
        unknownCallback.setProcessed(false);

        unknownCallbackRepository.save(unknownCallback);

        String msg = String.format("Received request with unknown operation type: [%s] " +
                "for transaction with id: [%s]", operationType, transactionId);

        throw new UnknownOperationTypeException(msg);
    }
}