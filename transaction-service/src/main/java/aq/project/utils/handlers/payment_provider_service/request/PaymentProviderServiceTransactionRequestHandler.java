package aq.project.utils.handlers.payment_provider_service.request;

import aq.project.dto.PaymentProviderServiceCreateTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.properties.PaymentProviderServiceKafkaProperties;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProviderServiceTransactionRequestHandler {

    @Value("${spring.application.name}")
    private String serviceName;

    private final PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    private final PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;

    private final TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;

    private final PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentProviderServiceCreateTransactionRequest(
            PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest
    ) {
        String traceId = paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().getTraceId();
        String operation = paymentProviderServiceTransactionRequest.getOperation().getValue().toLowerCase();
        String action = String.format("schedule-handle-create-%s-transaction-on-payment-provider-service", operation);
        String tracerName = serviceName + "." + action + "-tracer";

        UUID transactionId = paymentProviderServiceTransactionRequest.getTransactionId();

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();
        String logMessageOnReceive = String.format(
                "Start scheduled handle creation of %s transaction with id: [%s] on payment provider service",
                operation, transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        PaymentProviderServiceCreateTransactionRequestDto createTransactionRequestPaymentProviderServiceDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceCreateTransactionRequestDto(paymentProviderServiceTransactionRequest);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();
        try(Scope scope = span.makeCurrent()) {
            String paymentProviderServiceCreateTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_create_transaction_request");

            kafkaTemplate.send(paymentProviderServiceCreateTransactionRequestTopic, createTransactionRequestPaymentProviderServiceDto).get();

            String logMessageOnSuccess = String.format(
                    "Success scheduled handle creation of %s transaction with id: [%s] on payment provider service",
                    operation, transactionId);

            log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnSuccess);

            applicationMetricsRegistry.countAction(true, action);
        } catch (ExecutionException | InterruptedException | NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while schedule handle creation of %s transaction with id: [%s] " +
                            "on payment provider service",
                    operation, transactionId);

            log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                    traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

            applicationMetricsRegistry.countAction(false, action);

            Optional<TransactionServiceDepositTransaction> transactionServiceDepositTransactionOptional = transactionServiceDepositTransactionRepository.findById(transactionId);

            if(transactionServiceDepositTransactionOptional.isPresent()) {
                TransactionServiceDepositTransaction transactionServiceDepositTransaction = transactionServiceDepositTransactionOptional.get();
                transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

                transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);
            } else {
                String onErrorLogMessage = String.format(
                        "Error occurred while handle transaction with id: [%s]. Transaction wasn't found",
                        transactionId);

                log.error("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, onErrorLogMessage);
            }
//              TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
        } finally {
            paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(true);

            paymentProviderServiceTransactionRequestRepository.save(paymentProviderServiceTransactionRequest);

            applicationMetricsRegistry.finishTimer(sample, action);
            span.end();
        }
    }
}
