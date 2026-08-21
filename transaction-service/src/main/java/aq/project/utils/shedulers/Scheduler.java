package aq.project.utils.shedulers;

import aq.project.dto.CancelTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.ExceedAttemptLimitException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.MessageBrokerHandler;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.telemetry.TraceContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    @Value("${spring.application.name}")
    private String serviceName;

    private final OpenTelemetry openTelemetry;

    private final TransactionRepository transactionRepository;

    private final MessageBrokerHandler messageBrokerHandler;

    private final PaymentServiceHandler paymentServiceHandler;

    private final TraceContext traceContext;

    @Scheduled(
            timeUnit = TimeUnit.SECONDS,
            fixedRateString = "${service.transaction-service.schedule.send-transaction-request-rate}"
    )
    public void scheduleSendTransactionRequestToMessageBroker() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.PENDING);
        for(Transaction transaction : transactions) {
            String traceId = transaction.getMetadata().getTraceId();
            traceContext.setTraceId(traceId);
            try {
                messageBrokerHandler.sendTransactionRequestToMessageBroker(transaction);
            } catch(ExceedAttemptLimitException | FallbackOperationException exc) {
                try {
                    sendCancelTransactionRequestToPaymentService(transaction);

                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setProcessed(true);
                    transactionRepository.save(transaction);
                } catch(Exception e) {
                    String message = String.format(
                            "Fatal error occurred while schedule of sending transaction with id: [%s]. Potential inconsistent state of transaction",
                            transaction.getId());

                    logException(traceId, message, e);
                }
                String message = String.format(
                        "Exception occurred while schedule of sending transaction with id: [%s]",
                        transaction.getId());

                logException(traceId, message, exc);
            } catch(Exception exc) {
                int retryCount = transaction.getMetadata().getRetryCount() + 1;
                transaction.getMetadata().setRetryCount(retryCount);
                transactionRepository.save(transaction);

                String message = String.format(
                        "Exception occurred while schedule of sending transaction with id: [%s]",
                        transaction.getId()
                );

                logException(traceId, message, exc);
            } finally {
                traceContext.clean();
            }
        }
    }

    private void sendCancelTransactionRequestToPaymentService(Transaction transaction) {
        CancelTransactionDto cancelTransactionDto = new CancelTransactionDto();
        cancelTransactionDto.setId(transaction.getId());
        cancelTransactionDto.setStatus(transaction.getStatus());

        paymentServiceHandler.sendCancelTransactionRequestToPaymentService(cancelTransactionDto);
    }

    private void logException(String traceId, String message, Exception exc) {
        String actionName = "schedule-send-transaction-request";
        String tracerName = serviceName + "." + actionName + "-tracer";
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(actionName).startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = span.getSpanContext().getTraceId();
            log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                    traceId, spanId, serviceName, actionName, message, exc.getMessage());
        } finally {
            span.end();
        }
    }
}
