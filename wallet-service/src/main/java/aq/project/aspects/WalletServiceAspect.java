package aq.project.aspects;

import aq.project.entities.CreditCard;
import aq.project.entities.Wallet;
import aq.project.entities.WalletDetails;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.metrics.ApplicationMeterRegistry;
import aq.project.repositories.WalletRepository;
import io.micrometer.core.annotation.Timed;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Matcher;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class WalletServiceAspect {

    @Value("${spring.application.name}")
    private String tracerName;

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final WalletRepository walletRepository;

    private final ApplicationMeterRegistry applicationMeterRegistry;

    @Timed(value = "wallet-service.create_wallet_time")
    @Around("execution(* aq.project.services.WalletService.createWallet(..)) && args(wallet)")
    public String aspectCreateWallet(ProceedingJoinPoint pjp, Wallet wallet) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("create_wallet").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
//            Validation
            if(wallet == null) {
                applicationMeterRegistry.incrementFailCreateWalletCounter();
                String msg = "Wallet is null";
                ConstraintViolationException exc = new ConstraintViolationException(msg, null);
                log.warn(String.format("[%s-%s]: %s", spanId, traceId, msg));
                throw exc;
            }
            Set<ConstraintViolation<Wallet>> walletViolations = validator.validate(wallet);
            Set<ConstraintViolation<CreditCard>> creditCardViolations = validator.validate(wallet.getCreditCard());
            Set<ConstraintViolation<WalletDetails>> walletDetailsViolations = validator.validate(wallet.getWalletDetails());
            if(!walletViolations.isEmpty()) {
                applicationMeterRegistry.incrementFailCreateWalletCounter();
                ConstraintViolationException exc = new ConstraintViolationException(walletViolations);
                log.warn(String.format("[%s-%s]: %s", spanId, traceId, exc.getMessage()));
                throw exc;
            }
            if(!creditCardViolations.isEmpty()) {
                applicationMeterRegistry.incrementFailCreateWalletCounter();
                ConstraintViolationException exc = new ConstraintViolationException(creditCardViolations);
                log.warn(String.format("[%s-%s]: %s", spanId, traceId, exc.getMessage()));
                throw exc;
            }
            if(!walletDetailsViolations.isEmpty()) {
                applicationMeterRegistry.incrementFailCreateWalletCounter();
                ConstraintViolationException exc = new ConstraintViolationException(walletDetailsViolations);
                log.warn(String.format("[%s-%s]: %s", spanId, traceId, exc.getMessage()));
                throw exc;
            }
//            Telemetry
            log.info(String.format("[%s-%s]: Received wallet creation request", traceId, spanId));
//            Main logic
            String createdWalletId = (String) pjp.proceed();
//            Telemetry
            log.info(String.format("[%s-%s]: Wallet with id [%s] created successfully", traceId, spanId, createdWalletId));
            applicationMeterRegistry.incrementSuccessCreateWalletCounter();
            return createdWalletId;
        } finally {
            span.end();
        }
    }

    @Timed(value = "wallet-service.get_wallet_info_time")
    @Around("execution(* aq.project.services.WalletService.getWalletInfo(..)) && args(walletId)")
    public Wallet aspectGetWalletInfo(ProceedingJoinPoint pjp, String walletId) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("get_wallet_info").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
//            Validation
            if(walletId == null || walletId.isEmpty()) {
                String msg = "The wallet id is null or empty";
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
                throw new ConstraintViolationException(msg, null);
            }
            if(isInvalidUuid(walletId)) {
                String msg = String.format("Received invalid walletId: %s", walletId);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
                throw new ConstraintViolationException(msg, null);
            }
//            Telemetry
            log.info(String.format("[%s-%s]: Received walletId [%s]", traceId, spanId, walletId));
            if(walletRepository.findById(walletId).isEmpty()) {
                applicationMeterRegistry.incrementFailGetWalletInfoRequestCounter();
                String msg = String.format("Wallet with id [%s] not found", walletId);
                NoSuchWalletException exc = new NoSuchWalletException(msg);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, exc.getMessage()));
                throw exc;
            }
//            Main logic
            Wallet wallet = (Wallet) pjp.proceed();
//            Telemetry
            applicationMeterRegistry.incrementSuccessGetWalletInfoRequestCounter();
            log.info(String.format("[%s-%s]: Handle get info for wallet with id [%s] completed successfully",
                    traceId, spanId, walletId));
            return wallet;
        } finally {
            span.end();
        }
    }

    private boolean isInvalidUuid(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        return !matcher.find();
    }
}
