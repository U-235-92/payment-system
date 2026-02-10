package aq.project.util.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ApplicationMetricsRegistry implements MeterBinder {

    private static final String METRIC_PREFIX = "individuals_api.";
    private static final String METRIC_LOGIN_COUNT = "login.count";
    private static final String METRIC_REGISTRATION_COUNT = "registration.count";

    private static final String STATUS = "status";

    private static final String FAIL = "fail";
    private static final String SUCCESS = "success";

    private Timer requestLatencyTimer;

    private Counter successLoginCounter;
    private Counter failLoginCounter;

    private Counter successRegistrationCounter;
    private Counter failRegistrationCounter;

    @Override
    public void bindTo(MeterRegistry registry) {
        requestLatencyTimer = Timer.builder(METRIC_PREFIX + "request_latency").register(registry);
        successLoginCounter = getCounter(METRIC_LOGIN_COUNT, registry, List.of(Tag.of(STATUS, SUCCESS)));
        failLoginCounter = getCounter(METRIC_LOGIN_COUNT, registry, List.of(Tag.of(STATUS, FAIL)));
        successRegistrationCounter = getCounter(METRIC_REGISTRATION_COUNT, registry, List.of(Tag.of(STATUS, SUCCESS)));
        failRegistrationCounter = getCounter(METRIC_REGISTRATION_COUNT, registry, List.of(Tag.of(STATUS, FAIL)));
    }

    private Counter getCounter(String metricName, MeterRegistry meterRegistry, Iterable<Tag> tags) {
        return Counter.builder(METRIC_PREFIX + metricName)
                .tags(tags)
                .register(meterRegistry);
    }

    public void incrementSuccessLoginCounter() {
        successLoginCounter.increment();
    }

    public void incrementFailLoginCounter() {
        failLoginCounter.increment();
    }

    public void incrementSuccessRegistrationCounter() {
        successRegistrationCounter.increment();
    }

    public void incrementFailRegistrationCounter() {
        failRegistrationCounter.increment();
    }

    public void evaluateRequestLatency(long start, long end) {
        long latency = end - start;
        requestLatencyTimer.record(latency, TimeUnit.NANOSECONDS);
    }
}
