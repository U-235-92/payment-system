package aq.project.utils.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ApplicationMetricsRegistry implements MeterBinder {

    private static final String STATUS = "status";
    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";
    private static final String ACTION = "action";

    @Value("${spring.application.name}")
    private String serviceName;

    private MeterRegistry meterRegistry;

    @Override
    public void bindTo(@NonNull MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public long finishTimer(Timer.Sample sample, String action) {
        Timer timer = Timer.builder(serviceName + "." + "execution_time")
                .tags(ACTION, action)
                .register(meterRegistry);
        long executionTime = sample.stop(timer);
        return TimeUnit.NANOSECONDS.toMillis(executionTime);
    }

    public void countAction(boolean isSuccess, String action) {
        Counter counter = Counter.builder(serviceName + "." + "count_action")
                .tag(ACTION, action)
                .tag(STATUS, isSuccess ? SUCCESS : FAIL)
                .register(meterRegistry);
        counter.increment();
    }
}
