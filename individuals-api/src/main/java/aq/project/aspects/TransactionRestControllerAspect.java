package aq.project.aspects;

import aq.project.aspects.handlers.AbstractAspectHandler;
import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import aq.project.util.metrics.ApplicationMetricsRegistry;
import io.micrometer.core.annotation.Timed;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TransactionRestControllerAspect {

    @Value("${spring.application.name}")
    private String tracerName;

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    @Timed(value = "individuals_api.get_transaction_status_time")
    @Around("execution(* aq.project.controllers.TransactionRestController.getTransactionStatus(..)) && args(transactionId)")
    public Mono<ResponseEntity<TransactionStatus>> getTransactionStatus(ProceedingJoinPoint pjp, String transactionId) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Received transaction id is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return String.format("Received get transaction status request for transaction with id [%s]",
                        transactionId);
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return String.format("The handle of getting transaction status for transaction with id [%s] completed successfully",
                        transactionId);
            }
        };
        String spanName = "get_transaction_status";
        return handler.handleAspect(
                pjp,
                transactionId,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessGetTransactionStatusCounter,
                ApplicationMetricsRegistry::incrementFailGetTransactionStatusCounter
        );
    }

    @Timed(value = "individuals_api.do_transaction_time")
    @Around("execution(* aq.project.controllers.TransactionRestController.doTransaction(..)) && args(dto)")
    public Mono<ResponseEntity<String>> doTransaction(ProceedingJoinPoint pjp, TransactionRequestDTO dto) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Transaction request dto is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return String.format("Received %s transaction request",
                        dto.getOperationType().name().toLowerCase());
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return String.format("Handle of %s transaction request completed successfully",
                        dto.getOperationType().name().toLowerCase());
            }
        };
        String spanName = "create_wallet";
        return handler.handleAspect(
                pjp,
                dto,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessDoTransactionCounter,
                ApplicationMetricsRegistry::incrementFailDoTransactionCounter
        );
    }
}
