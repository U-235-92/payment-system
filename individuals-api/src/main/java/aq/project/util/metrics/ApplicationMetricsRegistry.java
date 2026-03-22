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

    private static final String LOGIN_USER_METRIC_COUNT = "login_user.count";
    private static final String CREATE_USER_METRIC_COUNT = "create_user.count";
    private static final String UPDATE_USER_METRIC_COUNT = "update_user.count";
    private static final String DELETE_USER_METRIC_COUNT = "delete_user.count";
    private static final String GET_USER_INFO_METRIC_COUNT = "get_user_info.count";
    private static final String REFRESH_TOKEN_METRIC_COUNT = "refresh_token.count";

    private static final String STATUS = "status";

    private static final String FAIL = "fail";
    private static final String SUCCESS = "success";

    private Timer requestLatencyTimer;

    private Counter successLoginUserCounter;
    private Counter failLoginUserCounter;

    private Counter successCreateUserCounter;
    private Counter failCreateUserCounter;

    private Counter successUpdateUserCounter;
    private Counter failUpdateUserCounter;

    private Counter successDeleteUserByKeycloakIdCounter;
    private Counter failDeleteUserByKeycloakIdCounter;

    private Counter successGetUserInfoCounter;
    private Counter failGetUserInfoCounter;

    private Counter successRefreshTokenCounter;
    private Counter failRefreshTokenCounter;

    @Override
    public void bindTo(MeterRegistry registry) {
        requestLatencyTimer = Timer.builder(METRIC_PREFIX + "request_latency").register(registry);

        successLoginUserCounter = getCounter(LOGIN_USER_METRIC_COUNT, registry, List.of(Tag.of(STATUS, SUCCESS)));
        failLoginUserCounter = getCounter(LOGIN_USER_METRIC_COUNT, registry, List.of(Tag.of(STATUS, FAIL)));

        successCreateUserCounter = getCounter(CREATE_USER_METRIC_COUNT, registry, List.of(Tag.of(STATUS, SUCCESS)));
        failCreateUserCounter = getCounter(CREATE_USER_METRIC_COUNT, registry, List.of(Tag.of(STATUS, FAIL)));

        successUpdateUserCounter = getCounter(UPDATE_USER_METRIC_COUNT, registry, List.of(Tag.of(STATUS, SUCCESS)));
        failUpdateUserCounter = getCounter(UPDATE_USER_METRIC_COUNT, registry, List.of(Tag.of(STATUS, FAIL)));

        successDeleteUserByKeycloakIdCounter = getCounter(DELETE_USER_METRIC_COUNT, registry, List.of(Tag.of(STATUS, SUCCESS)));
        failDeleteUserByKeycloakIdCounter = getCounter(DELETE_USER_METRIC_COUNT, registry, List.of(Tag.of(STATUS, FAIL)));

        successGetUserInfoCounter = getCounter(GET_USER_INFO_METRIC_COUNT, registry, List.of(Tag.of(STATUS, SUCCESS)));
        failGetUserInfoCounter = getCounter(GET_USER_INFO_METRIC_COUNT, registry, List.of(Tag.of(STATUS, FAIL)));

        successRefreshTokenCounter = getCounter(REFRESH_TOKEN_METRIC_COUNT, registry, List.of(Tag.of(STATUS, SUCCESS)));
        failRefreshTokenCounter = getCounter(REFRESH_TOKEN_METRIC_COUNT, registry, List.of(Tag.of(STATUS, FAIL)));
    }

    private Counter getCounter(String metricName, MeterRegistry meterRegistry, Iterable<Tag> tags) {
        return Counter.builder(METRIC_PREFIX + metricName)
                .tags(tags)
                .register(meterRegistry);
    }

    public void incrementSuccessLoginUserCounter() {
        successLoginUserCounter.increment();
    }

    public void incrementFailLoginUserCounter() {
        failLoginUserCounter.increment();
    }

    public void incrementSuccessCreateUserCounter() {
        successCreateUserCounter.increment();
    }

    public void incrementFailCreateUserCounter() {
        failCreateUserCounter.increment();
    }

    public void incrementSuccessUpdateUserCounter() {
        successUpdateUserCounter.increment();
    }

    public void incrementFailUpdateUserCounter() {
        failUpdateUserCounter.increment();
    }

    public void incrementSuccessDeleteUserByKeycloakIdCounter() {
        successDeleteUserByKeycloakIdCounter.increment();
    }

    public void incrementFailDeleteUserByKeycloakIdCounter() {
        failDeleteUserByKeycloakIdCounter.increment();
    }

    public void incrementSuccessGetUserInfoCounter() {
        successGetUserInfoCounter.increment();
    }

    public void incrementFailGetUserInfoCounter() {
        failGetUserInfoCounter.increment();
    }

    public void incrementSuccessRefreshTokenCounter() {
        successRefreshTokenCounter.increment();
    }

    public void incrementFailRefreshTokenCounter() {
        failRefreshTokenCounter.increment();
    }

    public void evaluateRequestLatency(long start, long end) {
        long latency = end - start;
        requestLatencyTimer.record(latency, TimeUnit.NANOSECONDS);
    }
}
