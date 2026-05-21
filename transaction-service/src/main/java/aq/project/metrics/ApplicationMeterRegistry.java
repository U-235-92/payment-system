package aq.project.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationMeterRegistry implements MeterBinder {

    private static final String STATUS = "status";
    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";

    private Counter totalSuccessTransactionRequestCounter;
    private Counter totalFailTransactionRequestCounter;

    private Counter successDepositTransactionRequestCounter;
    private Counter failDepositTransactionRequestCounter;

    private Counter successWithdrawTransactionRequestCounter;
    private Counter failWithdrawTransactionRequestCounter;

    private Counter successTransferTransactionRequestCounter;
    private Counter failTransferTransactionRequestCounter;

    private Counter successTransactionResponseCounter;
    private Counter failTransactionResponseCounter;

    private Counter successGetTransactionStatusCounter;
    private Counter failGetTransactionStatusCounter;

    @Value("${spring.application.name}")
    private String counterNamePrefix;

    @Override
    public void bindTo(MeterRegistry registry) {
        totalSuccessTransactionRequestCounter = getCounter("total_transaction_request_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        totalFailTransactionRequestCounter = getCounter("total_transaction_request_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successDepositTransactionRequestCounter = getCounter("deposit_transaction_request_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failDepositTransactionRequestCounter = getCounter("deposit_transaction_request_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successWithdrawTransactionRequestCounter = getCounter("withdraw_transaction_request_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failWithdrawTransactionRequestCounter = getCounter("withdraw_transaction_request_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successTransferTransactionRequestCounter = getCounter("transfer_transaction_request_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failTransferTransactionRequestCounter = getCounter("transfer_transaction_request_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successTransactionResponseCounter = getCounter("success_transaction_response_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failTransactionResponseCounter = getCounter("fail_transaction_response_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successGetTransactionStatusCounter = getCounter("success_get_transaction_status_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failGetTransactionStatusCounter = getCounter("fail_get_transaction_status_count", registry, List.of(Tag.of(STATUS, FAIL)));
    }

    private Counter getCounter(String metricName, MeterRegistry meterRegistry) {
        return Counter.builder(counterNamePrefix + "." + metricName)
                .register(meterRegistry);
    }

    private Counter getCounter(String metricName, MeterRegistry meterRegistry, Iterable<Tag> tags) {
        return Counter.builder(counterNamePrefix + "." + metricName)
                .tags(tags)
                .register(meterRegistry);
    }

    public void incrementTotalSuccessTransactionRequestCounter() {
        totalSuccessTransactionRequestCounter.increment();
    }

    public void incrementTotalFailTransactionRequestCounter() {
        totalFailTransactionRequestCounter.increment();
    }

    public void incrementSuccessDepositTransactionRequestCounter() {
        successDepositTransactionRequestCounter.increment();
    }

    public void incrementFailDepositTransactionRequestCounter() {
        failDepositTransactionRequestCounter.increment();
    }

    public void incrementSuccessWithdrawTransactionRequestCounter() {
        successWithdrawTransactionRequestCounter.increment();
    }

    public void incrementFailWithdrawTransactionRequestCounter() {
        failWithdrawTransactionRequestCounter.increment();
    }

    public void incrementSuccessTransferTransactionRequestCounter() {
        successTransferTransactionRequestCounter.increment();
    }

    public void incrementFailTransferTransactionRequestCounter() {
        failTransferTransactionRequestCounter.increment();
    }

    public void incrementSuccessTransactionResponseCounter() {
        successTransactionResponseCounter.increment();
    }

    public void incrementFailTransactionResponseCounter() {
        failTransactionResponseCounter.increment();
    }

    public void incrementSuccessGetTransactionStatusCounter() {
        successGetTransactionStatusCounter.increment();
    }

    public void incrementFailGetTransactionStatusCounter() {
        failGetTransactionStatusCounter.increment();
    }
}
