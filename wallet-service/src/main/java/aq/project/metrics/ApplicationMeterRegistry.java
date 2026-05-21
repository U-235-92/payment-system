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

    private Counter successGetWalletInfoRequestCounter;
    private Counter failGetWalletInfoRequestCounter;

    private Counter totalSuccessRequestMessageCounter;
    private Counter totalFailRequestMessageCounter;

    private Counter successDepositRequestMessageCounter;
    private Counter failDepositRequestMessageCounter;

    private Counter successWithdrawRequestMessageCounter;
    private Counter failWithdrawRequestMessageCounter;

    private Counter successTransferRequestMessageCounter;
    private Counter failTransferRequestMessageCounter;

    private Counter successCreateWalletCounter;
    private Counter failCreateWalletCounter;

    private Counter successGetTransactionStatusCounter;
    private Counter failGetTransactionStatusCounter;

    @Value("${spring.application.name}")
    private String counterNamePrefix;

    @Override
    public void bindTo(MeterRegistry registry) {
        successGetWalletInfoRequestCounter = getCounter("get_wallet_info_request_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failGetWalletInfoRequestCounter = getCounter("get_wallet_info_request_count", registry, List.of(Tag.of(STATUS, FAIL)));

        totalSuccessRequestMessageCounter = getCounter("total_request_message_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        totalFailRequestMessageCounter = getCounter("total_request_message_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successDepositRequestMessageCounter = getCounter("deposit_request_message_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failDepositRequestMessageCounter = getCounter("deposit_request_message_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successWithdrawRequestMessageCounter = getCounter("withdraw_request_message_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failWithdrawRequestMessageCounter = getCounter("withdraw_request_message_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successTransferRequestMessageCounter = getCounter("transfer_request_message_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failTransferRequestMessageCounter = getCounter("transfer_request_message_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successCreateWalletCounter = getCounter("create_wallet_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failCreateWalletCounter = getCounter("create_wallet_count", registry, List.of(Tag.of(STATUS, FAIL)));

        successGetTransactionStatusCounter = getCounter("get_transaction_status_count", registry, List.of(Tag.of(STATUS, SUCCESS)));
        failGetTransactionStatusCounter = getCounter("get_transaction_status_count", registry, List.of(Tag.of(STATUS, FAIL)));
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

    public void incrementSuccessGetWalletInfoRequestCounter() {
        successGetWalletInfoRequestCounter.increment();
    }

    public void incrementFailGetWalletInfoRequestCounter() {
        failGetWalletInfoRequestCounter.increment();
    }

    public void incrementTotalSuccessRequestMessageCounter() {
        totalSuccessRequestMessageCounter.increment();
    }

    public void incrementTotalFailRequestMessageCounter() {
        totalFailRequestMessageCounter.increment();
    }

    public void incrementSuccessDepositRequestMessageCounter() {
        successDepositRequestMessageCounter.increment();
    }

    public void incrementFailDepositRequestMessageCounter() {
        failDepositRequestMessageCounter.increment();
    }

    public void incrementSuccessWithdrawRequestMessageCounter() {
        successWithdrawRequestMessageCounter.increment();
    }

    public void incrementFailWithdrawRequestMessageCounter() {
        failWithdrawRequestMessageCounter.increment();
    }

    public void incrementSuccessTransferRequestMessageCounter() {
        successTransferRequestMessageCounter.increment();
    }

    public void incrementFailTransferRequestMessageCounter() {
        failTransferRequestMessageCounter.increment();
    }

    public void incrementSuccessCreateWalletCounter() {
        successCreateWalletCounter.increment();
    }

    public void incrementFailCreateWalletCounter() {
        failCreateWalletCounter.increment();
    }

    public void incrementSuccessGetTransactionStatusCounter() {
        successGetTransactionStatusCounter.increment();
    }

    public void incrementFailGetTransactionStatusCounter() {
        failGetTransactionStatusCounter.increment();
    }
}
